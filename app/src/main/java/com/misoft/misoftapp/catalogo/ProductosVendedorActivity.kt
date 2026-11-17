package com.misoft.misoftapp.catalogo

import com.misoft.misoftapp.catalogo.DetalleProductoActivity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.api.EditarProductoResponse
import com.misoft.misoftapp.api.ProductoResponse
import com.misoft.misoftapp.api.RegistrarProductoRequest
import com.misoft.misoftapp.api.SubirImagenResponse
import com.misoft.misoftapp.home.HomeVendedorActivity
import com.misoft.misoftapp.pedidos.GestionPedidosActivity
import com.misoft.misoftapp.perfil_vendedor.PerfilVendedorActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale
import com.misoft.misoftapp.api.CambiarEstadoProductoResponse
import com.misoft.misoftapp.api.ImagenUrlHelper

class ProductosVendedorActivity : AppCompatActivity() {

    private var imagenBytes: ByteArray? = null
    private var imagenVistaPrevia: ImageView? = null
    private var dialogoProducto: BottomSheetDialog? = null

    // SELECCIONAR FOTO DE GALERÍA
    private val selectorGaleria =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null && dialogoProducto?.isShowing == true) {
                cargarImagenDesdeUri(uri)
            }
        }

    // TOMAR FOTO CON CÁMARA
    private val selectorCamara =
        registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->

            if (bitmap != null && dialogoProducto?.isShowing == true) {

                val output = ByteArrayOutputStream()

                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    85,
                    output
                )

                imagenBytes = output.toByteArray()


                imagenVistaPrevia?.apply {

                    (parent as? android.view.ViewGroup)
                        ?.findViewById<LinearLayout>(
                            R.id.layoutIndicacionFoto
                        )
                        ?.visibility = View.GONE

                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    visibility = View.VISIBLE
                }


                mostrarMensaje("Fotografía capturada")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.vendedor_activity_productos)

        // NAVEGACIÓN
        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationSeller
            )

        bottomNavigation.selectedItemId = R.id.productos

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

                R.id.productos -> true

                R.id.pedidos -> {
                    startActivity(
                        Intent(
                            this,
                            GestionPedidosActivity::class.java
                        )
                    )
                    finish()
                    true
                }

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

        // CREAR NUEVO PRODUCTO
        findViewById<MaterialButton>(
            R.id.btnNuevo
        ).setOnClickListener {
            mostrarProducto()
        }

        // CONSULTAR PRODUCTOS REALES
        cargarProductosVendedor()
    }

    // CONSULTAR PRODUCTOS DEL VENDEDOR
    private fun cargarProductosVendedor() {

        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val idVendedor = preferencias.getInt(
            "idUsuario",
            -1
        )

        val rolId = preferencias.getInt(
            "rolId",
            -1
        )

        if (idVendedor <= 0 || rolId != 2) {
            mostrarMensaje("Sesión de vendedor inválida")
            return
        }

        val listaProductos =
            findViewById<LinearLayout>(
                R.id.listaProductos
            )

        val layoutSinProductos =
            findViewById<LinearLayout>(
                R.id.layoutSinProductos
            )


        ApiClient.productosApi
            .obtenerProductosVendedor(idVendedor)
            .enqueue(object : Callback<List<ProductoResponse>> {

                override fun onResponse(
                    call: Call<List<ProductoResponse>>,
                    response: Response<List<ProductoResponse>>
                ) {

                    if (!response.isSuccessful) {
                        mostrarMensaje(
                            "Error al consultar productos: ${response.code()}"
                        )
                        return
                    }

                    val productos = response.body().orEmpty()

                    listaProductos.removeAllViews()

                    if (productos.isEmpty()) {
                        layoutSinProductos.visibility = View.VISIBLE
                        return
                    }

                    layoutSinProductos.visibility = View.GONE

                    val formatoPrecio =
                        NumberFormat.getCurrencyInstance(
                            Locale("es", "CO")
                        ).apply {
                            maximumFractionDigits = 0
                            minimumFractionDigits = 0
                        }

                    for (producto in productos) {

                        val vista = layoutInflater.inflate(
                            R.layout.vendedor_item_producto,
                            listaProductos,
                            false
                        )

                        val imgProducto =
                            vista.findViewById<ImageView>(
                                R.id.imgProducto
                            )

                        val txtNombre =
                            vista.findViewById<TextView>(
                                R.id.txtNombreItem
                            )

                        val txtPrecio =
                            vista.findViewById<TextView>(
                                R.id.txtPrecioItem
                            )

                        val txtStock =
                            vista.findViewById<TextView>(
                                R.id.txtStockItem
                            )

                        val txtEstado =
                            vista.findViewById<TextView>(
                                R.id.txtEstadoItem
                            )

                        val btnEditar =
                            vista.findViewById<ImageView>(
                                R.id.imgEditarProducto
                            )

                        val btnEstado =
                            vista.findViewById<ImageView>(
                                R.id.imgEstadoProducto
                            )

                        // DATOS DEL PRODUCTO
                        txtNombre.text = producto.nombre

                        txtPrecio.text = formatoPrecio.format(
                            producto.precio
                        )

                        txtStock.text = "Stock: ${producto.stock}"

                        // MOSTRAR IMAGEN
                        val rutaImagen = producto.imagenUrl

                        if (!rutaImagen.isNullOrBlank()) {

                            Glide.with(this@ProductosVendedorActivity)
                                .load(ImagenUrlHelper.obtenerUrl(rutaImagen))
                                .placeholder(R.drawable.usuario)
                                .error(R.drawable.usuario)
                                .centerCrop()
                                .into(imgProducto)

                        } else {
                            imgProducto.setImageResource(
                                R.drawable.usuario
                            )
                        }


                        // ABRIR VISTA PREVIA DEL PRODUCTO
                        imgProducto.isClickable = true
                        imgProducto.isFocusable = true

                        imgProducto.setOnClickListener {

                            val intent = Intent(
                                this@ProductosVendedorActivity,
                                DetalleProductoActivity::class.java
                            )

                            intent.putExtra(
                                "idProducto",
                                producto.idProducto
                            )

                            intent.putExtra(
                                "esVistaPrevia",
                                true
                            )

                            startActivity(intent)
                        }


                        // ESTADO DEL PRODUCTO
                        val colorTexto: String
                        val colorFondo: String

                        when {
                            !producto.estado -> {
                                txtEstado.text = "Inactivo"
                                colorTexto = "#666666"
                                colorFondo = "#EEEEEE"
                            }

                            producto.stock == 0 -> {
                                txtEstado.text = "Sin stock"
                                colorTexto = "#C62828"
                                colorFondo = "#FFE4E4"
                            }

                            producto.stock <= producto.stockMinimo -> {
                                txtEstado.text = "Stock bajo"
                                colorTexto = "#9A6700"
                                colorFondo = "#FFF3CD"
                            }

                            else -> {
                                txtEstado.text = "En stock"
                                colorTexto = "#2E7D32"
                                colorFondo = "#DDF5E1"
                            }
                        }

                        txtEstado.setTextColor(
                            Color.parseColor(colorTexto)
                        )

                        txtEstado.setBackgroundColor(
                            Color.parseColor(colorFondo)
                        )

                        // EDITAR PRODUCTO REAL
                        btnEditar.setOnClickListener {
                            mostrarProducto(producto)
                        }


                        // ACTIVAR O DESACTIVAR PRODUCTO
                        btnEstado.contentDescription =
                            if (producto.estado) {
                                "Desactivar producto"
                            } else {
                                "Reactivar producto"
                            }

                        btnEstado.setImageResource(
                            if (producto.estado) {
                                R.drawable.desactivar_producto
                            } else {
                                R.drawable.reactivar_producto
                            }
                        )

                        btnEstado.setOnClickListener {
                            val accion = if (producto.estado) {
                                "desactivar"
                            } else {
                                "reactivar"
                            }

                            AlertDialog.Builder(this@ProductosVendedorActivity)
                                .setTitle(
                                    if (producto.estado) {
                                        "Desactivar producto"
                                    } else {
                                        "Reactivar producto"
                                    }
                                )
                                .setMessage(
                                    "¿Deseas $accion el producto '${producto.nombre}'?"
                                )
                                .setPositiveButton(
                                    if (producto.estado) "Desactivar" else "Reactivar"
                                ) { _, _ ->
                                    cambiarEstadoProducto(
                                        producto.idProducto,
                                        btnEstado
                                    )
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }

                        listaProductos.addView(vista)
                    }
                }

                override fun onFailure(
                    call: Call<List<ProductoResponse>>,
                    t: Throwable
                ) {
                    mostrarMensaje(
                        "Error de conexión: ${t.message}"
                    )
                }
            })
    }

    // CREAR O EDITAR PRODUCTO
    private fun mostrarProducto(
        productoEditar: ProductoResponse? = null
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.vendedor_bottom_crear_producto,
            null
        )

        val txtTitulo =
            view.findViewById<TextView>(
                R.id.txtTituloProducto
            )

        val edtNombre =
            view.findViewById<TextInputEditText>(
                R.id.edtNombreProducto
            )

        val edtDescripcion =
            view.findViewById<TextInputEditText>(
                R.id.edtDescripcion
            )

        val edtPrecio =
            view.findViewById<TextInputEditText>(
                R.id.edtPrecio
            )

        val edtStock =
            view.findViewById<TextInputEditText>(
                R.id.edtStock
            )

        val actTalla =
            view.findViewById<AutoCompleteTextView>(
                R.id.actTalla
            )

        val actCategoria =
            view.findViewById<AutoCompleteTextView>(
                R.id.actCategoria
            )

        val btnGuardar =
            view.findViewById<MaterialButton>(
                R.id.btnGuardarProducto
            )

        val cardAgregarFoto =
            view.findViewById<CardView>(
                R.id.cardAgregarFoto
            )

        val esEdicion = productoEditar != null

        // CONFIGURAR TÍTULO Y BOTÓN
        txtTitulo.text = if (esEdicion) {
            "Editar Producto"
        } else {
            "Nuevo Producto"
        }

        btnGuardar.text = if (esEdicion) {
            "Actualizar producto"
        } else {
            "Guardar producto"
        }

        // CONFIGURAR TALLAS
        val tallas = listOf(
            "S - Pequeño (10-15 cm)",
            "M - Mediano (16-20 cm)",
            "L - Grande (21-30 cm)",
            "XL - Extra grande (31-40 cm)"
        )

        actTalla.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                tallas
            )
        )

        actTalla.setOnClickListener {
            actTalla.showDropDown()
        }

        // CONFIGURAR CATEGORÍAS
        val categorias = listOf(
            "Animales",
            "Flores",
            "Personajes"
        )

        actCategoria.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categorias
            )
        )

        actCategoria.setOnClickListener {
            actCategoria.showDropDown()
        }

        // RELLENAR DATOS AL EDITAR
        if (productoEditar != null) {

            edtNombre.setText(productoEditar.nombre)

            edtDescripcion.setText(
                productoEditar.descripcion.orEmpty()
            )

            edtPrecio.setText(
                productoEditar.precio.toLong().toString()
            )

            edtStock.setText(
                productoEditar.stock.toString()
            )

            val tallaActual = tallas.firstOrNull {
                it.substringBefore(" - ") == productoEditar.talla
            }

            actTalla.setText(
                tallaActual.orEmpty(),
                false
            )

            val categoriaActual = when (
                productoEditar.categoriaId
            ) {
                1 -> "Animales"
                2 -> "Flores"
                3 -> "Personajes"
                else -> ""
            }

            actCategoria.setText(
                categoriaActual,
                false
            )
        }


        // PREPARAR IMAGEN
        imagenBytes = null

        // OBTENER LA VISTA PREVIA DESDE EL XML
        val vistaPrevia = view.findViewById<ImageView>(
            R.id.imgVistaPreviaProducto
        )

        val layoutIndicacionFoto =
            view.findViewById<LinearLayout>(
                R.id.layoutIndicacionFoto
            )

        vistaPrevia.scaleType =
            ImageView.ScaleType.FIT_CENTER

        vistaPrevia.visibility = View.GONE

        imagenVistaPrevia = vistaPrevia

        // MOSTRAR FOTO EXISTENTE EN EDICIÓN
        if (!productoEditar?.imagenUrl.isNullOrBlank()) {

            layoutIndicacionFoto.visibility = View.GONE
            vistaPrevia.visibility = View.VISIBLE

            Glide.with(this)
                .load(
                    ImagenUrlHelper.obtenerUrl(
                        productoEditar!!.imagenUrl
                    )
                )
                .placeholder(R.drawable.usuario)
                .error(R.drawable.usuario)
                .fitCenter()
                .into(vistaPrevia)
        }


        // ELEGIR O CAMBIAR FOTO
        cardAgregarFoto.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle(
                    if (esEdicion) {
                        "Cambiar fotografía"
                    } else {
                        "Agregar foto del producto"
                    }
                )
                .setItems(
                    arrayOf(
                        "Tomar fotografía",
                        "Seleccionar de galería"
                    )
                ) { _, opcion ->

                    when (opcion) {
                        0 -> selectorCamara.launch(null)
                        1 -> selectorGaleria.launch("image/*")
                    }
                }
                .show()
        }






        // GUARDAR O ACTUALIZAR
        btnGuardar.setOnClickListener {

            val nombre =
                edtNombre.text.toString().trim()

            val descripcion =
                edtDescripcion.text.toString().trim()

            val precio =
                edtPrecio.text.toString().trim().toDoubleOrNull()

            val stock =
                edtStock.text.toString().trim().toIntOrNull()

            val tallaSeleccionada =
                actTalla.text.toString().trim()

            val categoriaSeleccionada =
                actCategoria.text.toString().trim()

            // VALIDACIONES
            if (nombre.isBlank()) {
                edtNombre.error = "Ingresa el nombre"
                return@setOnClickListener
            }

            if (precio == null || precio <= 0) {
                edtPrecio.error = "Ingresa un precio válido"
                return@setOnClickListener
            }

            if (stock == null || stock < 0) {
                edtStock.error = "Ingresa un stock válido"
                return@setOnClickListener
            }

            if (tallaSeleccionada !in tallas) {
                mostrarMensaje("Selecciona una talla válida")
                return@setOnClickListener
            }

            if (categoriaSeleccionada !in categorias) {
                mostrarMensaje("Selecciona una categoría")
                return@setOnClickListener
            }

            val talla =
                tallaSeleccionada.substringBefore(" - ")

            val categoriaId = when (categoriaSeleccionada) {
                "Animales" -> 1
                "Flores" -> 2
                "Personajes" -> 3
                else -> return@setOnClickListener
            }

            val preferencias = getSharedPreferences(
                "MisoftSesion",
                MODE_PRIVATE
            )

            val idVendedor = preferencias.getInt(
                "idUsuario",
                -1
            )

            val rolId = preferencias.getInt(
                "rolId",
                -1
            )

            if (idVendedor <= 0 || rolId != 2) {
                mostrarMensaje("Sesión de vendedor inválida")
                return@setOnClickListener
            }

            // COMPROBAR QUE EL PRODUCTO SEA DEL VENDEDOR
            if (
                productoEditar != null &&
                productoEditar.idVendedor != idVendedor
            ) {
                mostrarMensaje("Este producto no pertenece al vendedor")
                return@setOnClickListener
            }

            val bytes = imagenBytes

            // CREAR REQUIERE FOTO
            if (!esEdicion && bytes == null) {
                mostrarMensaje("Selecciona una fotografía")
                return@setOnClickListener
            }

            if (
                bytes != null &&
                bytes.size > 5 * 1024 * 1024
            ) {
                mostrarMensaje("La imagen supera los 5 MB")
                return@setOnClickListener
            }

            // PREPARAR LOS DATOS
            val producto = RegistrarProductoRequest(
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                stockMinimo = productoEditar?.stockMinimo ?: 5,
                talla = talla,
                imagenUrl = productoEditar?.imagenUrl,
                categoriaId = categoriaId,
                idVendedor = idVendedor
            )

            if (bytes != null) {

                // FOTO NUEVA: SUBIRLA PRIMERO
                subirImagenYGuardar(
                    bytes,
                    producto,
                    productoEditar?.idProducto,
                    btnGuardar,
                    dialog
                )

            } else {

                // EDICIÓN SIN CAMBIAR LA FOTO
                guardarProductoEnMysql(
                    producto,
                    productoEditar?.idProducto,
                    btnGuardar,
                    dialog
                )
            }
        }

        dialog.setContentView(view)
        dialogoProducto = dialog

        dialog.setOnDismissListener {
            if (dialogoProducto === dialog) {
                dialogoProducto = null
                imagenVistaPrevia = null
                imagenBytes = null
            }
        }

        dialog.show()
    }

    // LEER FOTO DE GALERÍA
    private fun cargarImagenDesdeUri(uri: Uri) {

        try {

            val bytes = contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            }

            if (bytes == null) {
                mostrarMensaje("No se pudo leer la imagen")
                return
            }

            val bitmap = BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            )

            if (bitmap == null) {
                mostrarMensaje("Imagen no válida")
                return
            }

            val output = ByteArrayOutputStream()

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                85,
                output
            )

            imagenBytes = output.toByteArray()


            imagenVistaPrevia?.apply {

                (parent as? android.view.ViewGroup)
                    ?.findViewById<LinearLayout>(
                        R.id.layoutIndicacionFoto
                    )
                    ?.visibility = View.GONE

                setImageBitmap(bitmap)
                scaleType = ImageView.ScaleType.FIT_CENTER
                visibility = View.VISIBLE
            }


            mostrarMensaje("Imagen seleccionada")

        } catch (e: Exception) {
            mostrarMensaje("Error al seleccionar la imagen")
        }
    }


    // SUBIR FOTO Y GUARDAR PRODUCTO
    private fun subirImagenYGuardar(
        bytes: ByteArray,
        producto: RegistrarProductoRequest,
        idProductoEditar: Int?,
        btnGuardar: MaterialButton,
        dialog: BottomSheetDialog
    ) {

        val requestBody = bytes.toRequestBody(
            "image/jpeg".toMediaType()
        )

        val archivo = MultipartBody.Part.createFormData(
            "imagen",
            "producto_${System.currentTimeMillis()}.jpg",
            requestBody
        )

        btnGuardar.isEnabled = false
        btnGuardar.text = "Subiendo imagen..."
        dialog.setCancelable(false)

        ApiClient.productosApi.subirImagen(archivo)
            .enqueue(object : Callback<SubirImagenResponse> {

                override fun onResponse(
                    call: Call<SubirImagenResponse>,
                    response: Response<SubirImagenResponse>
                ) {

                    val imagenUrl = response.body()?.imagenUrl

                    if (!response.isSuccessful || imagenUrl.isNullOrBlank()) {

                        restaurarFormulario(
                            btnGuardar,
                            dialog,
                            idProductoEditar != null
                        )

                        mostrarMensaje(
                            "Error al subir imagen: ${response.code()}"
                        )
                        return
                    }

                    val productoConImagen = producto.copy(
                        imagenUrl = imagenUrl
                    )

                    guardarProductoEnMysql(
                        productoConImagen,
                        idProductoEditar,
                        btnGuardar,
                        dialog
                    )
                }

                override fun onFailure(
                    call: Call<SubirImagenResponse>,
                    t: Throwable
                ) {

                    restaurarFormulario(
                        btnGuardar,
                        dialog,
                        idProductoEditar != null
                    )

                    mostrarMensaje(
                        "Error al subir imagen: ${t.message}"
                    )
                }
            })
    }

    // REGISTRAR O EDITAR EN MYSQL
    private fun guardarProductoEnMysql(
        producto: RegistrarProductoRequest,
        idProductoEditar: Int?,
        btnGuardar: MaterialButton,
        dialog: BottomSheetDialog
    ) {

        btnGuardar.isEnabled = false
        btnGuardar.text = if (idProductoEditar == null) {
            "Guardando producto..."
        } else {
            "Actualizando producto..."
        }

        dialog.setCancelable(false)

        if (idProductoEditar == null) {

            // REGISTRAR NUEVO PRODUCTO
            ApiClient.productosApi.registrarProducto(producto)
                .enqueue(object : Callback<ProductoResponse> {

                    override fun onResponse(
                        call: Call<ProductoResponse>,
                        response: Response<ProductoResponse>
                    ) {

                        if (response.isSuccessful) {

                            mostrarMensaje(
                                "Producto registrado correctamente"
                            )

                            dialog.dismiss()
                            cargarProductosVendedor()

                        } else {

                            restaurarFormulario(
                                btnGuardar,
                                dialog,
                                false
                            )

                            mostrarMensaje(
                                "Error al registrar: ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ProductoResponse>,
                        t: Throwable
                    ) {

                        restaurarFormulario(
                            btnGuardar,
                            dialog,
                            false
                        )

                        mostrarMensaje(
                            "Error de conexión: ${t.message}"
                        )
                    }
                })

        } else {

            // ACTUALIZAR PRODUCTO EXISTENTE
            ApiClient.productosApi
                .editarProducto(idProductoEditar, producto)
                .enqueue(object : Callback<EditarProductoResponse> {

                    override fun onResponse(
                        call: Call<EditarProductoResponse>,
                        response: Response<EditarProductoResponse>
                    ) {

                        if (response.isSuccessful) {

                            mostrarMensaje(
                                "Producto actualizado correctamente"
                            )

                            dialog.dismiss()
                            cargarProductosVendedor()

                        } else {

                            restaurarFormulario(
                                btnGuardar,
                                dialog,
                                true
                            )

                            mostrarMensaje(
                                "Error al actualizar: ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<EditarProductoResponse>,
                        t: Throwable
                    ) {

                        restaurarFormulario(
                            btnGuardar,
                            dialog,
                            true
                        )

                        mostrarMensaje(
                            "Error de conexión: ${t.message}"
                        )
                    }
                })
        }
    }

    // RESTAURAR BOTÓN SI FALLA
    private fun restaurarFormulario(
        btnGuardar: MaterialButton,
        dialog: BottomSheetDialog,
        esEdicion: Boolean
    ) {

        btnGuardar.isEnabled = true

        btnGuardar.text = if (esEdicion) {
            "Actualizar producto"
        } else {
            "Guardar producto"
        }

        dialog.setCancelable(true)
    }


    // ACTIVAR O DESACTIVAR PRODUCTO
    private fun cambiarEstadoProducto(
        idProducto: Int,
        btnEstado: ImageView
    ) {

        btnEstado.isEnabled = false

        ApiClient.productosApi
            .cambiarEstadoProducto(idProducto)
            .enqueue(
                object : Callback<CambiarEstadoProductoResponse> {

                    override fun onResponse(
                        call: Call<CambiarEstadoProductoResponse>,
                        response: Response<CambiarEstadoProductoResponse>
                    ) {

                        btnEstado.isEnabled = true

                        if (response.isSuccessful &&
                            response.body() != null
                        ) {

                            val resultado = response.body()!!

                            mostrarMensaje(resultado.mensaje)

                            // Actualizar listado desde MySQL
                            cargarProductosVendedor()

                        } else {

                            mostrarMensaje(
                                "Error al cambiar estado: ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<CambiarEstadoProductoResponse>,
                        t: Throwable
                    ) {

                        btnEstado.isEnabled = true

                        mostrarMensaje(
                            "Error de conexión: ${t.message}"
                        )
                    }
                }
            )
    }


    // MENSAJES
    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}
