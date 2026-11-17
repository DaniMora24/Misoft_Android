package com.misoft.misoftapp.models

data class LoginResponse(
    val idUsuario: Int,
    val nombreCompleto: String,
    val email: String,
    val rolId: Int,
    val estado: Boolean
)