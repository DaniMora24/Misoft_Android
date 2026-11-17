package com.misoft.misoftapp.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// PRODUCTO DENTRO DEL CARRITO
data class ProductoCarritoResponse(
    val idDetalle: Int,
    val productoId: Int,
    val nombre: String,
    val precio: Double,
    val imagenUrl: String?,
    val cantidad: Int,
    val stockDisponible: Int,
    val disponible: Boolean,
    val subtotal: Double
)

// RESPUESTA DEL CARRITO
data class CarritoResponse(
    val idCarrito: Int,
    val usuarioId: Int,
    val productos: List<ProductoCarritoResponse>,
    val total: Double,
    val cantidadProductos: Int
)

// AGREGAR PRODUCTO
data class AgregarCarritoRequest(
    val usuarioId: Int,
    val productoId: Int,
    val cantidad: Int
)

// ACTUALIZAR CANTIDAD
data class ActualizarCantidadCarritoRequest(
    val usuarioId: Int,
    val cantidad: Int
)

// RESPUESTA DE OPERACIONES
data class OperacionCarritoResponse(
    val mensaje: String,
    val carrito: CarritoResponse
)

interface CarritoApi {

    // CONSULTAR CARRITO
    @GET("api/Carrito/usuario/{idUsuario}")
    fun obtenerCarrito(
        @Path("idUsuario") idUsuario: Int
    ): Call<CarritoResponse>

    // AGREGAR PRODUCTO
    @POST("api/Carrito/agregar")
    fun agregarProducto(
        @Body request: AgregarCarritoRequest
    ): Call<OperacionCarritoResponse>

    // ACTUALIZAR CANTIDAD
    @PUT("api/Carrito/detalle/{idDetalle}")
    fun actualizarCantidad(
        @Path("idDetalle") idDetalle: Int,
        @Body request: ActualizarCantidadCarritoRequest
    ): Call<OperacionCarritoResponse>

    // ELIMINAR PRODUCTO
    @DELETE("api/Carrito/detalle/{idDetalle}")
    fun eliminarProducto(
        @Path("idDetalle") idDetalle: Int,
        @Query("idUsuario") idUsuario: Int
    ): Call<OperacionCarritoResponse>

    // VACIAR CARRITO
    @DELETE("api/Carrito/vaciar/{idUsuario}")
    fun vaciarCarrito(
        @Path("idUsuario") idUsuario: Int
    ): Call<OperacionCarritoResponse>
}
