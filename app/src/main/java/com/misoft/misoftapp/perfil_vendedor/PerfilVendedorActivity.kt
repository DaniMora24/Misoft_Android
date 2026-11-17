
package com.misoft.misoftapp.perfil_vendedor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.catalogo.ProductosVendedorActivity
import com.misoft.misoftapp.estadisticas.EstadisticasVendedorActivity
import com.misoft.misoftapp.home.HomeVendedorActivity
import com.misoft.misoftapp.pedidos.GestionPedidosActivity
import com.misoft.misoftapp.auth.LoginActivity
import java.io.ByteArrayOutputStream
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.bottomsheet.BottomSheetDialog

class PerfilVendedorActivity : AppCompatActivity() {

    private lateinit var imgPerfil: ImageView
    private lateinit var txtNombre: TextView
    private lateinit var txtCorreo: TextView

    private var llamadaPerfil: Call<JsonObject>? = null
    private var llamadaFoto: Call<JsonObject>? = null
    private var subiendoFoto = false
    private val whatsappTienda = ""
    private val instagramTienda = ""
    private val horarioWhatsapp = ""

    private val seleccionarFoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            subirFotoPerfil(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.vendedor_activity_perfil)

        imgPerfil = findViewById(R.id.imgPerfilTienda)
        txtNombre = findViewById(R.id.txtNombreTienda)
        txtCorreo = findViewById(R.id.txtCorreoTienda)

        mostrarDatosVendedor()
        configurarAcciones()
        configurarNavegacion()
    }

    override fun onResume() {
        super.onResume()
        cargarFotoPerfil()
    }

    // DATOS DE LA SESIÓN

    private fun obtenerIdVendedor(): Int {
        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val idUsuario = preferencias.getInt("idUsuario", -1)
        val rolId = preferencias.getInt("rolId", -1)

        return if (rolId == 2) idUsuario else -1
    }

    private fun mostrarDatosVendedor() {
        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val nombre = preferencias.getString(
            "nombreCompleto",
            ""
        ).orEmpty()

        val correo = preferencias.getString(
            "email",
            ""
        ).orEmpty()

        txtNombre.text = nombre.ifBlank {
            "Vendedor"
        }

        txtCorreo.text = correo.ifBlank {
            "Correo no disponible"
        }
    }


    private fun mostrarInformacionTienda() {

        val dialogo = BottomSheetDialog(this)

        val vista = layoutInflater.inflate(
            R.layout.vendedor_bottom_contacto,
            null
        )

        val preferencias = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        )

        val nombre = preferencias.getString(
            "nombreCompleto",
            ""
        ).orEmpty()

        val correo = preferencias.getString(
            "email",
            ""
        ).orEmpty()

        val txtNombre = vista.findViewById<TextView>(
            R.id.txtContactoNombre
        )

        val txtCorreo = vista.findViewById<TextView>(
            R.id.txtContactoCorreo
        )

        val txtWhatsapp = vista.findViewById<TextView>(
            R.id.txtContactoWhatsapp
        )

        val txtHorario = vista.findViewById<TextView>(
            R.id.txtHorarioWhatsapp
        )

        val txtInstagram = vista.findViewById<TextView>(
            R.id.txtInstagramTienda
        )

        val btnWhatsapp = vista.findViewById<MaterialButton>(
            R.id.btnAbrirWhatsapp
        )

        val btnInstagram = vista.findViewById<MaterialButton>(
            R.id.btnAbrirInstagram
        )

        val btnCerrar = vista.findViewById<MaterialButton>(
            R.id.btnCerrarContacto
        )

        // Información de la cuenta

        txtNombre.text =
            "👤 Responsable: ${nombre.ifBlank { "No disponible" }}"

        txtCorreo.text =
            "✉️ Correo: ${correo.ifBlank { "No disponible" }}"

        // WhatsApp

        val numero = whatsappTienda.trim()

        val numeroValido =
            numero.matches(Regex("[1-9][0-9]{7,14}"))

        txtWhatsapp.text =
            if (numeroValido) {
                "📱 WhatsApp: +$numero"
            } else {
                "📱 WhatsApp: No configurado"
            }

        btnWhatsapp.isEnabled = numeroValido

        btnWhatsapp.setOnClickListener {
            if (numeroValido) {
                abrirEnlace(
                    "https://wa.me/$numero"
                )
            }
        }

        // Horarios de atención

        txtHorario.text =
            horarioWhatsapp.ifBlank {
                "Horario de WhatsApp por configurar"
            }

        // Instagram

        val usuarioInstagram = instagramTienda
            .trim()
            .removePrefix("@")

        val instagramValido = usuarioInstagram.matches(
            Regex("[A-Za-z0-9._]{1,30}")
        )

        txtInstagram.text =
            if (instagramValido) {
                "Instagram: @$usuarioInstagram"
            } else {
                "Instagram por configurar"
            }

        btnInstagram.isEnabled = instagramValido

        btnInstagram.setOnClickListener {
            if (instagramValido) {
                abrirEnlace(
                    "https://www.instagram.com/$usuarioInstagram/"
                )
            }
        }

        btnCerrar.setOnClickListener {
            dialogo.dismiss()
        }

        dialogo.setContentView(vista)
        dialogo.show()
    }

// Abrir enlaces externos de forma segura

    private fun abrirEnlace(url: String) {

        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )

            startActivity(intent)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "No se pudo abrir el enlace.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // CARGAR FOTO DESDE LA API

    private fun cargarFotoPerfil() {
        val idVendedor = obtenerIdVendedor()
        if (idVendedor <= 0) return

        llamadaPerfil?.cancel()

        val llamada = ApiClient.perfilApi
            .obtenerPerfil(idVendedor)

        llamadaPerfil = llamada

        llamada.enqueue(object : Callback<JsonObject> {

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                if (call.isCanceled ||
                    isFinishing ||
                    isDestroyed) return

                if (!response.isSuccessful) {
                    Log.e(
                        "PERFIL_VENDEDOR",
                        "Error al cargar perfil: HTTP ${response.code()}"
                    )
                    return
                }

                val datos = response.body() ?: return

                val fotoUrl = datos.get("fotoUrl")
                    ?.takeUnless { it.isJsonNull }
                    ?.asString.orEmpty()

                if (fotoUrl.isBlank()) {
                    Glide.with(this@PerfilVendedorActivity)
                        .clear(imgPerfil)

                    imgPerfil.setImageResource(
                        R.drawable.usuario
                    )
                    return
                }

                Glide.with(this@PerfilVendedorActivity)
                    .load(obtenerUrlImagen(fotoUrl))
                    .placeholder(R.drawable.usuario)
                    .error(R.drawable.usuario)
                    .circleCrop()
                    .into(imgPerfil)
            }

            override fun onFailure(
                call: Call<JsonObject>,
                t: Throwable
            ) {
                if (call.isCanceled ||
                    isFinishing ||
                    isDestroyed) return

                Log.e(
                    "PERFIL_VENDEDOR",
                    "No se pudo cargar la fotografía",
                    t
                )
            }
        })
    }

    private fun obtenerUrlImagen(ruta: String): String {
        val servidor = "http://192.168.78.111:5271"

        if (ruta.startsWith("http://") ||
            ruta.startsWith("https://")) {
            return ruta
        }

        return servidor + "/" + ruta.trimStart('/')
    }

    // SUBIR FOTO A LA API

    private fun subirFotoPerfil(uri: Uri) {
        val idVendedor = obtenerIdVendedor()

        if (idVendedor <= 0) {
            mostrarMensaje("Inicia sesión como vendedor.")
            return
        }

        if (subiendoFoto) {
            mostrarMensaje("Ya se está subiendo una fotografía.")
            return
        }

        subiendoFoto = true

        Thread {
            try {
                val tipo = contentResolver.getType(uri)
                    ?: "application/octet-stream"

                if (tipo !in listOf(
                        "image/jpeg",
                        "image/png",
                        "image/webp"
                    )
                ) {
                    throw IllegalArgumentException(
                        "Selecciona una imagen JPG, PNG o WEBP."
                    )
                }

                val limite = 5 * 1024 * 1024

                val datos = contentResolver
                    .openInputStream(uri)
                    ?.use { entrada ->
                        val salida = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var total = 0

                        while (true) {
                            val leidos = entrada.read(buffer)

                            if (leidos == -1) break

                            total += leidos

                            if (total > limite) {
                                throw IllegalArgumentException(
                                    "La imagen supera los 5 MB."
                                )
                            }

                            salida.write(buffer, 0, leidos)
                        }

                        salida.toByteArray()
                    }
                    ?: throw IllegalArgumentException(
                        "No fue posible abrir la imagen."
                    )

                if (datos.isEmpty()) {
                    throw IllegalArgumentException(
                        "La imagen está vacía."
                    )
                }

                val extension = when (tipo) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    else -> "jpg"
                }

                val archivo = MultipartBody.Part.createFormData(
                    "foto",
                    "perfil.$extension",
                    datos.toRequestBody(tipo.toMediaType())
                )

                val cuerpoId = idVendedor.toString()
                    .toRequestBody("text/plain".toMediaType())

                runOnUiThread {
                    if (isFinishing || isDestroyed) {
                        subiendoFoto = false
                        return@runOnUiThread
                    }

                    mostrarMensaje("Subiendo fotografía...")

                    val llamada = ApiClient.perfilApi
                        .cambiarFoto(cuerpoId, archivo)

                    llamadaFoto = llamada

                    llamada.enqueue(object : Callback<JsonObject> {

                        override fun onResponse(
                            call: Call<JsonObject>,
                            response: Response<JsonObject>
                        ) {
                            subiendoFoto = false

                            if (call.isCanceled ||
                                isFinishing ||
                                isDestroyed) return

                            if (response.isSuccessful) {
                                mostrarMensaje(
                                    "Fotografía actualizada correctamente."
                                )
                                cargarFotoPerfil()
                            } else {
                                mostrarMensaje(
                                    "No se pudo actualizar la foto. " +
                                            "HTTP ${response.code()}"
                                )
                            }
                        }

                        override fun onFailure(
                            call: Call<JsonObject>,
                            t: Throwable
                        ) {
                            subiendoFoto = false

                            if (call.isCanceled ||
                                isFinishing ||
                                isDestroyed) return

                            Log.e(
                                "PERFIL_VENDEDOR",
                                "Error al subir fotografía",
                                t
                            )

                            mostrarMensaje(
                                "No se pudo confirmar la subida. " +
                                        "Consulta tu perfil antes de reintentar."
                            )
                        }
                    })
                }

            } catch (e: Exception) {
                runOnUiThread {
                    subiendoFoto = false

                    if (!isFinishing && !isDestroyed) {
                        mostrarMensaje(
                            e.message ?: "No se pudo procesar la imagen."
                        )
                    }
                }
            }
        }.start()
    }

    // ACCIONES DEL PERFIL

    private fun configurarAcciones() {

        findViewById<FrameLayout>(
            R.id.contenedorFotoPerfilVendedor
        ).setOnClickListener {
            seleccionarFoto.launch("image/*")
        }

        findViewById<TextView>(
            R.id.txtCambiarFotoVendedor
        ).setOnClickListener {
            seleccionarFoto.launch("image/*")
        }

        findViewById<CardView>(
            R.id.cardInfoCuentaVendedor
        ).setOnClickListener {
            mostrarInformacionTienda()
        }

        findViewById<CardView>(
            R.id.cardEstadisticasPerfilVendedor
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    EstadisticasVendedorActivity::class.java
                )
            )
        }

        findViewById<MaterialButton>(
            R.id.btnCerrarSesionVendedor
        ).setOnClickListener {
            confirmarCerrarSesion()
        }
    }

    // CERRAR SESIÓN

    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage(
                "¿Deseas cerrar tu sesión de vendedor?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Cerrar sesión") { _, _ ->
                cerrarSesion()
            }
            .show()
    }

    private fun cerrarSesion() {
        llamadaPerfil?.cancel()
        llamadaFoto?.cancel()

        getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        ).edit().clear().apply()

        val intent = Intent(
            this,
            LoginActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    // MENÚ INFERIOR

    private fun configurarNavegacion() {
        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationSeller
            )

        bottomNavigation.selectedItemId = R.id.perfil

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

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

                R.id.perfil -> true

                else -> false
            }
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        llamadaPerfil?.cancel()
        llamadaFoto?.cancel()
        super.onDestroy()
    }
}
