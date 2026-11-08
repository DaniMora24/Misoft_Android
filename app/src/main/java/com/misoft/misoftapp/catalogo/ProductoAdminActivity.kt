
package com.misoft.misoftapp.catalogo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.misoft.misoftapp.R

class ProductoAdminActivity : AppCompatActivity () {


            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                setContentView(
                    R.layout.admin_activity_producto
                )

                val nombreTienda =
                    intent.getStringExtra(
                        "NOMBRE_TIENDA"
                    ) ?: "Tienda"

                val txtTitulo =
                    findViewById<TextView>(
                        R.id.txtNombreTienda
                    )

                txtTitulo.text =
                    "📦 Productos de $nombreTienda"

                val cardProducto1 =
                    findViewById<CardView>(
                        R.id.cardProducto1
                    )

                val cardProducto2 =
                    findViewById<CardView>(
                        R.id.cardProducto2
                    )

                val cardProducto3 =
                    findViewById<CardView>(
                        R.id.cardProducto3
                    )

                cardProducto1.setOnClickListener {

                    mostrarDetalleProducto(
                        "Snoopy",
                        "$45.000",
                        "Amigurumi tejido a mano inspirado en Snoopy.",
                        "5",
                        nombreTienda
                    )

                }

                cardProducto2.setOnClickListener {

                    mostrarDetalleProducto(
                        "Gerbera Rosa",
                        "$35.000",
                        "Flor tejida artesanalmente.",
                        "10",
                        nombreTienda
                    )

                }

                cardProducto3.setOnClickListener {

                    mostrarDetalleProducto(
                        "Goku",
                        "$55.000",
                        "Personaje anime tejido a mano.",
                        "0",
                        nombreTienda
                    )

                }

            }

    private fun mostrarDetalleProducto(
        nombre: String,
        precio: String,
        descripcion: String,
        stock: String,
        vendedor: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.admin_bottom_detalle_producto,
            null
        )

        dialog.setContentView(view)

        dialog.show()

    }



}




