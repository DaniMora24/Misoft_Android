package com.misoft.misoftapp.compras

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.misoft.misoftapp.perfil_cliente.DireccionActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.api.CarritoResponse
import com.misoft.misoftapp.api.ProductoCarritoResponse
import com.misoft.misoftapp.api.OperacionCarritoResponse
import com.misoft.misoftapp.api.ActualizarCantidadCarritoRequest
import com.misoft.misoftapp.api.ImagenUrlHelper
import com.misoft.misoftapp.api.CrearOrdenRequest
import com.misoft.misoftapp.api.CrearOrdenResponse
import com.misoft.misoftapp.catalogo.CatalogoActivity
import com.misoft.misoftapp.home.HomeBuyerActivity
import com.misoft.misoftapp.perfil_cliente.PerfilClienteActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.ScrollView
import android.widget.RadioButton
import com.google.android.material.card.MaterialCardView

class CarritoActivity : AppCompatActivity() {

    private lateinit var layoutProductos: LinearLayout
    private lateinit var layoutResumen: LinearLayout
    private lateinit var txtCarritoVacio: TextView
    private lateinit var txtSubtotal: TextView
    private lateinit var txtTotal: TextView
    private lateinit var imgVaciarCarrito: ImageView
    private lateinit var btnProcederPago: MaterialButton

    private var idUsuario = -1
    private var operacionEnCurso = false

    private var consultaCarrito: Call<CarritoResponse>? = null
    private var llamadaCrearOrden: Call<CrearOrdenResponse>? = null
    private var carritoActual: CarritoResponse? = null
    private var compraEnCurso = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.cliente_activity_carrito)

        // REFERENCIAS DEL XML
        layoutProductos = findViewById(
            R.id.layoutProductosCarrito
        )

        layoutResumen = findViewById(
            R.id.layoutResumenCarrito
        )

        txtCarritoVacio = findViewById(
            R.id.txtCarritoVacio
        )

        txtSubtotal = findViewById(
            R.id.txtSubtotal
        )

        txtTotal = findViewById(
            R.id.txtTotal
        )

        imgVaciarCarrito = findViewById(
            R.id.imgVaciarCarrito
        )

        btnProcederPago = findViewById(
            R.id.btnProcederPago
        )

        // OBTENER USUARIO DE LA SESIÓN
        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        idUsuario = preferencias.getInt(
            "idUsuario",
            -1
        )

        configurarBotones()
        configurarNavegacion()
    }

    override fun onResume() {
        super.onResume()

        if (idUsuario <= 0) {
            mostrarMensaje(
                "Debes iniciar sesión para consultar tu carrito"
            )
            finish()
            return
        }

        cargarCarrito()
    }

    // BOTONES PRINCIPALES
    private fun configurarBotones() {

        imgVaciarCarrito.setOnClickListener {
            confirmarVaciarCarrito()
        }

        // SOLO HABILITAR CUANDO SE CARGUE UN CARRITO VÁLIDO
        btnProcederPago.isEnabled = false
        btnProcederPago.text = "Confirmar compra"
        btnProcederPago.setOnClickListener {
            mostrarConfirmacionCompra()
        }
    }

    // CONSULTAR CARRITO REAL
    private fun cargarCarrito() {

        consultaCarrito?.cancel()
        if (!compraEnCurso) btnProcederPago.isEnabled = false
        carritoActual = null

        txtCarritoVacio.text = "Cargando carrito..."
        txtCarritoVacio.visibility = View.VISIBLE

        layoutProductos.removeAllViews()
        layoutResumen.visibility = View.GONE

        imgVaciarCarrito.visibility = View.GONE

        val llamada = ApiClient.carritoApi.obtenerCarrito(
            idUsuario
        )

        consultaCarrito = llamada

        llamada.enqueue(object : Callback<CarritoResponse> {

            override fun onResponse(
                call: Call<CarritoResponse>,
                response: Response<CarritoResponse>
            ) {

                if (
                    call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) {
                    return
                }

                val carrito = response.body()

                if (response.isSuccessful && carrito != null) {

                    mostrarCarrito(carrito)

                } else {

                    mostrarError(
                        "No se pudo consultar el carrito (${response.code()})"
                    )
                }
            }

            override fun onFailure(
                call: Call<CarritoResponse>,
                t: Throwable
            ) {

                if (
                    call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) {
                    return
                }

                mostrarError(
                    "No se pudo conectar con el servidor"
                )
            }
        })
    }

    // MOSTRAR PRODUCTOS Y RESUMEN
    private fun mostrarCarrito(
        carrito: CarritoResponse
    ) {

        layoutProductos.removeAllViews()
        carritoActual = carrito
        btnProcederPago.isEnabled = false

        if (carrito.productos.isEmpty()) {

            txtCarritoVacio.text =
                "Tu carrito está vacío. ¡Explora nuestros amigurumis!"

            txtCarritoVacio.visibility = View.VISIBLE
            layoutResumen.visibility = View.GONE
            imgVaciarCarrito.visibility = View.GONE

            return
        }

        txtCarritoVacio.visibility = View.GONE
        layoutResumen.visibility = View.VISIBLE
        imgVaciarCarrito.visibility = View.VISIBLE

        carrito.productos.forEach { producto ->

            val tarjeta = crearTarjeta(producto)

            layoutProductos.addView(tarjeta)
        }

        // TOTALES REALES DE LA API
        txtSubtotal.text =
            "Subtotal: ${formatearPrecio(carrito.total)}"

        txtTotal.text =
            "Total de productos: ${formatearPrecio(carrito.total)}"

        // El envío aún no tiene tarifa calculada.
        findViewById<TextView>(
            R.id.txtEnvio
        ).text = "Envío: pendiente de calcular"

        // Evitar confirmar compras de productos agotados o inactivos.
        btnProcederPago.isEnabled = !compraEnCurso && !operacionEnCurso &&
                carrito.productos.all {
                    it.disponible && it.cantidad > 0 &&
                            it.cantidad <= it.stockDisponible
                }
    }

    // CONFIRMAR COMPRA CON PAGO CONTRAENTREGA

    private fun mostrarConfirmacionCompra() {

        if (compraEnCurso || operacionEnCurso) return

        val carrito = carritoActual ?: run {
            mostrarMensaje("Espera a que cargue el carrito")
            return
        }

        // VALIDAR PRODUCTOS
        if (
            carrito.productos.isEmpty() ||
            carrito.productos.any {
                !it.disponible ||
                        it.cantidad <= 0 ||
                        it.cantidad > it.stockDisponible
            }
        ) {
            mostrarMensaje(
                "Revisa la disponibilidad de tus productos"
            )
            return
        }

        // CREAR BOTTOM SHEET
        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.cliente_bottom_confirmar_pedido,
            null
        )

        // REFERENCIAS DEL XML
        val txtResumenProductos =
            view.findViewById<TextView>(
                R.id.txtResumenProductos
            )

        val txtTotalProvisional =
            view.findViewById<TextView>(
                R.id.txtTotalProvisional
            )

        val edtDireccionEntrega =
            view.findViewById<TextInputEditText>(
                R.id.edtDireccionEntrega
            )

        val btnElegirDireccion =
            view.findViewById<MaterialButton>(
                R.id.btnElegirDireccion
            )

        val btnAdministrarDirecciones =
            view.findViewById<MaterialButton>(
                R.id.btnAdministrarDirecciones
            )

        val btnConfirmarPedido =
            view.findViewById<MaterialButton>(
                R.id.btnConfirmarPedido
            )

        val btnCancelarPedido =
            view.findViewById<MaterialButton>(
                R.id.btnCancelarPedido
            )

        // RESUMEN REAL DE LA COMPRA
        txtResumenProductos.text =
            formatearPrecio(carrito.total)

        txtTotalProvisional.text =
            formatearPrecio(carrito.total)

        // La dirección puede escribirse o seleccionarse desde MySQL.
        edtDireccionEntrega.setText("")

        edtDireccionEntrega.hint =
            "Escribe o selecciona una dirección"

        edtDireccionEntrega.isFocusable = true
        edtDireccionEntrega.isFocusableInTouchMode = true
        edtDireccionEntrega.isClickable = true
        edtDireccionEntrega.isCursorVisible = true


        // CONSULTAR DIRECCIONES GUARDADAS
        btnElegirDireccion.setOnClickListener {

            if (idUsuario <= 0) {
                mostrarMensaje("No se encontró la sesión")
                return@setOnClickListener
            }

            btnElegirDireccion.isEnabled = false
            btnElegirDireccion.text = "Cargando direcciones..."

            ApiClient.direccionApi
                .obtenerDirecciones(idUsuario)
                .enqueue(object : Callback<JsonArray> {

                    override fun onResponse(
                        call: Call<JsonArray>,
                        response: Response<JsonArray>
                    ) {

                        if (isFinishing || isDestroyed ||
                            !dialog.isShowing
                        ) return

                        btnElegirDireccion.isEnabled = true
                        btnElegirDireccion.text =
                            "Seleccionar dirección guardada"

                        if (!response.isSuccessful) {
                            mostrarMensaje(
                                "No se pudieron consultar las direcciones"
                            )
                            return
                        }

                        val direcciones = response.body()
                            ?.mapNotNull { elemento ->
                                if (elemento.isJsonObject)
                                    elemento.asJsonObject
                                else null
                            }
                            .orEmpty()

                        if (direcciones.isEmpty()) {

                            AlertDialog.Builder(
                                this@CarritoActivity
                            )
                                .setTitle("Sin direcciones")
                                .setMessage(
                                    "Todavía no tienes direcciones " +
                                            "guardadas. Registra una para continuar."
                                )
                                .setPositiveButton(
                                    "Agregar dirección"
                                ) { _, _ ->

                                    dialog.dismiss()

                                    startActivity(
                                        Intent(
                                            this@CarritoActivity,
                                            DireccionActivity::class.java
                                        )
                                    )
                                }
                                .setNegativeButton(
                                    "Cancelar",
                                    null
                                )
                                .show()

                            return
                        }

                        // MOSTRAR TARJETAS PERSONALIZADAS
                        mostrarSelectorDirecciones(direcciones) { direccionCompleta ->
                            // El campo sigue siendo editable manualmente.
                            edtDireccionEntrega.setText(direccionCompleta)
                            edtDireccionEntrega.setSelection(direccionCompleta.length)
                        }
                    }

                    override fun onFailure(
                        call: Call<JsonArray>,
                        t: Throwable
                    ) {

                        if (isFinishing || isDestroyed ||
                            !dialog.isShowing
                        ) return

                        btnElegirDireccion.isEnabled = true
                        btnElegirDireccion.text =
                            "Seleccionar dirección guardada"

                        mostrarMensaje(
                            "Error de conexión al consultar direcciones"
                        )
                    }
                })
        }

        // ABRIR GESTIÓN DE DIRECCIONES EXISTENTE
        btnAdministrarDirecciones.setOnClickListener {

            dialog.dismiss()

            startActivity(
                Intent(
                    this@CarritoActivity,
                    DireccionActivity::class.java
                )
            )
        }

        // CANCELAR
        btnCancelarPedido.setOnClickListener {
            dialog.dismiss()
        }

        // CONFIRMAR PEDIDO
        btnConfirmarPedido.setOnClickListener {


            val direccion = edtDireccionEntrega.text
                ?.toString()
                ?.trim()
                .orEmpty()


            if (direccion.isBlank()) {
                mostrarMensaje(
                    "Selecciona una dirección de entrega"
                )
                return@setOnClickListener
            }

            if (direccion.length > 255) {
                mostrarMensaje(
                    "La dirección supera 255 caracteres"
                )
                return@setOnClickListener
            }

            if (compraEnCurso || operacionEnCurso) {
                return@setOnClickListener
            }

            dialog.dismiss()

            crearOrdenContraentrega(direccion)
        }

        dialog.setContentView(view)
        dialog.show()
    }



    private fun crearOrdenContraentrega(direccion: String) {
        if (compraEnCurso || operacionEnCurso || idUsuario <= 0) return

        compraEnCurso = true
        btnProcederPago.isEnabled = false
        btnProcederPago.text = "Registrando pedido..."

        val solicitud = CrearOrdenRequest(
            usuarioId = idUsuario,
            direccionEntrega = direccion,
            metodoPago = "CONTRAENTREGA"
        )

        val llamada = ApiClient.ordenesApi.crearOrden(solicitud)
        llamadaCrearOrden = llamada
        llamada.enqueue(object : Callback<CrearOrdenResponse> {
            override fun onResponse(
                call: Call<CrearOrdenResponse>,
                response: Response<CrearOrdenResponse>
            ) {
                if (isFinishing || isDestroyed) return
                compraEnCurso = false
                btnProcederPago.text = "Confirmar compra"


                val orden = response.body()?.orden

                if (response.isSuccessful && orden != null) {

                    carritoActual = null
                    btnProcederPago.isEnabled = false

                    // MOSTRAR NUEVO DIÁLOGO PERSONALIZADO
                    mostrarDialogoPedidoRegistrado(
                        orden.idOrden
                    )
                } else {
                    mostrarMensaje(
                        "No se pudo crear el pedido (${response.code()})"
                    )
                    cargarCarrito()
                }

            }

            override fun onFailure(call: Call<CrearOrdenResponse>, t: Throwable) {
                if (isFinishing || isDestroyed) return
                compraEnCurso = false
                btnProcederPago.text = "Confirmar compra"
                btnProcederPago.isEnabled = false
                // Si se perdió la conexión después de enviar la petición,
                // el servidor podría haber registrado la orden.
                AlertDialog.Builder(this@CarritoActivity)
                    .setTitle("No se pudo confirmar la respuesta")
                    .setMessage(
                        "Comprueba tu historial antes de intentar de nuevo: " +
                                "la orden podría haberse registrado."
                    )
                    .setPositiveButton("Entendido") { _, _ -> cargarCarrito() }
                    .show()
            }
        })
    }

    // DIÁLOGO PERSONALIZADO DESPUÉS DE REGISTRAR LA ORDEN
    private fun mostrarDialogoPedidoRegistrado(idOrden: Int) {

        // CARGAR DISEÑO PERSONALIZADO
        val view = layoutInflater.inflate(
            R.layout.cliente_dialog_pedido_registrado,
            null
        )

        // REFERENCIAS DEL XML
        val txtPedidoNumero =
            view.findViewById<TextView>(
                R.id.txtPedidoNumero
            )

        val txtPedidoPago =
            view.findViewById<TextView>(
                R.id.txtPedidoPago
            )

        val txtPedidoEstado =
            view.findViewById<TextView>(
                R.id.txtPedidoEstado
            )

        val btnVerCompras =
            view.findViewById<MaterialButton>(
                R.id.btnVerCompras
            )

        val btnAceptarPedido =
            view.findViewById<MaterialButton>(
                R.id.btnAceptarPedido
            )

        // MOSTRAR INFORMACIÓN REAL DEL PEDIDO
        txtPedidoNumero.text = "Pedido #$idOrden"

        txtPedidoPago.text = "Pago: Contraentrega"

        txtPedidoEstado.text = "Estado: Pendiente"

        // CREAR DIÁLOGO PERSONALIZADO
        val dialog = MaterialAlertDialogBuilder(
            this@CarritoActivity
        )
            .setView(view)
            .setCancelable(false)
            .create()

        // BOTÓN ACEPTAR
        btnAceptarPedido.setOnClickListener {

            dialog.dismiss()

            // ACTUALIZAR CARRITO DESPUÉS DE LA COMPRA
            cargarCarrito()
        }

        // BOTÓN VER MIS COMPRAS
        btnVerCompras.setOnClickListener {

            dialog.dismiss()

            // ABRIR HISTORIAL DE COMPRAS
            val intent = Intent(
                this@CarritoActivity,
                ComprasActivity::class.java
            )

            startActivity(intent)

            // CERRAR CARRITO
            finish()
        }

        // MOSTRAR EL DIÁLOGO
        dialog.show()
    }


    // CREAR UNA TARJETA POR PRODUCTO
    private fun crearTarjeta(
        producto: ProductoCarritoResponse
    ): CardView {

        val tarjeta = LayoutInflater.from(this)
            .inflate(
                R.layout.cliente_item_carrito,
                layoutProductos,
                false
            ) as CardView

        val imgProducto = tarjeta.findViewById<ImageView>(
            R.id.imgProductoCarrito
        )

        val txtNombre = tarjeta.findViewById<TextView>(
            R.id.txtNombreCarrito
        )

        val txtPrecio = tarjeta.findViewById<TextView>(
            R.id.txtPrecioCarrito
        )

        val txtCantidad = tarjeta.findViewById<TextView>(
            R.id.txtCantidadCarrito
        )

        val txtSubtotalProducto = tarjeta.findViewById<TextView>(
            R.id.txtSubtotalProducto
        )

        val btnMenos = tarjeta.findViewById<AppCompatButton>(
            R.id.btnMenosCarrito
        )

        val btnSumar = tarjeta.findViewById<AppCompatButton>(
            R.id.btnSumarCarrito
        )

        val imgEliminar = tarjeta.findViewById<ImageView>(
            R.id.imgEliminarProductoCarrito
        )

        // DATOS DEL PRODUCTO
        txtNombre.text = producto.nombre

        txtPrecio.text = formatearPrecio(
            producto.precio
        )

        txtCantidad.text = producto.cantidad.toString()

        txtSubtotalProducto.text =
            "Subtotal: ${formatearPrecio(producto.subtotal)}"

        // IMAGEN REAL DEL PRODUCTO

        val urlImagen = ImagenUrlHelper.obtenerUrl(
            producto.imagenUrl
        )

        if (!urlImagen.isNullOrBlank()) {

            Glide.with(this@CarritoActivity)
                .load(urlImagen)
                .placeholder(R.drawable.usuario)
                .error(R.drawable.usuario)
                .centerCrop()
                .into(imgProducto)

        } else {

            imgProducto.setImageResource(
                R.drawable.usuario
            )
        }


        // RESTAR UNA UNIDAD
        btnMenos.isEnabled =
            producto.disponible && producto.cantidad > 1

        btnMenos.setOnClickListener {

            if (producto.cantidad > 1) {

                actualizarCantidad(
                    producto.idDetalle,
                    producto.cantidad - 1
                )
            }
        }

        // SUMAR UNA UNIDAD
        btnSumar.isEnabled =
            producto.disponible &&
                    producto.cantidad < producto.stockDisponible

        btnSumar.setOnClickListener {

            if (producto.cantidad < producto.stockDisponible) {

                actualizarCantidad(
                    producto.idDetalle,
                    producto.cantidad + 1
                )

            } else {

                mostrarMensaje(
                    "No puedes superar el stock disponible"
                )
            }
        }

        // ELIMINAR PRODUCTO
        imgEliminar.setOnClickListener {

            confirmarEliminarProducto(producto)
        }

        return tarjeta
    }

    // ACTUALIZAR CANTIDAD EN MYSQL
    private fun actualizarCantidad(
        idDetalle: Int,
        nuevaCantidad: Int
    ) {

        if (operacionEnCurso) return

        if (nuevaCantidad <= 0) {
            mostrarMensaje(
                "La cantidad debe ser mayor a cero"
            )
            return
        }

        val request = ActualizarCantidadCarritoRequest(
            usuarioId = idUsuario,
            cantidad = nuevaCantidad
        )

        ejecutarOperacion(
            ApiClient.carritoApi.actualizarCantidad(
                idDetalle,
                request
            )
        )
    }

    // CONFIRMAR ELIMINACIÓN
    private fun confirmarEliminarProducto(
        producto: ProductoCarritoResponse
    ) {

        if (operacionEnCurso) return

        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage(
                "¿Deseas eliminar ${producto.nombre} de tu carrito?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->

                ejecutarOperacion(
                    ApiClient.carritoApi.eliminarProducto(
                        producto.idDetalle,
                        idUsuario
                    )
                )
            }
            .show()
    }

    // CONFIRMAR VACIADO
    private fun confirmarVaciarCarrito() {

        if (operacionEnCurso) return

        AlertDialog.Builder(this)
            .setTitle("Vaciar carrito")
            .setMessage(
                "¿Deseas eliminar todos los productos de tu carrito?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Vaciar") { _, _ ->

                ejecutarOperacion(
                    ApiClient.carritoApi.vaciarCarrito(
                        idUsuario
                    )
                )
            }
            .show()
    }

    // EJECUTAR PUT O DELETE
    private fun ejecutarOperacion(
        llamada: Call<OperacionCarritoResponse>
    ) {

        if (operacionEnCurso || compraEnCurso) return

        operacionEnCurso = true

        llamada.enqueue(
            object : Callback<OperacionCarritoResponse> {

                override fun onResponse(
                    call: Call<OperacionCarritoResponse>,
                    response: Response<OperacionCarritoResponse>
                ) {

                    operacionEnCurso = false

                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {

                        val resultado = response.body()

                        mostrarMensaje(
                            resultado?.mensaje ?: "Operación realizada"
                        )

                        // RECARGAR DATOS Y TOTALES
                        cargarCarrito()

                    } else {

                        mostrarMensaje(
                            "No se pudo realizar la operación (${response.code()})"
                        )

                        cargarCarrito()
                    }
                }

                override fun onFailure(
                    call: Call<OperacionCarritoResponse>,
                    t: Throwable
                ) {

                    operacionEnCurso = false

                    if (isFinishing || isDestroyed) return

                    mostrarMensaje(
                        "Error de conexión con el servidor"
                    )

                    cargarCarrito()
                }
            }
        )
    }

    // FORMATO DE PRECIO COLOMBIANO
    private fun formatearPrecio(
        precio: Double
    ): String {

        val formato = NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        ).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }

        return formato.format(precio)
    }

    // MENSAJES DE ERROR
    private fun mostrarError(mensaje: String) {

        layoutProductos.removeAllViews()
        carritoActual = null
        btnProcederPago.isEnabled = false

        txtCarritoVacio.text = mensaje
        txtCarritoVacio.visibility = View.VISIBLE

        layoutResumen.visibility = View.GONE
        imgVaciarCarrito.visibility = View.GONE

        mostrarMensaje(mensaje)
    }

    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }

    // NAVEGACIÓN INFERIOR
    private fun configurarNavegacion() {

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNavigation.selectedItemId = R.id.carrito

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> {

                    startActivity(
                        Intent(
                            this,
                            HomeBuyerActivity::class.java
                        )
                    )

                    finish()
                    true
                }

                R.id.categoria -> {

                    startActivity(
                        Intent(
                            this,
                            CatalogoActivity::class.java
                        )
                    )

                    finish()
                    true
                }

                R.id.carrito -> true

                R.id.perfil -> {

                    startActivity(
                        Intent(
                            this,
                            PerfilClienteActivity::class.java
                        )
                    )

                    finish()
                    true
                }

                else -> false
            }
        }
    }


    // SELECTOR DE DIRECCIONES CON TARJETAS PERSONALIZADAS
    private fun mostrarSelectorDirecciones(
        direcciones: List<JsonObject>,
        alSeleccionar: (String) -> Unit
    ) {
        if (direcciones.isEmpty()) {
            mostrarMensaje("No tienes direcciones guardadas")
            return
        }

        val morado = Color.parseColor("#8E6AD8")
        val moradoOscuro = Color.parseColor("#6A4C93")
        val borde = Color.parseColor("#E8DDEE")
        val gris = Color.parseColor("#80758A")
        val dialog = BottomSheetDialog(this)

        fun dp(valor: Int): Int =
            (valor * resources.displayMetrics.density).toInt()

        val principal = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(20))
            setBackgroundColor(Color.WHITE)
        }

        val barra = View(this).apply {
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(borde)
                cornerRadius = dp(3).toFloat()
            }
        }
        principal.addView(barra, LinearLayout.LayoutParams(dp(48), dp(5)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            bottomMargin = dp(20)
        })

        principal.addView(TextView(this).apply {
            text = "¿Dónde recibirás tu pedido?"
            textSize = 21f
            setTextColor(moradoOscuro)
            setTypeface(null, Typeface.BOLD)
        })

        principal.addView(TextView(this).apply {
            text = "Selecciona una de tus direcciones guardadas"
            textSize = 13f
            setTextColor(gris)
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(6)
            bottomMargin = dp(18)
        })

        val scroll = ScrollView(this).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = false
        }
        val listaTarjetas = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(listaTarjetas)
        // Evitar que el selector ocupe más espacio del necesario.
        val altoLista = minOf(dp(360), dp(132) * direcciones.size)
        principal.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, altoLista
        ))

        var indiceSeleccionado = direcciones.indexOfFirst {
            booleanoDireccionJson(it, "principal")
        }.let { if (it >= 0) it else 0 }

        val tarjetas = mutableListOf<MaterialCardView>()
        val radios = mutableListOf<RadioButton>()

        fun actualizarSeleccion() {
            tarjetas.forEachIndexed { indice, tarjeta ->
                val seleccionada = indice == indiceSeleccionado
                tarjeta.strokeColor = if (seleccionada) morado else borde
                tarjeta.strokeWidth = dp(if (seleccionada) 2 else 1)
                tarjeta.setCardBackgroundColor(
                    if (seleccionada) Color.parseColor("#F8F3FF") else Color.WHITE
                )
                radios[indice].isChecked = seleccionada
            }
        }

        direcciones.forEachIndexed { indice, direccion ->
            val nombre = textoDireccionJson(direccion, "nombreDireccion")
                .ifBlank { "Dirección" }
            val calle = textoDireccionJson(direccion, "direccion")
            val barrio = textoDireccionJson(direccion, "barrio")
            val ciudad = textoDireccionJson(direccion, "ciudad")
            val esPrincipal = booleanoDireccionJson(direccion, "principal")

            val tarjeta = MaterialCardView(this).apply {
                radius = dp(16).toFloat()
                cardElevation = 0f
                strokeColor = borde
                strokeWidth = dp(1)
                isClickable = true
                isFocusable = true
            }
            val contenido = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
            }
            val encabezado = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val icono = when (nombre.lowercase()) {
                "casa" -> "🏠"
                "trabajo" -> "💼"
                else -> "📍"
            }
            encabezado.addView(TextView(this).apply {
                text = "$icono  $nombre"
                textSize = 16f
                setTextColor(moradoOscuro)
                setTypeface(null, Typeface.BOLD)
            }, LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ))

            if (esPrincipal) {
                encabezado.addView(TextView(this).apply {
                    text = "Principal"
                    textSize = 11f
                    setTextColor(morado)
                    setPadding(dp(4), 0, dp(4), 0)
                })
            }
            val radio = RadioButton(this).apply {
                buttonTintList = android.content.res.ColorStateList.valueOf(morado)
                isClickable = false
                isFocusable = false
            }
            encabezado.addView(radio)
            contenido.addView(encabezado)

            contenido.addView(TextView(this).apply {
                text = calle
                textSize = 14f
                setTextColor(Color.parseColor("#444444"))
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(6) })

            val ubicacion = listOf(barrio, ciudad)
                .filter { it.isNotBlank() }.joinToString(", ")
            if (ubicacion.isNotBlank()) {
                contenido.addView(TextView(this).apply {
                    text = ubicacion
                    textSize = 13f
                    setTextColor(gris)
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(4) })
            }

            tarjeta.addView(contenido)
            tarjetas.add(tarjeta)
            radios.add(radio)
            tarjeta.setOnClickListener {
                indiceSeleccionado = indice
                actualizarSeleccion()
            }
            listaTarjetas.addView(tarjeta, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) })
        }
        actualizarSeleccion()

        val btnUsarDireccion = MaterialButton(this).apply {
            text = "Usar esta dirección"
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(morado)
            cornerRadius = dp(26)
        }
        principal.addView(btnUsarDireccion, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(54)
        ).apply { topMargin = dp(14) })

        val btnCancelar = MaterialButton(this).apply {
            text = "Cancelar"
            isAllCaps = false
            setTextColor(morado)
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor("#F3EAFE")
            )
            cornerRadius = dp(24)
        }
        principal.addView(btnCancelar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
        ).apply { topMargin = dp(8) })

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnUsarDireccion.setOnClickListener {
            val seleccion = direcciones[indiceSeleccionado]
            val calle = textoDireccionJson(seleccion, "direccion")
            val barrio = textoDireccionJson(seleccion, "barrio")
            val ciudad = textoDireccionJson(seleccion, "ciudad")
            val referencia = textoDireccionJson(seleccion, "referencia")

            val direccionCompleta = listOf(
                calle, barrio, ciudad,
                referencia.takeIf { it.isNotBlank() }
                    ?.let { "Referencia: $it" }.orEmpty()
            ).filter { it.isNotBlank() }.joinToString(", ")

            if (calle.isBlank()) {
                mostrarMensaje("La dirección seleccionada está incompleta")
                return@setOnClickListener
            }
            if (direccionCompleta.length > 255) {
                mostrarMensaje("La dirección supera 255 caracteres. Edítala antes de continuar.")
                return@setOnClickListener
            }

            alSeleccionar(direccionCompleta)
            dialog.dismiss()
        }

        dialog.setContentView(principal)
        dialog.show()
    }

    // LEER TEXTO DE UNA DIRECCIÓN JSON
    private fun textoDireccionJson(
        objeto: JsonObject,
        nombre: String
    ): String {

        return try {

            val campo = objeto.get(nombre)
                ?: objeto.get(
                    nombre.replaceFirstChar {
                        it.uppercase()
                    }
                )

            if (campo == null || campo.isJsonNull) {
                ""
            } else {
                campo.asString
            }

        } catch (e: Exception) {
            ""
        }
    }

    // LEER SI LA DIRECCIÓN ES PRINCIPAL
    private fun booleanoDireccionJson(
        objeto: JsonObject,
        nombre: String
    ): Boolean {

        return try {

            val campo = objeto.get(nombre)
                ?: objeto.get(
                    nombre.replaceFirstChar {
                        it.uppercase()
                    }
                )

            if (campo == null || campo.isJsonNull) {
                false
            } else {
                campo.asBoolean
            }

        } catch (e: Exception) {
            false
        }
    }


    override fun onDestroy() {

        consultaCarrito?.cancel()
        llamadaCrearOrden?.cancel()
        super.onDestroy()
    }
}
