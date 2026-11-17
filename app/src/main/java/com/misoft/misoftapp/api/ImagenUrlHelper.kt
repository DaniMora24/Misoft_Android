package com.misoft.misoftapp.api

import android.net.Uri

object ImagenUrlHelper {

    fun obtenerUrl(rutaImagen: String?): String? {

        if (rutaImagen.isNullOrBlank()) {
            return null
        }

        val ruta = rutaImagen.trim()

        // SI LA API DEVUELVE UNA URL COMPLETA
        if (
            ruta.startsWith("http://", ignoreCase = true) ||
            ruta.startsWith("https://", ignoreCase = true)
        ) {

            val uri = Uri.parse(ruta)
            val host = uri.host?.lowercase()

            // REEMPLAZAR DIRECCIONES EXCLUSIVAS DEL
            // EMULADOR O DEL PROPIO COMPUTADOR
            if (
                host == "10.0.2.2" ||
                host == "localhost" ||
                host == "127.0.0.1"
            ) {

                val rutaArchivo = uri.encodedPath.orEmpty()
                val parametros = uri.encodedQuery?.let {
                    "?$it"
                }.orEmpty()

                return ApiClient.BASE_URL.trimEnd('/') +
                        "/" + rutaArchivo.trimStart('/') +
                        parametros
            }

            // URL EXTERNA O YA CORRECTA
            return ruta
        }

        // SI LA API DEVUELVE UNA RUTA RELATIVA
        return ApiClient.BASE_URL.trimEnd('/') +
                "/" + ruta.trimStart('/')
    }
}
