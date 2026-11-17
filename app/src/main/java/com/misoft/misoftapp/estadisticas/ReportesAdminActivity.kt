package com.misoft.misoftapp.estadisticas

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.home.HomeAdminActivity
import com.misoft.misoftapp.usuarios.UsuariosAdminActivity
import com.misoft.misoftapp.usuarios.VendedoresAdminActivity
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.res.ColorStateList
import com.google.android.material.chip.Chip

class ReportesAdminActivity : AppCompatActivity() {

    private lateinit var chipGroupPeriodo: ChipGroup
    private lateinit var layoutFechas: LinearLayout
    private lateinit var edtFechaInicio: TextInputEditText
    private lateinit var edtFechaFin: TextInputEditText
    private lateinit var btnAplicarFiltros: MaterialButton
    private lateinit var txtPeriodo: TextView
    private lateinit var txtVentas: TextView
    private lateinit var txtPedidos: TextView
    private lateinit var txtTicket: TextView
    private lateinit var txtEntregas: TextView
    private lateinit var txtPendientes: TextView
    private lateinit var txtPreparando: TextView
    private lateinit var txtEnCamino: TextView
    private lateinit var txtEntregados: TextView
    private lateinit var txtCancelados: TextView
    private lateinit var contenedorTop: LinearLayout
    private lateinit var contenedorGrafico: FrameLayout

    private var llamadaReporte: Call<JsonObject>? = null

    private val formatoFecha =
        DateTimeFormatter.ISO_LOCAL_DATE

    private val formatoMoneda =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("es-CO")
        ).apply {
            maximumFractionDigits = 0
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.admin_activity_reportes)

        inicializarVistas()
        configurarNavegacion()
        configurarFiltros()

        // DESACTIVADO TEMPORALMENTE
        // consultarReporte()
    }

    // VINCULAR VISTAS

    private fun inicializarVistas() {

        chipGroupPeriodo = findViewById(
            R.id.chipGroupPeriodo
        )

        layoutFechas = findViewById(
            R.id.layoutFechasPersonalizadas
        )

        edtFechaInicio = findViewById(
            R.id.edtFechaInicio
        )

        edtFechaFin = findViewById(
            R.id.edtFechaFin
        )

        btnAplicarFiltros = findViewById(
            R.id.btnAplicarFiltros
        )

        txtPeriodo = findViewById(
            R.id.txtPeriodoSeleccionado
        )

        txtVentas = findViewById(
            R.id.txtVentasCobradas
        )

        txtPedidos = findViewById(
            R.id.txtPedidosPeriodo
        )

        txtTicket = findViewById(
            R.id.txtTicketPromedio
        )

        txtEntregas = findViewById(
            R.id.txtEntregasPeriodo
        )

        txtPendientes = findViewById(
            R.id.txtReportePendientes
        )

        txtPreparando = findViewById(
            R.id.txtReportePreparando
        )

        txtEnCamino = findViewById(
            R.id.txtReporteEnCamino
        )

        txtEntregados = findViewById(
            R.id.txtReporteEntregados
        )

        txtCancelados = findViewById(
            R.id.txtReporteCancelados
        )

        contenedorTop = findViewById(
            R.id.contenedorTopProductos
        )

        contenedorGrafico = findViewById(
            R.id.contenedorGraficoVentas
        )
    }

    // FILTROS POR FECHA

    private fun configurarFiltros() {

        // Colores de los filtros
        val colorSeleccionado = Color.parseColor("#6A4C93")
        val colorNormal = Color.parseColor("#F0E6FA")
        val textoSeleccionado = Color.WHITE
        val textoNormal = Color.parseColor("#6A4C93")

        val chips = listOf(
            R.id.chipSemana,
            R.id.chipMes,
            R.id.chipAnio,
            R.id.chipPersonalizado
        )

        fun actualizarColores() {
            chips.forEach { id ->

                val chip = findViewById<Chip>(id)

                val seleccionado = chip.isChecked

                chip.chipBackgroundColor = ColorStateList.valueOf(
                    if (seleccionado) colorSeleccionado else colorNormal
                )

                chip.setTextColor(
                    if (seleccionado) textoSeleccionado else textoNormal
                )

                chip.isCheckedIconVisible = false
            }
        }

        chipGroupPeriodo.check(R.id.chipMes)

        chipGroupPeriodo.setOnCheckedStateChangeListener {
                _, seleccionados ->

            val personalizado =
                seleccionados.contains(R.id.chipPersonalizado)

            layoutFechas.visibility =
                if (personalizado) View.VISIBLE
                else View.GONE

            actualizarColores()
        }
        actualizarColores()

        edtFechaInicio.setOnClickListener {
            seleccionarFecha(edtFechaInicio)
        }

        edtFechaFin.setOnClickListener {
            seleccionarFecha(edtFechaFin)
        }

        btnAplicarFiltros.setOnClickListener {
            consultarReporte()
        }
    }

    private fun seleccionarFecha(
        campo: TextInputEditText
    ) {

        val hoy = LocalDate.now()

        DatePickerDialog(
            this,
            { _, anio, mes, dia ->

                val fecha = LocalDate.of(
                    anio,
                    mes + 1,
                    dia
                )

                campo.setText(
                    fecha.format(formatoFecha)
                )
            },
            hoy.year,
            hoy.monthValue - 1,
            hoy.dayOfMonth
        ).show()
    }

    private fun periodoSeleccionado(): String {

        return when (chipGroupPeriodo.checkedChipId) {

            R.id.chipSemana -> "semana"

            R.id.chipAnio -> "anio"

            R.id.chipPersonalizado -> "personalizado"

            else -> "mes"
        }
    }

    // ==========================================
    // CONSULTAR API
    // ==========================================

    private fun consultarReporte() {

        val periodo = periodoSeleccionado()

        var inicio: String? = null
        var fin: String? = null

        if (periodo == "personalizado") {

            inicio = edtFechaInicio.text
                ?.toString()
                ?.trim()

            fin = edtFechaFin.text
                ?.toString()
                ?.trim()

            if (inicio.isNullOrBlank() ||
                fin.isNullOrBlank()) {

                mostrarMensaje(
                    "Selecciona ambas fechas."
                )
                return
            }

            try {
                val fechaInicial = LocalDate.parse(
                    inicio,
                    formatoFecha
                )

                val fechaFinal = LocalDate.parse(
                    fin,
                    formatoFecha
                )

                if (fechaFinal.isBefore(fechaInicial)) {

                    mostrarMensaje(
                        "La fecha final no puede ser anterior."
                    )
                    return
                }

                val dias = java.time.temporal.ChronoUnit
                    .DAYS.between(
                        fechaInicial,
                        fechaFinal
                    )

                if (dias > 366) {
                    mostrarMensaje(
                        "El período no puede superar 366 días."
                    )
                    return
                }

            } catch (e: Exception) {
                mostrarMensaje(
                    "Las fechas no son válidas."
                )
                return
            }
        }

        llamadaReporte?.cancel()

        btnAplicarFiltros.isEnabled = false
        btnAplicarFiltros.text = "Consultando..."

        mostrarCargando()

        val llamada = ApiClient.reportesAdminApi
            .obtenerReporte(
                periodo,
                inicio,
                fin
            )

        llamadaReporte = llamada

        llamada.enqueue(
            object : Callback<JsonObject> {

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    btnAplicarFiltros.isEnabled = true
                    btnAplicarFiltros.text = "Aplicar filtros"

                    if (response.isSuccessful) {

                        val datos = response.body()

                        if (datos != null) {
                            actualizarReporte(datos)
                        } else {
                            mostrarError(
                                "La API devolvió una respuesta vacía."
                            )
                        }

                    } else {

                        mostrarError(
                            "No se pudo consultar el reporte. " +
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

                    btnAplicarFiltros.isEnabled = true
                    btnAplicarFiltros.text = "Aplicar filtros"

                    mostrarError(
                        "No se pudo conectar con la API."
                    )
                }
            }
        )
    }

    // ==========================================
    // MOSTRAR INDICADORES
    // ==========================================

    private fun actualizarReporte(
        datos: JsonObject
    ) {

        txtVentas.text = formatoMoneda.format(
            numero(datos, "ventasCobradas")
        )

        txtPedidos.text =
            entero(datos, "totalPedidos").toString()

        txtTicket.text = formatoMoneda.format(
            numero(datos, "ticketPromedio")
        )

        txtEntregas.text =
            entero(datos, "entregados").toString()

        val inicio = texto(datos, "fechaInicio")
        val fin = texto(datos, "fechaFin")

        txtPeriodo.text = "Período: $inicio a $fin"

        val estados = datos.getAsJsonObject("estados")

        if (estados != null) {

            txtPendientes.text =
                "🕒 Pendientes: " +
                        entero(estados, "pendientes")

            txtPreparando.text =
                "🧶 En preparación: " +
                        entero(estados, "preparando")

            txtEnCamino.text =
                "🚚 En camino: " +
                        entero(estados, "enCamino")

            txtEntregados.text =
                "✅ Entregados: " +
                        entero(estados, "entregados")

            txtCancelados.text =
                "❌ Cancelados: " +
                        entero(estados, "cancelados")
        }

        mostrarTopProductos(
            datos.getAsJsonArray("topProductos")
        )

        mostrarGrafico(
            datos.getAsJsonArray("graficoVentas")
        )
    }

    // PRODUCTOS MÁS VENDIDOS

    private fun mostrarTopProductos(
        productos: JsonArray?
    ) {

        contenedorTop.removeAllViews()

        if (productos == null ||
            productos.size() == 0) {

            agregarTexto(
                contenedorTop,
                "No hay productos vendidos en este período."
            )

            return
        }

        productos.forEachIndexed { indice, elemento ->

            if (!elemento.isJsonObject) {
                return@forEachIndexed
            }

            val producto = elemento.asJsonObject

            // Nombre real desde MySQL
            val nombreProducto = texto(
                producto,
                "nombreProducto"
            ).ifBlank {
                "Producto sin nombre"
            }

            // Cantidad de unidades vendidas
            val unidades = entero(
                producto,
                "unidadesVendidas"
            )

            // Mostrar ranking
            agregarTexto(
                contenedorTop,
                "${indice + 1}. $nombreProducto\n" +
                        "    $unidades unidades vendidas"
            )
        }
    }

    // GRÁFICO DE VENTAS

    private fun mostrarGrafico(
        registros: JsonArray?
    ) {

        contenedorGrafico.removeAllViews()

        if (registros == null ||
            registros.size() == 0) {

            agregarTexto(
                contenedorGrafico,
                "No hay datos para el gráfico."
            )

            return
        }

        val puntos = mutableListOf<PuntoVenta>()

        registros.forEach { elemento ->

            if (!elemento.isJsonObject) {
                return@forEach
            }

            val dato = elemento.asJsonObject

            puntos.add(
                PuntoVenta(
                    etiqueta = texto(dato, "etiqueta"),
                    total = numero(dato, "total")
                        .toFloat()
                )
            )
        }

        val grafico = GraficoVentasView(
            this,
            puntos
        )

        contenedorGrafico.addView(
            grafico,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    // ==========================================
    // FUNCIONES AUXILIARES
    // ==========================================

    private fun texto(
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

    private fun entero(
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

    private fun numero(
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

    private fun mostrarCargando() {

        txtVentas.text = "—"
        txtPedidos.text = "—"
        txtTicket.text = "—"
        txtEntregas.text = "—"

        contenedorTop.removeAllViews()
        contenedorGrafico.removeAllViews()

        agregarTexto(
            contenedorTop,
            "Cargando productos..."
        )

        agregarTexto(
            contenedorGrafico,
            "Cargando gráfico..."
        )
    }

    private fun agregarTexto(
        contenedor: android.view.ViewGroup,
        contenido: String
    ) {

        val vista = TextView(this).apply {
            text = contenido
            textSize = 14f
            setTextColor(
                Color.parseColor("#777777")
            )
            setPadding(
                12,
                16,
                12,
                16
            )
        }

        contenedor.addView(vista)
    }

    private fun mostrarError(
        mensaje: String
    ) {

        contenedorTop.removeAllViews()
        contenedorGrafico.removeAllViews()

        agregarTexto(
            contenedorGrafico,
            mensaje
        )

        mostrarMensaje(mensaje)
    }

    private fun mostrarMensaje(
        mensaje: String
    ) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    // ==========================================
    // NAVEGACIÓN ADMIN ORIGINAL
    // ==========================================

    private fun configurarNavegacion() {

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationAdmin
            )

        bottomNavigation.selectedItemId =
            R.id.reportes

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

                R.id.reportes -> true

                else -> false
            }
        }
    }

    override fun onDestroy() {

        llamadaReporte?.cancel()

        super.onDestroy()
    }
}
