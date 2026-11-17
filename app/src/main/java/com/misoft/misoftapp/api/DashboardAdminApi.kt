package com.misoft.misoftapp.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET

interface DashboardAdminApi {

    @GET("api/DashboardAdmin/resumen")
    fun obtenerResumen(): Call<JsonObject>
}
