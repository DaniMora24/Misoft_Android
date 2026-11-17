package com.misoft.misoftapp.catalogo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.home.HomeAdminActivity
import com.misoft.misoftapp.estadisticas.ReportesAdminActivity
import com.misoft.misoftapp.usuarios.UsuariosAdminActivity
import com.misoft.misoftapp.usuarios.VendedoresAdminActivity
import java.text.NumberFormat
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductoAdminActivity : AppCompatActivity() {

    // VARIABLES

    private lateinit var contenedorProductos: LinearLayout
    private lateinit var edtBuscar: TextInputEditText

    private lateinit var txtTitulo: TextView
    private lateinit var txtTotalProductos: TextView
    private lateinit var txtSinStock: TextView

    private var idVendedor = -1
    private var nombreVendedor = "Vendedor"

    private var llamadaProductos: Call<JsonArray>? = null

    private val listaProductos = mutableListOf<JsonObject>()

    private val formatoMoneda =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("es-CO")
        ).apply {
            maximumFractionDigits = 0
        }

    // INICIAR ACTIVITY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.admin_activity_producto)

        obtenerDatosVendedor()
        inicializarVistas()
        configurarBuscador()
        configurarNavegacion()

        if (idVendedor <= 0) {

            mostrarEstado(
                "No se recibió un vendedor válido."
            )

            return
        }

        cargarProductos()
    }

    // OBTENER VENDEDOR SELECCIONADO

    private fun obtenerDatosVendedor() {

        idVendedor = intent.getIntExtra(
            "ID_VENDEDOR",
            -1
        )

        nombreVendedor = intent.getStringExtra(
            "NOMBRE_TIENDA"
        ) ?: intent.getStringExtra(
            "nombreVendedor"
        ) ?: "Vendedor"
    }

    // VINCULAR XML CON KOTLIN

    private fun inicializarVistas() {

        txtTitulo = findViewById(
            R.id.txtNombreTienda
        )

        txtTotalProductos = findViewById(
            R.id.txtTotalProductosVendedor
        )

        txtSinStock = findViewById(
            R.id.txtProductosSinStock
        )

        edtBuscar = findViewById(
            R.id.edtBuscarProducto
        )

        contenedorProductos = findViewById(
            R.id.contenedorProductosVendedor
        )

        txtTitulo.text = "📦 $nombreVendedor"

        findViewById<View>(
            R.id.imgBack
        ).setOnClickListener {
            finish()
        }
    }
    // CONFIGURAR BUSCADOR=

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

                    filtrarProductos(
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

// CONSTRUIR URL DE IMÁGENES

    private fun obtenerUrlImagen(ruta: String): String? {

        if (ruta.isBlank()) {
            return null
        }

        // Si ya tenemos una URL completa, utilizarla.
        if (ruta.startsWith("http://") ||
            ruta.startsWith("https://")) {
            return ruta
        }

        // Servidor de MisoftApp en la red local.
        val servidor = "http://192.168.78.111:5271"

        return servidor + "/" + ruta.trimStart('/')
    }


    private fun cargarProductos() {

        llamadaProductos?.cancel()

        mostrarEstado("Cargando productos...")

        val llamada = ApiClient.productosAdminApi
            .obtenerPorVendedor(idVendedor)

        Log.d(
            "PRODUCTOS_ADMIN",
            "ID vendedor: $idVendedor"
        )

        Log.d(
            "PRODUCTOS_ADMIN",
            "URL solicitada: ${llamada.request().url}"
        )

        llamadaProductos = llamada

        llamada.enqueue(
            object : Callback<JsonArray> {

                override fun onResponse(
                    call: Call<JsonArray>,
                    response: Response<JsonArray>
                ) {

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    if (!response.isSuccessful) {

                        mostrarEstado(
                            "No fue posible cargar los productos. " +
                                    "HTTP ${response.code()}"
                        )

                        return
                    }

                    listaProductos.clear()

                    response.body()?.forEach { elemento ->

                        if (elemento.isJsonObject) {
                            listaProductos.add(
                                elemento.asJsonObject
                            )
                        }
                    }

                    actualizarResumen()

                    filtrarProductos(
                        edtBuscar.text?.toString().orEmpty()
                    )
                }

                override fun onFailure(
                    call: Call<JsonArray>,
                    t: Throwable
                ) {

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    Log.e(
                        "PRODUCTOS_ADMIN",
                        "Error consultando productos",
                        t
                    )

                    mostrarEstado(
                        "No se pudo conectar con la API."
                    )
                }
            }
        )
    }

    // ACTUALIZAR RESUMEN

    private fun actualizarResumen() {

        txtTotalProductos.text =
            "Productos: ${listaProductos.size}"

        val sinStock = listaProductos.count {
            obtenerEntero(it, "stock") <= 0
        }

        txtSinStock.text = "Sin stock: $sinStock"
    }

    // FILTRAR PRODUCTOS

    private fun filtrarProductos(
        busqueda: String
    ) {

        val textoBusqueda = busqueda
            .trim()
            .lowercase(Locale.ROOT)

        val filtrados = listaProductos.filter { producto ->

            val nombre = obtenerTexto(
                producto,
                "nombre"
            ).lowercase(Locale.ROOT)

            val id = obtenerEntero(
                producto,
                "idProducto"
            ).toString()

            nombre.contains(textoBusqueda) ||
                    id.contains(textoBusqueda)
        }

        mostrarProductos(filtrados)
    }

    // MOSTRAR PRODUCTOS DINÁMICAMENTE

    private fun mostrarProductos(
        productos: List<JsonObject>
    ) {

        contenedorProductos.removeAllViews()

        if (productos.isEmpty()) {

            mostrarEstado(
                if (listaProductos.isEmpty()) {
                    "Este vendedor todavía no tiene productos."
                } else {
                    "No se encontraron productos."
                }
            )

            return
        }

        productos.forEach { producto ->

            crearTarjetaProducto(producto)
        }
    }

    // CREAR TARJETA DE PRODUCTO

    private fun crearTarjetaProducto(
        producto: JsonObject
    ) {

        val nombre = obtenerTexto(
            producto,
            "nombre"
        ).ifBlank { "Producto sin nombre" }

        val precio = obtenerDecimal(
            producto,
            "precio"
        )

        val stock = obtenerEntero(
            producto,
            "stock"
        )

        val visible = obtenerBooleano(
            producto,
            "estado"
        )

        val imagenUrl = obtenerTexto(
            producto,
            "imagenUrl"
        )

        val tarjeta = CardView(this).apply {

            radius = dp(16).toFloat()
            cardElevation = dp(2).toFloat()

            setCardBackgroundColor(Color.WHITE)

            isClickable = true
            isFocusable = true

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(104)
            ).apply {
                bottomMargin = dp(12)
            }
        }

        val fila = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
            )
        }

        // IMAGEN

        val imagen = ImageView(this).apply {

            layoutParams = LinearLayout.LayoutParams(
                dp(76),
                dp(76)
            )

            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        Glide.with(this)
            .load(obtenerUrlImagen(imagenUrl))
            .placeholder(R.drawable.logo_michigurumis_final)
            .error(R.drawable.logo_michigurumis_final)
            .centerCrop()
            .into(imagen)

        fila.addView(imagen)

        // INFORMACIÓN

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

            text = nombre
            textSize = 16f
            setTextColor(
                Color.parseColor("#6A4C93")
            )
            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val txtPrecio = TextView(this).apply {

            text = formatoMoneda.format(precio)
            textSize = 14f

            setTextColor(
                Color.parseColor("#D46AA7")
            )
        }

        val txtStock = TextView(this).apply {

            text = "Stock: $stock"
            textSize = 12f

            setTextColor(
                if (stock <= 0) {
                    Color.parseColor("#C62828")
                } else {
                    Color.parseColor("#777777")
                }
            )
        }

        val txtEstado = TextView(this).apply {

            text = if (visible) {
                "● Visible"
            } else {
                "● Oculto"
            }

            textSize = 12f

            setTextColor(
                if (visible) {
                    Color.parseColor("#2E7D32")
                } else {
                    Color.parseColor("#C62828")
                }
            )
        }

        columna.addView(txtNombre)
        columna.addView(txtPrecio)
        columna.addView(txtStock)
        columna.addView(txtEstado)

        fila.addView(columna)

        // FLECHA

        val flecha = ImageView(this).apply {

            layoutParams = LinearLayout.LayoutParams(
                dp(20),
                dp(20)
            )

            setImageResource(
                R.drawable.angulo_derecho
            )

            contentDescription = "Ver detalle"
        }

        fila.addView(flecha)

        tarjeta.addView(fila)

        // ABRIR DETALLE

        tarjeta.setOnClickListener {

            mostrarDetalleProducto(producto)
        }

        contenedorProductos.addView(tarjeta)
    }
    // MOSTRAR DETALLE DEL PRODUCTO

    private fun mostrarDetalleProducto(
        producto: JsonObject
    ) {

        val dialogo = BottomSheetDialog(this)

        val vista = layoutInflater.inflate(
            R.layout.admin_bottom_detalle_producto,
            null
        )

        dialogo.setContentView(vista)

        val nombre = obtenerTexto(
            producto,
            "nombre"
        )

        val descripcion = obtenerTexto(
            producto,
            "descripcion"
        ).ifBlank {
            "Sin descripción disponible."
        }

        val precio = obtenerDecimal(
            producto,
            "precio"
        )

        val stock = obtenerEntero(
            producto,
            "stock"
        )

        val stockMinimo = obtenerEntero(
            producto,
            "stockMinimo"
        )

        val talla = obtenerTexto(
            producto,
            "talla"
        ).ifBlank {
            "No especificada"
        }

        val categoriaId = obtenerEntero(
            producto,
            "categoriaId"
        )

        val visible = obtenerBooleano(
            producto,
            "estado"
        )

        val imagenUrl = obtenerTexto(
            producto,
            "imagenUrl"
        )

        vista.findViewById<TextView>(
            R.id.txtNombreProducto
        ).text = nombre

        vista.findViewById<TextView>(
            R.id.txtPrecioProducto
        ).text = formatoMoneda.format(precio)

        vista.findViewById<TextView>(
            R.id.txtDescripcionProducto
        ).text = descripcion

        vista.findViewById<TextView>(
            R.id.txtStockProducto
        ).text = "📦 Stock: $stock"

        vista.findViewById<TextView>(
            R.id.txtStockMinimoProducto
        ).text = "⚠️ Stock mínimo: $stockMinimo"

        vista.findViewById<TextView>(
            R.id.txtTallaProducto
        ).text = "📏 Talla: $talla"

        vista.findViewById<TextView>(
            R.id.txtVendedorProducto
        ).text = "🏪 Vendedor: $nombreVendedor"

        vista.findViewById<TextView>(
            R.id.txtCategoriaProducto
        ).text = "🏷️ Categoría ID: $categoriaId"

        val txtEstado = vista.findViewById<TextView>(
            R.id.txtEstadoProducto
        )

        txtEstado.text = if (visible) {
            "✅ Visible"
        } else {
            "⛔ Oculto"
        }

        txtEstado.setTextColor(
            if (visible) {
                Color.parseColor("#2E7D32")
            } else {
                Color.parseColor("#C62828")
            }
        )

        val imagen = vista.findViewById<ImageView>(
            R.id.imgProductoAdmin
        )

        Glide.with(this)
            .load(obtenerUrlImagen(imagenUrl))
            .placeholder(R.drawable.logo_michigurumis_final)
            .error(R.drawable.logo_michigurumis_final)
            .centerCrop()
            .into(imagen)

        val txtVentas = vista.findViewById<TextView>(
            R.id.txtVentasProducto
        )

        val idProducto = obtenerEntero(
            producto,
            "idProducto"
        )


        val btnOcultar = vista.findViewById<MaterialButton>(
            R.id.btnOcultarProducto
        )

        btnOcultar.text = if (visible) {
            "Ocultar producto"
        } else {
            "Mostrar producto"
        }

        btnOcultar.isEnabled = false

        dialogo.show()

        if (idProducto > 0) {
            cargarUnidadesVendidas(
                idProducto,
                txtVentas,
                dialogo
            )
        } else {
            txtVentas.text =
                "🛒 Unidades vendidas: No disponible"
        }
    }

    // CARGAR UNIDADES VENDIDAS DESDE BD

    private fun cargarUnidadesVendidas(
        idProducto: Int,
        txtVentas: TextView,
        dialogo: BottomSheetDialog
    ) {

        txtVentas.text = "🛒 Unidades vendidas: Cargando..."

        val llamada = ApiClient.productosAdminApi
            .obtenerVentasProducto(idProducto)

        dialogo.setOnDismissListener {
            llamada.cancel()
        }

        llamada.enqueue(object : Callback<JsonObject> {

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {

                if (call.isCanceled ||
                    isFinishing ||
                    isDestroyed ||
                    !dialogo.isShowing) return

                if (response.isSuccessful) {

                    val unidades = response.body()
                        ?.get("unidadesVendidas")
                        ?.takeUnless { it.isJsonNull }
                        ?.asInt ?: 0

                    txtVentas.text =
                        "🛒 Unidades vendidas: $unidades"

                } else {

                    txtVentas.text =
                        "🛒 Unidades vendidas: No disponible"

                    Log.e(
                        "PRODUCTOS_ADMIN",
                        "Error al consultar ventas: " +
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
                    isDestroyed ||
                    !dialogo.isShowing) return

                txtVentas.text =
                    "🛒 Unidades vendidas: No disponible"

                Log.e(
                    "PRODUCTOS_ADMIN",
                    "Error consultando unidades vendidas",
                    t
                )
            }
        })
    }


    // MENSAJE DE ESTADO

    private fun mostrarEstado(
        mensaje: String
    ) {

        contenedorProductos.removeAllViews()

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

        contenedorProductos.addView(texto)
    }

    // LECTURA SEGURA DE JSON

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

    private fun obtenerBooleano(
        datos: JsonObject,
        clave: String
    ): Boolean {

        return try {

            datos.get(clave)
                ?.takeUnless { it.isJsonNull }
                ?.asBoolean ?: false

        } catch (e: Exception) {
            false
        }
    }

    private fun dp(valor: Int): Int {

        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }

    // NAVEGACION INFERIOR
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

                R.id.vendedores -> {

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

    // FINALIZAR ACTIVITY

    override fun onDestroy() {

        llamadaProductos?.cancel()

        super.onDestroy()
    }
}
