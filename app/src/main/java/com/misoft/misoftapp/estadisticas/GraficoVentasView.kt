package com.misoft.misoftapp.estadisticas

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

data class PuntoVenta(
    val etiqueta: String,
    val total: Float
)

class GraficoVentasView(
    context: Context,
    private val puntos: List<PuntoVenta>
) : View(context) {

    private val pintura = Paint(
        Paint.ANTI_ALIAS_FLAG
    )

    private val morado = Color.parseColor("#9B7AE0")
    private val gris = Color.parseColor("#999999")
    private val verde = Color.parseColor("#2E7D32")

    private fun dp(valor: Float): Float =
        valor * resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (puntos.isEmpty()) return

        val izquierda = dp(16f)
        val derecha = width - dp(12f)
        val arriba = dp(28f)
        val abajo = height - dp(36f)

        val ancho = derecha - izquierda
        val alto = abajo - arriba

        if (ancho <= 0 || alto <= 0) return

        val maximo = puntos.maxOf {
            it.total.coerceAtLeast(0f)
        }.coerceAtLeast(1f)

        // Dibujar líneas de referencia.
        pintura.color = Color.parseColor("#EAE3F2")
        pintura.strokeWidth = dp(1f)

        for (i in 0..4) {
            val y = abajo - (alto * i / 4f)

            canvas.drawLine(
                izquierda,
                y,
                derecha,
                y,
                pintura
            )
        }

        val cantidad = puntos.size
        val espacio = ancho / cantidad

        val anchoBarra = (
                espacio * 0.65f
                ).coerceAtMost(dp(26f))

        val saltoEtiquetas = when {
            cantidad <= 8 -> 1
            cantidad <= 16 -> 2
            cantidad <= 35 -> 5
            else -> (cantidad / 8).coerceAtLeast(1)
        }

        puntos.forEachIndexed { indice, punto ->

            val xCentro = izquierda +
                    espacio * (indice + 0.5f)

            val valor = punto.total
                .coerceAtLeast(0f)

            val alturaBarra = alto * (
                    valor / maximo
                    )

            val ySuperior = abajo - alturaBarra

            pintura.color = morado
            pintura.style = Paint.Style.FILL

            canvas.drawRoundRect(
                xCentro - anchoBarra / 2f,
                ySuperior,
                xCentro + anchoBarra / 2f,
                abajo,
                dp(4f),
                dp(4f),
                pintura
            )

            if (indice % saltoEtiquetas == 0) {

                pintura.color = gris
                pintura.textAlign =
                    Paint.Align.CENTER

                pintura.textSize = dp(9f)

                canvas.drawText(
                    punto.etiqueta,
                    xCentro,
                    abajo + dp(17f),
                    pintura
                )
            }
        }

        // Valor máximo del gráfico.
        pintura.color = verde
        pintura.textAlign = Paint.Align.LEFT
        pintura.textSize = dp(11f)

        val maximoTexto = when {
            maximo >= 1_000_000f ->
                "${"%.1f".format(maximo / 1_000_000f)} M"

            maximo >= 1_000f ->
                "${"%.0f".format(maximo / 1_000f)} mil"

            else ->
                "${maximo.toInt()}"
        }

        canvas.drawText(
            "$ $maximoTexto",
            izquierda,
            dp(16f),
            pintura
        )
    }
}
