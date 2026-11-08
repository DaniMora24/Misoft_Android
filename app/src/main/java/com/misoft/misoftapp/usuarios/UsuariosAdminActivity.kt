package com.misoft.misoftapp.usuarios

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.misoft.misoftapp.R
import com.misoft.misoftapp.estadisticas.ReportesAdminActivity
import com.misoft.misoftapp.home.HomeAdminActivity

class UsuariosAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.admin_activity_usuarios
        )

        val cardUsuario1 =
            findViewById<CardView>(
                R.id.cardUsuario1
            )

        val cardUsuario2 =
            findViewById<CardView>(
                R.id.cardUsuario2
            )

        val cardUsuario3 =
            findViewById<CardView>(
                R.id.cardUsuario3
            )

        // Navegación Admin

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationAdmin
            )

        bottomNavigation.selectedItemId =
            R.id.usuarios

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

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

                R.id.usuarios -> {

                    true

                }

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

        cardUsuario1.setOnClickListener {

            mostrarDetalleUsuario(
                "Cristian Mora",
                "Cliente",
                "Activo",
                "cristian@gmail.com",
                "3001234567",
                "Bogotá",
                "15/03/2026",
                "12",
                "Hoy"
            )

        }

        cardUsuario2.setOnClickListener {

            mostrarDetalleUsuario(
                "Ana López",
                "Cliente",
                "Activo",
                "ana@gmail.com",
                "3201234567",
                "Medellín",
                "20/05/2026",
                "8",
                "Hace 2 días"
            )

        }

        cardUsuario3.setOnClickListener {

            mostrarDetalleUsuario(
                "Juan Pérez",
                "Cliente",
                "Bloqueado",
                "juan@gmail.com",
                "3109876543",
                "Cali",
                "12/01/2026",
                "5",
                "Hace 1 semana"
            )

        }

    }

    private fun mostrarDetalleUsuario(
        nombre: String,
        rol: String,
        estado: String,
        correo: String,
        telefono: String,
        ciudad: String,
        fechaRegistro: String,
        compras: String,
        ultimoAcceso: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.admin_bottom_detalle_usuario,
            null
        )

        val txtNombreUsuario =
            view.findViewById<TextView>(
                R.id.txtNombreUsuario
            )

        val txtRolUsuario =
            view.findViewById<TextView>(
                R.id.txtRolUsuario
            )

        val txtEstadoUsuario =
            view.findViewById<TextView>(
                R.id.txtEstadoUsuario
            )

        val txtCorreoUsuario =
            view.findViewById<TextView>(
                R.id.txtCorreoUsuario
            )

        val txtTelefonoUsuario =
            view.findViewById<TextView>(
                R.id.txtTelefonoUsuario
            )

        val txtCiudadUsuario =
            view.findViewById<TextView>(
                R.id.txtCiudadUsuario
            )

        val txtRegistro =
            view.findViewById<TextView>(
                R.id.txtRegistro
            )

        val txtCompras =
            view.findViewById<TextView>(
                R.id.txtCompras
            )

        val txtUltimoAcceso =
            view.findViewById<TextView>(
                R.id.txtUltimoAcceso
            )

        val btnActivarUsuario =
            view.findViewById<MaterialButton>(
                R.id.btnActivarUsuario
            )

        val btnBloquearUsuario =
            view.findViewById<MaterialButton>(
                R.id.btnBloquearUsuario
            )

        txtNombreUsuario.text =
            "👤 $nombre"

        txtRolUsuario.text =
            "Rol: $rol"

        txtEstadoUsuario.text =
            if (estado == "Activo")
                "✅ $estado"
            else
                "⛔ $estado"

        txtCorreoUsuario.text =
            "📧 $correo"

        txtTelefonoUsuario.text =
            "📱 $telefono"

        txtCiudadUsuario.text =
            "📍 $ciudad"

        txtRegistro.text =
            "📅 Registro: $fechaRegistro"

        txtCompras.text =
            "🛒 Compras realizadas: $compras"

        txtUltimoAcceso.text =
            "🕒 Último acceso: $ultimoAcceso"

        btnActivarUsuario.setOnClickListener {

            dialog.dismiss()

        }

        btnBloquearUsuario.setOnClickListener {

            dialog.dismiss()

        }

        dialog.setContentView(view)

        dialog.show()

    }

}