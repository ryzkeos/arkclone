package com.arkeos.arkclone.engine

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.android.apksig.ApkSigner
import java.io.File
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

class CloneSigner {
    private val keyStore: KeyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

    fun sign(input: File, output: File) {
        ensureKey()
        val key = keyStore.getKey(ALIAS, null) as PrivateKey
        val cert = keyStore.getCertificate(ALIAS) as X509Certificate
        val config = ApkSigner.SignerConfig.Builder("ark_clone", key, listOf(cert)).build()
        ApkSigner.Builder(listOf(config))
            .setInputApk(input)
            .setOutputApk(output)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .build()
            .sign()
    }

    private fun ensureKey() {
        if (keyStore.containsAlias(ALIAS)) return
        val spec = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .setCertificateSubject(X500Principal("CN=Ark Clone"))
            .build()
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, PROVIDER).apply {
            initialize(spec)
            generateKeyPair()
        }
    }

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val ALIAS = "ark_clone_signing_key"
    }
}
