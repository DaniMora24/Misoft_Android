package com.misoft.misoftapp.usuarios

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.catalogo.ProductoAdminActivity
import com.misoft.misoftapp.estadisticas.ReportesAdminActivity
import com.misoft.misoftapp.home.HomeAdminActivity
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VendedoresAdminActivity : AppCompatActivity() {

    // VARIABLES

    private lateinit var contenedorVendedores: LinearLayout
    private lateinit var edtBuscar: TextInputEditText

    private lateinit var txtCantidadVendedores: TextView
    private lateinit var txtVendedoresActivos: TextView

    private val listaVendedores = mutableListOf<Vendedor>()

    private var llamadaVendedores: Call<JsonElement>? = null

    data class Vendedor(
        val idUsuario: Int,
        val nombre: String,
        val correo: String,
        val activo: Boolean
    )

    // INICIAR PANTALLA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.admin_activity_vendedores)

        inicializarVistas()
        configurarBuscador()
        configurarNavegacion()

        cargarVendedores()
    }


    // VINCULAR XML
    private fun inicializarVistas() {

        contenedorVendedores = findViewById(
            R.id.contenedorVendedores
        )

        edtBuscar = findViewById(
            R.id.edtBuscarVendedor
        )

        txtCantidadVendedores = findViewById(
            R.id.txtCantidadVendedores
        )

        txtVendedoresActivos = findViewById(
            R.id.txtVendedoresActivos
        )
    }

    // CONSULTAR VENDEDORES DESDE BD

    private fun cargarVendedores() {

        llamadaVendedores?.cancel()

        mostrarEstado("Cargando vendedores...")

        val llamada = ApiClient.vendedoresAdminApi
            .obtenerVendedores()

        llamadaVendedores = llamada

        llamada.enqueue(
            object : Callback<JsonElement> {

                override fun onResponse(
                    call: Call<JsonElement>,
                    response: Response<JsonElement>
                ) {

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    if (!response.isSuccessful) {

                        mostrarEstado(
                            "No se pudieron cargar los vendedores. " +
                                    "HTTP ${response.code()}"
                        )

                        return
                    }

                    val usuarios = extraerUsuarios(
                        response.body()
                    )

                    if (usuarios == null) {

                        mostrarEstado(
                            "La respuesta de usuarios no tiene " +
                                    "el formato esperado."
                        )

                        return
                    }

                    listaVendedores.clear()

                    usuarios.forEach { elemento ->

                        if (!elemento.isJsonObject) {
                            return@forEach
                        }

                        val usuario = elemento.asJsonObject

                        if (!esVendedor(usuario)) {
                            return@forEach
                        }

                        val id = entero(
                            usuario,
                            "idUsuario",
                            "id"
                        )

                        if (id <= 0) {
                            return@forEach
                        }

                        val nombre = texto(
                            usuario,
                            "nombreCompleto",
                            "nombre"
                        ).ifBlank {

                            val nombres = texto(
                                usuario,
                                "nombres"
                            )

                            val apellidos = texto(
                                usuario,
                                "apellidos"
                            )

                            "$nombres $apellidos".trim()
                        }.ifBlank {
                            "Vendedor #$id"
                        }

                        val correo = texto(
                            usuario,
                            "email",
                            "correo"
                        )

                        val activo = booleano(
                            usuario,
                            "estado",
                            "activo",
                            "estadoUsuario"
                        )

                        listaVendedores.add(
                            Vendedor(
                                idUsuario = id,
                                nombre = nombre,
                                correo = correo,
                                activo = activo
                            )
                        )
                    }

                    listaVendedores.sortBy {
                        it.nombre.lowercase(Locale.ROOT)
                    }

                    actualizarResumen()

                    filtrarVendedores(
                        edtBuscar.text?.toString().orEmpty()
                    )
                }

                override fun onFailure(
                    call: Call<JsonElement>,
                    t: Throwable
                ) {

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    Log.e(
                        "VENDEDORES_ADMIN",
                        "Error al consultar vendedores",
                        t
                    )

                    mostrarEstado(
                        "No se pudo conectar con la API."
                    )
                }
            }
        )
    }

    // LEER LISTA DE USUARIOS

    private fun extraerUsuarios(
        respuesta: JsonElement?
    ): JsonArray? {

        if (respuesta == null ||
            respuesta.isJsonNull) {
            return null
        }

        if (respuesta.isJsonArray) {
            return respuesta.asJsonArray
        }

        if (respuesta.isJsonObject) {

            val objeto = respuesta.asJsonObject

            for (clave in listOf(
                "usuarios",
                "vendedores",
                "datos",
                "items"
            )) {

                val elemento = objeto.get(clave)

                if (elemento != null &&
                    elemento.isJsonArray) {

                    return elemento.asJsonArray
                }
            }
        }

        return null
    }

    // IDENTIFICAR USUARIOS VENDEDORES

    private fun esVendedor(
        usuario: JsonObject
    ): Boolean {

        val rolId = entero(
            usuario,
            "rolId",
            "idRol"
        )

        if (rolId == 2) {
            return true
        }

        val rolTexto = texto(
            usuario,
            "rol",
            "nombreRol"
        )

        return rolTexto.equals(
            "Vendedor",
            ignoreCase = true
        )
    }

    // MOSTRAR RESUMEN

    private fun actualizarResumen() {

        txtCantidadVendedores.text =
            "Vendedores: ${listaVendedores.size}"

        val activos = listaVendedores.count {
            it.activo
        }

        txtVendedoresActivos.text =
            "Activos: $activos"
    }

    // BUSCADOR

    private fun configurarBuscador() {

        edtBuscar.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    filtrarVendedores(
                        s?.toString().orEmpty()
                    )
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )
    }

    private fun filtrarVendedores(
        busqueda: String
    ) {

        val filtro = busqueda
            .trim()
            .lowercase(Locale.ROOT)

        val resultados = listaVendedores.filter {

            it.nombre.lowercase(Locale.ROOT)
                .contains(filtro) ||

                    it.correo.lowercase(Locale.ROOT)
                        .contains(filtro) ||

                    it.idUsuario.toString().contains(filtro)
        }

        mostrarVendedores(resultados)
    }

    // GENERAR LISTADO
    private fun mostrarVendedores(
        vendedores: List<Vendedor>
    ) {

        contenedorVendedores.removeAllViews()

        if (vendedores.isEmpty()) {

            mostrarEstado(
                if (listaVendedores.isEmpty()) {
                    "No hay vendedores registrados."
                } else {
                    "No se encontraron vendedores."
                }
            )

            return
        }

        vendedores.forEach { vendedor ->
            crearTarjetaVendedor(vendedor)
        }
    }

    // CREAR TARJETA

    private fun crearTarjetaVendedor(
        vendedor: Vendedor
    ) {

        val tarjeta = CardView(this).apply {

            radius = dp(16).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(Color.WHITE)

            isClickable = true
            isFocusable = true

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        }

        val fila = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(16),
                dp(18),
                dp(16),
                dp(18)
            )
        }

        val icono = TextView(this).apply {

            text = "🏪"
            textSize = 28f

            gravity = Gravity.CENTER

            layoutParams = LinearLayout.LayoutParams(
                dp(52),
                dp(52)
            )
        }

        fila.addView(icono)

        val columna = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = dp(12)
            }
        }

        val txtNombre = TextView(this).apply {

            text = vendedor.nombre
            textSize = 16f

            setTextColor(
                Color.parseColor("#6A4C93")
            )

            setTypeface(null, Typeface.BOLD)
        }

        val txtCorreo = TextView(this).apply {

            text = vendedor.correo.ifBlank {
                "Correo no disponible"
            }

            textSize = 12f
            setTextColor(
                Color.parseColor("#777777")
            )

            maxLines = 1

            ellipsize =
                android.text.TextUtils.TruncateAt.END
        }

        val txtEstado = TextView(this).apply {

            text = if (vendedor.activo) {
                "● Activo"
            } else {
                "● Inactivo"
            }

            textSize = 13f

            setTextColor(
                if (vendedor.activo) {
                    Color.parseColor("#2E7D32")
                } else {
                    Color.parseColor("#C62828")
                }
            )
        }

        columna.addView(txtNombre)
        columna.addView(txtCorreo)
        columna.addView(txtEstado)

        fila.addView(columna)

        val flecha = TextView(this).apply {

            text = "›"
            textSize = 30f

            setTextColor(
                Color.parseColor("#9B7AE0")
            )
        }

        fila.addView(flecha)

        tarjeta.addView(fila)

        // ABRIR PRODUCTOS DEL VENDEDOR

        tarjeta.setOnClickListener {

            val intent = Intent(
                this,
                ProductoAdminActivity::class.java
            )

            intent.putExtra(
                "ID_VENDEDOR",
                vendedor.idUsuario
            )

            intent.putExtra(
                "NOMBRE_TIENDA",
                vendedor.nombre
            )

            startActivity(intent)
        }

        contenedorVendedores.addView(tarjeta)
    }

    // ESTADO DE LA LISTA

    private fun mostrarEstado(
        mensaje: String
    ) {

        contenedorVendedores.removeAllViews()

        val texto = TextView(this).apply {

            text = mensaje
            textSize = 14f
            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#888888")
            )

            setPadding(
                dp(12),
                dp(30),
                dp(12),
                dp(30)
            )
        }

        contenedorVendedores.addView(texto)
    }

    // FUNCIONES AUXILIARES JSON

    private fun texto(
        datos: JsonObject,
        vararg claves: String
    ): String {

        for (clave in claves) {

            try {

                val valor = datos.get(clave)

                if (valor != null &&
                    !valor.isJsonNull &&
                    valor.isJsonPrimitive) {

                    return valor.asString
                }

            } catch (_: Exception) {
            }
        }

        return ""
    }

    private fun entero(
        datos: JsonObject,
        vararg claves: String
    ): Int {

        for (clave in claves) {

            try {

                val valor = datos.get(clave)

                if (valor != null &&
                    !valor.isJsonNull &&
                    valor.isJsonPrimitive) {

                    return valor.asInt
                }

            } catch (_: Exception) {
            }
        }

        return -1
    }

    private fun booleano(
        datos: JsonObject,
        vararg claves: String
    ): Boolean {

        for (clave in claves) {

            try {

                val valor = datos.get(clave)

                if (valor != null &&
                    !valor.isJsonNull &&
                    valor.isJsonPrimitive) {

                    return when (
                        valor.asString.trim().lowercase(Locale.ROOT)
                    ) {
                        "true", "1", "activo" -> true
                        else -> false
                    }
                }

            } catch (_: Exception) {
            }
        }

        return false
    }

    private fun dp(valor: Int): Int {

        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }

    // NAVEGACIÓN INFERIOR

    private fun configurarNavegacion() {

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationAdmin
            )

        bottomNavigation.selectedItemId =
            R.id.vendedores

        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.home -> {

                    startActivity(
                        Intent(
                            this,
                            HomeAdminActivity::class.java
                        )
                    )

                    finish()
                    true
                }

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

                R.id.vendedores -> true

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

        llamadaVendedores?.cancel()

        super.onDestroy()
    }
}
