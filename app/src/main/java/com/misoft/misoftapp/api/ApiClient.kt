package com.misoft.misoftapp.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {


    const val BASE_URL = "http://192.168.78.111:5271/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val productosApi: ProductosApi by lazy {
        retrofit.create(ProductosApi::class.java)
    }

    val carritoApi: CarritoApi by lazy {
        retrofit.create(CarritoApi::class.java)
    }

    val ordenesApi: OrdenesApi by lazy {
        retrofit.create(OrdenesApi::class.java)
    }

    val direccionApi: DireccionApi by lazy {
        retrofit.create(DireccionApi::class.java)
    }

    val pedidosVendedorApi: PedidosVendedorApi by lazy {
        retrofit.create(PedidosVendedorApi::class.java)
    }

    val usuariosAdminApi: UsuariosAdminApi by lazy {
        retrofit.create(UsuariosAdminApi::class.java)
    }

    val dashboardAdminApi: DashboardAdminApi by lazy {
        retrofit.create(DashboardAdminApi::class.java)
    }
    val perfilApi: PerfilApi by lazy {
        retrofit.create(PerfilApi::class.java)
    }

    val reportesAdminApi: ReportesAdminApi by lazy {
        retrofit.create(ReportesAdminApi::class.java)
    }

    val productosAdminApi: ProductosAdminApi by lazy {
        retrofit.create(ProductosAdminApi::class.java)
    }

    val vendedoresAdminApi: VendedoresAdminApi by lazy {
        retrofit.create(VendedoresAdminApi::class.java)
    }

    val dashboardVendedorApi: DashboardVendedorApi by lazy {
        retrofit.create(DashboardVendedorApi::class.java)
    }

    val estadisticasVendedorApi: EstadisticasVendedorApi by lazy {
        retrofit.create(EstadisticasVendedorApi::class.java)
    }

}
