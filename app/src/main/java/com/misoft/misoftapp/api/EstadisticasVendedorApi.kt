package com.misoft.misoftapp.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EstadisticasVendedorApi {

    @GET("api/EstadisticasVendedor/resumen/{idVendedor}")
    fun obtenerResumen(
        @Path("idVendedor") idVendedor: Int,
        @Query("periodo") periodo: String,
        @Query("fechaInicio") fechaInicio: String? = null,
        @Query("fechaFin") fechaFin: String? = null
    ): Call<JsonObject>
}
