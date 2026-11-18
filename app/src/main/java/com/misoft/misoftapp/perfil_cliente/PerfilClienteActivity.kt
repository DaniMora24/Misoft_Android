package com.misoft.misoftapp.perfil_cliente

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.api.ImagenUrlHelper
import com.misoft.misoftapp.auth.LoginActivity
import com.misoft.misoftapp.catalogo.CatalogoActivity
import com.misoft.misoftapp.catalogo.FavoritosActivity
import com.misoft.misoftapp.compras.CarritoActivity
import com.misoft.misoftapp.compras.ComprasActivity
import com.misoft.misoftapp.home.HomeBuyerActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilClienteActivity : AppCompatActivity() {

    private lateinit var imgPerfil: ImageView
    private lateinit var txtNombre: TextView
    private lateinit var txtCorreo: TextView

    private var idUsuario: Int = -1

    private val selectorFoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { subirFotoPerfil(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cliente_activity_perfil)

        val sesion = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        idUsuario = sesion.getInt("idUsuario", -1)

        imgPerfil = findViewById(R.id.imgPerfil)
        txtNombre = findViewById(R.id.txtNombreClientePerfil)
        txtCorreo = findViewById(R.id.txtCorreoClientePerfil)

        configurarDatosSesion()
        configurarFotoPerfil()
        configurarOpciones()
        configurarNavegacion()
    }

    override fun onResume() {
        super.onResume()

        configurarDatosSesion()
        cargarPerfil()
    }

    // DATOS DE LA SESIÓN

    private fun configurarDatosSesion() {

        val sesion = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        txtNombre.text = sesion.getString(
            "nombreCompleto",
            "Comprador"
        )

        txtCorreo.text = sesion.getString(
            "email",
            ""
        )
    }

    // FOTO DE PERFIL

    private fun configurarFotoPerfil() {

        val contenedorFoto =
            findViewById<FrameLayout>(
                R.id.contenedorFotoPerfilCliente
            )

        val botonFoto =
            findViewById<TextView>(
                R.id.btnCambiarFotoCliente
            )

        val textoCambiar =
            findViewById<TextView>(
                R.id.txtCambiarFotoCliente
            )

        contenedorFoto.setOnClickListener {
            abrirSelectorFoto()
        }

        botonFoto.setOnClickListener {
            abrirSelectorFoto()
        }

        textoCambiar.setOnClickListener {
            abrirSelectorFoto()
        }
    }

    private fun abrirSelectorFoto() {

        if (idUsuario <= 0) {
            mostrarMensaje("No se encontró la sesión del comprador")
            return
        }

        selectorFoto.launch("image/*")
    }

    // OBTENER FOTO DESDE LA API

    private fun cargarPerfil() {

        if (idUsuario <= 0) return

        ApiClient.perfilApi
            .obtenerPerfil(idUsuario)
            .ejecutar(
                onSuccess = { datos ->

                    if (datos == null) return@ejecutar

                    try {
                        val json = Gson().toJsonTree(datos)

                        if (!json.isJsonObject) return@ejecutar

                        val foto = json.asJsonObject.get("fotoUrl")

                        val fotoUrl =
                            if (foto != null && !foto.isJsonNull)
                                foto.asString
                            else
                                ""

                        mostrarFoto(fotoUrl)

                    } catch (e: Exception) {
                        imgPerfil.setImageResource(R.drawable.person)
                    }
                },
                onError = {
                    // Conservar la imagen actual si falla la consulta
                }
            )
    }

    private fun mostrarFoto(fotoUrl: String) {

        val url = ImagenUrlHelper.obtenerUrl(fotoUrl)

        if (url.isNullOrBlank()) {
            imgPerfil.setImageResource(R.drawable.person)
            return
        }

        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .centerCrop()
            .into(imgPerfil)
    }

    // SUBIR NUEVA FOTO A LA API

    private fun subirFotoPerfil(uri: Uri) {

        if (idUsuario <= 0) {
            mostrarMensaje("No se encontró la sesión")
            return
        }

        try {

            val tipo = contentResolver.getType(uri)
                ?: "image/jpeg"

            if (tipo !in listOf(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
                )
            ) {
                mostrarMensaje("Selecciona una imagen JPG, PNG o WEBP")
                return
            }

            // Límite de 5 MB
            val limite = 5 * 1024 * 1024

            val bytes = contentResolver
                .openInputStream(uri)
                ?.use { entrada ->

                    val salida = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)

                    while (true) {

                        val cantidad = entrada.read(buffer)

                        if (cantidad == -1) break

                        salida.write(buffer, 0, cantidad)

                        if (salida.size() > limite) {
                            return@use null
                        }
                    }

                    salida.toByteArray()
                }

            if (bytes == null) {
                mostrarMensaje(
                    "No se pudo leer la imagen o supera los 5 MB"
                )
                return
            }

            val extension = when (tipo) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val archivo = MultipartBody.Part.createFormData(
                "foto",
                "perfil_${idUsuario}.$extension",
                bytes.toRequestBody(tipo.toMediaType())
            )

            val cuerpoId = idUsuario
                .toString()
                .toRequestBody("text/plain".toMediaType())

            mostrarMensaje("Actualizando fotografía...")

            ApiClient.perfilApi
                .cambiarFoto(cuerpoId, archivo)
                .ejecutar(
                    onSuccess = {

                        mostrarMensaje(
                            "Fotografía actualizada correctamente"
                        )

                        cargarPerfil()
                    },
                    onError = { mensaje ->
                        mostrarMensaje(mensaje)
                    }
                )

        } catch (e: Exception) {

            mostrarMensaje(
                "No se pudo procesar la fotografía"
            )
        }
    }

    // AYUDANTE PARA RESPUESTAS RETROFIT

    private fun <T> Call<T>.ejecutar(
        onSuccess: (T?) -> Unit,
        onError: (String) -> Unit
    ) {

        enqueue(object : Callback<T> {

            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {

                if (isFinishing || isDestroyed) return

                if (response.isSuccessful) {

                    onSuccess(response.body())

                } else {

                    onError(
                        "Error del servidor: ${response.code()}"
                    )
                }
            }

            override fun onFailure(
                call: Call<T>,
                t: Throwable
            ) {

                if (call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) return

                onError(
                    "No se pudo conectar con el servidor"
                )
            }
        })
    }

    // OPCIONES DEL PERFIL

    private fun configurarOpciones() {

        findViewById<CardView>(
            R.id.cardPersonalInfo
        ).setOnClickListener {

            startActivity(
                Intent(this, PersonalInfoActivity::class.java)
            )
        }

        findViewById<CardView>(
            R.id.cardDirecciones
        ).setOnClickListener {

            startActivity(
                Intent(this, DireccionActivity::class.java)
            )
        }

        findViewById<CardView>(
            R.id.cardMiscompras
        ).setOnClickListener {

            startActivity(
                Intent(this, ComprasActivity::class.java)
            )
        }

        findViewById<CardView>(
            R.id.cardMisfavoritos
        ).setOnClickListener {

            startActivity(
                Intent(this, FavoritosActivity::class.java)
            )
        }

        findViewById<CardView>(
            R.id.cardAyudaContactoCliente
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AyudaContactoActivity::class.java
                )
            )
        }

        findViewById<MaterialButton>(
            R.id.btnCerrarSesion
        ).setOnClickListener {

            confirmarCierreSesion()
        }
    }

    // NAVEGACIÓN INFERIOR

    private fun configurarNavegacion() {

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNavigation.selectedItemId = R.id.perfil

        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

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

                R.id.perfil -> true

                else -> false
            }
        }
    }

    // CERRAR SESIÓN

    private fun confirmarCierreSesion() {

        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage(
                "¿Estás seguro de que deseas cerrar tu sesión?"
            )
            .setPositiveButton("Sí, cerrar sesión") { _, _ ->

                getSharedPreferences(
                    "MisoftSesion",
                    MODE_PRIVATE
                ).edit().clear().apply()

                val intent = Intent(
                    this,
                    LoginActivity::class.java
                ).apply {

                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }
}
