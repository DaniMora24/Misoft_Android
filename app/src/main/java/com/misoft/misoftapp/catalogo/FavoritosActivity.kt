package com.misoft.misoftapp.catalogo

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.misoft.misoftapp.R

class FavoritosActivity :AppCompatActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cliente_activity_favoritos)

        //Flecha de retroceso

        val imgBack =
            findViewById<ImageView>(
                R.id.imgBack
            )

        imgBack.setOnClickListener {

            finish()

        }

    }
}