package com.misoft.misoftapp.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.JsonObject
import retrofit2.http.PUT

// SOLICITUD PARA CREAR UNA ORDEN
data class CrearOrdenRequest(
    val usuarioId: Int,
    val direccionEntrega: String,
    val metodoPago: String = "CONTRAENTREGA"
)

// PRODUCTO DE UNA ORDEN RECIÉN CREADA
data class ProductoOrdenCreadaResponse(
    val productoId: Int,
    val nombre: String,
    val talla: String?,
    val imagenUrl: String?,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)

// INFORMACIÓN DE LA ORDEN CREADA
data class OrdenCreadaResponse(
    val idOrden: Int,
    val usuarioId: Int,
    val fechaOrden: String,
    val estado: String,
    val metodoPago: String = "CONTRAENTREGA",
    val estadoPago: String = "PENDIENTE",
    val direccionEntrega: String?,
    val subtotal: Double,
    val costoEnvio: Double,
    val total: Double,
    val envioCalculado: Boolean = false,
    val totalDefinitivo: Boolean = false,
    val productos: List<ProductoOrdenCreadaResponse> = emptyList()
)

// RESPUESTA AL CREAR UNA ORDEN
data class CrearOrdenResponse(
    val mensaje: String,
    val orden: OrdenCreadaResponse
)

// PRIMER PRODUCTO DE UNA ORDEN
data class PrimerProductoOrdenResponse(
    val productoId: Int,
    val nombre: String,
    val talla: String?,
    val imagenUrl: String?,
    val cantidad: Int
)

// ORDEN DEL HISTORIAL
data class OrdenHistorialResponse(
    val idOrden: Int,
    val fechaOrden: String,
    val estado: String,
    val direccionEntrega: String?,
    val subtotal: Double,
    val costoEnvio: Double,
    val total: Double,
    val cantidadProductos: Int,
    val cantidadProductosDiferentes: Int,
    val primerProducto: PrimerProductoOrdenResponse?,
    val metodoPago: String? = null,
    val estadoPago: String? = null
)

// PRODUCTO COMPRADO
data class DetalleProductoOrdenResponse(
    val idDetalle: Int,
    val productoId: Int,
    val nombre: String,
    val talla: String?,
    val imagenUrl: String?,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)

// INFORMACIÓN COMPLETA DE LA ORDEN
data class OrdenDetalleResponse(
    val idOrden: Int,
    val usuarioId: Int,
    val fechaOrden: String,
    val estado: String,
    val direccionEntrega: String?,
    val subtotal: Double,
    val costoEnvio: Double,
    val total: Double,
    val productos: List<DetalleProductoOrdenResponse>,
    val metodoPago: String? = null,
    val estadoPago: String? = null
)

// ENDPOINTS DE ÓRDENES
interface OrdenesApi {

    // CREAR ORDEN CON PAGO CONTRAENTREGA
    @POST("api/Ordenes/crear")
    fun crearOrden(
        @Body request: CrearOrdenRequest
    ): Call<CrearOrdenResponse>

    // CONSULTAR HISTORIAL DEL COMPRADOR
    @GET("api/Ordenes/usuario/{idUsuario}")
    fun obtenerHistorial(
        @Path("idUsuario") idUsuario: Int
    ): Call<List<OrdenHistorialResponse>>

    // CONSULTAR DETALLE DE UNA ORDEN
    @GET("api/Ordenes/{idOrden}")
    fun obtenerDetalleOrden(
        @Path("idOrden") idOrden: Int,
        @Query("idUsuario") idUsuario: Int
    ): Call<OrdenDetalleResponse>

    @PUT("api/Ordenes/{idOrden}/confirmar-pago")
    fun confirmarPago(
        @Path("idOrden") idOrden: Int
    ): Call<JsonObject>


    // CANCELAR PEDIDO DEL COMPRADOR
    @PUT("api/Ordenes/usuario/{idUsuario}/orden/{idOrden}/cancelar")
    fun cancelarPedidoComprador(
        @Path("idUsuario") idUsuario: Int,
        @Path("idOrden") idOrden: Int
    ): Call<JsonObject>

}
