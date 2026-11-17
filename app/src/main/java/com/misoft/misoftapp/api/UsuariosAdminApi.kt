package com.misoft.misoftapp.api

import com.google.gson.JsonObject
import com.misoft.misoftapp.models.UsuarioAdminResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuariosAdminApi {

    @GET("api/UsuariosAdmin")
    fun obtenerUsuarios():
            Call<List<UsuarioAdminResponse>>

    @GET("api/UsuariosAdmin/{idUsuario}")
    fun obtenerUsuario(
        @Path("idUsuario") idUsuario: Int
    ): Call<UsuarioAdminResponse>

    // ACTIVAR O DESHABILITAR
    @PUT("api/UsuariosAdmin/{idUsuario}/estado")
    fun cambiarEstadoUsuario(
        @Path("idUsuario") idUsuario: Int,
        @Body request: JsonObject
    ): Call<JsonObject>

}
