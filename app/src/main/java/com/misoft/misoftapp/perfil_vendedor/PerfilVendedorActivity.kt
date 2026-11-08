package com.misoft.misoftapp.perfil_vendedor

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.misoft.misoftapp.R
import com.misoft.misoftapp.catalogo.ProductosVendedorActivity
import com.misoft.misoftapp.pedidos.GestionPedidosActivity
import com.misoft.misoftapp.home.HomeVendedorActivity

class PerfilVendedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.vendedor_activity_perfil
        )

        val cardInfoTienda =
            findViewById<CardView>(
                R.id.cardInfoTienda
            )

        val cardContacto =
            findViewById<CardView>(
                R.id.cardContacto
            )

        val cardEntrega =
            findViewById<CardView>(
                R.id.cardEntrega
            )

        val cardHorario =
            findViewById<CardView>(
                R.id.cardHorario
            )

        // ------------------------------------
        // Navegación Menú

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationSeller
            )

        // PERFIL seleccionado

        bottomNavigation.selectedItemId =
            R.id.perfil

        // Navegación inferior

        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    startActivity(
                        Intent(
                            this,
                            HomeVendedorActivity::class.java
                        )
                    )
                    true
                }
                R.id.productos -> {
                    startActivity(
                        Intent(
                            this,
                            ProductosVendedorActivity::class.java
                        )
                    )
                    true
                }
                R.id.pedidos -> {
                    startActivity(
                        Intent(
                            this,
                            GestionPedidosActivity::class.java
                        )
                    )
                    true
                }
                R.id.perfil -> {
                    true
                }
                else -> false
            }
        }


        // Información de la tienda

        cardInfoTienda.setOnClickListener {

            mostrarBottomPerfil(
                "🏪",
                "Información de la Tienda",
                "Actualiza la información de tu negocio",
                "Nombre de la tienda",
                "Descripción",
                "Slogan",
                "Cristian Crochet",
                "Amigurumis tejidos a mano",
                "Hechos con amor"
            )

        }

        // Contacto

        cardContacto.setOnClickListener {

            mostrarBottomPerfil(
                "📱",
                "Información de Contacto",
                "Actualiza tus datos de contacto",
                "WhatsApp",
                "Correo",
                "Ciudad",
                "3001234567",
                "ventas@michigurumis.com",
                "Bogotá"
            )

        }

        // Métodos de entrega

        cardEntrega.setOnClickListener {

            mostrarBottomPerfil(
                "🚚",
                "Métodos de Entrega",
                "Configura las opciones disponibles",
                "Método principal",
                "Cobertura",
                "Tiempo estimado",
                "Domicilio",
                "Bogotá",
                "1 a 2 días"
            )

        }

        // Horario

        cardHorario.setOnClickListener {

            mostrarBottomPerfil(
                "🕒",
                "Horario de Atención",
                "Configura el horario de tu tienda",
                "Hora apertura",
                "Hora cierre",
                "Días",
                "08:00",
                "18:00",
                "Lunes a Viernes"
            )

        }

    }

    private fun mostrarBottomPerfil(
        icono: String,
        titulo: String,
        subtitulo: String,
        label1: String,
        label2: String,
        label3: String,
        valor1: String,
        valor2: String,
        valor3: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.vendedor_bottom_perfil,
            null
        )

        val txtIcono =
            view.findViewById<TextView>(
                R.id.txtIcono
            )

        val txtTitulo =
            view.findViewById<TextView>(
                R.id.txtTituloBottom
            )

        val txtSubtitulo =
            view.findViewById<TextView>(
                R.id.txtSubtituloBottom
            )

        val txtLabel1 =
            view.findViewById<TextView>(
                R.id.txtLabel1
            )

        val txtLabel2 =
            view.findViewById<TextView>(
                R.id.txtLabel2
            )

        val txtLabel3 =
            view.findViewById<TextView>(
                R.id.txtLabel3
            )

        val edtCampo1 =
            view.findViewById<TextInputEditText>(
                R.id.edtCampo1
            )

        val edtCampo2 =
            view.findViewById<TextInputEditText>(
                R.id.edtCampo2
            )

        val edtCampo3 =
            view.findViewById<TextInputEditText>(
                R.id.edtCampo3
            )

        val btnGuardar =
            view.findViewById<MaterialButton>(
                R.id.btnGuardarCambios
            )

        txtIcono.text = icono
        txtTitulo.text = titulo
        txtSubtitulo.text = subtitulo

        txtLabel1.text = label1
        txtLabel2.text = label2
        txtLabel3.text = label3

        edtCampo1.setText(valor1)
        edtCampo2.setText(valor2)
        edtCampo3.setText(valor3)

        btnGuardar.setOnClickListener {

            dialog.dismiss()

        }

        dialog.setContentView(view)
        dialog.show()


        }

        }


