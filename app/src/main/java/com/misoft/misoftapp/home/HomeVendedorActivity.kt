package com.misoft.misoftapp.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.catalogo.ProductosVendedorActivity
import com.misoft.misoftapp.pedidos.GestionPedidosActivity
import com.misoft.misoftapp.perfil_vendedor.PerfilVendedorActivity
import java.text.NumberFormat
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeVendedorActivity : AppCompatActivity() {

    // ==========================================
    // COMPONENTES DEL HOME
    // ==========================================

    private lateinit var txtSaludoVendedor: TextView
    private lateinit var txtProductos: TextView
    private lateinit var txtVentas: TextView
    private lateinit var txtPedidos: TextView
    private lateinit var txtStockBajoVendedor: TextView
    private lateinit var txtPedidosPrepararVendedor: TextView
    private lateinit var txtAlertaStockVendedor: TextView
    private lateinit var imgVendedor: ImageView
    private var llamadaDashboard: Call<JsonObject>? = null
    private var llamadaPerfil: Call<JsonObject>? = null

    // Servidor local accesible desde el celular
    private val servidor = "http://192.168.78.111:5271"

    // INICIAR HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.vendedor_activity_home)

        inicializarVistas()
        mostrarDatosVendedor()
        configurarAcciones()
        configurarNavegacion()
    }

    override fun onResume() {
        super.onResume()

        mostrarDatosVendedor()

        // Actualizar estadísticas
        cargarDashboard()

        // Actualizar fotografía
        cargarFotoPerfil()
    }

    // VINCULAR VISTAS
    private fun inicializarVistas() {

        txtSaludoVendedor =
            findViewById(R.id.txtSaludoVendedor)

        txtProductos =
            findViewById(R.id.txtProductos)

        txtVentas =
            findViewById(R.id.txtVentas)

        txtPedidos =
            findViewById(R.id.txtPedidos)

        txtStockBajoVendedor =
            findViewById(R.id.txtStockBajoVendedor)

        txtPedidosPrepararVendedor =
            findViewById(R.id.txtPedidosPrepararVendedor)

        txtAlertaStockVendedor =
            findViewById(R.id.txtAlertaStockVendedor)

        // Imagen definida en vendedor_activity_home.xml
        imgVendedor =
            findViewById(R.id.imgVendedor)
    }

    // OBTENER ID DEL VENDEDOR

    private fun obtenerIdVendedor(): Int {

        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val idUsuario = preferencias.getInt(
            "idUsuario",
            -1
        )

        val rolId = preferencias.getInt(
            "rolId",
            -1
        )

        return if (idUsuario > 0 && rolId == 2) {
            idUsuario
        } else {
            -1
        }
    }

    // ==========================================
    // MOSTRAR NOMBRE DEL VENDEDOR
    // ==========================================

    private fun mostrarDatosVendedor() {

        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val nombreCompleto = preferencias.getString(
            "nombreCompleto",
            ""
        ).orEmpty()

        val primerNombre = nombreCompleto
            .trim()
            .split("\\s+".toRegex())
            .firstOrNull()
            .orEmpty()

        txtSaludoVendedor.text =
            if (primerNombre.isNotBlank()) {
                "Hola, $primerNombre 👋"
            } else {
                "Hola, Vendedor 👋"
            }
    }

    // CARGAR FOTO REAL DESDE LA API

    private fun cargarFotoPerfil() {

        val idVendedor = obtenerIdVendedor()

        if (idVendedor <= 0) {

            imgVendedor.setImageResource(
                R.drawable.usuario
            )

            return
        }

        llamadaPerfil?.cancel()

        val llamada = ApiClient.perfilApi
            .obtenerPerfil(idVendedor)

        llamadaPerfil = llamada

        llamada.enqueue(object : Callback<JsonObject> {

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {

                if (
                    call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) return

                if (!response.isSuccessful) {

                    Log.e(
                        "FOTO_VENDEDOR",
                        "Error HTTP ${response.code()}"
                    )

                    imgVendedor.setImageResource(
                        R.drawable.usuario
                    )

                    return
                }

                val datos = response.body()

                if (datos == null) {

                    imgVendedor.setImageResource(
                        R.drawable.usuario
                    )

                    return
                }

                val fotoUrl = try {
                    datos.get("fotoUrl")
                        ?.takeUnless { it.isJsonNull }
                        ?.asString
                        .orEmpty()
                } catch (e: Exception) {
                    ""
                }

                if (fotoUrl.isBlank()) {

                    Glide.with(this@HomeVendedorActivity)
                        .clear(imgVendedor)

                    imgVendedor.setImageResource(
                        R.drawable.usuario
                    )

                    return
                }

                val urlCompleta =
                    obtenerUrlImagen(fotoUrl)

                Log.d(
                    "FOTO_VENDEDOR",
                    "URL imagen: $urlCompleta"
                )

                Glide.with(this@HomeVendedorActivity)
                    .load(urlCompleta)
                    .placeholder(R.drawable.usuario)
                    .error(R.drawable.usuario)
                    .circleCrop()
                    .into(imgVendedor)
            }

            override fun onFailure(
                call: Call<JsonObject>,
                t: Throwable
            ) {

                if (
                    call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) return

                Log.e(
                    "FOTO_VENDEDOR",
                    "No fue posible cargar el perfil",
                    t
                )

                imgVendedor.setImageResource(
                    R.drawable.usuario
                )
            }
        })
    }

    // CONSTRUIR URL DE LA IMAGEN

    private fun obtenerUrlImagen(
        ruta: String
    ): String {

        val rutaLimpia = ruta.trim()

        if (
            rutaLimpia.startsWith("http://") ||
            rutaLimpia.startsWith("https://")
        ) {
            return rutaLimpia
        }

        return servidor.trimEnd('/') +
                "/" +
                rutaLimpia.trimStart('/')
    }

    // CONSULTAR DASHBOARD DESDE MYSQL

    private fun cargarDashboard() {

        llamadaDashboard?.cancel()

        val idVendedor = obtenerIdVendedor()

        if (idVendedor <= 0) {

            mostrarError(
                "No se encontró una sesión válida de vendedor."
            )

            return
        }

        mostrarCargando()

        val llamada = ApiClient.dashboardVendedorApi
            .obtenerResumen(idVendedor)

        llamadaDashboard = llamada

        llamada.enqueue(object : Callback<JsonObject> {

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {

                if (
                    call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) return

                if (response.isSuccessful) {

                    val datos = response.body()

                    if (datos != null) {
                        actualizarDashboard(datos)
                    } else {
                        mostrarError(
                            "El servidor no devolvió datos."
                        )
                    }

                } else {

                    Log.e(
                        "DASHBOARD_VENDEDOR",
                        "HTTP ${response.code()}"
                    )

                    mostrarError(
                        "No fue posible cargar las estadísticas. " +
                                "HTTP ${response.code()}"
                    )
                }
            }

            override fun onFailure(
                call: Call<JsonObject>,
                t: Throwable
            ) {

                if (
                    call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) return

                Log.e(
                    "DASHBOARD_VENDEDOR",
                    "Error de conexión con la API",
                    t
                )

                mostrarError(
                    "No se pudo conectar con la API."
                )
            }
        })
    }


    private fun actualizarDashboard(
        datos: JsonObject
    ) {

        val totalProductos =
            obtenerEntero(datos, "totalProductos")

        val pedidosPendientes =
            obtenerEntero(datos, "pedidosPendientes")

        val pedidosPorPreparar =
            obtenerEntero(datos, "pedidosPorPreparar")

        val totalStockBajo =
            obtenerEntero(datos, "totalStockBajo")

        val ventasConfirmadas =
            obtenerDecimal(datos, "ventasConfirmadas")

        val formatoPesos = NumberFormat
            .getCurrencyInstance(
                Locale.forLanguageTag("es-CO")
            )

        formatoPesos.maximumFractionDigits = 0

        txtProductos.text =
            totalProductos.toString()

        txtPedidos.text =
            pedidosPendientes.toString()

        txtVentas.text =
            formatoPesos.format(ventasConfirmadas)

        txtStockBajoVendedor.text =
            totalStockBajo.toString()

        txtPedidosPrepararVendedor.text =
            if (pedidosPorPreparar > 0) {
                "🧶 $pedidosPorPreparar pedido(s) por preparar"
            } else {
                "✅ No tienes pedidos por preparar"
            }

        val productosStockBajo =
            datos.getAsJsonArray("productosStockBajo")

        mostrarAlertasStock(productosStockBajo)
    }


    private fun mostrarAlertasStock(
        productos: JsonArray?
    ) {

        if (
            productos == null ||
            productos.size() == 0
        ) {

            txtAlertaStockVendedor.text =
                "✅ Todos tus productos tienen stock suficiente"

            return
        }

        val nombres = mutableListOf<String>()

        productos.forEach { elemento ->

            if (!elemento.isJsonObject) {
                return@forEach
            }

            val nombre = obtenerTexto(
                elemento.asJsonObject,
                "nombre"
            )

            if (nombre.isNotBlank()) {
                nombres.add(nombre)
            }
        }

        val primeros = nombres.take(3)

        txtAlertaStockVendedor.text =
            if (primeros.isEmpty()) {

                "⚠️ Tienes productos que necesitan reposición"

            } else {

                val adicionales =
                    nombres.size - primeros.size

                val texto =
                    primeros.joinToString(", ")

                if (adicionales > 0) {
                    "⚠️ Reponer: $texto y $adicionales más"
                } else {
                    "⚠️ Reponer: $texto"
                }
            }
    }


    private fun obtenerEntero(
        datos: JsonObject,
        clave: String
    ): Int {

        return try {

            datos.get(clave)
                ?.takeUnless { it.isJsonNull }
                ?.asInt ?: 0

        } catch (e: Exception) {
            0
        }
    }

    private fun obtenerDecimal(
        datos: JsonObject,
        clave: String
    ): Double {

        return try {

            datos.get(clave)
                ?.takeUnless { it.isJsonNull }
                ?.asDouble ?: 0.0

        } catch (e: Exception) {
            0.0
        }
    }

    private fun obtenerTexto(
        datos: JsonObject,
        clave: String
    ): String {

        return try {

            datos.get(clave)
                ?.takeUnless { it.isJsonNull }
                ?.asString.orEmpty()

        } catch (e: Exception) {
            ""
        }
    }
    // ESTADOS DE CARGA Y ERROR

    private fun mostrarCargando() {

        txtProductos.text = "—"
        txtPedidos.text = "—"
        txtVentas.text = "$ —"
        txtStockBajoVendedor.text = "—"

        txtPedidosPrepararVendedor.text =
            "🧶 Consultando pedidos..."

        txtAlertaStockVendedor.text =
            "📦 Consultando productos..."
    }

    private fun mostrarError(
        mensaje: String
    ) {

        txtProductos.text = "—"
        txtPedidos.text = "—"
        txtVentas.text = "—"
        txtStockBajoVendedor.text = "—"

        txtPedidosPrepararVendedor.text =
            mensaje

        txtAlertaStockVendedor.text =
            "No se pudieron cargar las alertas."

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    // ACCIONES DEL HOME

    private fun configurarAcciones() {

        // Acceso para crear productos
        findViewById<CardView>(
            R.id.cardCrearProducto
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ProductosVendedorActivity::class.java
                )
            )
        }

        // Abrir Perfil al tocar la fotografía
        findViewById<FrameLayout>(
            R.id.contenedorFotoVendedor
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    PerfilVendedorActivity::class.java
                )
            )
        }

        // Notificaciones
        findViewById<ImageView>(
            R.id.imgNotificaciones
        ).setOnClickListener {

            Toast.makeText(
                this,
                "Las alertas de tu tienda aparecen " +
                        "en Atención requerida.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // MENÚ INFERIOR

    private fun configurarNavegacion() {

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationSeller
            )

        bottomNavigation.selectedItemId = R.id.home

        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.home -> true

                R.id.productos -> {

                    startActivity(
                        Intent(
                            this,
                            ProductosVendedorActivity::class.java
                        )
                    )

                    true
                }

                R.id.pedidos -> {

                    startActivity(
                        Intent(
                            this,
                            GestionPedidosActivity::class.java
                        )
                    )

                    true
                }

                R.id.perfil -> {

                    startActivity(
                        Intent(
                            this,
                            PerfilVendedorActivity::class.java
                        )
                    )

                    true
                }

                else -> false
            }
        }
    }

    override fun onDestroy() {

        llamadaDashboard?.cancel()
        llamadaPerfil?.cancel()

        super.onDestroy()
    }
}
