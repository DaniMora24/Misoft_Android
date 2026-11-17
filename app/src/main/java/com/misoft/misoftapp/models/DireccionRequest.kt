package com.misoft.misoftapp.models

import com.google.gson.annotations.SerializedName

data class DireccionRequest(

    @SerializedName("usuarioId")
    val usuarioId: Int,

    @SerializedName("nombreDireccion")
    val nombreDireccion: String,

    @SerializedName("direccion")
    val direccion: String,

    @SerializedName("ciudad")
    val ciudad: String?,

    @SerializedName("barrio")
    val barrio: String?,

    @SerializedName("referencia")
    val referencia: String?,

    @SerializedName("latitud")
    val latitud: Double?,

    @SerializedName("longitud")
    val longitud: Double?,

    @SerializedName("principal")
    val principal: Boolean
)
