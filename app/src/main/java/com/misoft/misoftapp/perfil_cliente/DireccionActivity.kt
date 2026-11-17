
package com.misoft.misoftapp.perfil_cliente

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.misoft.misoftapp.R
import com.misoft.misoftapp.api.ApiClient
import com.misoft.misoftapp.models.DireccionRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class DireccionActivity : AppCompatActivity() {

    private lateinit var contenedorDirecciones: LinearLayout
    private lateinit var layoutSinDirecciones: LinearLayout
    private lateinit var progressDirecciones: ProgressBar

    private var latitudSeleccionada: Double? = null
    private var longitudSeleccionada: Double? = null
    private var alActualizarUbicacion: ((Double, Double) -> Unit)? = null

    private val idUsuario: Int
        get() = getSharedPreferences(
            "MisoftSesion",
            MODE_PRIVATE
        ).getInt("idUsuario", -1)

    // RECIBIR UBICACIÓN DESDE GOOGLE MAPS
    private val seleccionarUbicacion =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado ->

            if (resultado.resultCode == Activity.RESULT_OK) {

                val datos = resultado.data

                if (
                    datos != null &&
                    datos.hasExtra("latitud") &&
                    datos.hasExtra("longitud")
                ) {

                    latitudSeleccionada =
                        datos.getDoubleExtra("latitud", 0.0)

                    longitudSeleccionada =
                        datos.getDoubleExtra("longitud", 0.0)

                    mostrarBottomSheetDireccion()

                } else {
                    mostrarMensaje(
                        "No se recibieron las coordenadas"
                    )
                }
            }
        }


    private val cambiarUbicacion =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado ->

            val actualizar = alActualizarUbicacion
            alActualizarUbicacion = null

            if (
                resultado.resultCode == Activity.RESULT_OK &&
                resultado.data?.hasExtra("latitud") == true &&
                resultado.data?.hasExtra("longitud") == true
            ) {
                val latitud = resultado.data!!
                    .getDoubleExtra("latitud", 0.0)

                val longitud = resultado.data!!
                    .getDoubleExtra("longitud", 0.0)

                actualizar?.invoke(latitud, longitud)
            }
        }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.cliente_activity_direccion)

        // COMPONENTES DEL NUEVO XML
        contenedorDirecciones = findViewById(
            R.id.contenedorDirecciones
        )

        layoutSinDirecciones = findViewById(
            R.id.layoutSinDirecciones
        )

        progressDirecciones = findViewById(
            R.id.progressDirecciones
        )

        val imgBack = findViewById<ImageView>(
            R.id.imgBack
        )

        val btnAgregarDireccion =
            findViewById<MaterialButton>(
                R.id.btnAgregarDireccion
            )

        // REGRESAR
        imgBack.setOnClickListener {
            finish()
        }

        // AGREGAR DIRECCIÓN CON GOOGLE MAPS
        btnAgregarDireccion.setOnClickListener {

            val intent = Intent(
                this,
                UbicacionActivity::class.java
            )

            seleccionarUbicacion.launch(intent)
        }

        // CARGAR DIRECCIONES REALES
        cargarDirecciones()
    }

    override fun onResume() {
        super.onResume()
        // La lista también se actualiza después de guardar.
    }

    // =========================================
    // CONSULTAR DIRECCIONES EN MYSQL
    // =========================================

    private fun cargarDirecciones() {

        if (idUsuario <= 0) {
            progressDirecciones.visibility = View.GONE
            contenedorDirecciones.removeAllViews()
            layoutSinDirecciones.visibility = View.VISIBLE

            mostrarMensaje(
                "No se encontró la sesión del comprador"
            )
            return
        }

        progressDirecciones.visibility = View.VISIBLE
        layoutSinDirecciones.visibility = View.GONE

        ApiClient.direccionApi
            .obtenerDirecciones(idUsuario)
            .enqueue(object : Callback<JsonArray> {

                override fun onResponse(
                    call: Call<JsonArray>,
                    response: Response<JsonArray>
                ) {

                    progressDirecciones.visibility = View.GONE

                    if (!response.isSuccessful) {
                        mostrarMensaje(
                            "Error al consultar direcciones: HTTP ${response.code()}"
                        )
                        return
                    }

                    val direcciones = response.body()

                    contenedorDirecciones.removeAllViews()

                    if (direcciones == null ||
                        direcciones.size() == 0
                    ) {

                        layoutSinDirecciones.visibility =
                            View.VISIBLE

                        return
                    }

                    layoutSinDirecciones.visibility = View.GONE

                    for (elemento in direcciones) {
                        if (elemento.isJsonObject) {
                            crearTarjetaDireccion(
                                elemento.asJsonObject
                            )
                        }
                    }
                }

                override fun onFailure(
                    call: Call<JsonArray>,
                    t: Throwable
                ) {

                    progressDirecciones.visibility = View.GONE

                    mostrarMensaje(
                        "Error de conexión: ${t.message}"
                    )
                }
            })
    }

    // GENERAR TARJETAS DINÁMICAS

    private fun crearTarjetaDireccion(
        direccion: JsonObject
    ) {

        val nombre = textoJson(
            direccion,
            "nombreDireccion"
        ).ifBlank { "Dirección" }

        val calle = textoJson(
            direccion,
            "direccion"
        )

        val ciudad = textoJson(
            direccion,
            "ciudad"
        )

        val barrio = textoJson(
            direccion,
            "barrio"
        )

        val principal = booleanoJson(
            direccion,
            "principal"
        )

        val idDireccion = enteroJson(
            direccion,
            "idDireccion"
        )

        // TARJETA
        val tarjeta = CardView(this).apply {

            radius = dp(16).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(
                android.graphics.Color.WHITE
            )
        }

        val parametrosTarjeta =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

        // CONTENIDO
        val contenido = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
            )
        }

        // FILA SUPERIOR
        val filaTitulo = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val emoji = when (nombre.lowercase()) {
            "casa" -> "🏠"
            "trabajo" -> "💼"
            else -> "📍"
        }

        val txtNombre = TextView(this).apply {

            text = "$emoji $nombre"
            textSize = 16f

            setTextColor(
                android.graphics.Color.parseColor(
                    "#6A4C93"
                )
            )

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )
        }

        filaTitulo.addView(
            txtNombre,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        // BOTÓN EDITAR
        val btnEditar = ImageView(this).apply {

            setImageResource(R.drawable.lapiz)

            contentDescription = "Editar $nombre"

            setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
            )

            setOnClickListener {

                if (idDireccion > 0) {
                    mostrarBottomSheetDireccion(direccion)
                }
            }
        }

        filaTitulo.addView(
            btnEditar,
            LinearLayout.LayoutParams(
                dp(40),
                dp(40)
            )
        )

        // BOTÓN ELIMINAR
        val btnEliminar = ImageView(this).apply {

            setImageResource(R.drawable.basura_roja)

            contentDescription = "Eliminar $nombre"

            setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
            )

            setOnClickListener {

                if (idDireccion > 0) {
                    confirmarEliminacion(
                        idDireccion,
                        nombre
                    )
                }
            }
        }

        filaTitulo.addView(
            btnEliminar,
            LinearLayout.LayoutParams(
                dp(40),
                dp(40)
            )
        )

        contenido.addView(filaTitulo)

        // ETIQUETA PRINCIPAL
        if (principal) {

            val txtPrincipal = TextView(this).apply {

                text = "✓ Dirección principal"
                textSize = 12f

                setTextColor(
                    android.graphics.Color.parseColor(
                        "#7B4FA3"
                    )
                )
            }

            val parametrosPrincipal =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(4)
                }

            contenido.addView(
                txtPrincipal,
                parametrosPrincipal
            )
        }

        // CALLE
        val txtCalle = TextView(this).apply {

            text = calle
            textSize = 14f

            setTextColor(
                android.graphics.Color.parseColor(
                    "#38313C"
                )
            )
        }

        contenido.addView(
            txtCalle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )

        // BARRIO Y CIUDAD
        val ubicacion = listOf(
            barrio,
            ciudad
        ).filter {
            it.isNotBlank()
        }.joinToString(", ")

        val txtCiudad = TextView(this).apply {

            text = ubicacion
            textSize = 13f

            setTextColor(
                android.graphics.Color.parseColor(
                    "#80758A"
                )
            )
        }

        contenido.addView(txtCiudad)

        tarjeta.addView(contenido)

        contenedorDirecciones.addView(
            tarjeta,
            parametrosTarjeta
        )
    }

    // BOTTOM SHEET: CREAR O EDITAR

    private fun mostrarBottomSheetDireccion(
        direccionExistente: JsonObject? = null
    ) {

        val editando = direccionExistente != null



        var latitud = if (editando) {
            direccionExistente?.let {
                decimalJson(it, "latitud")
            }
        } else {
            latitudSeleccionada
        }

        var longitud = if (editando) {
            direccionExistente?.let {
                decimalJson(it, "longitud")
            }
        } else {
            longitudSeleccionada
        }



        if (!editando &&
            (latitud == null || longitud == null)
        ) {

            mostrarMensaje(
                "Primero selecciona una ubicación"
            )
            return
        }

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.bottom_agregar_direccion,
            null
        )

        val txtTituloDireccion =
            view.findViewById<TextView>(
                R.id.txtTituloDireccion
            )

        val txtUbicacionMapa =
            view.findViewById<TextView>(
                R.id.txtUbicacionMapa
            )
        val btnCambiarUbicacion =
            view.findViewById<MaterialButton>(
                R.id.btnCambiarUbicacion
            )

        val edtNombreDireccion =
            view.findViewById<TextInputEditText>(
                R.id.edtNombreDireccion
            )

        val edtDireccion =
            view.findViewById<TextInputEditText>(
                R.id.edtDireccion
            )

        val edtCiudad =
            view.findViewById<TextInputEditText>(
                R.id.edtCiudad
            )

        val edtBarrio =
            view.findViewById<TextInputEditText>(
                R.id.edtBarrio
            )

        val edtReferencia =
            view.findViewById<TextInputEditText>(
                R.id.edtReferencia
            )

        val checkPrincipal =
            view.findViewById<MaterialCheckBox>(
                R.id.checkPrincipal
            )

        val btnGuardarDireccion =
            view.findViewById<MaterialButton>(
                R.id.btnGuardarDireccion
            )

        // CONFIGURAR EL FORMULARIO
        txtTituloDireccion.text =
            if (editando) {
                "Editar dirección"
            } else {
                "Nueva dirección"
            }

        btnGuardarDireccion.text =
            if (editando) {
                "Actualizar dirección"
            } else {
                "Guardar dirección"
            }

        txtUbicacionMapa.text =
            if (latitud != null && longitud != null) {
                "📍 Ubicación seleccionada\n" +
                        "Latitud: $latitud\n" +
                        "Longitud: $longitud"
            } else {
                "📍 Sin coordenadas registradas"
            }

        if (editando) {

            val actual = direccionExistente!!

            edtNombreDireccion.setText(
                textoJson(actual, "nombreDireccion")
            )

            edtDireccion.setText(
                textoJson(actual, "direccion")
            )

            edtCiudad.setText(
                textoJson(actual, "ciudad")
            )

            edtBarrio.setText(
                textoJson(actual, "barrio")
            )

            edtReferencia.setText(
                textoJson(actual, "referencia")
            )

            checkPrincipal.isChecked =
                booleanoJson(actual, "principal")

            if (checkPrincipal.isChecked) {
                checkPrincipal.isEnabled = false
            }

        } else {

            // AUTOCOMPLETAR DESDE GOOGLE MAPS
            autocompletarDireccion(
                latitud!!,
                longitud!!,
                edtDireccion,
                edtCiudad,
                edtBarrio
            )
        }


// MOSTRAR EL BOTÓN SOLO AL EDITAR
        btnCambiarUbicacion.visibility =
            if (editando) View.VISIBLE else View.GONE

        btnCambiarUbicacion.setOnClickListener {

            val intentMapa = Intent(
                this,
                UbicacionActivity::class.java
            )

            // ENVIAR LAS COORDENADAS REGISTRADAS
            if (latitud != null && longitud != null) {

                intentMapa.putExtra(
                    "latitudInicial",
                    latitud
                )

                intentMapa.putExtra(
                    "longitudInicial",
                    longitud
                )
            }

            // RECIBIR LAS COORDENADAS NUEVAS
            alActualizarUbicacion = { nuevaLatitud, nuevaLongitud ->

                latitud = nuevaLatitud
                longitud = nuevaLongitud

                txtUbicacionMapa.text =
                    "📍 Nueva ubicación seleccionada\n" +
                            "Latitud: $nuevaLatitud\n" +
                            "Longitud: $nuevaLongitud"

                // LIMPIAR LA DIRECCIÓN ANTERIOR
                edtDireccion.setText("")
                edtCiudad.setText("")
                edtBarrio.setText("")

                // AUTOCOMPLETAR LA NUEVA UBICACIÓN
                autocompletarDireccion(
                    nuevaLatitud,
                    nuevaLongitud,
                    edtDireccion,
                    edtCiudad,
                    edtBarrio
                )

                Toast.makeText(
                    this,
                    "Ubicación actualizada. Revisa los datos antes de guardar.",
                    Toast.LENGTH_LONG
                ).show()
            }

            cambiarUbicacion.launch(intentMapa)
        }


        // GUARDAR O ACTUALIZAR
        btnGuardarDireccion.setOnClickListener {

            val nombre = edtNombreDireccion.text
                .toString().trim()

            val calle = edtDireccion.text
                .toString().trim()

            val ciudad = edtCiudad.text
                .toString().trim()

            val barrio = edtBarrio.text
                .toString().trim()

            val referencia = edtReferencia.text
                .toString().trim()

            val principal = checkPrincipal.isChecked


            android.util.Log.d(
                "DIRECCION_DEBUG",
                "Checkbox principal: $principal"
            )

            // VALIDACIONES
            if (nombre.isBlank() || nombre.length > 50) {

                edtNombreDireccion.error =
                    "Ingresa un nombre de máximo 50 caracteres"

                edtNombreDireccion.requestFocus()
                return@setOnClickListener
            }

            if (calle.isBlank() || calle.length > 255) {

                edtDireccion.error =
                    "Ingresa una dirección de máximo 255 caracteres"

                edtDireccion.requestFocus()
                return@setOnClickListener
            }

            if (ciudad.isBlank() || ciudad.length > 100) {

                edtCiudad.error =
                    "Ingresa una ciudad de máximo 100 caracteres"

                edtCiudad.requestFocus()
                return@setOnClickListener
            }

            if (barrio.length > 100) {

                edtBarrio.error =
                    "El barrio no puede superar 100 caracteres"

                edtBarrio.requestFocus()
                return@setOnClickListener
            }

            if (referencia.length > 255) {

                edtReferencia.error =
                    "La referencia no puede superar 255 caracteres"

                edtReferencia.requestFocus()
                return@setOnClickListener
            }

            if (idUsuario <= 0) {
                mostrarMensaje(
                    "No se encontró la sesión del comprador"
                )
                return@setOnClickListener
            }

            // DATOS PARA LA API
            val request = DireccionRequest(
                usuarioId = idUsuario,
                nombreDireccion = nombre,
                direccion = calle,
                ciudad = ciudad,
                barrio = barrio,
                referencia = referencia,
                latitud = latitud,
                longitud = longitud,
                principal = principal
            )

            btnGuardarDireccion.isEnabled = false

            val llamada: Call<JsonObject> =
                if (editando) {

                    val idDireccion = enteroJson(
                        direccionExistente!!,
                        "idDireccion"
                    )

                    if (idDireccion <= 0) {
                        btnGuardarDireccion.isEnabled = true

                        mostrarMensaje(
                            "No se pudo identificar la dirección"
                        )

                        return@setOnClickListener
                    }

                    ApiClient.direccionApi.actualizarDireccion(
                        idDireccion,
                        request
                    )

                } else {

                    ApiClient.direccionApi.crearDireccion(
                        request
                    )
                }

            llamada.enqueue(object : Callback<JsonObject> {

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {

                    if (response.isSuccessful) {

                        mostrarMensaje(
                            if (editando) {
                                "Dirección actualizada correctamente"
                            } else {
                                "Dirección registrada correctamente"
                            }
                        )

                        dialog.dismiss()

                        // ACTUALIZAR LISTA AUTOMÁTICAMENTE
                        cargarDirecciones()

                    } else {

                        btnGuardarDireccion.isEnabled = true

                        mostrarMensaje(
                            "No se pudo guardar la dirección. " +
                                    "HTTP ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<JsonObject>,
                    t: Throwable
                ) {

                    btnGuardarDireccion.isEnabled = true

                    mostrarMensaje(
                        "Error de conexión: ${t.message}"
                    )
                }
            })
        }

        dialog.setContentView(view)
        dialog.show()

        dialog.setOnDismissListener {
            alActualizarUbicacion = null
        }
    }



    // ELIMINAR DIRECCIÓN
    private fun confirmarEliminacion(
        idDireccion: Int,
        nombreDireccion: String
    ) {

        AlertDialog.Builder(this)
            .setTitle("Eliminar dirección")
            .setMessage(
                "¿Deseas eliminar la dirección $nombreDireccion?"
            )
            .setPositiveButton("Eliminar") { _, _ ->

                if (idUsuario <= 0) {
                    mostrarMensaje(
                        "No se encontró la sesión del comprador"
                    )
                    return@setPositiveButton
                }

                ApiClient.direccionApi.eliminarDireccion(
                    idDireccion,
                    idUsuario
                ).enqueue(object : Callback<JsonObject> {

                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {

                        if (response.isSuccessful) {

                            mostrarMensaje(
                                "Dirección eliminada correctamente"
                            )

                            cargarDirecciones()

                        } else {

                            mostrarMensaje(
                                "No se pudo eliminar. " +
                                        "HTTP ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<JsonObject>,
                        t: Throwable
                    ) {

                        mostrarMensaje(
                            "Error de conexión: ${t.message}"
                        )
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // =========================================
    // AUTOCOMPLETAR CON GEOCODER

    private fun autocompletarDireccion(
        latitud: Double,
        longitud: Double,
        edtDireccion: TextInputEditText,
        edtCiudad: TextInputEditText,
        edtBarrio: TextInputEditText
    ) {

        if (!Geocoder.isPresent()) return

        val geocoder = Geocoder(
            this,
            Locale("es", "CO")
        )

        fun completarCampos(address: Address?) {

            if (address == null) return

            val direccion = listOfNotNull(
                address.thoroughfare,
                address.subThoroughfare
            )
                .filter { it.isNotBlank() }
                .joinToString(" # ")

            val direccionFinal = direccion.ifBlank {
                address.getAddressLine(0)
                    ?.substringBeforeLast(",")
                    .orEmpty()
            }

            val ciudad = address.locality
                ?: address.subAdminArea
                ?: ""

            val barrio = address.subLocality ?: ""

            if (edtDireccion.text.isNullOrBlank()) {
                edtDireccion.setText(direccionFinal)
            }

            if (edtCiudad.text.isNullOrBlank()) {
                edtCiudad.setText(ciudad)
            }

            if (edtBarrio.text.isNullOrBlank()) {
                edtBarrio.setText(barrio)
            }
        }

        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            geocoder.getFromLocation(
                latitud,
                longitud,
                1,
                object : Geocoder.GeocodeListener {

                    override fun onGeocode(
                        addresses: MutableList<Address>
                    ) {

                        runOnUiThread {
                            completarCampos(
                                addresses.firstOrNull()
                            )
                        }
                    }

                    override fun onError(
                        errorMessage: String?
                    ) {
                    }
                }
            )

        } else {

            Thread {

                try {

                    @Suppress("DEPRECATION")
                    val addresses =
                        geocoder.getFromLocation(
                            latitud,
                            longitud,
                            1
                        )

                    runOnUiThread {
                        completarCampos(
                            addresses?.firstOrNull()
                        )
                    }

                } catch (e: Exception) {
                }

            }.start()
        }
    }

    // LEER PROPIEDADES JSON

    private fun campoJson(
        objeto: JsonObject,
        nombre: String
    ): com.google.gson.JsonElement? {

        val campo = objeto.get(nombre)
            ?: objeto.get(
                nombre.replaceFirstChar {
                    it.uppercase()
                }
            )

        return if (campo == null || campo.isJsonNull) {
            null
        } else {
            campo
        }
    }

    private fun textoJson(
        objeto: JsonObject,
        nombre: String
    ): String {

        return try {
            campoJson(objeto, nombre)?.asString ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun enteroJson(
        objeto: JsonObject,
        nombre: String
    ): Int {

        return try {
            campoJson(objeto, nombre)?.asInt ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    private fun decimalJson(
        objeto: JsonObject,
        nombre: String
    ): Double? {

        return try {
            campoJson(objeto, nombre)?.asDouble
        } catch (e: Exception) {
            null
        }
    }

    private fun booleanoJson(
        objeto: JsonObject,
        nombre: String
    ): Boolean {

        return try {
            campoJson(objeto, nombre)?.asBoolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    // UTILIDADES

    private fun dp(valor: Int): Int {

        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }

    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}
