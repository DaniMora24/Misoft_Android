
package com.misoft.misoftapp.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.RSAKeyGenParameterSpec

object BiometricKeyManager {

    private const val KEY_ALIAS = "misoft_biometric_key"

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    /**
     * Genera una clave RSA protegida por autenticación biométrica.
     * Si ya existe, reutiliza la clave almacenada.
     */
    fun obtenerOCrearClaves(): KeyPair {

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val privateKey = keyStore.getKey(KEY_ALIAS, null) as java.security.PrivateKey
            val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey

            return KeyPair(publicKey, privateKey)
        }

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            ANDROID_KEYSTORE
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(
                RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4)
            )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(
                0,
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
            .build()

        keyPairGenerator.initialize(parameterSpec)

        return keyPairGenerator.generateKeyPair()
    }

    /**
     * Obtiene la clave pública y la convierte a Base64
     * para enviarla al backend.
     */
    fun obtenerClavePublicaBase64(): String {

        val keyPair = obtenerOCrearClaves()

        return Base64.encodeToString(
            keyPair.public.encoded,
            Base64.NO_WRAP
        )
    }

    /**
     * Prepara la firma RSA que posteriormente utilizará
     * BiometricPrompt para autorizar la autenticación.
     */
    fun prepararFirma(): Signature {

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val privateKey = keyStore.getKey(
            KEY_ALIAS,
            null
        ) as java.security.PrivateKey

        val signature = Signature.getInstance("SHA256withRSA")

        signature.initSign(privateKey)

        return signature
    }

    /**
     * Comprueba si ya existe una clave biométrica registrada
     * localmente en el dispositivo.
     */
    fun existeClave(): Boolean {

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        return keyStore.containsAlias(KEY_ALIAS)
    }

    /**
     * Elimina la clave biométrica almacenada en el dispositivo.
     */
    fun eliminarClave() {

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }
}
