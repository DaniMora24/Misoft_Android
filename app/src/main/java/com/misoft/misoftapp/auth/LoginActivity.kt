
package com.misoft.misoftapp.auth

import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.util.Base64
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.home.HomeBuyerActivity
import com.misoft.misoftapp.home.HomeAdminActivity
import com.misoft.misoftapp.home.HomeVendedorActivity
import com.misoft.misoftapp.models.LoginRequest
import com.misoft.misoftapp.models.LoginResponse
import com.misoft.misoftapp.models.RegistrarBiometriaRequest
import com.misoft.misoftapp.models.RegistrarBiometriaResponse
import com.misoft.misoftapp.models.SolicitarDesafioRequest
import com.misoft.misoftapp.models.DesafioBiometricoResponse
import com.misoft.misoftapp.models.AutenticarBiometriaRequest

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val preferencias by lazy {
        getSharedPreferences("MisoftBiometria", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val textViewRegister =
            findViewById<TextView>(R.id.txtRegistro)

        val txtOlvide =
            findViewById<TextView>(R.id.txtOlvide)

        val btnLogin =
            findViewById<Button>(R.id.btnLogin)

        val btnBiometria =
            findViewById<Button>(R.id.btnBiometria)

        val etCorreo =
            findViewById<EditText>(R.id.etCorreo)

        val etPassword =
            findViewById<EditText>(R.id.etPassword)

        // Mostrar el botón biométrico si existe un dispositivo registrado.
        val idDispositivo = preferencias.getInt("idDispositivo", -1)

        if (idDispositivo != -1 && BiometricKeyManager.existeClave()) {
            btnBiometria.visibility = View.VISIBLE
        } else {
            btnBiometria.visibility = View.GONE

            if (idDispositivo != -1) {
                preferencias.edit()
                    .remove("idDispositivo")
                    .remove("email")
                    .apply()
            }
        }

        // Ir a Registro
        textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Inicio de sesión tradicional
        btnLogin.setOnClickListener {

            val email = etCorreo.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Debe completar todos los campos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val loginRequest = LoginRequest(
                email = email,
                password = password
            )

            ApiClient.authApi.login(loginRequest)
                .enqueue(object : Callback<LoginResponse> {

                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {

                        if (response.isSuccessful && response.body() != null) {

                            val usuario = response.body()!!

                            Toast.makeText(
                                this@LoginActivity,
                                "Bienvenido ${usuario.nombreCompleto}",
                                Toast.LENGTH_SHORT
                            ).show()

                            val dispositivoRegistrado =
                                preferencias.getInt("idDispositivo", -1)

                            // Si ya tiene biometría registrada, continuar.
                            if (dispositivoRegistrado != -1) {
                                preferencias.edit()
                                    .putString("email", email)
                                    .apply()

                                navegarAlInicio(usuario)
                                return
                            }

                            // Verificar disponibilidad biométrica.
                            val biometricManager =
                                BiometricPromptManager(this@LoginActivity)

                            if (!biometricManager.verificarDisponibilidad()) {
                                navegarAlInicio(usuario)
                                return
                            }

                            // Preguntar si desea activar la biometría.
                            AlertDialog.Builder(this@LoginActivity)
                                .setTitle("Activar acceso biométrico")
                                .setMessage(
                                    "¿Deseas utilizar tu huella o biometría " +
                                            "para iniciar sesión más rápidamente " +
                                            "en este dispositivo?"
                                )
                                .setPositiveButton("Activar") { _, _ ->
                                    registrarBiometria(
                                        email,
                                        password,
                                        usuario
                                    )
                                }
                                .setNegativeButton("Ahora no") { _, _ ->
                                    navegarAlInicio(usuario)
                                }
                                .setCancelable(false)
                                .show()

                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Credenciales incorrectas",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<LoginResponse>,
                        t: Throwable
                    ) {
                        t.printStackTrace()

                        Toast.makeText(
                            this@LoginActivity,
                            "Error de conexión: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }

        // Inicio de sesión biométrico
        btnBiometria.setOnClickListener {

            val dispositivo = preferencias.getInt("idDispositivo", -1)

            if (dispositivo == -1 || !BiometricKeyManager.existeClave()) {
                Toast.makeText(
                    this,
                    "No hay un acceso biométrico registrado",
                    Toast.LENGTH_LONG
                ).show()

                btnBiometria.visibility = View.GONE
                return@setOnClickListener
            }

            autenticarConBiometria(dispositivo)
        }

        // Ir a Olvidé Contraseña
        txtOlvide.setOnClickListener {
            val intent = Intent(this, ForgetPassword::class.java)
            startActivity(intent)
        }
    }

    /*
     * Registra la clave pública del dispositivo en el backend.
     */
    private fun registrarBiometria(
        email: String,
        password: String,
        usuario: LoginResponse
    ) {
        try {

            val clavePublica =
                BiometricKeyManager.obtenerClavePublicaBase64()

            val request = RegistrarBiometriaRequest(
                email = email,
                password = password,
                clavePublica = clavePublica,
                nombreDispositivo = Build.MODEL
            )

            ApiClient.authApi.registrarBiometria(request)
                .enqueue(object : Callback<RegistrarBiometriaResponse> {

                    override fun onResponse(
                        call: Call<RegistrarBiometriaResponse>,
                        response: Response<RegistrarBiometriaResponse>
                    ) {

                        if (response.isSuccessful && response.body() != null) {

                            val resultado = response.body()!!

                            preferencias.edit()
                                .putInt(
                                    "idDispositivo",
                                    resultado.idDispositivo
                                )
                                .putString("email", email)
                                .apply()

                            Toast.makeText(
                                this@LoginActivity,
                                "Acceso biométrico activado correctamente",
                                Toast.LENGTH_LONG
                            ).show()

                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "No fue posible registrar la biometría",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        navegarAlInicio(usuario)
                    }

                    override fun onFailure(
                        call: Call<RegistrarBiometriaResponse>,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error al registrar biometría: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        navegarAlInicio(usuario)
                    }
                })

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Error al preparar la biometría: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            navegarAlInicio(usuario)
        }
    }

    /*
     * Solicita un desafío al backend y muestra la autenticación biométrica.
     */
    private fun autenticarConBiometria(idDispositivo: Int) {

        val request = SolicitarDesafioRequest(
            idDispositivo = idDispositivo
        )

        ApiClient.authApi.solicitarDesafio(request)
            .enqueue(object : Callback<DesafioBiometricoResponse> {

                override fun onResponse(
                    call: Call<DesafioBiometricoResponse>,
                    response: Response<DesafioBiometricoResponse>
                ) {

                    if (!response.isSuccessful || response.body() == null) {
                        Toast.makeText(
                            this@LoginActivity,
                            "No fue posible iniciar la autenticación biométrica",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val desafio = response.body()!!

                    try {

                        val nonceBytes = Base64.decode(
                            desafio.nonce,
                            Base64.DEFAULT
                        )

                        val signature =
                            BiometricKeyManager.prepararFirma()

                        val biometricPromptManager =
                            BiometricPromptManager(this@LoginActivity)

                        biometricPromptManager.autenticar(
                            signature = signature,

                            onSuccess = { firmaAutenticada ->

                                try {

                                    firmaAutenticada.update(nonceBytes)

                                    val firmaBytes =
                                        firmaAutenticada.sign()

                                    val firmaBase64 =
                                        Base64.encodeToString(
                                            firmaBytes,
                                            Base64.NO_WRAP
                                        )

                                    enviarFirmaAlBackend(
                                        desafio.idDesafio,
                                        idDispositivo,
                                        firmaBase64
                                    )

                                } catch (e: Exception) {

                                    runOnUiThread {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Error al generar la firma: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },

                            onError = { mensaje ->

                                Toast.makeText(
                                    this@LoginActivity,
                                    mensaje,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )

                    } catch (e: Exception) {

                        Toast.makeText(
                            this@LoginActivity,
                            "Error al preparar la autenticación: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<DesafioBiometricoResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error al solicitar el desafío: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    /*
     * Envía la firma digital al backend para validar la identidad.
     */
    private fun enviarFirmaAlBackend(
        idDesafio: Int,
        idDispositivo: Int,
        firma: String
    ) {

        val request = AutenticarBiometriaRequest(
            idDesafio = idDesafio,
            idDispositivo = idDispositivo,
            firma = firma
        )

        ApiClient.authApi.autenticarBiometria(request)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {

                    if (response.isSuccessful && response.body() != null) {

                        val usuario = response.body()!!

                        Toast.makeText(
                            this@LoginActivity,
                            "Bienvenido ${usuario.nombreCompleto}",
                            Toast.LENGTH_SHORT
                        ).show()

                        navegarAlInicio(usuario)

                    } else {

                        Toast.makeText(
                            this@LoginActivity,
                            "No fue posible validar la autenticación biométrica",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<LoginResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    /*
     * Conserva la navegación original según el rol.
     */
    private fun navegarAlInicio(usuario: LoginResponse) {


        // Guardar la sesión del usuario
        getSharedPreferences("MisoftSesion", MODE_PRIVATE)
            .edit()
            .putInt("idUsuario", usuario.idUsuario)
            .putString("nombreCompleto", usuario.nombreCompleto)
            .putString("email", usuario.email)
            .putInt("rolId", usuario.rolId)
            .apply()


        when (usuario.rolId) {

            1 -> {
                startActivity(
                    Intent(
                        this,
                        HomeAdminActivity::class.java
                    )
                )
            }

            2 -> {
                startActivity(
                    Intent(
                        this,
                        HomeVendedorActivity::class.java
                    )
                )
            }

            3 -> {
                startActivity(
                    Intent(
                        this,
                        HomeBuyerActivity::class.java
                    )
                )
            }

            else -> {
                Toast.makeText(
                    this,
                    "No se reconoce el rol del usuario",
                    Toast.LENGTH_LONG
                ).show()

                return
            }
        }

        finish()
    }
}
