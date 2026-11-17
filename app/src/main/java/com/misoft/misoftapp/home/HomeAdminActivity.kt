package com.misoft.misoftapp.home

import com.misoft.misoftapp.auth.LoginActivity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.InputType
import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.estadisticas.ReportesAdminActivity
import com.misoft.misoftapp.usuarios.UsuariosAdminActivity
import com.misoft.misoftapp.usuarios.VendedoresAdminActivity
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeAdminActivity : AppCompatActivity() {

    private lateinit var txtTotalUsuarios: TextView
    private lateinit var txtTotalPedidos: TextView
    private lateinit var txtTotalProductos: TextView
    private lateinit var txtVentasMes: TextView

    private lateinit var txtUsuariosActivos: TextView
    private lateinit var txtUsuariosInactivos: TextView
    private lateinit var txtVendedoresActivos: TextView
    private lateinit var txtVendedoresInactivos: TextView
    private lateinit var txtProductosVisibles: TextView
    private lateinit var txtProductosOcultos: TextView

    private lateinit var txtPedidosPendientes: TextView
    private lateinit var txtPedidosEnCamino: TextView
    private lateinit var txtPedidosEntregados: TextView
    private lateinit var txtPedidosCancelados: TextView

    private lateinit var contenedorActividad: LinearLayout

    private var llamadaDashboard: Call<JsonObject>? = null
    private var llamadaPerfil: Call<JsonObject>? = null
    private var llamadaSubidaFoto: Call<JsonObject>? = null

    private val seleccionarFoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        if (uri != null) {
            subirFotoPerfil(uri)
        }
    }


// CARGAR FOTO DE PERFIL DESDE LA API

    private fun cargarFotoPerfil() {

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

        if (idUsuario <= 0 || rolId != 1) {
            return
        }

        llamadaPerfil?.cancel()

        val llamada = ApiClient.perfilApi
            .obtenerPerfil(idUsuario)

        llamadaPerfil = llamada

        llamada.enqueue(object : Callback<JsonObject> {

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {

                if (call.isCanceled ||
                    isFinishing ||
                    isDestroyed) return

                if (!response.isSuccessful) {
                    Log.e(
                        "PERFIL_ADMIN",
                        "Error HTTP ${response.code()}"
                    )
                    return
                }

                val datos = response.body() ?: return

                val imagen = findViewById<ImageView>(
                    R.id.imgAdmin
                )

                val fotoUrl = datos.get("fotoUrl")
                    ?.takeUnless { it.isJsonNull }
                    ?.asString
                    .orEmpty()

                if (fotoUrl.isBlank()) {

                    Glide.with(this@HomeAdminActivity)
                        .clear(imagen)

                    imagen.setImageResource(
                        R.drawable.usuario
                    )

                    return
                }

                Glide.with(this@HomeAdminActivity)
                    .load(fotoUrl)
                    .placeholder(R.drawable.usuario)
                    .error(R.drawable.usuario)
                    .circleCrop()
                    .into(imagen)
            }

            override fun onFailure(
                call: Call<JsonObject>,
                t: Throwable
            ) {

                if (call.isCanceled ||
                    isFinishing ||
                    isDestroyed) return

                Log.e(
                    "PERFIL_ADMIN",
                    "No se pudo consultar el perfil",
                    t
                )
            }
        })
    }

// SUBIR FOTOGRAFÍA A LA API .NET

    private fun subirFotoPerfil(uri: Uri) {

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

        if (idUsuario <= 0 || rolId != 1) {

            mostrarMensaje(
                "Debes iniciar sesión como Administrador."
            )

            return
        }

        if (llamadaSubidaFoto != null) {
            mostrarMensaje(
                "Ya se está subiendo una fotografía."
            )
            return
        }

        mostrarMensaje("Subiendo fotografía...")

        Thread {

            try {

                val tipo = contentResolver.getType(uri)
                    ?: "application/octet-stream"

                val tiposPermitidos = listOf(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
                )

                if (tipo !in tiposPermitidos) {
                    throw IllegalArgumentException(
                        "Selecciona una imagen JPG, PNG o WEBP."
                    )
                }

                val limite = 5 * 1024 * 1024

                val datos = contentResolver
                    .openInputStream(uri)
                    ?.use { entrada ->

                        val salida = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var total = 0

                        while (true) {

                            val leidos = entrada.read(buffer)

                            if (leidos == -1) break

                            total += leidos

                            if (total > limite) {
                                throw IllegalArgumentException(
                                    "La imagen supera los 5 MB."
                                )
                            }

                            salida.write(
                                buffer,
                                0,
                                leidos
                            )
                        }

                        salida.toByteArray()
                    }
                    ?: throw IllegalArgumentException(
                        "No fue posible abrir la imagen."
                    )

                if (datos.isEmpty()) {
                    throw IllegalArgumentException(
                        "La imagen está vacía."
                    )
                }

                val extension = when (tipo) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    else -> "jpg"
                }

                val archivo = MultipartBody.Part
                    .createFormData(
                        "foto",
                        "perfil.$extension",
                        datos.toRequestBody(
                            tipo.toMediaType()
                        )
                    )

                val tipoTexto = "text/plain".toMediaType()

                val cuerpoIdUsuario = idUsuario
                    .toString()
                    .toRequestBody(tipoTexto)

                runOnUiThread {

                    if (isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    val llamada = ApiClient.perfilApi
                        .cambiarFoto(
                            cuerpoIdUsuario,
                            archivo
                        )

                    llamadaSubidaFoto = llamada

                    llamada.enqueue(
                        object : Callback<JsonObject> {

                            override fun onResponse(
                                call: Call<JsonObject>,
                                response: Response<JsonObject>
                            ) {

                                if (llamadaSubidaFoto === call) {
                                    llamadaSubidaFoto = null
                                }

                                if (call.isCanceled ||
                                    isFinishing ||
                                    isDestroyed) return

                                if (response.isSuccessful) {

                                    mostrarMensaje(
                                        "Fotografía actualizada correctamente."
                                    )

                                    cargarFotoPerfil()

                                } else {

                                    val mensaje = when (response.code()) {

                                        400 ->
                                            "La imagen no es válida."

                                        403 ->
                                            "La operación no está permitida."

                                        404 ->
                                            "El usuario no existe."

                                        413 ->
                                            "La imagen supera el tamaño permitido."

                                        else ->
                                            "No se pudo subir la fotografía. " +
                                                    "HTTP ${response.code()}"
                                    }

                                    mostrarMensaje(mensaje)
                                }
                            }

                            override fun onFailure(
                                call: Call<JsonObject>,
                                t: Throwable
                            ) {

                                if (llamadaSubidaFoto === call) {
                                    llamadaSubidaFoto = null
                                }

                                if (call.isCanceled ||
                                    isFinishing ||
                                    isDestroyed) return

                                Log.e(
                                    "PERFIL_ADMIN",
                                    "Error al subir la fotografía",
                                    t
                                )

                                mostrarMensaje(
                                    "No se pudo confirmar la subida. " +
                                            "Consulta el perfil antes de reintentar."
                                )
                            }
                        }
                    )
                }

            } catch (e: Exception) {

                runOnUiThread {

                    if (!isFinishing && !isDestroyed) {

                        mostrarMensaje(
                            e.message
                                ?: "No se pudo procesar la imagen."
                        )
                    }
                }
            }
        }.start()
    }

    // INICIAR HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.admin_activity_home)

        inicializarVistas()
        configurarNavegacion()
        mostrarDatosAdministrador()

        findViewById<View>(
            R.id.btnCerrarSesion
        ).setOnClickListener {
            confirmarCerrarSesion()
        }

        // Abrir la galería al tocar la foto.
        findViewById<FrameLayout>(
            R.id.contenedorFotoAdmin
        ).setOnClickListener {

            seleccionarFoto.launch("image/*")
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDashboard()
        cargarFotoPerfil()
    }


// ==========================================
// CONFIRMAR CIERRE DE SESIÓN
// ==========================================

    private fun confirmarCerrarSesion() {

        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage(
                "¿Estás seguro de que deseas cerrar sesión?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Cerrar sesión") { _, _ ->
                cerrarSesion()
            }
            .show()
    }

// CERRAR SESIÓN

    private fun cerrarSesion() {

        // Cancelar consultas pendientes
        llamadaDashboard?.cancel()
        llamadaPerfil?.cancel()
        llamadaSubidaFoto?.cancel()

        // Eliminar datos de la sesión local
        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        preferencias.edit()
            .clear()
            .apply()

        // Volver al Login
        val intent = Intent(
            this,
            LoginActivity::class.java
        )

        // Impedir regresar con el botón Atrás
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }


    private fun mostrarDatosAdministrador() {

        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val idUsuario = preferencias.getInt(
            "idUsuario",
            -1
        )

        val nombreCompleto = preferencias.getString(
            "nombreCompleto",
            ""
        ).orEmpty()

        val rolId = preferencias.getInt(
            "rolId",
            -1
        )

        val txtSaludoAdmin = findViewById<TextView>(
            R.id.txtSaludoAdmin
        )

        if (idUsuario == -1 || rolId != 1) {
            txtSaludoAdmin.text = "Hola, Administrador"
            return
        }

        val primerNombre = nombreCompleto
            .trim()
            .split("\\s+".toRegex())
            .firstOrNull()
            .orEmpty()

        txtSaludoAdmin.text =
            if (primerNombre.isNotBlank()) {
                "Hola, $primerNombre 👋"
            } else {
                "Hola, Administrador 👋"
            }
    }


    // VINCULAR XML CON KOTLIN

    private fun inicializarVistas() {

        txtTotalUsuarios =
            findViewById(R.id.txtTotalUsuarios)

        txtTotalPedidos =
            findViewById(R.id.txtTotalPedidos)

        txtTotalProductos =
            findViewById(R.id.txtTotalProductos)

        txtVentasMes =
            findViewById(R.id.txtVentasMes)

        txtUsuariosActivos =
            findViewById(R.id.txtUsuariosActivos)

        txtUsuariosInactivos =
            findViewById(R.id.txtUsuariosInactivos)

        txtVendedoresActivos =
            findViewById(R.id.txtVendedoresActivos)

        txtVendedoresInactivos =
            findViewById(R.id.txtVendedoresInactivos)

        txtProductosVisibles =
            findViewById(R.id.txtProductosVisibles)

        txtProductosOcultos =
            findViewById(R.id.txtProductosOcultos)

        txtPedidosPendientes =
            findViewById(R.id.txtPedidosPendientes)

        txtPedidosEnCamino =
            findViewById(R.id.txtPedidosEnCamino)

        txtPedidosEntregados =
            findViewById(R.id.txtPedidosEntregados)

        txtPedidosCancelados =
            findViewById(R.id.txtPedidosCancelados)

        contenedorActividad =
            findViewById(R.id.contenedorActividadReciente)
    }

    // CARGAR ESTADÍSTICAS DESDE MYSQL

    private fun cargarDashboard() {

        llamadaDashboard?.cancel()

        mostrarCargando()

        val llamada =
            ApiClient.dashboardAdminApi.obtenerResumen()

        llamadaDashboard = llamada

        llamada.enqueue(
            object : Callback<JsonObject> {

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

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

                        mostrarError(
                            "No fue posible cargar el panel. " +
                                    "HTTP ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<JsonObject>,
                    t: Throwable
                ) {

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    mostrarError(
                        "No se pudo conectar con la API."
                    )
                }
            }
        )
    }

    // ACTUALIZAR TARJETAS

    private fun actualizarDashboard(datos: JsonObject) {

        txtTotalUsuarios.text =
            obtenerEntero(datos, "totalUsuarios").toString()

        txtTotalPedidos.text =
            obtenerEntero(datos, "totalPedidos").toString()

        txtTotalProductos.text =
            obtenerEntero(datos, "totalProductos").toString()

        val ventas = obtenerDecimal(datos, "ventasMes")

        val formatoPesos =
            NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("es-CO")
            )

        formatoPesos.maximumFractionDigits = 0

        txtVentasMes.text = formatoPesos.format(ventas)

        // RESUMEN DE USUARIOS

        txtUsuariosActivos.text =
            "✅ Usuarios activos: " +
                    obtenerEntero(datos, "usuariosActivos")

        txtUsuariosInactivos.text =
            "⛔ Usuarios inactivos: " +
                    obtenerEntero(datos, "usuariosInactivos")

        // RESUMEN DE VENDEDORES

        txtVendedoresActivos.text =
            "✅ Vendedores activos: " +
                    obtenerEntero(datos, "vendedoresActivos")

        txtVendedoresInactivos.text =
            "⛔ Vendedores inactivos: " +
                    obtenerEntero(datos, "vendedoresInactivos")

        // RESUMEN DE PRODUCTOS

        txtProductosVisibles.text =
            "📦 Productos visibles: " +
                    obtenerEntero(datos, "productosVisibles")

        txtProductosOcultos.text =
            "🙈 Productos ocultos: " +
                    obtenerEntero(datos, "productosOcultos")

        // ESTADOS DE PEDIDOS

        txtPedidosPendientes.text =
            "🕒 Pendientes: " +
                    obtenerEntero(datos, "pedidosPendientes")

        txtPedidosEnCamino.text =
            "🚚 En camino: " +
                    obtenerEntero(datos, "pedidosEnCamino")

        txtPedidosEntregados.text =
            "✅ Entregados: " +
                    obtenerEntero(datos, "pedidosEntregados")

        txtPedidosCancelados.text =
            "❌ Cancelados: " +
                    obtenerEntero(datos, "pedidosCancelados")

        // ACTIVIDAD RECIENTE

        val actividad =
            datos.getAsJsonArray("actividadReciente")

        mostrarActividadReciente(actividad)
    }

    // MOSTRAR PEDIDOS RECIENTES

    private fun mostrarActividadReciente(
        actividad: JsonArray?
    ) {

        contenedorActividad.removeAllViews()

        if (actividad == null || actividad.size() == 0) {

            agregarTextoActividad(
                "Todavía no hay pedidos registrados.",
                "#777777"
            )

            return
        }

        actividad.forEach { elemento ->

            if (!elemento.isJsonObject) return@forEach

            val pedido = elemento.asJsonObject

            val id = obtenerEntero(pedido, "idOrden")
            val estado = obtenerTexto(pedido, "estado")
            val fecha = obtenerTexto(pedido, "fechaOrden")

            val fechaCorta = fecha
                .take(10)
                .ifBlank { "Sin fecha" }

            val descripcion = when (estado) {

                "PENDIENTE" ->
                    "🕒 Pedido #$id pendiente"

                "PREPARANDO" ->
                    "📦 Pedido #$id en preparación"

                "EN_CAMINO" ->
                    "🚚 Pedido #$id en camino"

                "ENTREGADA" ->
                    "✅ Pedido #$id entregado"

                "CANCELADA" ->
                    "❌ Pedido #$id cancelado"

                else ->
                    "🛍️ Pedido #$id: $estado"
            }

            agregarTextoActividad(
                "$descripcion\n$fechaCorta",
                "#444444"
            )
        }
    }

    private fun agregarTextoActividad(
        contenido: String,
        color: String
    ) {

        val texto = TextView(this).apply {

            text = contenido
            textSize = 14f
            setTextColor(Color.parseColor(color))

            setPadding(
                0,
                convertirDp(8),
                0,
                convertirDp(8)
            )
        }

        contenedorActividad.addView(texto)
    }

    // MANEJO DE DATOS JSON
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

    // ESTADOS DE CARGA
    private fun mostrarCargando() {

        txtTotalUsuarios.text = "—"
        txtTotalPedidos.text = "—"
        txtTotalProductos.text = "—"
        txtVentasMes.text = "$ —"

        contenedorActividad.removeAllViews()

        agregarTextoActividad(
            "Cargando actividad reciente...",
            "#777777"
        )
    }

    private fun mostrarError(mensaje: String) {

        contenedorActividad.removeAllViews()

        agregarTextoActividad(
            mensaje,
            "#C62828"
        )

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun convertirDp(valor: Int): Int {
        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }
// MOSTRAR MENSAJES

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }


    // MENÚ INFERIOR

    private fun configurarNavegacion() {

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationAdmin
            )

        bottomNavigation.selectedItemId = R.id.home

        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.home -> true

                R.id.usuarios -> {

                    startActivity(
                        Intent(
                            this,
                            UsuariosAdminActivity::class.java
                        )
                    )

                    finish()
                    true
                }

                R.id.vendedores -> {

                    startActivity(
                        Intent(
                            this,
                            VendedoresAdminActivity::class.java
                        )
                    )

                    finish()
                    true
                }

                R.id.reportes -> {

                    startActivity(
                        Intent(
                            this,
                            ReportesAdminActivity::class.java
                        )
                    )

                    finish()
                    true
                }

                else -> false
            }
        }
    }

    override fun onDestroy() {
        llamadaDashboard?.cancel()
        llamadaPerfil?.cancel()
        llamadaSubidaFoto?.cancel()
        super.onDestroy()
    }
}
