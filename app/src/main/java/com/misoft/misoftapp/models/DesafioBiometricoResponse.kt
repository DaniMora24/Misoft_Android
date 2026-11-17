
package com.misoft.misoftapp.models

data class DesafioBiometricoResponse(
    val idDesafio: Int,
    val nonce: String,
    val fechaExpiracion: String
)
