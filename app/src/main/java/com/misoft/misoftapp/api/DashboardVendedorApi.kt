package com.misoft.misoftapp.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface DashboardVendedorApi {

    @GET("api/DashboardVendedor/resumen/{idVendedor}")
    fun obtenerResumen(
        @Path("idVendedor") idVendedor: Int
    ): Call<JsonObject>
}
