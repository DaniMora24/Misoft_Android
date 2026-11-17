package com.misoft.misoftapp.models

data class UsuarioAdminResponse(
    val idUsuario: Int,
    val tipoDocumento: String?,
    val numeroDocumento: String?,
    val nombres: String?,
    val apellidos: String?,
    val telefono: String?,
    val fechaNacimiento: String?,
    val email: String?,
    val fotoPerfil: String?,
    val rolId: Int,
    val estado: Boolean,
    val fechaRegistro: String?,
    val cantidadCompras: Int
)
