package com.misoft.misoftapp.catalogo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.api.ProductoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import com.misoft.misoftapp.api.AgregarCarritoRequest
import com.misoft.misoftapp.api.ImagenUrlHelper
import com.misoft.misoftapp.api.OperacionCarritoResponse
import com.misoft.misoftapp.compras.CarritoActivity

class DetalleProductoActivity : AppCompatActivity() {

    private var cantidad = 1
    private var productoActual: ProductoResponse? = null

    private lateinit var imgProducto: ImageView
    private lateinit var txtNombre: TextView
    private lateinit var txtPrecio: TextView
    private lateinit var txtDescripcion: TextView
    private lateinit var txtTalla: TextView
    private lateinit var txtStock: TextView
    private lateinit var txtCantidad: TextView
    private lateinit var layoutAccionesCompra: View
    private lateinit var btnAgregarCarrito: MaterialButton
    private lateinit var fabCarrito: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.cliente_activity_detalle_producto)

        // REFERENCIAS DEL XML
        imgProducto = findViewById(R.id.imgProducto)
        txtNombre = findViewById(R.id.txtNombreDetalle)
        txtPrecio = findViewById(R.id.txtPrecioDetalle)
        txtDescripcion = findViewById(R.id.txtDescripcionDetalle)
        txtTalla = findViewById(R.id.txtTallaDetalle)
        txtStock = findViewById(R.id.txtStockDetalle)
        txtCantidad = findViewById(R.id.txtCantidad)

        layoutAccionesCompra = findViewById(
            R.id.layoutAccionesCompra
        )

        btnAgregarCarrito = findViewById(
            R.id.btnAgregarCarrito
        )

        fabCarrito = findViewById(R.id.fabCarrito)

        val txtVistaPrevia = findViewById<TextView>(
            R.id.txtVistaPrevia
        )

        // BOTÓN VOLVER
        findViewById<ImageView>(
            R.id.imgBack
        ).setOnClickListener {
            finish()
        }

        // OBTENER PRODUCTO SELECCIONADO
        val idProducto = intent.getIntExtra(
            "idProducto",
            -1
        )

        // IDENTIFICAR LA SESIÓN
        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val rolId = preferencias.getInt(
            "rolId",
            -1
        )

        val esVistaPrevia = rolId == 2 &&
                intent.getBooleanExtra(
                    "esVistaPrevia",
                    false
                )

        // CONFIGURAR VISTA SEGÚN ROL
        if (esVistaPrevia) {

            layoutAccionesCompra.visibility = View.GONE
            fabCarrito.visibility = View.GONE
            txtVistaPrevia.visibility = View.VISIBLE

        } else {

            layoutAccionesCompra.visibility = View.GONE
            fabCarrito.visibility = View.GONE
            txtVistaPrevia.visibility = View.GONE
        }

        if (idProducto <= 0) {
            mostrarMensaje("No se encontró el producto")
            finish()
            return
        }

        // CONFIGURAR SELECTOR DE CANTIDAD
        configurarCantidad()

        // CONSULTAR PRODUCTO REAL
        cargarDetalleProducto(
            idProducto,
            esVistaPrevia
        )
    }

    // OBTENER INFORMACIÓN DESDE MYSQL
    private fun cargarDetalleProducto(
        idProducto: Int,
        esVistaPrevia: Boolean
    ) {

        ApiClient.productosApi
            .obtenerProductoPorId(idProducto)
            .enqueue(object : Callback<ProductoResponse> {

                override fun onResponse(
                    call: Call<ProductoResponse>,
                    response: Response<ProductoResponse>
                ) {

                    if (!response.isSuccessful ||
                        response.body() == null
                    ) {

                        mostrarMensaje(
                            "No se pudo consultar el producto: ${response.code()}"
                        )

                        finish()
                        return
                    }

                    val producto = response.body()!!

                    // NO MOSTRAR PRODUCTOS INACTIVOS AL CLIENTE
                    if (!producto.estado && !esVistaPrevia) {
                        mostrarMensaje(
                            "Este producto no está disponible"
                        )
                        finish()
                        return
                    }

                    // VERIFICAR PROPIETARIO EN VISTA PREVIA
                    if (esVistaPrevia) {

                        val idVendedor = getSharedPreferences(
                            "MisoftSesion",
                            MODE_PRIVATE
                        ).getInt("idUsuario", -1)

                        if (producto.idVendedor != idVendedor) {
                            mostrarMensaje(
                                "No puedes visualizar este producto como vendedor"
                            )
                            finish()
                            return
                        }
                    }

                    productoActual = producto

                    mostrarDatosProducto(producto)

                    if (!esVistaPrevia) {

                        layoutAccionesCompra.visibility =
                            View.VISIBLE

                        fabCarrito.visibility = View.VISIBLE

                        configurarAccionesCompra(producto)
                    }
                }

                override fun onFailure(
                    call: Call<ProductoResponse>,
                    t: Throwable
                ) {

                    mostrarMensaje(
                        "Error de conexión: ${t.message}"
                    )
                }
            })
    }

    // MOSTRAR INFORMACIÓN DEL PRODUCTO
    private fun mostrarDatosProducto(
        producto: ProductoResponse
    ) {

        txtNombre.text = producto.nombre

        // PRECIO EN PESOS COLOMBIANOS
        val formatoPrecio =
            NumberFormat.getCurrencyInstance(
                Locale("es", "CO")
            ).apply {
                maximumFractionDigits = 0
                minimumFractionDigits = 0
            }

        txtPrecio.text = formatoPrecio.format(
            producto.precio
        )

        // DESCRIPCIÓN
        txtDescripcion.text =
            producto.descripcion?.takeIf {
                it.isNotBlank()
            } ?: "Sin descripción disponible"

        // TALLA Y EQUIVALENCIA VISUAL
        txtTalla.text = when (producto.talla) {

            "S" -> "S - Pequeño (10-15 cm)"

            "M" -> "M - Mediano (16-20 cm)"

            "L" -> "L - Grande (21-30 cm)"

            "XL" -> "XL - Extra grande (31-40 cm)"

            else -> "Talla no especificada"
        }

        // STOCK
        when {

            !producto.estado -> {

                txtStock.text = "Producto inactivo"

                txtStock.setTextColor(
                    Color.parseColor("#888888")
                )
            }

            producto.stock == 0 -> {

                txtStock.text = "Sin existencias"

                txtStock.setTextColor(
                    Color.parseColor("#C62828")
                )
            }

            producto.stock <= producto.stockMinimo -> {

                txtStock.text =
                    "Stock bajo: ${producto.stock} unidades"

                txtStock.setTextColor(
                    Color.parseColor("#9A6700")
                )
            }

            else -> {

                txtStock.text =
                    "Stock disponible: ${producto.stock} unidades"

                txtStock.setTextColor(
                    Color.parseColor("#4CAF50")
                )
            }
        }


        // FOTOGRAFÍA REAL DEL PRODUCTO
        val urlImagen = ImagenUrlHelper.obtenerUrl(
            producto.imagenUrl
        )

        if (!urlImagen.isNullOrBlank()) {

            Glide.with(this@DetalleProductoActivity)
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

    }


        // SELECTOR DE CANTIDAD
    private fun configurarCantidad() {

        val btnMenos = findViewById<AppCompatButton>(
            R.id.btnMenos
        )

        val btnSumar = findViewById<AppCompatButton>(
            R.id.btnSumar
        )

        cantidad = 1
        txtCantidad.text = cantidad.toString()

        btnMenos.setOnClickListener {

            if (cantidad > 1) {

                cantidad--

                txtCantidad.text = cantidad.toString()
            }
        }

        btnSumar.setOnClickListener {

            val stockDisponible = productoActual?.stock ?: 0

            if (cantidad < stockDisponible) {

                cantidad++

                txtCantidad.text = cantidad.toString()

            } else {

                mostrarMensaje(
                    "No puedes superar el stock disponible"
                )
            }
        }
    }


    private fun agregarProductoAlCarrito(
        producto: ProductoResponse
    ) {

        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val idUsuario = preferencias.getInt(
            "idUsuario",
            -1
        )

        if (idUsuario <= 0) {
            mostrarMensaje("Debes iniciar sesión")
            return
        }

        if (!producto.estado || producto.stock <= 0) {
            mostrarMensaje("Este producto no está disponible")
            return
        }

        if (cantidad <= 0 || cantidad > producto.stock) {
            mostrarMensaje("Cantidad no disponible")
            return
        }

        val request = AgregarCarritoRequest(
            usuarioId = idUsuario,
            productoId = producto.idProducto,
            cantidad = cantidad
        )

        // Evitar pulsaciones repetidas mientras se guarda
        btnAgregarCarrito.isEnabled = false

        ApiClient.carritoApi.agregarProducto(request)
            .enqueue(object : Callback<OperacionCarritoResponse> {

                override fun onResponse(
                    call: Call<OperacionCarritoResponse>,
                    response: Response<OperacionCarritoResponse>
                ) {

                    if (isFinishing || isDestroyed) return

                    btnAgregarCarrito.isEnabled = true

                    if (response.isSuccessful) {

                        mostrarMensaje(
                            "Producto agregado al carrito"
                        )

                    } else {

                        mostrarMensaje(
                            "No se pudo agregar el producto (${response.code()})"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<OperacionCarritoResponse>,
                    t: Throwable
                ) {

                    if (isFinishing || isDestroyed) return

                    btnAgregarCarrito.isEnabled = true

                    mostrarMensaje(
                        "Error de conexión con el servidor"
                    )
                }
            })
    }




    // CONTROLES DE COMPRA
    private fun configurarAccionesCompra(
        producto: ProductoResponse
    ) {

        val disponible = producto.estado &&
                producto.stock > 0

        btnAgregarCarrito.isEnabled = disponible

        btnAgregarCarrito.text = if (disponible) {
            "Agregar al carrito"
        } else {
            "Producto agotado"
        }

        if (disponible) {

            btnAgregarCarrito.setOnClickListener {

                agregarProductoAlCarrito(producto)

            }

        } else {

            btnAgregarCarrito.setOnClickListener(null)
        }

        fabCarrito.setOnClickListener {
            val intent = Intent(
                this,
                CarritoActivity::class.java
            )
            startActivity(intent)
        }
    }

    // MENSAJES
    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }
}
