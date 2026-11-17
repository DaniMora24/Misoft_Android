package com.misoft.misoftapp.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class ActualizarEstadoOrdenRequest(
    val estado: String
)

interface PedidosVendedorApi {

    // CONSULTAR PEDIDOS
    @GET("api/Ordenes")
    fun obtenerPedidos(): Call<JsonArray>

    // ACTUALIZAR ESTADO
    @PUT("api/Ordenes/{idOrden}/estado")
    fun actualizarEstado(
        @Path("idOrden") idOrden: Int,
        @Body request: ActualizarEstadoOrdenRequest
    ): Call<JsonObject>

    // Confirmar el dinero recibido
    @PUT("api/Ordenes/{idOrden}/confirmar-pago")
    fun confirmarPago(
        @Path("idOrden") idOrden: Int
    ): Call<JsonObject>

    // Cancelar y reponer inventario
    @PUT("api/Ordenes/{idOrden}/cancelar")
    fun cancelarPedido(
        @Path("idOrden") idOrden: Int
    ): Call<JsonObject>

}
