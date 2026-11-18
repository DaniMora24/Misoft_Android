package com.misoft.misoftapp.perfil_cliente

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.misoft.misoftapp.R

class AyudaContactoActivity : AppCompatActivity() {

    // Configurar cuando tengas los datos reales.
    private val whatsappTienda = ""
    private val instagramTienda = ""
    private val horarioAtencion = "Horario por confirmar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.cliente_activity_ayuda_contacto)

        findViewById<View>(R.id.btnVolverAyuda)
            .setOnClickListener {
                finish()
            }

        findViewById<TextView>(R.id.txtHorarioAtencionAyuda)
            .text = horarioAtencion

        findViewById<CardView>(R.id.cardWhatsappAyuda)
            .setOnClickListener {
                abrirWhatsApp()
            }

        findViewById<CardView>(R.id.cardInstagramAyuda)
            .setOnClickListener {
                abrirInstagram()
            }

        configurarPreguntas()
    }

    private fun abrirWhatsApp() {

        val numero = whatsappTienda.filter { it.isDigit() }

        if (numero.isBlank()) {
            mostrarMensaje("El WhatsApp de la tienda aún no está configurado")
            return
        }

        val mensaje = Uri.encode(
            "Hola, necesito ayuda con una consulta sobre Michigurumis."
        )

        abrirEnlace("https://wa.me/$numero?text=$mensaje")
    }

    private fun abrirInstagram() {

        val usuario = instagramTienda
            .trim()
            .removePrefix("@")

        if (usuario.isBlank()) {
            mostrarMensaje("El Instagram de la tienda aún no está configurado")
            return
        }

        abrirEnlace("https://www.instagram.com/$usuario/")
    }

    private fun abrirEnlace(url: String) {

        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        } catch (e: ActivityNotFoundException) {
            mostrarMensaje("No se encontró una aplicación para abrir el enlace")
        }
    }

    private fun configurarPreguntas() {

        val contenedor = findViewById<LinearLayout>(
            R.id.contenedorPreguntasAyuda
        )

        val preguntas = listOf(
            "¿Cómo consulto mis pedidos?" to
                    "Ingresa a Mi perfil y selecciona Mis compras para consultar tus pedidos.",

            "¿Puedo pedir un amigurumi personalizado?" to
                    "Puedes comunicarte con la tienda para consultar si se aceptan pedidos personalizados.",

            "¿Cuánto tarda mi pedido?" to
                    "El tiempo depende de la preparación y la entrega. Consulta con la tienda para conocer los detalles.",

            "¿Qué hago si tengo un problema con mi compra?" to
                    "Comunícate por WhatsApp y proporciona el número de tu pedido para recibir ayuda."
        )

        preguntas.forEach { (pregunta, respuesta) ->

            val tarjeta = CardView(this).apply {
                radius = dp(16).toFloat()
                cardElevation = dp(2).toFloat()
                setCardBackgroundColor(Color.WHITE)
                useCompatPadding = true
            }

            val contenido = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
            }

            val encabezado = TextView(this).apply {
                text = "$pregunta  ▾"
                textSize = 15f
                setTextColor(Color.parseColor("#6A4C93"))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val descripcion = TextView(this).apply {
                text = respuesta
                textSize = 14f
                setTextColor(Color.parseColor("#846D94"))
                setPadding(0, dp(12), 0, 0)
                visibility = View.GONE
            }

            contenido.addView(encabezado)
            contenido.addView(descripcion)
            tarjeta.addView(contenido)

            tarjeta.setOnClickListener {

                val expandir = descripcion.visibility != View.VISIBLE

                descripcion.visibility =
                    if (expandir) View.VISIBLE else View.GONE

                encabezado.text =
                    "$pregunta  ${if (expandir) "▴" else "▾"}"
            }

            contenedor.addView(tarjeta)
        }
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
