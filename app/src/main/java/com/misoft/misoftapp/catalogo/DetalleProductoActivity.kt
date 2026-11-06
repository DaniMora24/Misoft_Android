package com.misoft.misoftapp.catalogo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.misoft.misoftapp.R

class DetalleProductoActivity :AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_producto)

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