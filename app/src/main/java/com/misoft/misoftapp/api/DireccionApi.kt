
package com.misoft.misoftapp.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.misoft.misoftapp.models.DireccionRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DireccionApi {

    // CREAR DIRECCIÓN
    @POST("api/Direcciones")
    fun crearDireccion(
        @Body direccion: DireccionRequest
    ): Call<JsonObject>

    // CONSULTAR DIRECCIONES DEL COMPRADOR
    @GET("api/Direcciones/usuario/{idUsuario}")
    fun obtenerDirecciones(
        @Path("idUsuario") idUsuario: Int
    ): Call<JsonArray>

    // EDITAR DIRECCIÓN
    @PUT("api/Direcciones/{idDireccion}")
    fun actualizarDireccion(
        @Path("idDireccion") idDireccion: Int,
        @Body direccion: DireccionRequest
    ): Call<JsonObject>

    // ELIMINAR DIRECCIÓN
    @DELETE("api/Direcciones/{idDireccion}")
    fun eliminarDireccion(
        @Path("idDireccion") idDireccion: Int,
        @Query("idUsuario") idUsuario: Int
    ): Call<JsonObject>
}
