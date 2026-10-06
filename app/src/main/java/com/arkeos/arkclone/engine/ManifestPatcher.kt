package com.arkeos.arkclone.engine

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ManifestPatchResult(val bytes: ByteArray, val originalPackage: String)

class ManifestPatcher(
    private val newPackage: String,
    private val cloneSuffix: String,
    private val launcherLabel: String
) {
    private class PoolString(val raw: ByteArray, val text: String)

    private class Pool(
        val utf8: Boolean,
        val flags: Int,
        val strings: MutableList<PoolString>,
        val styleOffsets: ByteArray,
        val styleData: ByteArray,
        val styleCount: Int
    )

    fun patch(data: ByteArray): ManifestPatchResult {
        val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        require(buf.getShort(0).toInt() == RES_XML) { "not a binary xml file" }
        val fileHeader = buf.getShort(2).toInt() and 0xffff
        require(buf.getShort(fileHeader).toInt() == RES_STRING_POOL) { "string pool missing" }

        val pool = readPool(buf, fileHeader)
        val poolSize = buf.getInt(fileHeader + 4)
        val restStart = fileHeader + poolSize
        val rest = data.copyOfRange(restStart, data.size)
        val rb = ByteBuffer.wrap(rest).order(ByteOrder.LITTLE_ENDIAN)

        val resMap = readResourceMap(rb)
        val chunks = listChunks(rb)

        val oldPackage = findOldPackage(rb, chunks, pool, resMap)
        require(oldPackage.isNotEmpty()) { "package attribute missing" }

        val declaredPermissions = collectDeclaredPermissions(rb, chunks, pool, resMap)
        val launcherComponents = collectLauncherComponents(rb, chunks, pool, resMap)
        val editor = Editor(pool, rb, resMap, oldPackage, declaredPermissions)

        for (offset in chunks) {
            if (rb.getShort(offset).toInt() != RES_START_ELEMENT) continue
            editor.edit(offset, launcherComponents.contains(offset))
        }

        return ManifestPatchResult(assemble(fileHeader, pool, rest), oldPackage)
    }

    private inner class Editor(
        val pool: Pool,
        val rb: ByteBuffer,
        val resMap: IntArray,
        val oldPackage: String,
        val declaredPermissions: Set<String>
    ) {
        fun edit(offset: Int, isLauncher: Boolean) {
            val element = text(pool, rb.getInt(offset + 20))
            val count = rb.getShort(offset + 28).toInt() and 0xffff
            val attrStart = offset + 16 + (rb.getShort(offset + 24).toInt() and 0xffff)
            val attrSize = rb.getShort(offset + 26).toInt() and 0xffff

            for (i in 0 until count) {
                val attr = attrStart + i * attrSize
                val nameIdx = rb.getInt(attr + 4)
                val nsIdx = rb.getInt(attr + 0)
                val id = attrId(pool, resMap, nameIdx, nsIdx)
                val value = stringValue(rb, pool, attr) ?: continue

                when {
                    element == "manifest" && nsIdx == -1 && text(pool, nameIdx) == "package" ->
                        setString(attr, newPackage)

                    element == "manifest" && id == ATTR_SHARED_USER_ID ->
                        setString(attr, newPackage)

                    id == ATTR_NAME && element in COMPONENT_ELEMENTS ->
                        setString(attr, absoluteClass(value))

                    id == ATTR_NAME && element in PERMISSION_ELEMENTS ->
                        setString(attr, renamePermission(value))

                    id == ATTR_TARGET_ACTIVITY || id == ATTR_BACKUP_AGENT ||
                        id == ATTR_MANAGE_SPACE || id == ATTR_PARENT_ACTIVITY ||
                        id == ATTR_APP_COMPONENT_FACTORY ->
                        setString(attr, absoluteClass(value))

                    id == ATTR_PERMISSION || id == ATTR_READ_PERMISSION || id == ATTR_WRITE_PERMISSION ->
                        setString(attr, renamePermission(value))

                    id == ATTR_AUTHORITIES && element == "provider" ->
                        setString(attr, renameAuthorities(value))

                    (id == ATTR_PROCESS || id == ATTR_TASK_AFFINITY) && value.startsWith(oldPackage) ->
                        setString(attr, newPackage + value.substring(oldPackage.length))
                }
            }

            if (element == "application" || isLauncher) {
                for (i in 0 until count) {
                    val attr = attrStart + i * attrSize
                    val id = attrId(pool, resMap, rb.getInt(attr + 4), rb.getInt(attr + 0))
                    if (id == ATTR_LABEL) setString(attr, launcherLabel)
                }
            }
        }

        private fun setString(attr: Int, value: String) {
            val index = addString(pool, value)
            rb.putInt(attr + 8, index)
            rb.put(attr + 15, TYPE_STRING.toByte())
            rb.putInt(attr + 16, index)
        }

        private fun absoluteClass(value: String): String = when {
            value.startsWith(".") -> oldPackage + value
            !value.contains(".") -> "$oldPackage.$value"
            else -> value
        }

        private fun renamePermission(value: String): String = when {
            value.startsWith("$oldPackage.") -> newPackage + value.substring(oldPackage.length)
            declaredPermissions.contains(value) -> "$value.$cloneSuffix"
            else -> value
        }

        private fun renameAuthorities(value: String): String =
            value.split(";").joinToString(";") { part ->
                if (part.startsWith(oldPackage)) newPackage + part.substring(oldPackage.length)
                else "$part.$cloneSuffix"
            }
    }

    private fun findOldPackage(rb: ByteBuffer, chunks: List<Int>, pool: Pool, resMap: IntArray): String {
        for (offset in chunks) {
            if (rb.getShort(offset).toInt() != RES_START_ELEMENT) continue
            if (text(pool, rb.getInt(offset + 20)) != "manifest") continue
            forEachAttr(rb, offset) { attr ->
                val nameIdx = rb.getInt(attr + 4)
                if (rb.getInt(attr) == -1 && text(pool, nameIdx) == "package") {
                    return stringValue(rb, pool, attr) ?: ""
                }
            }
        }
        return ""
    }

    private fun collectDeclaredPermissions(
        rb: ByteBuffer, chunks: List<Int>, pool: Pool, resMap: IntArray
    ): Set<String> {
        val result = mutableSetOf<String>()
        for (offset in chunks) {
            if (rb.getShort(offset).toInt() != RES_START_ELEMENT) continue
            val element = text(pool, rb.getInt(offset + 20))
            if (element != "permission" && element != "permission-tree" && element != "permission-group") continue
            forEachAttr(rb, offset) { attr ->
                if (attrId(pool, resMap, rb.getInt(attr + 4), rb.getInt(attr)) == ATTR_NAME) {
                    stringValue(rb, pool, attr)?.let { result.add(it) }
                }
            }
        }
        return result
    }

    private fun collectLauncherComponents(
        rb: ByteBuffer, chunks: List<Int>, pool: Pool, resMap: IntArray
    ): Set<Int> {
        val result = mutableSetOf<Int>()
        var current = -1
        for (offset in chunks) {
            val type = rb.getShort(offset).toInt()
            val element = if (type == RES_START_ELEMENT || type == RES_END_ELEMENT)
                text(pool, rb.getInt(offset + 20)) else ""
            if (type == RES_START_ELEMENT) {
                if (element == "activity" || element == "activity-alias") current = offset
                if (element == "category" && current != -1) {
                    forEachAttr(rb, offset) { attr ->
                        if (attrId(pool, resMap, rb.getInt(attr + 4), rb.getInt(attr)) == ATTR_NAME &&
                            stringValue(rb, pool, attr) == CATEGORY_LAUNCHER
                        ) result.add(current)
                    }
                }
            } else if (type == RES_END_ELEMENT && (element == "activity" || element == "activity-alias")) {
                current = -1
            }
        }
        return result
    }

    private inline fun forEachAttr(rb: ByteBuffer, offset: Int, block: (Int) -> Unit) {
        val count = rb.getShort(offset + 28).toInt() and 0xffff
        val start = offset + 16 + (rb.getShort(offset + 24).toInt() and 0xffff)
        val size = rb.getShort(offset + 26).toInt() and 0xffff
        for (i in 0 until count) block(start + i * size)
    }

    private fun stringValue(rb: ByteBuffer, pool: Pool, attr: Int): String? {
        val type = rb.get(attr + 15).toInt() and 0xff
        if (type != TYPE_STRING) return null
        val index = rb.getInt(attr + 16)
        return if (index in 0 until pool.strings.size) pool.strings[index].text else null
    }

    private fun attrId(pool: Pool, resMap: IntArray, nameIdx: Int, nsIdx: Int): Int {
        if (nameIdx in resMap.indices && resMap[nameIdx] != 0) return resMap[nameIdx]
        if (nsIdx == -1) return 0
        if (text(pool, nsIdx) != ANDROID_NS) return 0
        return NAME_TO_ID[text(pool, nameIdx)] ?: 0
    }

    private fun text(pool: Pool, index: Int): String =
        if (index in 0 until pool.strings.size) pool.strings[index].text else ""

    private fun addString(pool: Pool, value: String): Int {
        for (i in pool.strings.size - 1 downTo 0) {
            if (pool.strings[i].text == value) return i
        }
        pool.strings.add(PoolString(encodeString(value, pool.utf8), value))
        return pool.strings.size - 1
    }

    private fun readResourceMap(rb: ByteBuffer): IntArray {
        var offset = 0
        while (offset + 8 <= rb.limit()) {
            val type = rb.getShort(offset).toInt() and 0xffff
            val size = rb.getInt(offset + 4)
            if (type == RES_RESOURCE_MAP) {
                val n = (size - 8) / 4
                return IntArray(n) { rb.getInt(offset + 8 + it * 4) }
            }
            if (size <= 0) break
            offset += size
        }
        return IntArray(0)
    }

    private fun listChunks(rb: ByteBuffer): List<Int> {
        val result = mutableListOf<Int>()
        var offset = 0
        while (offset + 8 <= rb.limit()) {
            result.add(offset)
            val size = rb.getInt(offset + 4)
            if (size <= 0) break
            offset += size
        }
        return result
    }

    private fun readPool(buf: ByteBuffer, start: Int): Pool {
        val headerSize = buf.getShort(start + 2).toInt() and 0xffff
        val chunkSize = buf.getInt(start + 4)
        val stringCount = buf.getInt(start + 8)
        val styleCount = buf.getInt(start + 12)
        val flags = buf.getInt(start + 16)
        val stringsStart = buf.getInt(start + 20)
        val stylesStart = buf.getInt(start + 24)
        val utf8 = (flags and FLAG_UTF8) != 0

        val strings = ArrayList<PoolString>(stringCount + 8)
        for (i in 0 until stringCount) {
            val offset = start + stringsStart + buf.getInt(start + headerSize + i * 4)
            strings.add(readString(buf, offset, utf8))
        }

        val styleOffsets = ByteArray(styleCount * 4)
        val styleData: ByteArray
        if (styleCount > 0) {
            val so = start + headerSize + stringCount * 4
            for (i in 0 until styleOffsets.size) styleOffsets[i] = buf.get(so + i)
            styleData = ByteArray(chunkSize - stylesStart)
            for (i in styleData.indices) styleData[i] = buf.get(start + stylesStart + i)
        } else {
            styleData = ByteArray(0)
        }
        return Pool(utf8, flags, strings, styleOffsets, styleData, styleCount)
    }

    private fun readString(buf: ByteBuffer, offset: Int, utf8: Boolean): PoolString {
        var p = offset
        return if (utf8) {
            var chars = buf.get(p++).toInt() and 0xff
            if (chars and 0x80 != 0) chars = ((chars and 0x7f) shl 8) or (buf.get(p++).toInt() and 0xff)
            var bytes = buf.get(p++).toInt() and 0xff
            if (bytes and 0x80 != 0) bytes = ((bytes and 0x7f) shl 8) or (buf.get(p++).toInt() and 0xff)
            val body = ByteArray(bytes)
            for (i in 0 until bytes) body[i] = buf.get(p + i)
            val raw = ByteArray(p - offset + bytes + 1)
            for (i in raw.indices) raw[i] = buf.get(offset + i)
            PoolString(raw, String(body, Charsets.UTF_8))
        } else {
            var chars = buf.getShort(p).toInt() and 0xffff
            p += 2
            if (chars and 0x8000 != 0) {
                chars = ((chars and 0x7fff) shl 16) or (buf.getShort(p).toInt() and 0xffff)
                p += 2
            }
            val body = ByteArray(chars * 2)
            for (i in body.indices) body[i] = buf.get(p + i)
            val raw = ByteArray(p - offset + chars * 2 + 2)
            for (i in raw.indices) raw[i] = buf.get(offset + i)
            PoolString(raw, String(body, Charsets.UTF_16LE))
        }
    }

    private fun encodeString(value: String, utf8: Boolean): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        if (utf8) {
            val body = value.toByteArray(Charsets.UTF_8)
            writeUtf8Length(out, value.length)
            writeUtf8Length(out, body.size)
            out.write(body)
            out.write(0)
        } else {
            val body = value.toByteArray(Charsets.UTF_16LE)
            require(value.length < 0x8000) { "string too long" }
            out.write(value.length and 0xff)
            out.write((value.length shr 8) and 0xff)
            out.write(body)
            out.write(0)
            out.write(0)
        }
        return out.toByteArray()
    }

    private fun writeUtf8Length(out: java.io.ByteArrayOutputStream, n: Int) {
        if (n > 0x7f) {
            out.write(((n shr 8) and 0x7f) or 0x80)
            out.write(n and 0xff)
        } else {
            out.write(n)
        }
    }

    private fun assemble(fileHeader: Int, pool: Pool, rest: ByteArray): ByteArray {
        val count = pool.strings.size
        val offsets = IntArray(count)
        var dataSize = 0
        for (i in 0 until count) {
            offsets[i] = dataSize
            dataSize += pool.strings[i].raw.size
        }
        val padded = (dataSize + 3) and 3.inv()
        val headerSize = 28
        val stringsStart = headerSize + count * 4 + pool.styleCount * 4
        val stylesStart = if (pool.styleCount > 0) stringsStart + padded else 0
        val chunkSize = stringsStart + padded + pool.styleData.size

        val poolBuf = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)
        poolBuf.putShort(RES_STRING_POOL.toShort())
        poolBuf.putShort(headerSize.toShort())
        poolBuf.putInt(chunkSize)
        poolBuf.putInt(count)
        poolBuf.putInt(pool.styleCount)
        poolBuf.putInt(pool.flags and 1.inv())
        poolBuf.putInt(stringsStart)
        poolBuf.putInt(stylesStart)
        for (o in offsets) poolBuf.putInt(o)
        poolBuf.put(pool.styleOffsets)
        for (s in pool.strings) poolBuf.put(s.raw)
        poolBuf.position(stringsStart + padded)
        poolBuf.put(pool.styleData)

        val total = fileHeader + chunkSize + rest.size
        val out = ByteBuffer.allocate(total).order(ByteOrder.LITTLE_ENDIAN)
        out.putShort(RES_XML.toShort())
        out.putShort(fileHeader.toShort())
        out.putInt(total)
        out.put(poolBuf.array())
        out.put(rest)
        return out.array()
    }

    companion object {
        private const val RES_XML = 0x0003
        private const val RES_STRING_POOL = 0x0001
        private const val RES_RESOURCE_MAP = 0x0180
        private const val RES_START_ELEMENT = 0x0102
        private const val RES_END_ELEMENT = 0x0103
        private const val FLAG_UTF8 = 0x100
        private const val TYPE_STRING = 0x03

        private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
        private const val CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER"

        private const val ATTR_LABEL = 0x01010001
        private const val ATTR_NAME = 0x01010003
        private const val ATTR_PERMISSION = 0x01010006
        private const val ATTR_READ_PERMISSION = 0x01010007
        private const val ATTR_WRITE_PERMISSION = 0x01010008
        private const val ATTR_SHARED_USER_ID = 0x0101000b
        private const val ATTR_PROCESS = 0x01010011
        private const val ATTR_TASK_AFFINITY = 0x01010012
        private const val ATTR_AUTHORITIES = 0x01010018
        private const val ATTR_TARGET_ACTIVITY = 0x01010202
        private const val ATTR_MANAGE_SPACE = 0x0101027a
        private const val ATTR_BACKUP_AGENT = 0x01010281
        private const val ATTR_PARENT_ACTIVITY = 0x010103a7
        private const val ATTR_APP_COMPONENT_FACTORY = 0x0101057a

        private val COMPONENT_ELEMENTS =
            setOf("application", "activity", "activity-alias", "service", "receiver", "provider")
        private val PERMISSION_ELEMENTS =
            setOf("permission", "permission-tree", "permission-group", "uses-permission", "uses-permission-sdk-23")

        private val NAME_TO_ID = mapOf(
            "label" to ATTR_LABEL,
            "name" to ATTR_NAME,
            "permission" to ATTR_PERMISSION,
            "readPermission" to ATTR_READ_PERMISSION,
            "writePermission" to ATTR_WRITE_PERMISSION,
            "sharedUserId" to ATTR_SHARED_USER_ID,
            "process" to ATTR_PROCESS,
            "taskAffinity" to ATTR_TASK_AFFINITY,
            "authorities" to ATTR_AUTHORITIES,
            "targetActivity" to ATTR_TARGET_ACTIVITY,
            "manageSpaceActivity" to ATTR_MANAGE_SPACE,
            "backupAgent" to ATTR_BACKUP_AGENT,
            "parentActivityName" to ATTR_PARENT_ACTIVITY,
            "appComponentFactory" to ATTR_APP_COMPONENT_FACTORY
        )
    }
}
