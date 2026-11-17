
package com.misoft.misoftapp.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

// RESPUESTA AL SUBIR UNA IMAGEN
data class SubirImagenResponse(
    val mensaje: String,
    val imagenUrl: String
)

// INFORMACIÓN DE UN PRODUCTO
data class ProductoResponse(
    val idProducto: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val stockMinimo: Int,
    val talla: String?,
    val imagenUrl: String?,
    val categoriaId: Int,
    val idVendedor: Int,
    val estado: Boolean
)

// DATOS PARA REGISTRAR UN PRODUCTO
data class RegistrarProductoRequest(
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val stockMinimo: Int,
    val talla: String?,
    val imagenUrl: String?,
    val categoriaId: Int,
    val idVendedor: Int
)

// RESPUESTA AL EDITAR UN PRODUCTO
data class EditarProductoResponse(
    val mensaje: String,
    val producto: ProductoResponse
)


data class CambiarEstadoProductoResponse(
    val mensaje: String,
    val estado: Boolean
)


interface ProductosApi {

    // SUBIR IMAGEN AL SERVIDOR
    @Multipart
    @POST("api/Productos/subir-imagen")
    fun subirImagen(
        @Part imagen: MultipartBody.Part
    ): Call<SubirImagenResponse>

    // CONSULTAR PRODUCTOS DEL VENDEDOR
    @GET("api/Productos/vendedor/{idVendedor}")
    fun obtenerProductosVendedor(
        @Path("idVendedor") idVendedor: Int
    ): Call<List<ProductoResponse>>

    // REGISTRAR PRODUCTO
    @POST("api/Productos")
    fun registrarProducto(
        @Body producto: RegistrarProductoRequest
    ): Call<ProductoResponse>

    // EDITAR PRODUCTO
    @PUT("api/Productos/{id}")
    fun editarProducto(
        @Path("id") idProducto: Int,
        @Body producto: RegistrarProductoRequest
    ): Call<EditarProductoResponse>


    // ACTIVAR O DESACTIVAR PRODUCTO
    @PATCH("api/Productos/estado/{id}")
    fun cambiarEstadoProducto(
        @Path("id") idProducto: Int
    ): Call<CambiarEstadoProductoResponse>


    // CONSULTAR UN PRODUCTO POR SU ID
    @GET("api/Productos/{id}")
    fun obtenerProductoPorId(
        @Path("id") idProducto: Int
    ): Call<ProductoResponse>

    @GET("api/Productos")
    fun obtenerProductos(): Call<List<ProductoResponse>>


}
