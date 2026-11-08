package com.misoft.misoftapp.estadisticas

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.misoft.misoftapp.R

class EstadisticasVendedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContentView(
            R.layout.vendedor_activity_estadisticas
        )

        val cardVentasTotales =
            findViewById<CardView>(
                R.id.cardVentasTotales
            )

        val cardProductoPublicados =
            findViewById<CardView>(
                R.id.cardProductoPublicados
            )

        val cardTotalPedido =
            findViewById<CardView>(
                R.id.cardTotalPedido
            )

        val cardPedidoPendiente =
            findViewById<CardView>(
                R.id.cardPedidoPendiente
            )

        val cardProductoVendido =
            findViewById<CardView>(
                R.id.cardProductoVendido
            )

        val cardIngresosMes =
            findViewById<CardView>(
                R.id.cardIngresosMes
            )


        //Flecha de retroceso

        val imgBack =
            findViewById<ImageView>(
                R.id.imgBack
            )

        imgBack.setOnClickListener {

            finish()

        }

        // Ventas Totales

        cardVentasTotales.setOnClickListener {

            mostrarDetalleEstadistica(
                "💰 Ventas Totales",
                "$1.250.000",
                "Ventas acumuladas de la tienda.",
                "📦 Productos vendidos: 35",
                "🏆 Más vendido: Naruto Amigurumi",
                "✅ Pedidos completados: 28"
            )

        }

        // Productos Publicados

        cardProductoPublicados.setOnClickListener {

            mostrarDetalleEstadistica(
                "📦 Productos Publicados",
                "12",
                "Productos activos en la tienda.",
                "🧸 Personajes: 5",
                "🌻 Flores: 4",
                "🐰 Animales: 3"
            )

        }

        // Total Pedidos

        cardTotalPedido.setOnClickListener {

            mostrarDetalleEstadistica(
                "📋 Total Pedidos",
                "28",
                "Pedidos registrados actualmente.",
                "⏳ Pendientes: 5",
                "🚚 Enviados: 10",
                "✅ Entregados: 13"
            )

        }

        // Pedidos Pendientes

        cardPedidoPendiente.setOnClickListener {

            mostrarDetalleEstadistica(
                "⏳ Pedidos Pendientes",
                "5",
                "Pedidos que están en proceso.",
                "📦 Hoy: 2",
                "🚚 Próximos envíos: 3",
                "✅ Esta semana: 5"
            )

        }

        // Producto Más Vendido

        cardProductoVendido.setOnClickListener {

            mostrarDetalleEstadistica(
                "🏆 Producto Más Vendido",
                "Naruto Amigurumi",
                "Producto con más ventas.",
                "📦 Unidades vendidas: 15",
                "⭐ Calificación: 4.8",
                "💰 Precio: $70.000"
            )

        }

        // Ingresos del Mes

        cardIngresosMes.setOnClickListener {

            mostrarDetalleEstadistica(
                "📈 Ingresos del Mes",
                "$450.000",
                "Ingresos registrados en el mes actual.",
                "📦 Productos vendidos: 14",
                "✅ Pedidos completados: 9",
                "🏆 Más vendido: Naruto"
            )

        }

    }

    private fun mostrarDetalleEstadistica(
        titulo: String,
        valorPrincipal: String,
        descripcion: String,
        dato1: String,
        dato2: String,
        dato3: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.vendedor_bottom_estadistica,
            null
        )

        val txtTitulo =
            view.findViewById<TextView>(
                R.id.txtTituloEstadistica
            )

        val txtValor =
            view.findViewById<TextView>(
                R.id.txtValorPrincipal
            )

        val txtDescripcion =
            view.findViewById<TextView>(
                R.id.txtDescripcion
            )

        val txtDato1 =
            view.findViewById<TextView>(
                R.id.txtDato1
            )

        val txtDato2 =
            view.findViewById<TextView>(
                R.id.txtDato2
            )

        val txtDato3 =
            view.findViewById<TextView>(
                R.id.txtDato3
            )

        txtTitulo.text = titulo

        txtValor.text = valorPrincipal

        txtDescripcion.text = descripcion

        txtDato1.text = dato1

        txtDato2.text = dato2

        txtDato3.text = dato3

        dialog.setContentView(view)

        dialog.show()

    }

}