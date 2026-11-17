package com.misoft.misoftapp.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PerfilApi {

    // Consultar fotografía y nombre del usuario
    @GET("api/Perfil/{idUsuario}")
    fun obtenerPerfil(
        @Path("idUsuario") idUsuario: Int
    ): Call<JsonObject>

    // Subir o cambiar la fotografía
    @Multipart
    @POST("api/Perfil/foto")
    fun cambiarFoto(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part foto: MultipartBody.Part
    ): Call<JsonObject>
}
