package com.misoft.misoftapp.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductosAdminApi {

    @GET("api/Productos/vendedor/{idVendedor}")
    fun obtenerPorVendedor(
        @Path("idVendedor") idVendedor: Int
    ): Call<JsonArray>

    // Obtener las unidades vendidas de un producto
    @GET("api/ProductosAdmin/{idProducto}/ventas")
    fun obtenerVentasProducto(
        @Path("idProducto") idProducto: Int
    ): Call<JsonObject>

}
