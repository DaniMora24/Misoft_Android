package com.misoft.misoftapp.models

data class RegisterRequest(
    val tipoDocumento: String,
    val numeroDocumento: String,
    val nombres: String,
    val apellidos: String,
    val telefono: String,
    val fechaNacimiento: String,
    val email: String,
    val password: String
)