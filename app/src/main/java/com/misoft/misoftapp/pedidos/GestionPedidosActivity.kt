package com.misoft.misoftapp.pedidos

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.button.MaterialButton
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.api.ImagenUrlHelper
import com.misoft.misoftapp.catalogo.ProductosVendedorActivity
import com.misoft.misoftapp.home.HomeVendedorActivity
import com.misoft.misoftapp.perfil_vendedor.PerfilVendedorActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.misoft.misoftapp.api.ActualizarEstadoOrdenRequest

class GestionPedidosActivity : AppCompatActivity() {

    private lateinit var contenedorPedidos: LinearLayout

    private lateinit var chipTodos: Chip
    private lateinit var chipPendientes: Chip
    private lateinit var chipEnviados: Chip
    private lateinit var chipEntregados: Chip

    private var pedidos = listOf<JsonObject>()
    private var filtroActual = "TODOS"

    private var consultaPedidos: Call<JsonArray>? = null

    private val morado = Color.parseColor("#8E6AD8")
    private val moradoOscuro = Color.parseColor("#6A4C93")
    private val gris = Color.parseColor("#777777")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.vendedor_activity_pedidos)

        contenedorPedidos = findViewById(
            R.id.contenedorPedidos
        )

        chipTodos = findViewById(R.id.chipTodos)
        chipPendientes = findViewById(R.id.chipPendientes)
        chipEnviados = findViewById(R.id.chipEnviados)
        chipEntregados = findViewById(R.id.chipEntregados)

        configurarFiltros()
        configurarNavegacion()
    }

    override fun onResume() {
        super.onResume()
        cargarPedidos()
    }

    // CONSULTAR PEDIDOS REALES

    private fun cargarPedidos() {

        consultaPedidos?.cancel()

        mostrarMensajeLista("Cargando pedidos...")

        val llamada = ApiClient.pedidosVendedorApi
            .obtenerPedidos()

        consultaPedidos = llamada

        llamada.enqueue(object : Callback<JsonArray> {

            override fun onResponse(
                call: Call<JsonArray>,
                response: Response<JsonArray>
            ) {

                if (
                    call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) return

                if (!response.isSuccessful) {

                    mostrarMensajeLista(
                        "No se pudieron cargar los pedidos " +
                                "(HTTP ${response.code()})"
                    )
                    return
                }

                pedidos = response.body()
                    ?.mapNotNull { elemento ->
                        if (elemento.isJsonObject) {
                            elemento.asJsonObject
                        } else {
                            null
                        }
                    }
                    .orEmpty()

                mostrarPedidosFiltrados()
            }

            override fun onFailure(
                call: Call<JsonArray>,
                t: Throwable
            ) {

                if (
                    call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) return

                mostrarMensajeLista(
                    "No se pudo conectar con el servidor"
                )
            }
        })
    }

    // FILTROS

    private fun configurarFiltros() {

        chipTodos.setOnClickListener {
            cambiarFiltro("TODOS")
        }

        chipPendientes.setOnClickListener {
            cambiarFiltro("PENDIENTES")
        }

        chipEnviados.setOnClickListener {
            cambiarFiltro("ENVIADOS")
        }

        chipEntregados.setOnClickListener {
            cambiarFiltro("ENTREGADAS")
        }

        actualizarDisenoFiltros()
    }

    private fun cambiarFiltro(filtro: String) {
        filtroActual = filtro
        actualizarDisenoFiltros()
        mostrarPedidosFiltrados()
    }

    private fun actualizarDisenoFiltros() {

        val filtros = listOf(
            chipTodos to "TODOS",
            chipPendientes to "PENDIENTES",
            chipEnviados to "ENVIADOS",
            chipEntregados to "ENTREGADAS"
        )

        filtros.forEach { (chip, filtro) ->

            val seleccionado = filtroActual == filtro

            chip.isCheckable = false

            chip.chipBackgroundColor =
                ColorStateList.valueOf(
                    if (seleccionado) morado
                    else Color.parseColor("#F0E8F8")
                )

            chip.setTextColor(
                if (seleccionado) Color.WHITE
                else moradoOscuro
            )
        }
    }

    private fun mostrarPedidosFiltrados() {

        contenedorPedidos.removeAllViews()

        val filtrados = pedidos.filter { pedido ->

            val estado = textoJson(
                pedido,
                "estado"
            ).uppercase()

            when (filtroActual) {

                "PENDIENTES" ->
                    estado == "PENDIENTE" ||
                            estado == "PREPARANDO"

                "ENVIADOS" ->
                    estado == "EN_CAMINO" ||
                            estado == "ENVIADO"

                "ENTREGADAS" ->
                    estado == "ENTREGADA" ||
                            estado == "ENTREGADA"

                else -> true
            }
        }

        if (filtrados.isEmpty()) {

            mostrarMensajeLista(
                if (pedidos.isEmpty()) {
                    "Todavía no hay pedidos registrados"
                } else {
                    "No hay pedidos en esta categoría"
                }
            )

            return
        }

        filtrados.forEach { pedido ->
            crearTarjetaPedido(pedido)
        }
    }

    // CREAR TARJETA DE PEDIDO

    private fun crearTarjetaPedido(
        pedido: JsonObject
    ) {

        val idOrden = enteroJson(pedido, "idOrden")
        val idUsuario = enteroJson(pedido, "usuarioId")
        val estado = textoJson(pedido, "estado")
        val total = decimalJson(pedido, "total")

        val cantidadProductos = enteroJson(
            pedido,
            "cantidadProductos"
        )

        val tarjeta = MaterialCardView(this).apply {
            radius = dp(16).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(Color.WHITE)
            isClickable = true
            isFocusable = true
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(16), dp(16), dp(16), dp(16)
            )
        }

        // INFORMACIÓN PRINCIPAL
        val informacion = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val txtPedido = TextView(this).apply {
            text = "Pedido #$idOrden"
            textSize = 16f
            setTextColor(moradoOscuro)
            setTypeface(null, Typeface.BOLD)
        }

        informacion.addView(txtPedido)


        val nombreComprador = textoJson(
            pedido,
            "nombreComprador"
        ).ifBlank {
            "Comprador #$idUsuario"
        }


        val txtComprador = TextView(this).apply {
            text = nombreComprador
            textSize = 13f
            setTextColor(gris)

            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }



        informacion.addView(
            txtComprador,
            LinearLayout.LayoutParams(
                -1, -2
            ).apply {
                topMargin = dp(5)
            }
        )

        val txtProductos = TextView(this).apply {
            text = "$cantidadProductos producto(s)"
            textSize = 12f
            setTextColor(gris)
        }

        informacion.addView(
            txtProductos,
            LinearLayout.LayoutParams(
                -1, -2
            ).apply {
                topMargin = dp(4)
            }
        )

        val txtTotal = TextView(this).apply {
            text = formatearPrecio(total)
            textSize = 16f
            setTextColor(Color.parseColor("#333333"))
            setTypeface(null, Typeface.BOLD)
        }

        informacion.addView(
            txtTotal,
            LinearLayout.LayoutParams(
                -1, -2
            ).apply {
                topMargin = dp(8)
            }
        )

        contenido.addView(
            informacion,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                rightMargin = dp(8)
            }
        )
        // ESTADO DEL PEDIDO

        val estadoNormalizado = estado.trim().uppercase()

        val txtEstado = TextView(this).apply {

            text = nombreEstado(estadoNormalizado)

            textSize = 11f

            setTypeface(null, Typeface.BOLD)

            gravity = Gravity.CENTER

            val (colorFondo, colorTexto) =
                coloresEstado(estadoNormalizado)

            background = android.graphics.drawable
                .GradientDrawable().apply {

                    shape = android.graphics.drawable
                        .GradientDrawable.RECTANGLE

                    cornerRadius = dp(8).toFloat()

                    setColor(colorFondo)
                }

            setTextColor(colorTexto)

            setPadding(
                dp(10),
                dp(7),
                dp(10),
                dp(7)
            )

            maxLines = 1

            isSingleLine = true
        }


        txtEstado.minWidth = dp(72)

        contenido.addView(
            txtEstado,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )


        // FLECHA
        val flecha = ImageView(this).apply {
            setImageResource(R.drawable.angulo_derecho)
            contentDescription = "Ver detalle del pedido"
        }

        contenido.addView(
            flecha,
            LinearLayout.LayoutParams(
                dp(16), dp(16)
            ).apply {
                leftMargin = dp(10)
            }
        )

        tarjeta.addView(contenido)

        contenedorPedidos.addView(
            tarjeta,
            LinearLayout.LayoutParams(
                -1, -2
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(2)
            }
        )

        tarjeta.setOnClickListener {
            mostrarDetallePedido(pedido)
        }
    }

    // BOTTOM SHEET - DETALLE REAL

    private fun mostrarDetallePedido(
        pedido: JsonObject
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.vendedor_bottom_detalle_pedido,
            null
        )

        val txtPedido =
            view.findViewById<TextView>(R.id.txtPedido)

        val txtFecha =
            view.findViewById<TextView>(R.id.txtFechaPedido)

        val txtCliente =
            view.findViewById<TextView>(R.id.txtCliente)

        val txtTelefono =
            view.findViewById<TextView>(R.id.txtTelefono)

        val txtDireccion =
            view.findViewById<TextView>(R.id.txtDireccion)

        val contenedorProductos =
            view.findViewById<LinearLayout>(
                R.id.contenedorProductosPedido
            )

        val txtMetodoPago =
            view.findViewById<TextView>(R.id.txtMetodoPago)

        val txtEstadoPago =
            view.findViewById<TextView>(R.id.txtEstadoPago)

        val txtSubtotal =
            view.findViewById<TextView>(
                R.id.txtSubtotalPedido
            )

        val txtCostoEnvio =
            view.findViewById<TextView>(
                R.id.txtCostoEnvio
            )

        val txtTotal =
            view.findViewById<TextView>(R.id.txtTotal)

        val txtAvisoEnvio =
            view.findViewById<TextView>(
                R.id.txtAvisoEnvio
            )

        val actEstadoPedido =
            view.findViewById<android.widget.AutoCompleteTextView>(
                R.id.actEstadoPedido
            )

        val btnActualizar =
            view.findViewById<MaterialButton>(
                R.id.btnActualizarEstado
            )

        // INFORMACIÓN DE LA ORDEN
        val idOrden = enteroJson(pedido, "idOrden")
        val idUsuario = enteroJson(pedido, "usuarioId")

        val estado = textoJson(pedido, "estado")
        val estadoPago = textoJson(pedido, "estadoPago")
        val metodoPago = textoJson(pedido, "metodoPago")

        val fecha = textoJson(pedido, "fechaOrden")
        val direccion = textoJson(pedido, "direccionEntrega")

        txtPedido.text = "Pedido #$idOrden"

        txtFecha.text = if (fecha.isNotBlank()) {
            "Fecha: ${fecha.replace("T", " ").take(16)}"
        } else {
            "Fecha no disponible"
        }

        val nombreComprador = textoJson(
            pedido,
            "nombreComprador"
        ).ifBlank {
            "Comprador #$idUsuario"
        }

        val telefonoComprador = textoJson(
            pedido,
            "telefonoComprador"
        )

        txtCliente.text = nombreComprador

        txtTelefono.text = telefonoComprador.ifBlank {
            "No registrado"
        }
        txtDireccion.text = direccion.ifBlank {
            "Dirección no disponible"
        }

        txtMetodoPago.text = when (metodoPago.uppercase()) {
            "CONTRAENTREGA" -> "Pago contraentrega"
            else -> metodoPago
        }

        txtEstadoPago.text = nombreEstado(estadoPago)

        txtEstadoPago.setTextColor(
            if (estadoPago.equals("PAGADO", true)) {
                Color.parseColor("#2E7D32")
            } else {
                Color.parseColor("#C08400")
            }
        )

        txtSubtotal.text = formatearPrecio(
            decimalJson(pedido, "subtotal")
        )

        val costoEnvio = decimalJson(
            pedido,
            "costoEnvio"
        )

        // EL ENVÍO SIGUE SIENDO PROVISIONAL
        txtCostoEnvio.text = if (costoEnvio > 0) {
            formatearPrecio(costoEnvio)
        } else {
            "Por calcular"
        }

        txtTotal.text = formatearPrecio(
            decimalJson(pedido, "total")
        )

        txtAvisoEnvio.visibility =
            if (costoEnvio > 0) View.GONE
            else View.VISIBLE

        // PRODUCTOS DEL PEDIDO
        contenedorProductos.removeAllViews()

        val productos = pedido.getAsJsonArray("productos")

        if (productos == null || productos.size() == 0) {

            val aviso = TextView(this).apply {
                text = "No hay productos disponibles"
                setTextColor(gris)
                textSize = 14f
            }

            contenedorProductos.addView(aviso)

        } else {

            productos.forEach { elemento ->

                if (elemento.isJsonObject) {
                    crearTarjetaProducto(
                        contenedorProductos,
                        elemento.asJsonObject
                    )
                }
            }
        }


// CONFIGURAR ACTUALIZACIÓN DEL ESTADO

        val estadoActual = estado.trim().uppercase()

        val siguienteEstado = when (estadoActual) {
            "PENDIENTE" -> "PREPARANDO"
            "PREPARANDO" -> "EN_CAMINO"
            "EN_CAMINO" -> "ENTREGADA"
            else -> null
        }

        actEstadoPedido.setText(
            nombreEstado(estadoActual),
            false
        )

        if (siguienteEstado == null) {

            // PEDIDO FINALIZADO O CANCELADO
            actEstadoPedido.isEnabled = false
            btnActualizar.isEnabled = false
            btnActualizar.text = "Pedido sin cambios disponibles"

        } else {

            val estadosDisponibles = listOf(
                nombreEstado(siguienteEstado)
            )

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                estadosDisponibles
            )

            actEstadoPedido.setAdapter(adapter)
            actEstadoPedido.isEnabled = true

            btnActualizar.isEnabled = false
            btnActualizar.text = "Selecciona el siguiente estado"

            // OBLIGAR A ELEGIR EL SIGUIENTE ESTADO
            actEstadoPedido.setOnClickListener {
                actEstadoPedido.showDropDown()
            }


            actEstadoPedido.setOnItemClickListener { _, _, _, _ ->

                val seleccionado = actEstadoPedido.text
                    .toString()
                    .trim()

                btnActualizar.isEnabled =
                    seleccionado == nombreEstado(siguienteEstado)

                btnActualizar.text =
                    if (btnActualizar.isEnabled) {
                        "Actualizar estado"
                    } else {
                        "Selecciona el siguiente estado"
                    }

                actEstadoPedido.clearFocus()
            }


            btnActualizar.setOnClickListener {

                val seleccionado = actEstadoPedido.text
                    .toString()
                    .trim()

                if (seleccionado != nombreEstado(siguienteEstado)) {

                    mostrarMensaje(
                        "Selecciona el siguiente estado del pedido"
                    )

                    return@setOnClickListener
                }

                AlertDialog.Builder(this@GestionPedidosActivity)
                    .setTitle("Actualizar pedido")
                    .setMessage(
                        "¿Deseas cambiar el pedido #$idOrden " +
                                "a ${nombreEstado(siguienteEstado)}?"
                    )
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Actualizar") { _, _ ->

                        actualizarEstadoPedido(
                            idOrden,
                            siguienteEstado,
                            dialog,
                            btnActualizar
                        )
                    }

                    .show()
            }
        }


// ACCIONES DE PAGO Y CANCELACIÓN
        val contenidoPrincipal =
            (view as? android.widget.ScrollView)
                ?.getChildAt(0) as? LinearLayout

        if (contenidoPrincipal != null) {

            val estadoNormalizado = estado.trim().uppercase()
            val pagoNormalizado = estadoPago.trim().uppercase()

            // CONFIRMACIÓN DE PAGO
            if (
                (estadoNormalizado == "ENTREGADA" ||
                        estadoNormalizado == "ENTREGADO") &&
                pagoNormalizado == "PENDIENTE" &&
                metodoPago.equals("CONTRAENTREGA", true)
            ) {

                val btnConfirmarPago = MaterialButton(this).apply {
                    text = "Confirmar pago recibido"
                    backgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#6A4C93")
                    )
                }

                contenidoPrincipal.addView(btnConfirmarPago)

                btnConfirmarPago.setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Confirmar pago")
                        .setMessage(
                            "¿Confirmas que recibiste el dinero " +
                                    "del pedido #$idOrden?"
                        )
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Confirmar") { _, _ ->
                            btnConfirmarPago.isEnabled = false
                            ejecutarAccionPedido(
                                ApiClient.pedidosVendedorApi
                                    .confirmarPago(idOrden),
                                dialog,
                                btnConfirmarPago
                            )
                        }
                        .show()
                }
            }

            // CANCELACIÓN DE PEDIDO
            if (
                (estadoNormalizado == "PENDIENTE" ||
                        estadoNormalizado == "PREPARANDO") &&
                pagoNormalizado == "PENDIENTE"
            ) {

                val btnCancelarPedido = MaterialButton(this).apply {
                    text = "Cancelar pedido"
                    backgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#C62828")
                    )
                }

                contenidoPrincipal.addView(btnCancelarPedido)

                btnCancelarPedido.setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Cancelar pedido")
                        .setMessage(
                            "¿Seguro que deseas cancelar el pedido " +
                                    "#$idOrden? Las unidades se devolverán " +
                                    "al inventario."
                        )
                        .setNegativeButton("Volver", null)
                        .setPositiveButton("Sí, cancelar") { _, _ ->
                            btnCancelarPedido.isEnabled = false
                            ejecutarAccionPedido(
                                ApiClient.pedidosVendedorApi
                                    .cancelarPedido(idOrden),
                                dialog,
                                btnCancelarPedido
                            )
                        }
                        .show()
                }
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

// ENVIAR CONFIRMACIÓN DE PAGO A LA API

    private fun confirmarPagoPedido(
        idOrden: Int,
        dialog: BottomSheetDialog,
        boton: MaterialButton
    ) {

        ApiClient.pedidosVendedorApi
            .confirmarPago(idOrden)
            .enqueue(object : Callback<JsonObject> {

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {

                        mostrarMensaje(
                            "Pago confirmado correctamente"
                        )

                        dialog.dismiss()

                        // RECARGAR LOS PEDIDOS DESDE MYSQL
                        cargarPedidos()

                    } else {

                        val mensaje = when (response.code()) {
                            403 -> "Operación no permitida"
                            404 -> "No se encontró el pedido"
                            409 -> "El pago ya fue registrado o el pedido cambió"
                            else -> "No se pudo confirmar el pago"
                        }

                        mostrarMensaje(mensaje)

                        if (response.code() == 409) {
                            dialog.dismiss()
                            cargarPedidos()
                        } else {
                            boton.isEnabled = true
                            boton.text = "Confirmar pago recibido"
                        }
                    }
                }

                override fun onFailure(
                    call: Call<JsonObject>,
                    t: Throwable
                ) {
                    if (isFinishing || isDestroyed) return

                    mostrarMensaje(
                        "No se pudo verificar el resultado. " +
                                "Consulta el pedido antes de intentar nuevamente."
                    )

                    dialog.dismiss()
                    cargarPedidos()
                }
            })
    }


    // TARJETA DE PRODUCTO DEL PEDIDO


    private fun crearTarjetaProducto(
        contenedor: LinearLayout,
        producto: JsonObject
    ) {

        val nombre = textoJson(
            producto,
            "nombre"
        ).ifBlank {
            "Producto"
        }

        val talla = textoJson(
            producto,
            "talla"
        )

        val cantidad = enteroJson(
            producto,
            "cantidad"
        )

        val precio = decimalJson(
            producto,
            "precioUnitario"
        )

        val subtotal = decimalJson(
            producto,
            "subtotal"
        )

        val imagen = textoJson(
            producto,
            "imagenUrl"
        )

        val tarjeta = MaterialCardView(this).apply {
            radius = dp(14).toFloat()
            cardElevation = 0f
            strokeColor = Color.parseColor("#EADFF3")
            strokeWidth = dp(1)
            setCardBackgroundColor(Color.WHITE)
        }

        val fila = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(12), dp(12), dp(12), dp(12)
            )
        }

        val imgProducto = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageResource(
                android.R.drawable.ic_menu_gallery
            )
        }

        fila.addView(
            imgProducto,
            LinearLayout.LayoutParams(
                dp(72), dp(72)
            )
        )

        val urlImagen = ImagenUrlHelper.obtenerUrl(imagen)

        if (!urlImagen.isNullOrBlank()) {

            Glide.with(this)
                .load(urlImagen)
                .placeholder(
                    android.R.drawable.ic_menu_gallery
                )
                .error(
                    android.R.drawable.ic_menu_gallery
                )
                .fitCenter()
                .into(imgProducto)
        }

        val informacion = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val txtNombre = TextView(this).apply {
            text = nombre
            textSize = 15f
            setTextColor(moradoOscuro)
            setTypeface(null, Typeface.BOLD)
        }

        informacion.addView(txtNombre)

        if (talla.isNotBlank()) {

            val txtTalla = TextView(this).apply {
                text = "Talla: $talla"
                textSize = 12f
                setTextColor(gris)
            }

            informacion.addView(txtTalla)
        }

        val txtCantidad = TextView(this).apply {
            text = "Cantidad: $cantidad"
            textSize = 13f
            setTextColor(gris)
        }

        informacion.addView(txtCantidad)

        val txtPrecio = TextView(this).apply {
            text = "Unitario: ${formatearPrecio(precio)}"
            textSize = 12f
            setTextColor(gris)
        }

        informacion.addView(txtPrecio)

        val txtSubtotal = TextView(this).apply {
            text = "Subtotal: ${formatearPrecio(subtotal)}"
            textSize = 14f
            setTextColor(moradoOscuro)
            setTypeface(null, Typeface.BOLD)
        }

        informacion.addView(txtSubtotal)

        fila.addView(
            informacion,
            LinearLayout.LayoutParams(
                0, -2, 1f
            ).apply {
                leftMargin = dp(12)
            }
        )

        tarjeta.addView(fila)

        contenedor.addView(
            tarjeta,
            LinearLayout.LayoutParams(
                -1, -2
            ).apply {
                bottomMargin = dp(10)
            }
        )
    }

    // ESTADOS Y COLORES

    private fun nombreEstado(estado: String): String {

        return when (estado.trim().uppercase()) {
            "PENDIENTE" -> "Pendiente"
            "PREPARANDO" -> "Preparando"
            "EN_CAMINO", "ENVIADO" -> "En camino"
            "ENTREGADA", "ENTREGADA" -> "Entregada"
            "CANCELADA" -> "Cancelada"
            "PAGADO" -> "Pagado"
            else -> estado
        }
    }

    private fun coloresEstado(
        estado: String
    ): Pair<Int, Int> {

        return when (estado.uppercase()) {

            "PENDIENTE" -> Pair(
                Color.parseColor("#FFF3CD"),
                Color.parseColor("#A66C00")
            )

            "PREPARANDO" -> Pair(
                Color.parseColor("#F3EAFE"),
                Color.parseColor("#6A4C93")
            )

            "EN_CAMINO", "ENVIADO" -> Pair(
                Color.parseColor("#DDF8E9"),
                Color.parseColor("#2E9B62")
            )

            "ENTREGADO", "ENTREGADA" -> Pair(
                Color.parseColor("#E3F2FD"),
                Color.parseColor("#1565C0")
            )

            "CANCELADA" -> Pair(
                Color.parseColor("#FDE7E7"),
                Color.parseColor("#C62828")
            )

            else -> Pair(
                Color.parseColor("#EEEEEE"),
                Color.parseColor("#555555")
            )
        }
    }

    // LEER VALORES JSON

    private fun textoJson(
        objeto: JsonObject,
        campo: String
    ): String {

        return try {

            val valor = objeto.get(campo)
                ?: objeto.get(
                    campo.replaceFirstChar {
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

    private fun enteroJson(
        objeto: JsonObject,
        campo: String
    ): Int {

        return try {
            objeto.get(campo)?.asInt
                ?: objeto.get(
                    campo.replaceFirstChar {
                        it.uppercase()
                    }
                )?.asInt
                ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun decimalJson(
        objeto: JsonObject,
        campo: String
    ): Double {

        return try {
            objeto.get(campo)?.asDouble
                ?: objeto.get(
                    campo.replaceFirstChar {
                        it.uppercase()
                    }
                )?.asDouble
                ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    // UTILIDADES

    private fun mostrarMensajeLista(
        mensaje: String
    ) {

        contenedorPedidos.removeAllViews()

        val texto = TextView(this).apply {
            text = mensaje
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(gris)
            setPadding(
                dp(12), dp(60), dp(12), dp(30)
            )
        }

        contenedorPedidos.addView(texto)
    }

    private fun formatearPrecio(
        valor: Double
    ): String {

        return NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        ).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }.format(valor)
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
                R.id.bottomNavigationSeller
            )

        bottomNavigation.selectedItemId = R.id.pedidos

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> {

                    startActivity(
                        Intent(
                            this,
                            HomeVendedorActivity::class.java
                        )
                    )

                    finish()
                    true
                }

                R.id.productos -> {

                    startActivity(
                        Intent(
                            this,
                            ProductosVendedorActivity::class.java
                        )
                    )

                    finish()
                    true
                }

                R.id.pedidos -> true

                R.id.perfil -> {

                    startActivity(
                        Intent(
                            this,
                            PerfilVendedorActivity::class.java
                        )
                    )

                    finish()
                    true
                }

                else -> false
            }
        }
    }

    private fun actualizarEstadoPedido(
        idOrden: Int,
        nuevoEstado: String,
        dialog: BottomSheetDialog,
        boton: MaterialButton
    ) {

        boton.isEnabled = false
        boton.text = "Actualizando..."

        val request = ActualizarEstadoOrdenRequest(
            estado = nuevoEstado
        )

        ApiClient.pedidosVendedorApi
            .actualizarEstado(idOrden, request)
            .enqueue(object : Callback<JsonObject> {

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {

                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {

                        mostrarMensaje(
                            "Estado actualizado correctamente"
                        )

                        dialog.dismiss()

                        // CONSULTAR LOS DATOS ACTUALIZADOS
                        cargarPedidos()

                    } else {

                        boton.isEnabled = true
                        boton.text = "Actualizar estado"

                        val mensaje = when (response.code()) {
                            401 -> "Debes iniciar sesión nuevamente"
                            403 -> "No tienes permisos para actualizar pedidos"
                            404 -> "El pedido no existe"
                            409 -> "El estado del pedido cambió. Actualiza la lista"
                            else -> "No se pudo actualizar el pedido " +
                                    "(HTTP ${response.code()})"
                        }

                        mostrarMensaje(mensaje)

                        if (response.code() == 409) {
                            dialog.dismiss()
                            cargarPedidos()
                        }
                    }
                }

                override fun onFailure(
                    call: Call<JsonObject>,
                    t: Throwable
                ) {

                    if (isFinishing || isDestroyed) return

                    boton.isEnabled = true
                    boton.text = "Actualizar estado"

                    mostrarMensaje(
                        "No se pudo confirmar la actualización. " +
                                "Consulta el estado antes de reintentar."
                    )

                    dialog.dismiss()
                    cargarPedidos()
                }
            })
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }


// CONFIRMAR PAGO O CANCELAR PEDIDO

    private fun ejecutarAccionPedido(
        llamada: Call<JsonObject>,
        dialog: BottomSheetDialog,
        boton: MaterialButton
    ) {
        llamada.enqueue(object : Callback<JsonObject> {

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                if (isFinishing || isDestroyed) return

                if (response.isSuccessful) {
                    val mensaje = response.body()
                        ?.get("mensaje")
                        ?.asString
                        ?: "Operación realizada correctamente"

                    mostrarMensaje(mensaje)

                    dialog.dismiss()
                    cargarPedidos()
                } else {
                    boton.isEnabled = true

                    val mensaje = when (response.code()) {
                        403 -> "Operación no permitida por el servidor"
                        404 -> "No se encontró el pedido"
                        409 -> "El pedido ya cambió de estado"
                        else -> "Error HTTP ${response.code()}"
                    }

                    mostrarMensaje(mensaje)

                    if (response.code() == 409) {
                        dialog.dismiss()
                        cargarPedidos()
                    }
                }
            }

            override fun onFailure(
                call: Call<JsonObject>,
                t: Throwable
            ) {
                if (isFinishing || isDestroyed) return

                boton.isEnabled = false

                mostrarMensaje(
                    "No se pudo confirmar el resultado. " +
                            "Actualiza los pedidos antes de reintentar."
                )

                dialog.dismiss()
                cargarPedidos()
            }
        })
    }


    override fun onDestroy() {

        consultaPedidos?.cancel()

        super.onDestroy()
    }
}
