package com.misoft.misoftapp.api

import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET

interface VendedoresAdminApi {

    @GET("api/UsuariosAdmin")
    fun obtenerVendedores(): Call<JsonElement>
}
