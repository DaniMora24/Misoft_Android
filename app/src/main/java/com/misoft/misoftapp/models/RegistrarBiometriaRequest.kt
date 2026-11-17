
package com.misoft.misoftapp.models

data class RegistrarBiometriaRequest(
    val email: String,
    val password: String,
    val clavePublica: String,
    val nombreDispositivo: String?
)
