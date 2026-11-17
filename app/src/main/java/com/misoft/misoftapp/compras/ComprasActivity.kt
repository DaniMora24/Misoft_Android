
package com.misoft.misoftapp.compras

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.api.DetalleProductoOrdenResponse
import com.misoft.misoftapp.api.ImagenUrlHelper
import com.misoft.misoftapp.api.OrdenDetalleResponse
import com.misoft.misoftapp.api.OrdenHistorialResponse
import com.misoft.misoftapp.catalogo.CatalogoActivity
import com.misoft.misoftapp.home.HomeBuyerActivity
import com.misoft.misoftapp.perfil_cliente.PerfilClienteActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ComprasActivity : AppCompatActivity() {

    private lateinit var layoutOrdenes: LinearLayout
    private lateinit var txtSinCompras: TextView
    private lateinit var chipGroupEstados: ChipGroup

    private var idUsuario = -1
    private var ordenes = listOf<OrdenHistorialResponse>()
    private var filtroActual = "TODAS"
    private var historialCargado = false

    private var llamadaHistorial:
            Call<List<OrdenHistorialResponse>>? = null

    private var llamadaDetalle:
            Call<OrdenDetalleResponse>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.cliente_activity_compras)

        layoutOrdenes = findViewById(R.id.layoutOrdenes)
        txtSinCompras = findViewById(R.id.txtSinCompras)
        chipGroupEstados = findViewById(R.id.chipGroupEstados)

        idUsuario = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        ).getInt("idUsuario", -1)

        findViewById<ImageView>(R.id.imgBack)
            .setOnClickListener {
                finish()
            }

        configurarFiltros()
        configurarNavegacion()
    }

    override fun onResume() {
        super.onResume()

        if (idUsuario <= 0) {
            mostrarMensaje("Debes iniciar sesión")
            finish()
            return
        }

        cargarHistorial()
    }

    // CONSULTAR ÓRDENES DESDE MYSQL

    private fun cargarHistorial() {

        llamadaHistorial?.cancel()

        historialCargado = false
        layoutOrdenes.removeAllViews()

        txtSinCompras.text = "Cargando pedidos..."
        txtSinCompras.visibility = View.VISIBLE

        val llamada = ApiClient.ordenesApi
            .obtenerHistorial(idUsuario)

        llamadaHistorial = llamada

        llamada.enqueue(
            object : Callback<List<OrdenHistorialResponse>> {

                override fun onResponse(
                    call: Call<List<OrdenHistorialResponse>>,
                    response: Response<List<OrdenHistorialResponse>>
                ) {
                    if (call.isCanceled || isFinishing || isDestroyed) {
                        return
                    }

                    if (response.isSuccessful) {
                        ordenes = response.body() ?: emptyList()
                        historialCargado = true
                        mostrarOrdenesFiltradas()
                    } else {
                        mostrarError(
                            "Error al consultar pedidos (${response.code()})"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<OrdenHistorialResponse>>,
                    t: Throwable
                ) {
                    if (call.isCanceled || isFinishing || isDestroyed) {
                        return
                    }

                    mostrarError(
                        "No se pudo conectar con el servidor"
                    )
                }
            }
        )
    }

    // FILTROS

    private fun configurarFiltros() {

        chipGroupEstados.setOnCheckedStateChangeListener {
                _, checkedIds ->

            filtroActual = when (checkedIds.firstOrNull()) {
                R.id.chipPendientes -> "PENDIENTES"
                R.id.chipEnviadas -> "EN_CAMINO"
                R.id.chipEntregadas -> "ENTREGADA"
                else -> "TODAS"
            }

            if (historialCargado) {
                mostrarOrdenesFiltradas()
            }
        }
    }

    private fun mostrarOrdenesFiltradas() {

        layoutOrdenes.removeAllViews()

        val filtradas = ordenes.filter { orden ->
            when (filtroActual) {
                "PENDIENTES" -> orden.estado == "PENDIENTE"
                "EN_CAMINO" -> orden.estado == "EN_CAMINO"
                "ENTREGADA" -> orden.estado == "ENTREGADA"
                else -> true
            }
        }

        if (filtradas.isEmpty()) {

            txtSinCompras.text = if (ordenes.isEmpty()) {
                "Todavía no tienes pedidos registrados."
            } else {
                "No tienes pedidos en esta categoría."
            }

            txtSinCompras.visibility = View.VISIBLE
            return
        }

        txtSinCompras.visibility = View.GONE

        filtradas.forEach { orden ->
            layoutOrdenes.addView(crearTarjeta(orden))
        }
    }

    // TARJETA CON FOTO Y NOMBRE DEL PRODUCTO

    private fun crearTarjeta(
        orden: OrdenHistorialResponse
    ): CardView {

        val tarjeta = LayoutInflater.from(this)
            .inflate(
                R.layout.cliente_item_compra,
                layoutOrdenes,
                false
            ) as CardView

        val imgCompra = tarjeta.findViewById<ImageView>(
            R.id.imgCompra
        )

        val txtNumero = tarjeta.findViewById<TextView>(
            R.id.txtNumeroOrden
        )

        val txtFecha = tarjeta.findViewById<TextView>(
            R.id.txtFechaOrden
        )

        val txtPrecio = tarjeta.findViewById<TextView>(
            R.id.txtPrecioOrden
        )

        val txtEstado = tarjeta.findViewById<TextView>(
            R.id.txtEstadoOrden
        )

        val txtCantidad = tarjeta.findViewById<TextView>(
            R.id.txtCantidadProductosOrden
        )

        val primerProducto = orden.primerProducto

        // NÚMERO REAL DEL PEDIDO
        txtNumero.text =
            "Pedido #${orden.idOrden.toString().padStart(5, '0')}"

        // FECHA REAL
        txtFecha.text =
            "Fecha: ${formatearFecha(orden.fechaOrden)}"

        // NOMBRE, TALLA Y CANTIDAD
        val descripcion = if (primerProducto != null) {

            val talla = primerProducto.talla
                ?.takeIf { it.isNotBlank() }
                ?: "No especificada"

            val adicionales =
                (orden.cantidadProductosDiferentes - 1)
                    .coerceAtLeast(0)

            buildString {
                append(primerProducto.nombre)
                append("\nTamaño: $talla")
                append(
                    "\n${orden.cantidadProductos} " +
                            if (orden.cantidadProductos == 1)
                                "unidad"
                            else
                                "unidades"
                )

                if (adicionales > 0) {
                    append(
                        "\n+$adicionales " +
                                if (adicionales == 1)
                                    "producto adicional"
                                else
                                    "productos adicionales"
                    )
                }
            }

        } else {
            "Sin información de productos"
        }

        txtCantidad.text = descripcion

        // TOTAL PROVISIONAL
        txtPrecio.text =
            "Total provisional: ${formatearPrecio(orden.total)}"

        // ESTADO DE LA ORDEN
        txtEstado.text = nombreEstado(orden.estado)
        txtEstado.setTextColor(colorEstado(orden.estado))

        // FOTOGRAFÍA REAL DEL PRIMER PRODUCTO
        cargarImagen(
            imgCompra,
            primerProducto?.imagenUrl
        )

        // TODA LA TARJETA ABRE EL DETALLE
        tarjeta.setOnClickListener {
            consultarDetalle(orden.idOrden)
        }

        tarjeta.findViewById<ImageView>(
            R.id.imgDetallePedido
        ).setOnClickListener {
            consultarDetalle(orden.idOrden)
        }

        return tarjeta
    }

    // CONSULTAR DETALLE DE UNA ORDEN

    private fun consultarDetalle(idOrden: Int) {

        if (llamadaDetalle != null) return

        val llamada = ApiClient.ordenesApi
            .obtenerDetalleOrden(idOrden, idUsuario)

        llamadaDetalle = llamada

        llamada.enqueue(
            object : Callback<OrdenDetalleResponse> {

                override fun onResponse(
                    call: Call<OrdenDetalleResponse>,
                    response: Response<OrdenDetalleResponse>
                ) {
                    llamadaDetalle = null

                    if (isFinishing || isDestroyed) return

                    val detalle = response.body()

                    if (response.isSuccessful && detalle != null) {
                        mostrarBottomSheet(detalle)
                    } else {
                        mostrarMensaje(
                            "No se pudo consultar el pedido (${response.code()})"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<OrdenDetalleResponse>,
                    t: Throwable
                ) {
                    llamadaDetalle = null

                    if (call.isCanceled || isFinishing || isDestroyed) {
                        return
                    }

                    mostrarMensaje(
                        "No se pudo conectar con el servidor"
                    )
                }
            }
        )
    }

    // BOTTOM SHEET CON TODOS LOS PRODUCTOS

    private fun mostrarBottomSheet(
        orden: OrdenDetalleResponse
    ) {
        val dialog = BottomSheetDialog(this)

        // CARGAR EL XML NUEVO SIN ELIMINAR SU CONTENIDO
        val vista = layoutInflater.inflate(
            R.layout.cliente_activity_detalle_compra,
            null
        )

        val txtNumero = vista.findViewById<TextView>(
            R.id.txtDetalleNumeroPedido
        )

        val txtFecha = vista.findViewById<TextView>(
            R.id.txtDetalleFechaPedido
        )

        val txtEstado = vista.findViewById<TextView>(
            R.id.txtDetalleEstadoPedido
        )

        val contenedorProductos = vista.findViewById<LinearLayout>(
            R.id.contenedorProductosDetalle
        )

        val txtDireccion = vista.findViewById<TextView>(
            R.id.txtDetalleDireccion
        )

        val txtMetodoPago = vista.findViewById<TextView>(
            R.id.txtDetalleMetodoPago
        )

        val txtEstadoPago = vista.findViewById<TextView>(
            R.id.txtDetalleEstadoPago
        )

        val txtSubtotal = vista.findViewById<TextView>(
            R.id.txtDetalleSubtotal
        )

        val txtEnvio = vista.findViewById<TextView>(
            R.id.txtDetalleEnvio
        )

        val txtTotal = vista.findViewById<TextView>(
            R.id.txtDetalleTotal
        )

        val btnCancelar = vista.findViewById<MaterialButton>(
            R.id.btnCancelarPedidoCliente
        )

        val txtAviso = vista.findViewById<TextView>(
            R.id.txtAvisoCancelacion
        )


        txtNumero.text =
            "Pedido #${orden.idOrden.toString().padStart(5, '0')}"

        txtFecha.text =
            "Fecha: ${formatearFecha(orden.fechaOrden)}"


        val estadoActual = orden.estado.trim().uppercase()

        txtEstado.text = nombreEstado(estadoActual)

        val colorTexto = colorEstado(estadoActual)

        txtEstado.setTextColor(colorTexto)

        txtEstado.background =
            android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()

                setColor(
                    when (estadoActual) {
                        "PENDIENTE" ->
                            Color.parseColor("#FFF3CD")

                        "PREPARANDO" ->
                            Color.parseColor("#F3EAFE")

                        "EN_CAMINO" ->
                            Color.parseColor("#E3F2FD")

                        "ENTREGADO", "ENTREGADA" ->
                            Color.parseColor("#E8F5E9")

                        "CANCELADO", "CANCELADA" ->
                            Color.parseColor("#FDE7E7")

                        else ->
                            Color.parseColor("#EEEEEE")
                    }
                )
            }
        val jsonOrden = com.google.gson.Gson()
            .toJsonTree(orden)
            .asJsonObject

        fun leerCampo(nombre: String): String {
            return try {
                val valor = jsonOrden.get(nombre)
                    ?: jsonOrden.get(
                        nombre.replaceFirstChar {
                            it.uppercase()
                        }
                    )

                if (valor == null || valor.isJsonNull) {
                    ""
                } else {
                    valor.asString
                }
            } catch (e: Exception) {
                ""
            }
        }

        val metodoPago = leerCampo("metodoPago")
            .ifBlank { "CONTRAENTREGA" }

        val estadoPago = leerCampo("estadoPago")
            .ifBlank { "PENDIENTE" }

        val costoEnvio = leerCampo("costoEnvio")
            .toDoubleOrNull() ?: 0.0

        txtDireccion.text =
            "Dirección: " +
                    (orden.direccionEntrega
                        ?.takeIf { it.isNotBlank() }
                        ?: "No registrada")

        txtMetodoPago.text = when (metodoPago.uppercase()) {
            "CONTRAENTREGA" ->
                "Método de pago: Contraentrega"

            else ->
                "Método de pago: $metodoPago"
        }

        txtEstadoPago.text = when (estadoPago.uppercase()) {
            "PAGADO" -> "Estado del pago: Pagado"
            "PENDIENTE" -> "Estado del pago: Pendiente"
            else -> "Estado del pago: $estadoPago"
        }

        txtEstadoPago.setTextColor(
            if (estadoPago.equals("PAGADO", true)) {
                Color.parseColor("#2E7D32")
            } else {
                Color.parseColor("#C08400")
            }
        )


        contenedorProductos.removeAllViews()

        if (orden.productos.isEmpty()) {

            agregarTexto(
                contenedorProductos,
                "No hay productos disponibles"
            )

        } else {

            orden.productos.forEach { producto ->

                agregarProductoBottomSheet(
                    contenedorProductos,
                    producto
                )
            }
        }

        txtSubtotal.text =
            "Subtotal: ${formatearPrecio(orden.subtotal)}"

        txtEnvio.text = if (costoEnvio > 0) {
            "Envío: ${formatearPrecio(costoEnvio)}"
        } else {
            "Envío: Por calcular"
        }

        txtTotal.text =
            "Total provisional: ${formatearPrecio(orden.total)}"

        // CANCELACIÓN DEL COMPRADOR

        val puedeCancelar =
            estadoActual == "PENDIENTE" &&
                    estadoPago.equals("PENDIENTE", true)

        btnCancelar.visibility =
            if (puedeCancelar) View.VISIBLE
            else View.GONE

        txtAviso.visibility =
            if (puedeCancelar) View.VISIBLE
            else View.GONE

        if (puedeCancelar) {

            btnCancelar.setOnClickListener {

                btnCancelar.isEnabled = false

                cancelarPedidoCliente(
                    orden.idOrden,
                    idUsuario
                ) {
                    dialog.dismiss()

                    cargarHistorial()
                }
                btnCancelar.isEnabled = true
            }
        }


        dialog.setContentView(vista)

        dialog.show()
    }




    private fun cancelarPedidoCliente(



        idOrden: Int,
        idUsuario: Int,
        alFinalizar: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar pedido")
            .setMessage(
                "¿Estás seguro de que deseas cancelar " +
                        "el pedido #$idOrden?\n\n"
            )
            .setNegativeButton("Volver", null)
            .setPositiveButton("Sí, cancelar") { _, _ ->

                ApiClient.ordenesApi
                    .cancelarPedidoComprador(
                        idUsuario,
                        idOrden
                    )


                    .enqueue(object : Callback<JsonObject> {

                        override fun onResponse(
                            call: Call<JsonObject>,
                            response: Response<JsonObject>
                        ) {
                            if (isFinishing || isDestroyed) return

                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ComprasActivity,
                                    "Pedido cancelado correctamente",
                                    Toast.LENGTH_LONG
                                ).show()

                                alFinalizar()
                            } else {
                                val mensaje = when (response.code()) {
                                    403 -> "Cancelación no permitida"
                                    404 -> "Pedido no encontrado"
                                    409 -> "El pedido ya no puede cancelarse"
                                    else -> "No se pudo cancelar el pedido"
                                }

                                Toast.makeText(
                                    this@ComprasActivity,
                                    mensaje,
                                    Toast.LENGTH_LONG
                                ).show()

                                if (response.code() == 409) {
                                    alFinalizar()
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<JsonObject>,
                            t: Throwable
                        ) {
                            if (isFinishing || isDestroyed) return

                            Toast.makeText(
                                this@ComprasActivity,
                                "No se pudo confirmar la cancelación. " +
                                        "Actualiza tus compras antes de reintentar.",
                                Toast.LENGTH_LONG
                            ).show()

                            alFinalizar()
                        }
                    })
            }
            .show()
    }


    // PRODUCTO CON IMAGEN DENTRO DEL BOTTOM SHEET

    private fun agregarProductoBottomSheet(
        contenedor: LinearLayout,
        producto: DetalleProductoOrdenResponse
    ) {

        val fila = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
        }

        val imagen = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dp(85),
                dp(85)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = producto.nombre
        }

        cargarImagen(imagen, producto.imagenUrl)

        fila.addView(imagen)

        val informacion = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        informacion.layoutParams =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

        agregarTexto(
            informacion,
            producto.nombre,
            16f,
            "#6A4C93",
            true
        )

        val talla = producto.talla
            ?.takeIf { it.isNotBlank() }
            ?: "No especificada"

        agregarTexto(
            informacion,
            "Tamaño: $talla"
        )

        agregarTexto(
            informacion,
            "Cantidad: ${producto.cantidad}"
        )

        agregarTexto(
            informacion,
            "Precio unitario: ${
                formatearPrecio(producto.precioUnitario)
            }"
        )

        agregarTexto(
            informacion,
            "Subtotal: ${
                formatearPrecio(producto.subtotal)
            }",
            14f,
            "#D46AA7",
            true
        )

        fila.addView(informacion)
        contenedor.addView(fila)

        // DIVISOR
        val divisor = View(this).apply {
            setBackgroundColor(Color.parseColor("#E8DDEE"))
        }

        contenedor.addView(
            divisor,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        )
    }

    // CARGAR FOTO
    private fun cargarImagen(
        imageView: ImageView,
        imagenUrl: String?
    ) {

        // OBTENER URL DESDE LA CONFIGURACIÓN CENTRAL
        val urlCompleta = ImagenUrlHelper.obtenerUrl(
            imagenUrl
        )

        if (urlCompleta.isNullOrBlank()) {

            imageView.setImageResource(
                R.drawable.carrito_de_compras
            )

            return
        }

        Glide.with(this@ComprasActivity)
            .load(urlCompleta)
            .placeholder(R.drawable.carrito_de_compras)
            .error(R.drawable.carrito_de_compras)
            .centerCrop()
            .into(imageView)
    }


    //  TEXTOS DINÁMICOS

    private fun agregarTexto(
        contenedor: LinearLayout,
        contenido: String,
        tamano: Float = 14f,
        color: String = "#555555",
        negrita: Boolean = false
    ) {

        val texto = TextView(this).apply {
            text = contenido
            textSize = tamano
            setTextColor(Color.parseColor(color))

            if (negrita) {
                setTypeface(null, Typeface.BOLD)
            }

            setPadding(0, dp(6), 0, dp(5))
        }

        contenedor.addView(texto)
    }

    private fun dp(valor: Int): Int {
        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }

    // FORMATEAR PRECIOS Y FECHAS

    private fun formatearPrecio(precio: Double): String {

        return NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        ).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }.format(precio)
    }

    private fun formatearFecha(fecha: String): String {

        return try {

            val entrada = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.US
            )

            val salida = SimpleDateFormat(
                "dd MMMM yyyy",
                Locale("es", "CO")
            )

            val fechaConvertida = entrada.parse(fecha)

            if (fechaConvertida != null) {
                salida.format(fechaConvertida)
            } else {
                fecha
            }

        } catch (e: Exception) {
            fecha
        }
    }

    // ESTADOS DE LAS ÓRDENES

    private fun nombreEstado(estado: String): String {

        return when (estado) {
            "PENDIENTE" -> "Pendiente"
            "PAGADA" -> "Pagada"
            "PREPARANDO" -> "Preparando"
            "EN_CAMINO" -> "En camino"
            "ENTREGADA" -> "Entregada"
            "CANCELADA" -> "Cancelada"
            else -> estado
        }
    }

    private fun colorHexEstado(estado: String): String {

        return when (estado) {
            "PENDIENTE" -> "#FF9800"
            "PAGADA" -> "#4CAF50"
            "PREPARANDO" -> "#9B7AE0"
            "EN_CAMINO" -> "#2196F3"
            "ENTREGADA" -> "#4CAF50"
            "CANCELADA" -> "#E53935"
            else -> "#666666"
        }
    }

    private fun colorEstado(estado: String): Int {
        return Color.parseColor(colorHexEstado(estado))
    }

    // MENSAJES

    private fun mostrarError(mensaje: String) {

        historialCargado = false
        ordenes = emptyList()
        layoutOrdenes.removeAllViews()

        txtSinCompras.text = mensaje
        txtSinCompras.visibility = View.VISIBLE
    }

    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }

    // NAVEGACIÓN

    private fun configurarNavegacion() {

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNavigation.setOnItemSelectedListener { item ->

            val destino = when (item.itemId) {
                R.id.home -> HomeBuyerActivity::class.java
                R.id.categoria -> CatalogoActivity::class.java
                R.id.carrito -> CarritoActivity::class.java
                R.id.perfil -> PerfilClienteActivity::class.java
                else -> null
            }

            if (destino != null) {
                startActivity(Intent(this, destino))
                finish()
                true
            } else {
                false
            }
        }
    }

    override fun onDestroy() {
        llamadaHistorial?.cancel()
        llamadaDetalle?.cancel()
        super.onDestroy()
    }
}
