package com.misoft.misoftapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.misoft.misoftapp.R
import android.widget.TextView
import android.widget.*
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.models.RegisterRequest
import com.misoft.misoftapp.models.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.DatePickerDialog
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val txtIrLogin =
            findViewById<TextView>(
                R.id.txtIrLogin
            )

        val etNombres = findViewById<EditText>(R.id.etNombres)
        val etApellidos = findViewById<EditText>(R.id.etApellidos)
        val etNumeroDocumento = findViewById<EditText>(R.id.etNumeroDocumento)
        val etFechaNacimiento = findViewById<EditText>(R.id.etFechaNacimiento)


        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)

        val spTipoDocumento =
            findViewById<AutoCompleteTextView>(R.id.spTipoDocumento)

        val btnCrearCuenta =
            findViewById<Button>(R.id.btnCrearCuenta)

        val tiposDocumento = listOf(
            "Cedula Ciudadania",
            "Cedula Extranjeria",
            "Pasaporte",
            "Tarjeta Identidad"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            tiposDocumento
        )

        spTipoDocumento.setAdapter(adapter)

        //Metodo para la fecha de nacimiento:
        etFechaNacimiento.setOnClickListener {

            val calendar = Calendar.getInstance()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->

                    val fecha =
                        String.format(
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                        )

                    etFechaNacimiento.setText(fecha)

                },
                year,
                month,
                day
            )

            datePicker.datePicker.maxDate =
                System.currentTimeMillis()

            datePicker.show()
        }


        //Boton de crear cuenta
        btnCrearCuenta.setOnClickListener {

            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val tipoDocumento = spTipoDocumento.text.toString().trim()
            val numeroDocumento = etNumeroDocumento.text.toString().trim()
            val fechaNacimiento = etFechaNacimiento.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (nombres.isEmpty()) {
                etNombres.error = "Ingrese sus nombres"
                etNombres.requestFocus()
                return@setOnClickListener
            }

            if (!nombres.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
                etNombres.error = "Ingrese solo letras"
                etNombres.requestFocus()
                return@setOnClickListener
            }

            if (apellidos.isEmpty()) {
                etApellidos.error = "Ingrese sus apellidos"
                etApellidos.requestFocus()
                return@setOnClickListener
            }

            if (!apellidos.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
                etApellidos.error = "Ingrese solo letras"
                etApellidos.requestFocus()
                return@setOnClickListener
            }

            if (tipoDocumento.isEmpty()) {
                Toast.makeText(
                    this,
                    "Seleccione un tipo de documento",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (numeroDocumento.isEmpty()) {
                etNumeroDocumento.error = "Ingrese su documento"
                etNumeroDocumento.requestFocus()
                return@setOnClickListener
            }

            if (numeroDocumento.length < 6) {
                etNumeroDocumento.error = "Documento inválido"
                etNumeroDocumento.requestFocus()
                return@setOnClickListener
            }

            if (fechaNacimiento.isEmpty()) {
                etFechaNacimiento.error = "Seleccione una fecha"
                etFechaNacimiento.requestFocus()
                return@setOnClickListener
            }

            if (telefono.isEmpty()) {
                etTelefono.error = "Ingrese su teléfono"
                etTelefono.requestFocus()
                return@setOnClickListener
            }

            if (telefono.length != 10) {
                etTelefono.error = "El teléfono debe tener 10 dígitos"
                etTelefono.requestFocus()
                return@setOnClickListener
            }

            if (!telefono.matches(Regex("^[0-9]+$"))) {
                etTelefono.error = "Ingrese solo números"
                etTelefono.requestFocus()
                return@setOnClickListener
            }

            if (correo.isEmpty()) {
                etCorreo.error = "Ingrese un correo"
                etCorreo.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(correo)
                    .matches()
            ) {

                etCorreo.error = "Correo inválido"
                etCorreo.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Ingrese una contraseña"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 8) {
                etPassword.error =
                    "La contraseña debe tener mínimo 8 caracteres"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                etConfirmPassword.error =
                    "Confirme la contraseña"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {

                etConfirmPassword.error =
                    "Las contraseñas no coinciden"

                etConfirmPassword.requestFocus()

                return@setOnClickListener
            }

            val request = RegisterRequest(
                tipoDocumento = tipoDocumento,
                numeroDocumento = numeroDocumento,
                nombres = nombres,
                apellidos = apellidos,
                telefono = telefono,
                fechaNacimiento = fechaNacimiento,
                email = correo,
                password = password
            )

            ApiClient.authApi.register(request)
                .enqueue(object : Callback<RegisterResponse> {

                    override fun onResponse(
                        call: Call<RegisterResponse>,
                        response: Response<RegisterResponse>
                    ) {

                        if (response.isSuccessful) {

                            Toast.makeText(
                                this@RegisterActivity,
                                "Usuario registrado correctamente",
                                Toast.LENGTH_LONG
                            ).show()

                            startActivity(
                                Intent(
                                    this@RegisterActivity,
                                    LoginActivity::class.java
                                )
                            )

                            finish()
                        }
                        else {

                            Toast.makeText(
                                this@RegisterActivity,
                                "No fue posible registrar el usuario",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<RegisterResponse>,
                        t: Throwable
                    ) {

                        Toast.makeText(
                            this@RegisterActivity,
                            t.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }



        // Ir a Inicio de sesopm

        txtIrLogin.setOnClickListener {

            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            startActivity(intent)

        }




    }
}