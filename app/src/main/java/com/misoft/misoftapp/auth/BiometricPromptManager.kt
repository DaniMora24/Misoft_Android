
package com.misoft.misoftapp.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.Signature

class BiometricPromptManager(
    private val activity: FragmentActivity
) {

    private val executor = ContextCompat.getMainExecutor(activity)

    fun verificarDisponibilidad(): Boolean {
        val biometricManager = BiometricManager.from(activity)

        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun autenticar(
        signature: Signature,
        onSuccess: (Signature) -> Unit,
        onError: (String) -> Unit
    ) {

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Verifica tu identidad para continuar")
            .setDescription("Utiliza tu huella o biometría registrada")
            .setNegativeButtonText("Cancelar")
            .setConfirmationRequired(true)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    val authenticatedSignature =
                        result.cryptoObject?.signature

                    if (authenticatedSignature != null) {
                        onSuccess(authenticatedSignature)
                    } else {
                        onError("No se pudo obtener la firma biométrica.")
                    }
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // La huella no fue reconocida.
                    // Android permite volver a intentarlo.
                }
            }
        )

        biometricPrompt.authenticate(
            promptInfo,
            BiometricPrompt.CryptoObject(signature)
        )
    }
}
