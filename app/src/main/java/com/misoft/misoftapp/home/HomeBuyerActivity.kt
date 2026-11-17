
package com.misoft.misoftapp.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.api.ProductoResponse
import com.misoft.misoftapp.catalogo.CatalogoActivity
import com.misoft.misoftapp.catalogo.DetalleProductoActivity
import com.misoft.misoftapp.compras.CarritoActivity
import com.misoft.misoftapp.perfil_cliente.PerfilClienteActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import com.misoft.misoftapp.api.ImagenUrlHelper
import android.os.Handler
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2

class HomeBuyerActivity : AppCompatActivity() {

    private lateinit var layoutProductos: LinearLayout
    private lateinit var txtSinProductos: TextView
    private lateinit var viewPagerBanners: ViewPager2

    private val handlerBanners = Handler(Looper.getMainLooper())

    private val banners = listOf(
        R.drawable.banner1,
        R.drawable.banner2,
        R.drawable.banner3
    )

    private val cambiarBanner = object : Runnable {
        override fun run() {
            if (::viewPagerBanners.isInitialized && banners.isNotEmpty()) {

                val siguiente = (
                        viewPagerBanners.currentItem + 1
                        ) % banners.size

                viewPagerBanners.setCurrentItem(siguiente, true)

                handlerBanners.postDelayed(this, 4000)
            }
        }
    }

    private fun configurarCarrusel() {

        viewPagerBanners = findViewById(R.id.viewPagerBanners)

        viewPagerBanners.adapter = BannerAdapter(banners)

        val indicadores = findViewById<LinearLayout>(
            R.id.indicadoresBanners
        )

        fun actualizarIndicadores(posicion: Int) {

            indicadores.removeAllViews()

            banners.indices.forEach { indice ->

                val indicador = TextView(this).apply {

                    text = "●"
                    textSize = 14f

                    setTextColor(
                        Color.parseColor(
                            if (indice == posicion)
                                "#9B7AE0"
                            else
                                "#E6D5EF"
                        )
                    )

                    setPadding(dp(4), 0, dp(4), 0)
                }

                indicadores.addView(indicador)
            }
        }

        actualizarIndicadores(0)

        viewPagerBanners.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    actualizarIndicadores(position)
                }
            }
        )
    }

    private var consultaProductos: Call<List<ProductoResponse>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.cliente_activity_home)

        layoutProductos = findViewById(R.id.layoutProductos)
        txtSinProductos = findViewById(R.id.txtSinProductos)

        configurarCarrusel()
        configurarBotones()
        configurarNavegacion()
    }

    override fun onResume() {
        super.onResume()

        cargarProductosDestacados()

        handlerBanners.removeCallbacks(cambiarBanner)
        handlerBanners.postDelayed(cambiarBanner, 4000)
    }

    override fun onPause() {
        handlerBanners.removeCallbacks(cambiarBanner)
        super.onPause()
    }

    // BOTONES Y FILTROS DEL INICIO
    private fun configurarBotones() {

        // VER TODOS LOS PRODUCTOS
        findViewById<TextView>(
            R.id.txtVerTodos
        ).setOnClickListener {
            abrirCatalogo()
        }

        // CATEGORÍA ANIMALES
        findViewById<ImageView>(
            R.id.ctgAnimales
        ).setOnClickListener {
            abrirCatalogo(categoriaId = 1)
        }

        // CATEGORÍA FLORES
        findViewById<ImageView>(
            R.id.ctgFlores
        ).setOnClickListener {
            abrirCatalogo(categoriaId = 2)
        }

        // CATEGORÍA PERSONAJES
        findViewById<ImageView>(
            R.id.ctgPersonajes
        ).setOnClickListener {
            abrirCatalogo(categoriaId = 3)
        }

        // BUSCADOR DEL HOME
        val etBuscar = findViewById<TextInputEditText>(
            R.id.etBuscar
        )

        etBuscar.setSingleLine(true)
        etBuscar.imeOptions = EditorInfo.IME_ACTION_SEARCH

        etBuscar.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val texto = etBuscar.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()

                abrirCatalogo(busqueda = texto)
                true

            } else {
                false
            }
        }


    }

    // ABRIR CATÁLOGO CON FILTROS OPCIONALES
    private fun abrirCatalogo(
        categoriaId: Int? = null,
        busqueda: String = ""
    ) {

        val intent = Intent(
            this,
            CatalogoActivity::class.java
        )

        categoriaId?.let {
            intent.putExtra("categoriaInicial", it)
        }

        intent.putExtra("busquedaInicial", busqueda)

        startActivity(intent)
    }

    // CARGAR PRODUCTOS DESTACADOS DESDE MYSQL
    private fun cargarProductosDestacados() {

        txtSinProductos.text = "Cargando productos..."
        txtSinProductos.visibility = View.VISIBLE

        consultaProductos?.cancel()

        val llamada = ApiClient.productosApi.obtenerProductos()

        consultaProductos = llamada

        llamada.enqueue(
            object : Callback<List<ProductoResponse>> {

                override fun onResponse(
                    call: Call<List<ProductoResponse>>,
                    response: Response<List<ProductoResponse>>
                ) {

                    if (
                        call.isCanceled ||
                        isFinishing ||
                        isDestroyed
                    ) {
                        return
                    }

                    if (response.isSuccessful) {

                        val productos = response.body().orEmpty()

                        // MOSTRAR LOS 6 PRODUCTOS ACTIVOS MÁS RECIENTES
                        val destacados = productos
                            .filter { it.estado }
                            .sortedByDescending { it.idProducto }
                            .take(6)

                        mostrarProductos(destacados)

                    } else {

                        mostrarError(
                            "No se pudieron cargar los productos."
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<ProductoResponse>>,
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
                        "No se pudo conectar con el servidor."
                    )
                }
            }
        )
    }

    // MOSTRAR PRODUCTOS EN DOS COLUMNAS
    private fun mostrarProductos(
        productos: List<ProductoResponse>
    ) {

        layoutProductos.removeAllViews()

        if (productos.isEmpty()) {

            txtSinProductos.text =
                "Todavía no hay productos disponibles."

            txtSinProductos.visibility = View.VISIBLE
            return
        }

        txtSinProductos.visibility = View.GONE

        productos.chunked(2).forEach { productosFila ->

            val fila = LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            productosFila.forEach { producto ->

                val tarjeta = crearTarjeta(producto)

                val margen = dp(4)

                tarjeta.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(
                        margen,
                        margen,
                        margen,
                        margen
                    )
                }

                fila.addView(tarjeta)
            }

            // CONSERVAR DOS COLUMNAS SI QUEDA UN PRODUCTO
            if (productosFila.size == 1) {

                val espacio = View(this)

                espacio.layoutParams = LinearLayout.LayoutParams(
                    0,
                    1,
                    1f
                )

                fila.addView(espacio)
            }

            layoutProductos.addView(fila)
        }
    }

    // CREAR TARJETA DE PRODUCTO
    private fun crearTarjeta(
        producto: ProductoResponse
    ): CardView {

        val tarjeta = LayoutInflater.from(this)
            .inflate(
                R.layout.cliente_item_producto,
                layoutProductos,
                false
            ) as CardView

        val imgProducto = tarjeta.findViewById<ImageView>(
            R.id.imgProducto
        )

        val txtNombre = tarjeta.findViewById<TextView>(
            R.id.txtNombreProducto
        )

        val txtCategoriaTalla = tarjeta.findViewById<TextView>(
            R.id.txtCategoriaTalla
        )

        val txtPrecio = tarjeta.findViewById<TextView>(
            R.id.txtPrecioProducto
        )

        val txtDisponibilidad = tarjeta.findViewById<TextView>(
            R.id.txtDisponibilidad
        )

        // NOMBRE
        txtNombre.text = producto.nombre

        // CATEGORÍA
        val categoria = when (producto.categoriaId) {

            1 -> "Animales"
            2 -> "Flores"
            3 -> "Personajes"
            else -> "Otra categoría"
        }

        // TALLA
        val talla = producto.talla
            ?.takeIf { it.isNotBlank() }
            ?: "No especificada"

        txtCategoriaTalla.text = "$categoria · Talla $talla"

        // PRECIO EN PESOS COLOMBIANOS
        val formatoPrecio = NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        ).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }

        txtPrecio.text = formatoPrecio.format(
            producto.precio
        )

        // DISPONIBILIDAD
        if (producto.stock > 0) {

            txtDisponibilidad.text = "Disponible"

            txtDisponibilidad.setTextColor(
                Color.parseColor("#4CAF50")
            )

        } else {

            txtDisponibilidad.text = "Agotado"

            txtDisponibilidad.setTextColor(
                Color.parseColor("#C62828")
            )
        }


        // FOTOGRAFÍA DEL PRODUCTO
        val urlImagen = ImagenUrlHelper.obtenerUrl(
            producto.imagenUrl
        )

        if (!urlImagen.isNullOrBlank()) {

            Glide.with(this)
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



        // ABRIR DETALLES DEL PRODUCTO
        tarjeta.setOnClickListener {

            val intent = Intent(
                this,
                DetalleProductoActivity::class.java
            )

            intent.putExtra(
                "idProducto",
                producto.idProducto
            )

            intent.putExtra(
                "esVistaPrevia",
                false
            )

            startActivity(intent)
        }

        return tarjeta
    }

    // MOSTRAR ERRORES
    private fun mostrarError(mensaje: String) {

        layoutProductos.removeAllViews()

        txtSinProductos.text = mensaje
        txtSinProductos.visibility = View.VISIBLE

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
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
                R.id.bottomNavigation
            )

        bottomNavigation.selectedItemId = R.id.home

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> {
                    true
                }

                R.id.categoria -> {

                    abrirCatalogo()
                    finish()
                    true
                }

                R.id.carrito -> {

                    startActivity(
                        Intent(
                            this,
                            CarritoActivity::class.java
                        )
                    )

                    finish()
                    true
                }

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

    override fun onDestroy() {

        handlerBanners.removeCallbacks(cambiarBanner)
        consultaProductos?.cancel()
        super.onDestroy()
    }
}
