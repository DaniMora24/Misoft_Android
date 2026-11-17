
package com.misoft.misoftapp.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject

import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgetPassword : AppCompatActivity() {

    // COMPONENTES DE LA INTERFAZ

    private lateinit var etCorreo: TextInputEditText
    private lateinit var etCodigo: TextInputEditText
    private lateinit var etNuevaPassword: TextInputEditText
    private lateinit var etConfirmarPassword: TextInputEditText

    private lateinit var tilCorreo: TextInputLayout
    private lateinit var tilCodigo: TextInputLayout
    private lateinit var tilNuevaPassword: TextInputLayout
    private lateinit var tilConfirmarPassword: TextInputLayout

    private lateinit var btnEnviar: MaterialButton
    private lateinit var btnRestablecer: MaterialButton

    private lateinit var layoutRestablecer: LinearLayout
    private lateinit var txtVolver: TextView

    private var correoSolicitado = ""

    private var enviandoCodigo = false
    private var restableciendo = false

    private var llamadaCodigo: Call<JsonObject>? = null
    private var llamadaRestablecer: Call<JsonObject>? = null

    // INICIAR PANTALLA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forget_password)

        inicializarComponentes()

        configurarEventos()
    }

    // REFERENCIAR COMPONENTES XML

    private fun inicializarComponentes() {

        etCorreo = findViewById(R.id.etCorreo)

        etCodigo = findViewById(R.id.etCodigo)

        etNuevaPassword = findViewById(
            R.id.etNuevaPassword
        )

        etConfirmarPassword = findViewById(
            R.id.etConfirmarPassword
        )

        tilCorreo = findViewById(R.id.tilCorreo)

        tilCodigo = findViewById(R.id.tilCodigo)

        tilNuevaPassword = findViewById(
            R.id.tilNuevaPassword
        )

        tilConfirmarPassword = findViewById(
            R.id.tilConfirmarPassword
        )

        btnEnviar = findViewById(R.id.btnEnviar)

        btnRestablecer = findViewById(
            R.id.btnRestablecer
        )

        layoutRestablecer = findViewById(
            R.id.layoutRestablecer
        )

        txtVolver = findViewById(R.id.txtVolver)

        // Inicialmente solo se solicita el correo
        layoutRestablecer.visibility = View.GONE
    }

    // CONFIGURAR BOTONES

    private fun configurarEventos() {

        btnEnviar.setOnClickListener {
            solicitarCodigo()
        }

        btnRestablecer.setOnClickListener {
            restablecerPassword()
        }

        txtVolver.setOnClickListener {
            finish()
        }
    }

    // SOLICITAR CÓDIGO POR CORREO

    private fun solicitarCodigo() {

        if (enviandoCodigo || restableciendo) return

        val correo = etCorreo.text
            ?.toString()
            ?.trim()
            .orEmpty()

        tilCorreo.error = null

        // VALIDAR CORREO VACÍO
        if (correo.isBlank()) {

            tilCorreo.error =
                "Ingresa tu correo electrónico"

            return
        }

        // VALIDAR FORMATO DEL CORREO
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

            tilCorreo.error =
                "Ingresa un correo electrónico válido"

            return
        }

        val request = JsonObject().apply {
            addProperty("email", correo)
        }

        enviandoCodigo = true
        btnEnviar.isEnabled = false
        etCorreo.isEnabled = false
        btnEnviar.text = "Enviando código..."

        val llamada = ApiClient.authApi
            .recuperarPassword(request)

        llamadaCodigo = llamada

        llamada.enqueue(object : Callback<JsonObject> {

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {

                if (isFinishing || isDestroyed) return

                enviandoCodigo = false
                btnEnviar.isEnabled = true
                etCorreo.isEnabled = true
                btnEnviar.text = "Reenviar código"

                if (response.isSuccessful) {

                    // Guardamos el correo utilizado
                    correoSolicitado = correo

                    // Mostrar formulario de nueva contraseña
                    layoutRestablecer.visibility = View.VISIBLE

                    mostrarMensaje(
                        "Si el correo está registrado, " +
                                "recibirás un código de recuperación."
                    )

                } else {

                    btnEnviar.text = "Enviar código"

                    mostrarMensaje(
                        when (response.code()) {
                            400 -> "Verifica el correo ingresado"
                            429 -> "Demasiadas solicitudes. " +
                                    "Intenta más tarde"
                            else -> "No se pudo procesar la solicitud. " +
                                    "HTTP ${response.code()}"
                        }
                    )
                }
            }

            override fun onFailure(
                call: Call<JsonObject>,
                t: Throwable
            ) {

                if (call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) return

                enviandoCodigo = false
                btnEnviar.isEnabled = true
                etCorreo.isEnabled = true
                btnEnviar.text = "Reenviar código"

                mostrarMensaje(
                    "No se pudo conectar con el servidor. " +
                            "Verifica tu conexión e intenta nuevamente."
                )
            }
        })
    }

    // RESTABLECER CONTRASEÑA

    private fun restablecerPassword() {

        if (restableciendo || enviandoCodigo) return

        val codigo = etCodigo.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val nuevaPassword = etNuevaPassword.text
            ?.toString()
            .orEmpty()

        val confirmarPassword = etConfirmarPassword.text
            ?.toString()
            .orEmpty()

        tilCodigo.error = null
        tilNuevaPassword.error = null
        tilConfirmarPassword.error = null

        // VALIDAR QUE SE HAYA SOLICITADO EL CÓDIGO
        if (correoSolicitado.isBlank()) {

            mostrarMensaje(
                "Primero solicita el código de recuperación"
            )

            return
        }

        // VALIDAR CÓDIGO
        if (codigo.length != 8 ||
            !codigo.all { it in '0'..'9' }) {

            tilCodigo.error =
                "El código debe contener 8 dígitos"

            return
        }

        // VALIDAR NUEVA CONTRASEÑA
        if (nuevaPassword.length < 8) {

            tilNuevaPassword.error =
                "La contraseña debe tener mínimo 8 caracteres"

            return
        }

        // CONFIRMAR CONTRASEÑA
        if (nuevaPassword != confirmarPassword) {

            tilConfirmarPassword.error =
                "Las contraseñas no coinciden"

            return
        }

        val request = JsonObject().apply {

            addProperty(
                "email",
                correoSolicitado
            )

            addProperty(
                "codigo",
                codigo
            )

            addProperty(
                "nuevaPassword",
                nuevaPassword
            )
        }

        restableciendo = true
        btnRestablecer.isEnabled = false
        btnEnviar.isEnabled = false
        btnRestablecer.text = "Actualizando contraseña..."

        val llamada = ApiClient.authApi
            .restablecerPassword(request)

        llamadaRestablecer = llamada

        llamada.enqueue(object : Callback<JsonObject> {

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {

                if (isFinishing || isDestroyed) return

                restableciendo = false
                btnRestablecer.isEnabled = true
                btnEnviar.isEnabled = true
                btnRestablecer.text = "Restablecer contraseña"

                if (response.isSuccessful) {

                    mostrarMensaje(
                        "Contraseña actualizada correctamente"
                    )

                    finish()

                } else {

                    val mensaje = when (response.code()) {

                        400 ->
                            "El código es incorrecto, " +
                                    "ha expirado o se agotaron los intentos."

                        429 ->
                            "Demasiados intentos. " +
                                    "Intenta más tarde."

                        else ->
                            "No se pudo actualizar la contraseña. " +
                                    "HTTP ${response.code()}"
                    }

                    mostrarMensaje(mensaje)
                }
            }

            override fun onFailure(
                call: Call<JsonObject>,
                t: Throwable
            ) {

                if (call.isCanceled ||
                    isFinishing ||
                    isDestroyed
                ) return

                restableciendo = false
                btnRestablecer.isEnabled = true
                btnEnviar.isEnabled = true
                btnRestablecer.text = "Restablecer contraseña"

                mostrarMensaje(
                    "No se pudo confirmar el cambio. " +
                            "Verifica la conexión antes de reintentar."
                )
            }
        })
    }

    // MOSTRAR MENSAJES

    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
    // LIBERAR SOLICITUDES

    override fun onDestroy() {

        llamadaCodigo?.cancel()
        llamadaRestablecer?.cancel()

        super.onDestroy()
    }
}
