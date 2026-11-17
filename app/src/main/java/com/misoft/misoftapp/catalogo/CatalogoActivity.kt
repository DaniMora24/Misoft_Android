package com.misoft.misoftapp.catalogo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
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
import com.misoft.misoftapp.compras.CarritoActivity
import com.misoft.misoftapp.home.HomeBuyerActivity
import com.misoft.misoftapp.perfil_cliente.PerfilClienteActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import com.google.android.material.chip.ChipGroup
import com.misoft.misoftapp.api.ImagenUrlHelper

class CatalogoActivity : AppCompatActivity() {

    private lateinit var layoutProductos: LinearLayout
    private lateinit var txtSinProductos: TextView
    private lateinit var etBuscar: TextInputEditText

    private var productosActivos = listOf<ProductoResponse>()
    private var categoriaSeleccionada: Int? = null
    private var consultaProductos: Call<List<ProductoResponse>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cliente_activity_catalogo)

        layoutProductos = findViewById(R.id.layoutProductos)
        txtSinProductos = findViewById(R.id.txtSinProductos)
        etBuscar = findViewById(R.id.etBuscar)

        configurarBuscador()
        configurarCategorias()
        configurarNavegacion()


// RECIBIR FILTROS DESDE EL HOMEBUYER
        val categoriaInicial = intent.getIntExtra(
            "categoriaInicial",
            0
        )

        val busquedaInicial = intent.getStringExtra(
            "busquedaInicial"
        ).orEmpty()

        val chipGroup = findViewById<
                com.google.android.material.chip.ChipGroup
                >(
            R.id.chipGroupCategorias
        )

        val chipSeleccionado = when (categoriaInicial) {
            1 -> R.id.chipAnimales
            2 -> R.id.chipFlores
            3 -> R.id.chipPersonajes
            else -> R.id.chipTodos
        }

        chipGroup.check(chipSeleccionado)

        if (busquedaInicial.isNotBlank()) {
            etBuscar.setText(busquedaInicial)
        }

    }

    override fun onResume() {
        super.onResume()
        cargarProductos()
    }

    // CONSULTAR PRODUCTOS DESDE LA API
    private fun cargarProductos() {

        txtSinProductos.text = "Cargando productos..."
        txtSinProductos.visibility = View.VISIBLE

        consultaProductos?.cancel()

        val llamada = ApiClient.productosApi.obtenerProductos()
        consultaProductos = llamada

        llamada.enqueue(object : Callback<List<ProductoResponse>> {

            override fun onResponse(
                call: Call<List<ProductoResponse>>,
                response: Response<List<ProductoResponse>>
            ) {

                if (call.isCanceled || isFinishing || isDestroyed) {
                    return
                }

                if (response.isSuccessful) {

                    val productos = response.body().orEmpty()

                    // El comprador solo ve productos activos.
                    productosActivos = productos.filter {
                        it.estado
                    }

                    mostrarProductosFiltrados()

                } else {

                    mostrarError(
                        "No se pudieron cargar los productos (${response.code()})"
                    )
                }
            }

            override fun onFailure(
                call: Call<List<ProductoResponse>>,
                t: Throwable
            ) {

                if (call.isCanceled || isFinishing || isDestroyed) {
                    return
                }

                mostrarError(
                    "Error de conexión. Comprueba que el servidor esté funcionando."
                )
            }
        })
    }

    // BUSCADOR POR NOMBRE
    private fun configurarBuscador() {

        etBuscar.addTextChangedListener(object : TextWatcher {

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
                mostrarProductosFiltrados()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }


    // FILTRAR PRODUCTOS POR CATEGORÍA
    private fun configurarCategorias() {

        val chipGroupCategorias =
            findViewById<ChipGroup>(
                R.id.chipGroupCategorias
            )

        chipGroupCategorias.setOnCheckedStateChangeListener {
                _, checkedIds ->

            categoriaSeleccionada = when (
                checkedIds.firstOrNull()
            ) {

                R.id.chipAnimales -> 1

                R.id.chipFlores -> 2

                R.id.chipPersonajes -> 3

                else -> null
            }

            mostrarProductosFiltrados()
        }
    }



    // APLICAR FILTROS Y MOSTRAR TARJETAS
    private fun mostrarProductosFiltrados() {

        val textoBusqueda = etBuscar.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val productosFiltrados = productosActivos.filter { producto ->

            val coincideNombre = producto.nombre.contains(
                textoBusqueda,
                ignoreCase = true
            )

            val coincideCategoria =
                categoriaSeleccionada == null ||
                        producto.categoriaId == categoriaSeleccionada

            coincideNombre && coincideCategoria
        }

        layoutProductos.removeAllViews()

        if (productosFiltrados.isEmpty()) {

            txtSinProductos.text =
                "No se encontraron productos disponibles."

            txtSinProductos.visibility = View.VISIBLE
            return
        }

        txtSinProductos.visibility = View.GONE

        // CREAR FILAS DE DOS PRODUCTOS
        productosFiltrados.chunked(2).forEach { filaProductos ->

            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            filaProductos.forEach { producto ->

                val tarjeta = crearTarjetaProducto(producto)

                val margen = dp(4)

                tarjeta.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(margen, margen, margen, margen)
                }

                fila.addView(tarjeta)
            }

            // Mantener el ancho cuando la fila tenga un producto.
            if (filaProductos.size == 1) {

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

    // CREAR TARJETA INDIVIDUAL
    private fun crearTarjetaProducto(
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
            ?: "Sin talla"

        txtCategoriaTalla.text = "$categoria · Talla $talla"

        // PRECIO EN PESOS COLOMBIANOS
        val formatoPrecio = NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        ).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }

        txtPrecio.text = formatoPrecio.format(producto.precio)

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

        // CARGAR FOTOGRAFÍA REAL
        val rutaImagen = producto.imagenUrl

        if (!rutaImagen.isNullOrBlank()) {


            val urlImagen = ImagenUrlHelper.obtenerUrl(rutaImagen)


            Glide.with(this)
                .load(urlImagen)
                .placeholder(R.drawable.usuario)
                .error(R.drawable.usuario)
                .centerCrop()
                .into(imgProducto)

        } else {

            imgProducto.setImageResource(R.drawable.usuario)
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

    private fun mostrarError(mensaje: String) {

        productosActivos = emptyList()
        layoutProductos.removeAllViews()

        txtSinProductos.text = mensaje
        txtSinProductos.visibility = View.VISIBLE

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    // MENÚ INFERIOR
    private fun configurarNavegacion() {

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNavigation.selectedItemId = R.id.categoria

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
        consultaProductos?.cancel()
        super.onDestroy()
    }
}
