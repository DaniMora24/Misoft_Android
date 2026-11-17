package com.misoft.misoftapp.usuarios

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.models.UsuarioAdminResponse
import com.misoft.misoftapp.estadisticas.ReportesAdminActivity
import com.misoft.misoftapp.home.HomeAdminActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.JsonObject

class UsuariosAdminActivity : AppCompatActivity() {

    private lateinit var contenedorUsuarios: LinearLayout
    private lateinit var edtBuscarUsuario: TextInputEditText

    private var usuarios = listOf<UsuarioAdminResponse>()

    private var historialCargado = false

    private var llamadaUsuarios:
            Call<List<UsuarioAdminResponse>>? = null

    private var llamadaDetalle:
            Call<UsuarioAdminResponse>? = null

    // INICIAR PANTALLA
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.admin_activity_usuarios)

        contenedorUsuarios = findViewById(
            R.id.contenedorUsuarios
        )

        edtBuscarUsuario = findViewById(
            R.id.edtBuscarUsuario
        )

        configurarBuscador()
        configurarNavegacion()
    }

    override fun onResume() {
        super.onResume()
        cargarUsuarios()
    }

    // CONSULTAR USUARIOS DESDE MYSQL

    private fun cargarUsuarios() {

        llamadaUsuarios?.cancel()

        historialCargado = false

        mostrarMensajeLista("Cargando usuarios...")

        val llamada = ApiClient.usuariosAdminApi
            .obtenerUsuarios()

        llamadaUsuarios = llamada

        llamada.enqueue(
            object : Callback<List<UsuarioAdminResponse>> {

                override fun onResponse(
                    call: Call<List<UsuarioAdminResponse>>,
                    response: Response<List<UsuarioAdminResponse>>
                ) {

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    if (response.isSuccessful) {

                        usuarios = response.body().orEmpty()

                        historialCargado = true

                        mostrarUsuariosFiltrados()

                    } else {

                        mostrarMensajeLista(
                            "No se pudieron consultar los usuarios " +
                                    "(HTTP ${response.code()})"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<UsuarioAdminResponse>>,
                    t: Throwable
                ) {

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    mostrarMensajeLista(
                        "No se pudo conectar con el servidor"
                    )
                }
            }
        )
    }

    // BUSCADOR DE USUARIOS

    private fun configurarBuscador() {

        edtBuscarUsuario.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    if (historialCargado) {
                        mostrarUsuariosFiltrados()
                    }
                }

                override fun afterTextChanged(
                    s: Editable?
                ) = Unit
            }
        )
    }

    // FILTRAR USUARIOS

    private fun mostrarUsuariosFiltrados() {

        contenedorUsuarios.removeAllViews()

        val busqueda = edtBuscarUsuario.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val filtrados = usuarios.filter { usuario ->

            val nombreCompleto = (
                    "${usuario.nombres.orEmpty()} " +
                            usuario.apellidos.orEmpty()
                    ).trim()

            nombreCompleto.contains(
                busqueda,
                ignoreCase = true
            ) ||
                    usuario.email.orEmpty().contains(
                        busqueda,
                        ignoreCase = true
                    ) ||
                    usuario.numeroDocumento.orEmpty().contains(
                        busqueda,
                        ignoreCase = true
                    )
        }

        if (filtrados.isEmpty()) {

            mostrarMensajeLista(
                if (usuarios.isEmpty()) {
                    "Todavía no hay usuarios registrados."
                } else {
                    "No se encontraron usuarios."
                }
            )

            return
        }

        filtrados.forEach { usuario ->
            crearTarjetaUsuario(usuario)
        }
    }

    // TARJETA DE USUARIO

    private fun crearTarjetaUsuario(
        usuario: UsuarioAdminResponse
    ) {

        val tarjeta = MaterialCardView(this).apply {

            radius = dp(14).toFloat()
            cardElevation = dp(2).toFloat()

            setCardBackgroundColor(Color.WHITE)

            isClickable = true
            isFocusable = true
        }

        val fila = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(16),
                dp(15),
                dp(16),
                dp(15)
            )
        }

        val informacion = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val nombre = (
                "${usuario.nombres.orEmpty()} " +
                        usuario.apellidos.orEmpty()
                ).trim().ifBlank {
                "Usuario sin nombre"
            }

        val txtNombre = TextView(this).apply {

            text = "👤 $nombre"

            textSize = 16f

            setTextColor(
                Color.parseColor("#6A4C93")
            )

            setTypeface(null, Typeface.BOLD)
        }

        val txtCorreo = TextView(this).apply {

            text = usuario.email.orEmpty()

            textSize = 13f

            setTextColor(
                Color.parseColor("#777777")
            )

            maxLines = 1

            ellipsize =
                android.text.TextUtils.TruncateAt.END
        }

        val txtEstado = TextView(this).apply {

            text = "${nombreRol(usuario.rolId)} • " +
                    if (usuario.estado) "Activo" else "Inactivo"

            textSize = 13f

            setTextColor(
                if (usuario.estado) {
                    Color.parseColor("#2E7D32")
                } else {
                    Color.parseColor("#C62828")
                }
            )
        }

        informacion.addView(txtNombre)
        informacion.addView(txtCorreo)
        informacion.addView(txtEstado)

        fila.addView(
            informacion,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val flecha = TextView(this).apply {

            text = "›"
            textSize = 28f

            setTextColor(
                Color.parseColor("#9B7AE0")
            )
        }

        fila.addView(flecha)

        tarjeta.addView(fila)

        contenedorUsuarios.addView(
            tarjeta,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        )

        tarjeta.setOnClickListener {

            consultarDetalleUsuario(
                usuario.idUsuario
            )
        }
    }

    // CONSULTAR DETALLE DEL USUARIO

    private fun consultarDetalleUsuario(
        idUsuario: Int
    ) {

        if (llamadaDetalle != null) return

        val llamada = ApiClient.usuariosAdminApi
            .obtenerUsuario(idUsuario)

        llamadaDetalle = llamada

        llamada.enqueue(
            object : Callback<UsuarioAdminResponse> {

                override fun onResponse(
                    call: Call<UsuarioAdminResponse>,
                    response: Response<UsuarioAdminResponse>
                ) {

                    llamadaDetalle = null

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    val usuario = response.body()

                    if (response.isSuccessful &&
                        usuario != null) {

                        mostrarDetalleUsuario(usuario)

                    } else {

                        mostrarMensaje(
                            "No se pudo consultar el usuario " +
                                    "(HTTP ${response.code()})"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<UsuarioAdminResponse>,
                    t: Throwable
                ) {

                    llamadaDetalle = null

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    mostrarMensaje(
                        "No se pudo cargar el detalle del usuario"
                    )
                }
            }
        )
    }

    // BOTTOM SHEET DE DETALLE
    private fun mostrarDetalleUsuario(
        usuario: UsuarioAdminResponse
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.admin_bottom_detalle_usuario,
            null
        )

        val nombre = (
                "${usuario.nombres.orEmpty()} " +
                        usuario.apellidos.orEmpty()
                ).trim().ifBlank {
                "Usuario sin nombre"
            }

        view.findViewById<TextView>(
            R.id.txtTituloUsuario
        ).text =
            "Detalle del usuario #${usuario.idUsuario}"

        view.findViewById<TextView>(
            R.id.txtNombreUsuario
        ).text = "👤 $nombre"

        view.findViewById<TextView>(
            R.id.txtRolUsuario
        ).text =
            "Rol: ${nombreRol(usuario.rolId)}"

        val txtEstado = view.findViewById<TextView>(
            R.id.txtEstadoUsuario
        )

        txtEstado.text =
            if (usuario.estado) {
                "✅ Activo"
            } else {
                "⛔ Inactivo"
            }

        txtEstado.setTextColor(
            if (usuario.estado) {
                Color.parseColor("#2E7D32")
            } else {
                Color.parseColor("#C62828")
            }
        )

        view.findViewById<TextView>(
            R.id.txtCorreoUsuario
        ).text =
            "📧 ${usuario.email.orEmpty()}"

        view.findViewById<TextView>(
            R.id.txtTelefonoUsuario
        ).text =
            "📱 " + usuario.telefono.orEmpty()
                .ifBlank {
                    "No registrado"
                }


        view.findViewById<TextView>(
            R.id.txtCiudadUsuario
        ).visibility = View.GONE

        view.findViewById<TextView>(
            R.id.txtUltimoAcceso
        ).visibility = View.GONE

        val fechaRegistro = usuario.fechaRegistro
            ?.take(10)
            .orEmpty()
            .ifBlank {
                "No disponible"
            }

        view.findViewById<TextView>(
            R.id.txtRegistro
        ).text =
            "📅 Registro: $fechaRegistro"

        view.findViewById<TextView>(
            R.id.txtCompras
        ).text =
            "🛒 Compras realizadas: ${usuario.cantidadCompras}"

        val btnActivarUsuario =
            view.findViewById<MaterialButton>(
                R.id.btnActivarUsuario
            )

        val btnBloquearUsuario =
            view.findViewById<MaterialButton>(
                R.id.btnBloquearUsuario
            )

// Consultar el administrador que inició sesión.
        val idAdministrador = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        ).getInt("idUsuario", -1)

// Evitar deshabilitar la cuenta propia desde Android.
        val esCuentaPropia =
            usuario.idUsuario == idAdministrador

// Mostrar solo la acción correspondiente.
        btnActivarUsuario.visibility =
            if (!usuario.estado) View.VISIBLE
            else View.GONE

        btnBloquearUsuario.visibility =
            if (usuario.estado && !esCuentaPropia) {
                View.VISIBLE
            } else {
                View.GONE
            }

// ACTIVAR USUARIO
        btnActivarUsuario.setOnClickListener {

            confirmarCambioEstado(
                usuario = usuario,
                nuevoEstado = true,
                dialog = dialog,
                boton = btnActivarUsuario
            )
        }

// DESHABILITAR USUARIO
        btnBloquearUsuario.setOnClickListener {

            confirmarCambioEstado(
                usuario = usuario,
                nuevoEstado = false,
                dialog = dialog,
                boton = btnBloquearUsuario
            )
        }

        dialog.setContentView(view)

        dialog.show()
    }

// CONFIRMAR CAMBIO DE ESTADO

    private fun confirmarCambioEstado(
        usuario: UsuarioAdminResponse,
        nuevoEstado: Boolean,
        dialog: BottomSheetDialog,
        boton: MaterialButton
    ) {

        val nombre = (
                "${usuario.nombres.orEmpty()} " +
                        usuario.apellidos.orEmpty()
                ).trim()

        val accion = if (nuevoEstado) {
            "activar"
        } else {
            "deshabilitar"
        }

        val mensaje = if (nuevoEstado) {
            "¿Deseas activar la cuenta de $nombre?\n\n" +
                    "El usuario podrá iniciar sesión nuevamente."
        } else {
            "¿Deseas deshabilitar la cuenta de $nombre?\n\n" +
                    "El usuario no podrá iniciar nuevas sesiones " +
                    "mediante contraseña o biometría."
        }

        AlertDialog.Builder(this)
            .setTitle(
                if (nuevoEstado) {
                    "Activar usuario"
                } else {
                    "Deshabilitar usuario"
                }
            )
            .setMessage(mensaje)
            .setNegativeButton("Volver", null)
            .setPositiveButton(
                if (nuevoEstado) "Activar" else "Deshabilitar"
            ) { _, _ ->

                // Comprobación adicional de la cuenta propia.
                val idAdministrador = getSharedPreferences(
                    "MisoftSesion",
                    MODE_PRIVATE
                ).getInt("idUsuario", -1)

                if (!nuevoEstado &&
                    usuario.idUsuario == idAdministrador) {

                    mostrarMensaje(
                        "No puedes deshabilitar tu propia cuenta."
                    )

                } else {

                    ejecutarCambioEstado(
                        idUsuario = usuario.idUsuario,
                        nuevoEstado = nuevoEstado,
                        dialog = dialog,
                        boton = boton
                    )
                }
            }
            .show()
    }

// ACTUALIZAR ESTADO EN MYSQL

    private fun ejecutarCambioEstado(
        idUsuario: Int,
        nuevoEstado: Boolean,
        dialog: BottomSheetDialog,
        boton: MaterialButton
    ) {

        boton.isEnabled = false

        val textoOriginal = boton.text

        boton.text = if (nuevoEstado) {
            "Activando..."
        } else {
            "Deshabilitando..."
        }

        val request = JsonObject().apply {
            addProperty("estado", nuevoEstado)
        }

        ApiClient.usuariosAdminApi
            .cambiarEstadoUsuario(
                idUsuario,
                request
            )
            .enqueue(object : Callback<JsonObject> {

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {

                    if (isFinishing || isDestroyed) return

                    if (response.isSuccessful) {

                        mostrarMensaje(
                            if (nuevoEstado) {
                                "Usuario activado correctamente"
                            } else {
                                "Usuario deshabilitado correctamente"
                            }
                        )

                        dialog.dismiss()

                        // Recargar la lista real desde MySQL.
                        cargarUsuarios()

                    } else {

                        val mensajeError = when (response.code()) {

                            400 ->
                                "Los datos enviados no son válidos."

                            403 ->
                                "No se permite realizar esta operación."

                            404 ->
                                "El usuario no existe."

                            409 ->
                                "Los datos cambiaron. Actualiza la lista."

                            else ->
                                "No se pudo actualizar el usuario. " +
                                        "HTTP ${response.code()}"
                        }

                        mostrarMensaje(mensajeError)

                        boton.isEnabled = true
                        boton.text = textoOriginal

                        if (response.code() == 404 ||
                            response.code() == 409) {

                            dialog.dismiss()
                            cargarUsuarios()
                        }
                    }
                }

                override fun onFailure(
                    call: Call<JsonObject>,
                    t: Throwable
                ) {

                    if (call.isCanceled ||
                        isFinishing ||
                        isDestroyed) return

                    mostrarMensaje(
                        "No se pudo confirmar el resultado. " +
                                "Actualiza los usuarios antes de reintentar."
                    )

                    dialog.dismiss()
                    cargarUsuarios()
                }
            })
    }



    // ROLES

    private fun nombreRol(rolId: Int): String {

        return when (rolId) {

            1 -> "Administrador"

            2 -> "Vendedor"

            3 -> "Comprador"

            else -> "Rol desconocido"
        }
    }

    // MENSAJES DE LISTA

    private fun mostrarMensajeLista(
        mensaje: String
    ) {

        contenedorUsuarios.removeAllViews()

        val texto = TextView(this).apply {

            text = mensaje

            textSize = 14f

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#777777")
            )

            setPadding(
                dp(12),
                dp(50),
                dp(12),
                dp(30)
            )
        }

        contenedorUsuarios.addView(texto)
    }


    private fun mostrarMensaje(
        mensaje: String
    ) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun dp(valor: Int): Int {

        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }

    // NAVEGACIÓN ADMIN

    private fun configurarNavegacion() {

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationAdmin
            )

        bottomNavigation.selectedItemId =
            R.id.usuarios

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

                R.id.usuarios -> true

                R.id.vendedores -> {

                    startActivity(
                        Intent(
                            this,
                            VendedoresAdminActivity::class.java
                        )
                    )

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

    // LIMPIAR LLAMADAS

    override fun onDestroy() {

        llamadaUsuarios?.cancel()
        llamadaDetalle?.cancel()

        super.onDestroy()
    }
}
