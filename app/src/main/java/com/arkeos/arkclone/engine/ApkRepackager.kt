package com.arkeos.arkclone.engine

import java.io.File

class ApkRepackager(private val signer: CloneSigner) {

    fun repackage(
        sourceApks: List<File>,
        newPackage: String,
        cloneSuffix: String,
        label: String,
        workDir: File
    ): List<File> {
        workDir.deleteRecursively()
        workDir.mkdirs()
        val patcher = ManifestPatcher(newPackage, cloneSuffix, label)
        val signed = mutableListOf<File>()

        sourceApks.forEachIndexed { index, source ->
            val patched = patcher.patch(ZipRewriter.readManifest(source))
            val unsigned = File(workDir, "unsigned_$index.apk")
            val output = File(workDir, if (index == 0) "base.apk" else "split_$index.apk")
            ZipRewriter.rewrite(source, unsigned, patched.bytes)
            signer.sign(unsigned, output)
            unsigned.delete()
            signed.add(output)
        }
        return signed
    }
}
