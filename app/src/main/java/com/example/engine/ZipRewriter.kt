package com.example.engine

import java.io.File
import java.io.FilterOutputStream
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipRewriter {
    private const val ALIGN_DEFAULT = 4
    private const val ALIGN_NATIVE_LIB = 16384
    private const val MANIFEST_ENTRY = "AndroidManifest.xml"

    private class CountingStream(out: OutputStream) : FilterOutputStream(out) {
        var count = 0L

        override fun write(b: Int) {
            out.write(b)
            count++
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            out.write(b, off, len)
            count += len
        }
    }

    fun readManifest(apk: File): ByteArray =
        ZipFile(apk).use { zip ->
            val entry = zip.getEntry(MANIFEST_ENTRY) ?: error("manifest missing in ${apk.name}")
            zip.getInputStream(entry).use { it.readBytes() }
        }

    fun rewrite(input: File, output: File, manifest: ByteArray) {
        ZipFile(input).use { zip ->
            val counter = CountingStream(output.outputStream().buffered())
            ZipOutputStream(counter).use { zos ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val source = entries.nextElement()
                    if (isSignatureEntry(source.name)) continue

                    zos.closeEntry()
                    if (source.name == MANIFEST_ENTRY) {
                        zos.putNextEntry(ZipEntry(source.name))
                        zos.write(manifest)
                        zos.closeEntry()
                        continue
                    }

                    if (source.isDirectory) {
                        zos.putNextEntry(ZipEntry(source.name))
                        zos.closeEntry()
                        continue
                    }

                    if (source.method == ZipEntry.STORED) {
                        val nameBytes = source.name.toByteArray(Charsets.UTF_8)
                        val align = if (source.name.endsWith(".so")) ALIGN_NATIVE_LIB else ALIGN_DEFAULT
                        val base = counter.count + 30 + nameBytes.size
                        val pad = ((align - ((base + 4) % align)) % align).toInt()
                        val data = zip.getInputStream(source).use { it.readBytes() }
                        val crc = CRC32().apply { update(data) }
                        val stored = ZipEntry(source.name).apply {
                            method = ZipEntry.STORED
                            size = data.size.toLong()
                            compressedSize = data.size.toLong()
                            this.crc = crc.value
                            extra = alignmentExtra(pad)
                        }
                        zos.putNextEntry(stored)
                        zos.write(data)
                        zos.closeEntry()
                    } else {
                        zos.putNextEntry(ZipEntry(source.name))
                        zip.getInputStream(source).use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
        }
    }

    private fun alignmentExtra(pad: Int): ByteArray {
        val extra = ByteArray(4 + pad)
        extra[0] = 0x35
        extra[1] = 0xD9.toByte()
        extra[2] = (pad and 0xff).toByte()
        extra[3] = ((pad shr 8) and 0xff).toByte()
        return extra
    }

    private fun isSignatureEntry(name: String): Boolean {
        if (!name.startsWith("META-INF/")) return false
        val upper = name.uppercase()
        return upper == "META-INF/MANIFEST.MF" ||
            upper.endsWith(".SF") || upper.endsWith(".RSA") ||
            upper.endsWith(".DSA") || upper.endsWith(".EC")
    }
}
