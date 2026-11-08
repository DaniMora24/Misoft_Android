package com.misoft.misoftapp.usuarios

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.misoft.misoftapp.R
import com.misoft.misoftapp.catalogo.ProductoAdminActivity

class VendedoresAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_vendedores_admin
        )

        val cardVendedor1 =
            findViewById<CardView>(
                R.id.cardVendedor1
            )

        val cardVendedor2 =
            findViewById<CardView>(
                R.id.cardVendedor2
            )

        val cardVendedor3 =
            findViewById<CardView>(
                R.id.cardVendedor3
            )

        // Vendedor 1

        cardVendedor1.setOnClickListener {

            mostrarDetalleVendedor(
                "Cristian Crochet",
                "Activo",
                "ventas@cristiancrochet.com",
                "3001234567",
                "Bogotá",
                "15",
                "120",
                "4.8",
                "15/03/2026"
            )

        }

        // Vendedor 2

        cardVendedor2.setOnClickListener {

            mostrarDetalleVendedor(
                "Amigurumis Bogotá",
                "Activo",
                "contacto@amigurumisbogota.com",
                "3105557788",
                "Bogotá",
                "32",
                "280",
                "4.9",
                "10/02/2026"
            )

        }

        // Vendedor 3

        cardVendedor3.setOnClickListener {

            mostrarDetalleVendedor(
                "Tejidos Ana",
                "Suspendido",
                "ana@tejidos.com",
                "3228884455",
                "Medellín",
                "8",
                "43",
                "4.5",
                "08/01/2026"
            )

        }

    }

    private fun mostrarDetalleVendedor(
        nombre: String,
        estado: String,
        correo: String,
        telefono: String,
        ciudad: String,
        productos: String,
        ventas: String,
        calificacion: String,
        registro: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.bottom_detalle_vendedor_admin,
            null
        )

        val txtNombreTienda =
            view.findViewById<TextView>(
                R.id.txtNombreTienda
            )

        val txtEstadoVendedor =
            view.findViewById<TextView>(
                R.id.txtEstadoVendedor
            )

        val txtCorreoVendedor =
            view.findViewById<TextView>(
                R.id.txtCorreoVendedor
            )

        val txtTelefonoVendedor =
            view.findViewById<TextView>(
                R.id.txtTelefonoVendedor
            )

        val txtCiudadVendedor =
            view.findViewById<TextView>(
                R.id.txtCiudadVendedor
            )

        val txtProductosPublicados =
            view.findViewById<TextView>(
                R.id.txtProductosPublicados
            )

        val txtVentasRealizadas =
            view.findViewById<TextView>(
                R.id.txtVentasRealizadas
            )

        val txtCalificacion =
            view.findViewById<TextView>(
                R.id.txtCalificacion
            )

        val txtFechaRegistro =
            view.findViewById<TextView>(
                R.id.txtFechaRegistro
            )

        val btnVerProductos =
            view.findViewById<MaterialButton>(
                R.id.btnVerProductos
            )

        val btnSuspenderVendedor =
            view.findViewById<MaterialButton>(
                R.id.btnSuspenderVendedor
            )

        txtNombreTienda.text =
            "🏪 $nombre"

        txtEstadoVendedor.text =
            if (estado == "Activo")
                "✅ $estado"
            else
                "⛔ $estado"

        txtCorreoVendedor.text =
            "📧 $correo"

        txtTelefonoVendedor.text =
            "📱 $telefono"

        txtCiudadVendedor.text =
            "📍 $ciudad"

        txtProductosPublicados.text =
            "📦 Productos publicados: $productos"

        txtVentasRealizadas.text =
            "💰 Ventas realizadas: $ventas"

        txtCalificacion.text =
            "⭐ Calificación promedio: $calificacion"

        txtFechaRegistro.text =
            "📅 Registro: $registro"

        btnVerProductos.setOnClickListener {

            val intent = Intent(
                this,
                ProductoAdminActivity::class.java
            )

            intent.putExtra(
                "NOMBRE_TIENDA",
                nombre
            )

            startActivity(intent)

            dialog.dismiss()

        }

        btnSuspenderVendedor.setOnClickListener {

            dialog.dismiss()

        }

        dialog.setContentView(view)

        dialog.show()

    }

}