package com.misoft.misoftapp.estadisticas

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EstadisticasVendedorActivity : AppCompatActivity() {

    private lateinit var txtVentasTotales: TextView
    private lateinit var txtPedidos: TextView
    private lateinit var txtProductos: TextView
    private lateinit var txtPendientes: TextView
    private lateinit var txtMasVendido: TextView
    private lateinit var txtEntregados: TextView
    private lateinit var txtCancelados: TextView
    private lateinit var txtPeriodo: TextView

    private lateinit var contenedorGrafica: FrameLayout
    private lateinit var contenedorTop: LinearLayout
    private lateinit var contenedorFechas: LinearLayout
    private lateinit var chipGroup: ChipGroup

    private var llamadaResumen: Call<JsonObject>? = null

    private var periodo = "mes"
    private var fechaInicio: LocalDate? = null
    private var fechaFin: LocalDate? = null

    private val colorMorado = Color.parseColor("#6A4C93")
    private val colorLila = Color.parseColor("#9B7AE0")
    private val colorGris = Color.parseColor("#777777")

    private val formatoFecha =
        DateTimeFormatter.ISO_LOCAL_DATE

    private val formatoPesos =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("es-CO")
        ).apply {
            maximumFractionDigits = 0
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.vendedor_activity_estadisticas)

        inicializarVistas()
        configurarFiltros()
        configurarFechas()

        findViewById<View>(R.id.imgBack)
            .setOnClickListener {
                finish()
            }
    }

    override fun onResume() {
        super.onResume()
        cargarEstadisticas()
    }

    private fun inicializarVistas() {

        txtVentasTotales =
            findViewById(R.id.txtVentasTotales)

        txtPedidos =
            findViewById(R.id.txtPedidos)

        txtProductos =
            findViewById(R.id.txtProductos)

        txtPendientes =
            findViewById(R.id.txtPendientes)

        txtMasVendido =
            findViewById(R.id.txtMasVendido)

        txtEntregados =
            findViewById(R.id.txtPedidosEntregadosVendedor)

        txtCancelados =
            findViewById(R.id.txtPedidosCanceladosVendedor)

        txtPeriodo =
            findViewById(R.id.txtPeriodoSeleccionadoVendedor)

        contenedorGrafica =
            findViewById(R.id.contenedorGraficaVendedor)

        contenedorTop =
            findViewById(R.id.contenedorTopProductosVendedor)

        contenedorFechas =
            findViewById(R.id.contenedorFechasVendedor)

        chipGroup =
            findViewById(R.id.chipGroupPeriodoVendedor)
    }

    // FILTROS DE PERÍODO

    private fun configurarFiltros() {

        chipGroup.check(R.id.chipMesVendedor)

        chipGroup.setOnCheckedStateChangeListener {
                _, checkedIds ->

            val seleccionado =
                checkedIds.firstOrNull()
                    ?: return@setOnCheckedStateChangeListener

            periodo = when (seleccionado) {
                R.id.chipSemanaVendedor -> "semana"
                R.id.chipMesVendedor -> "mes"
                R.id.chipAnioVendedor -> "anio"
                R.id.chipPersonalizadoVendedor -> "personalizado"
                else -> "mes"
            }

            contenedorFechas.visibility =
                if (periodo == "personalizado") {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            actualizarColoresChips()

            if (periodo != "personalizado") {
                cargarEstadisticas()
            } else {
                txtPeriodo.text =
                    "Selecciona un rango de fechas"
            }
        }

        actualizarColoresChips()
    }

    private fun actualizarColoresChips() {

        val ids = listOf(
            R.id.chipSemanaVendedor,
            R.id.chipMesVendedor,
            R.id.chipAnioVendedor,
            R.id.chipPersonalizadoVendedor
        )

        ids.forEach { id ->

            val chip = findViewById<Chip>(id)

            if (chip.isChecked) {
                chip.chipBackgroundColor =
                    ColorStateList.valueOf(colorMorado)

                chip.setTextColor(Color.WHITE)
            } else {
                chip.chipBackgroundColor =
                    ColorStateList.valueOf(
                        Color.parseColor("#F2EAF8")
                    )

                chip.setTextColor(colorMorado)
            }
        }
    }

    // SELECCIÓN DE FECHAS

    private fun configurarFechas() {

        val btnInicio =
            findViewById<MaterialButton>(
                R.id.btnFechaInicioVendedor
            )

        val btnFin =
            findViewById<MaterialButton>(
                R.id.btnFechaFinVendedor
            )

        val btnAplicar =
            findViewById<MaterialButton>(
                R.id.btnAplicarFechasVendedor
            )

        btnInicio.setOnClickListener {

            seleccionarFecha(fechaInicio) { fecha ->
                fechaInicio = fecha
                btnInicio.text =
                    "Desde: ${fecha.format(formatoFecha)}"
            }
        }

        btnFin.setOnClickListener {

            seleccionarFecha(fechaFin) { fecha ->
                fechaFin = fecha
                btnFin.text =
                    "Hasta: ${fecha.format(formatoFecha)}"
            }
        }

        btnAplicar.setOnClickListener {

            val inicio = fechaInicio
            val fin = fechaFin

            if (inicio == null || fin == null) {
                mostrarMensaje(
                    "Selecciona las dos fechas."
                )
                return@setOnClickListener
            }

            if (fin.isBefore(inicio)) {
                mostrarMensaje(
                    "La fecha final no puede ser anterior."
                )
                return@setOnClickListener
            }

            if (fin.isAfter(inicio.plusYears(2))) {
                mostrarMensaje(
                    "Selecciona un rango máximo de dos años."
                )
                return@setOnClickListener
            }

            cargarEstadisticas()
        }
    }

    private fun seleccionarFecha(
        actual: LocalDate?,
        alSeleccionar: (LocalDate) -> Unit
    ) {

        val fecha = actual ?: LocalDate.now()

        DatePickerDialog(
            this,
            { _, anio, mes, dia ->

                alSeleccionar(
                    LocalDate.of(anio, mes + 1, dia)
                )
            },
            fecha.year,
            fecha.monthValue - 1,
            fecha.dayOfMonth
        ).show()
    }

    // CONSULTAR API

    private fun cargarEstadisticas() {

        llamadaResumen?.cancel()

        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val idVendedor =
            preferencias.getInt("idUsuario", -1)

        val rolId =
            preferencias.getInt("rolId", -1)

        if (idVendedor <= 0 || rolId != 2) {
            mostrarError("No hay una sesión de vendedor válida.")
            return
        }

        if (periodo == "personalizado" &&
            (fechaInicio == null || fechaFin == null)) {
            return
        }

        mostrarCargando()

        val inicio = if (periodo == "personalizado") {
            fechaInicio?.format(formatoFecha)
        } else {
            null
        }

        val fin = if (periodo == "personalizado") {
            fechaFin?.format(formatoFecha)
        } else {
            null
        }

        val llamada = ApiClient.estadisticasVendedorApi
            .obtenerResumen(
                idVendedor = idVendedor,
                periodo = periodo,
                fechaInicio = inicio,
                fechaFin = fin
            )

        llamadaResumen = llamada

        llamada.enqueue(object : Callback<JsonObject> {

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
                        actualizarEstadisticas(datos)
                    } else {
                        mostrarError(
                            "El servidor devolvió una respuesta vacía."
                        )
                    }

                } else {

                    Log.e(
                        "ESTADISTICAS_VENDEDOR",
                        "HTTP ${response.code()}: " +
                                response.errorBody()?.string()
                    )

                    mostrarError(
                        "No fue posible cargar los datos. " +
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

                Log.e(
                    "ESTADISTICAS_VENDEDOR",
                    "Error consultando estadísticas",
                    t
                )

                mostrarError(
                    "No se pudo conectar con la API."
                )
            }
        })
    }

    // MOSTRAR RESULTADOS

    private fun actualizarEstadisticas(
        datos: JsonObject
    ) {

        val ingresos =
            obtenerDecimal(datos, "ingresosConfirmados")

        val completados =
            obtenerEntero(datos, "pedidosCompletados")

        val unidades =
            obtenerEntero(datos, "unidadesVendidas")

        val pendientes =
            obtenerEntero(datos, "pedidosPendientes")

        val cancelados =
            obtenerEntero(datos, "pedidosCancelados")

        txtVentasTotales.text =
            formatoPesos.format(ingresos)

        txtPedidos.text =
            completados.toString()

        txtProductos.text =
            unidades.toString()

        txtPendientes.text =
            "🕒 Pedidos pendientes: $pendientes"

        txtEntregados.text =
            "✅ Pedidos entregados y pagados: $completados"

        txtCancelados.text =
            "❌ Pedidos cancelados: $cancelados"

        val inicio = obtenerTexto(datos, "fechaInicio")
        val fin = obtenerTexto(datos, "fechaFin")

        txtPeriodo.text =
            "Período: $inicio al $fin"

        mostrarGrafica(
            datos.getAsJsonArray("evolucionVentas")
        )

        mostrarTopProductos(
            datos.getAsJsonArray("productosMasVendidos")
        )
    }

    // GRÁFICA DE VENTAS

    private fun mostrarGrafica(puntos: JsonArray?) {

        contenedorGrafica.removeAllViews()

        if (puntos == null || puntos.size() == 0) {
            mostrarEstadoGrafica("No hay datos disponibles.")
            return
        }

        val valores = puntos.map { elemento ->

            val objeto = elemento.asJsonObject

            Pair(
                obtenerTexto(objeto, "etiqueta"),
                obtenerDecimal(objeto, "total")
            )
        }

        if (valores.all { it.second <= 0.0 }) {
            mostrarEstadoGrafica(
                "No se registraron ventas confirmadas " +
                        "durante este período."
            )
            return
        }

        val maximo = valores.maxOf {
            it.second
        }.coerceAtLeast(1.0)

        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isFillViewport = true
        }

        val fila = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM
            setPadding(
                convertirDp(8),
                convertirDp(8),
                convertirDp(8),
                convertirDp(8)
            )
        }

        valores.forEach { (etiqueta, total) ->

            val columna = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }

            val altoMaximo = convertirDp(158)

            val altoBarra =
                if (total <= 0.0) {
                    convertirDp(4)
                } else {
                    maxOf(
                        convertirDp(8),
                        (altoMaximo * total / maximo).toInt()
                    )
                }

            val espacio = View(this)

            columna.addView(
                espacio,
                LinearLayout.LayoutParams(
                    1,
                    0,
                    1f
                )
            )

            val barra = View(this).apply {

                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE

                    cornerRadius = convertirDp(6).toFloat()

                    setColor(
                        if (total > 0) {
                            colorLila
                        } else {
                            Color.parseColor("#E6DDEE")
                        }
                    )
                }

                contentDescription =
                    "$etiqueta: ${formatoPesos.format(total)}"
            }

            columna.addView(
                barra,
                LinearLayout.LayoutParams(
                    convertirDp(21),
                    altoBarra
                )
            )

            val etiquetaTexto = TextView(this).apply {
                text = etiqueta
                textSize = 10f
                setTextColor(colorGris)
                gravity = Gravity.CENTER
                setPadding(0, convertirDp(8), 0, 0)
                maxLines = 2
            }

            columna.addView(
                etiquetaTexto,
                LinearLayout.LayoutParams(
                    convertirDp(64),
                    convertirDp(42)
                )
            )

            fila.addView(
                columna,
                LinearLayout.LayoutParams(
                    convertirDp(64),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        scroll.addView(
            fila,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        contenedorGrafica.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun mostrarEstadoGrafica(mensaje: String) {

        contenedorGrafica.removeAllViews()

        val texto = TextView(this).apply {
            text = mensaje
            setTextColor(colorGris)
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(convertirDp(12), 0, convertirDp(12), 0)
        }

        contenedorGrafica.addView(
            texto,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    // PRODUCTOS MÁS VENDIDOS

    private fun mostrarTopProductos(
        productos: JsonArray?
    ) {

        contenedorTop.removeAllViews()

        if (productos == null || productos.size() == 0) {
            txtMasVendido.text =
                "Todavía no hay productos vendidos en este período."
            return
        }

        txtMasVendido.text =
            "Ranking de tus productos"

        productos.forEachIndexed { indice, elemento ->

            val producto = elemento.asJsonObject

            val nombre =
                obtenerTexto(producto, "nombre")

            val unidades =
                obtenerEntero(producto, "unidades")

            val ingresos =
                obtenerDecimal(producto, "ingresos")

            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    convertirDp(12),
                    convertirDp(12),
                    convertirDp(12),
                    convertirDp(12)
                )

                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = convertirDp(12).toFloat()
                    setColor(Color.parseColor("#F8F3FC"))
                }
            }

            val nombreTexto = TextView(this).apply {
                text = "${indice + 1}. $nombre"
                textSize = 15f
                setTextColor(colorMorado)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val detalle = TextView(this).apply {
                text =
                    "📦 $unidades unidades  ·  " +
                            formatoPesos.format(ingresos)
                textSize = 12f
                setTextColor(colorGris)
                setPadding(0, convertirDp(5), 0, 0)
            }

            tarjeta.addView(nombreTexto)
            tarjeta.addView(detalle)

            val parametros = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = convertirDp(8)
            }

            contenedorTop.addView(tarjeta, parametros)
        }
    }

    // LECTURA DE DATOS

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

    // CARGA Y ERRORES

    private fun mostrarCargando() {

        txtVentasTotales.text = "$ —"
        txtPedidos.text = "—"
        txtProductos.text = "—"

        txtPendientes.text = "🕒 Pedidos pendientes: —"
        txtEntregados.text = "✅ Pedidos entregados y pagados: —"
        txtCancelados.text = "❌ Pedidos cancelados: —"

        txtMasVendido.text = "Cargando productos..."
        contenedorTop.removeAllViews()

        mostrarEstadoGrafica("Cargando gráfica...")
    }

    private fun mostrarError(mensaje: String) {

        txtVentasTotales.text = "—"
        txtPedidos.text = "—"
        txtProductos.text = "—"

        txtMasVendido.text =
            "No se pudieron cargar los productos."

        txtPendientes.text = "🕒 Pedidos pendientes: —"
        txtEntregados.text = "✅ Pedidos entregados y pagados: —"
        txtCancelados.text = "❌ Pedidos cancelados: —"

        contenedorTop.removeAllViews()
        mostrarEstadoGrafica(mensaje)

        mostrarMensaje(mensaje)
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun convertirDp(valor: Int): Int {
        return (valor * resources.displayMetrics.density)
            .toInt()
    }

    override fun onDestroy() {
        llamadaResumen?.cancel()
        super.onDestroy()
    }
}
