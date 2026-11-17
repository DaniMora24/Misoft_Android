
package com.misoft.misoftapp.models

data class AutenticarBiometriaRequest(
    val idDesafio: Int,
    val idDispositivo: Int,
    val firma: String
)
