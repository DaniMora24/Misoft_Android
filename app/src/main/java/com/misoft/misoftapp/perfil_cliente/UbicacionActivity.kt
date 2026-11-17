package com.misoft.misoftapp.perfil_cliente

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.gms.tasks.CancellationTokenSource
import com.misoft.misoftapp.R

class UbicacionActivity : AppCompatActivity(),
    OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private lateinit var txtDireccionSeleccionada: TextView
    private lateinit var btnConfirmarUbicacion: MaterialButton
    private lateinit var btnUbicacionActual: MaterialButton

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private var ubicacionSeleccionada: LatLng? = null
    private var marcador: Marker? = null

    private var mapaListo = false
    private var buscandoUbicacion = false

    private var cancellationTokenSource:
            CancellationTokenSource? = null

    // PEDIR PERMISOS
    private val solicitarPermisos =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->

            val permisoPreciso = permisos[
                Manifest.permission.ACCESS_FINE_LOCATION
            ] ?: false

            val permisoAproximado = permisos[
                Manifest.permission.ACCESS_COARSE_LOCATION
            ] ?: false

            if (permisoPreciso || permisoAproximado) {
                obtenerUbicacionActual()
            } else {
                mostrarMensaje(
                    "Debes permitir el acceso a la ubicación"
                )
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.cliente_activity_ubicacion)

        // REFERENCIAS DEL XML
        txtDireccionSeleccionada =
            findViewById(R.id.txtDireccionSeleccionada)

        btnConfirmarUbicacion =
            findViewById(R.id.btnConfirmarUbicacion)

        btnUbicacionActual =
            findViewById(R.id.btnUbicacionActual)

        // INICIALIZAR SERVICIO GPS
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        // FLECHA DE REGRESO
        findViewById<ImageView>(R.id.imgBack)
            .setOnClickListener {
                finish()
            }

        // INICIALIZAR GOOGLE MAPS
        val mapFragment =
            supportFragmentManager.findFragmentById(
                R.id.mapContainer
            ) as SupportMapFragment

        mapFragment.getMapAsync(this)

        // BOTÓN UBICACIÓN ACTUAL
        btnUbicacionActual.setOnClickListener {
            verificarPermisosUbicacion()
        }

        // CONFIRMAR UBICACIÓN
        btnConfirmarUbicacion.setOnClickListener {

            val ubicacion = ubicacionSeleccionada
                ?: return@setOnClickListener

            val resultado = Intent().apply {

                putExtra(
                    "latitud",
                    ubicacion.latitude
                )

                putExtra(
                    "longitud",
                    ubicacion.longitude
                )
            }

            setResult(Activity.RESULT_OK, resultado)
            finish()
        }
    }

    // MAPA LISTO

    override fun onMapReady(map: GoogleMap) {

        googleMap = map
        mapaListo = true


        // VERIFICAR SI VENIMOS DE EDITAR UNA DIRECCIÓN
        val tieneUbicacionGuardada =
            intent.hasExtra("latitudInicial") &&
                    intent.hasExtra("longitudInicial")

        if (tieneUbicacionGuardada) {

            val latitud = intent.getDoubleExtra(
                "latitudInicial",
                0.0
            )

            val longitud = intent.getDoubleExtra(
                "longitudInicial",
                0.0
            )

            val posicionGuardada = LatLng(
                latitud,
                longitud
            )

            // MOSTRAR LA UBICACIÓN REGISTRADA
            seleccionarUbicacion(posicionGuardada)

        } else {

            // UBICACIÓN INICIAL PARA NUEVAS DIRECCIONES
            val centroColombia = LatLng(
                4.5709,
                -74.2973
            )

            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    centroColombia,
                    5.5f
                )
            )
        }


        // SELECCIONAR PUNTO MANUALMENTE
        googleMap.setOnMapClickListener { ubicacion ->

            seleccionarUbicacion(ubicacion)
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true

        activarCapaUbicacionSiPermitida()
    }

    // VERIFICAR PERMISOS

    private fun verificarPermisosUbicacion() {

        if (!mapaListo) {
            mostrarMensaje("El mapa todavía está cargando")
            return
        }

        val permisoPreciso = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permisoPreciso || permisoAproximado) {

            obtenerUbicacionActual()

        } else {

            solicitarPermisos.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // OBTENER UBICACIÓN DEL DISPOSITIVO

    private fun obtenerUbicacionActual() {

        if (!mapaListo || buscandoUbicacion) return

        val tienePreciso = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val tieneAproximado = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!tienePreciso && !tieneAproximado) {
            mostrarMensaje("Permiso de ubicación no concedido")
            return
        }

        activarCapaUbicacionSiPermitida()

        buscandoUbicacion = true
        btnUbicacionActual.isEnabled = false

        txtDireccionSeleccionada.text =
            "Obteniendo tu ubicación actual..."

        cancellationTokenSource?.cancel()

        val tokenSource = CancellationTokenSource()
        cancellationTokenSource = tokenSource

        val prioridad = if (tienePreciso) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        try {

            fusedLocationClient.getCurrentLocation(
                prioridad,
                tokenSource.token
            ).addOnSuccessListener { location ->

                if (isFinishing || isDestroyed) {
                    return@addOnSuccessListener
                }

                finalizarBusquedaUbicacion()

                if (location != null) {

                    val posicion = LatLng(
                        location.latitude,
                        location.longitude
                    )

                    seleccionarUbicacion(posicion)

                    mostrarMensaje(
                        "Ubicación obtenida correctamente"
                    )

                } else {

                    txtDireccionSeleccionada.text =
                        "No pudimos obtener tu ubicación. " +
                                "Puedes seleccionar un punto en el mapa."

                    mostrarMensaje(
                        "Activa la ubicación del dispositivo e intenta nuevamente"
                    )
                }

            }.addOnFailureListener { error ->

                if (isFinishing || isDestroyed) {
                    return@addOnFailureListener
                }

                finalizarBusquedaUbicacion()

                txtDireccionSeleccionada.text =
                    "No se pudo obtener tu ubicación."

                mostrarMensaje(
                    "Error al obtener ubicación: ${error.message}"
                )
            }

        } catch (e: SecurityException) {

            finalizarBusquedaUbicacion()

            mostrarMensaje(
                "No se tienen permisos para acceder a la ubicación"
            )
        }
    }

    private fun finalizarBusquedaUbicacion() {
        buscandoUbicacion = false
        btnUbicacionActual.isEnabled = true
        cancellationTokenSource = null
    }

    // CAPA DE UBICACIÓN DE GOOGLE MAPS

    private fun activarCapaUbicacionSiPermitida() {

        if (!mapaListo) return

        val permisoPreciso = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permisoPreciso || permisoAproximado) {

            try {
                googleMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                googleMap.isMyLocationEnabled = false
            }
        }
    }

    // SELECCIONAR PUNTO DEL MAPA

    private fun seleccionarUbicacion(
        ubicacion: LatLng
    ) {

        ubicacionSeleccionada = ubicacion

        marcador?.remove()

        marcador = googleMap.addMarker(
            MarkerOptions()
                .position(ubicacion)
                .title("Ubicación de entrega")
        )

        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                ubicacion,
                16f
            )
        )

        txtDireccionSeleccionada.text =
            "Latitud: ${ubicacion.latitude}\n" +
                    "Longitud: ${ubicacion.longitude}"

        btnConfirmarUbicacion.isEnabled = true
    }

    // MENSAJES

    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {

        cancellationTokenSource?.cancel()

        super.onDestroy()
    }
}
