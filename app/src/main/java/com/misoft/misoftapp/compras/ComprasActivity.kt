package com.misoft.misoftapp.compras

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.misoft.misoftapp.R

class ComprasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compras)


        val imgDetalle = findViewById<ImageView>(R.id.imgDetallePedido)

        imgDetalle.setOnClickListener {

            val dialog = BottomSheetDialog(this)

            val view = layoutInflater.inflate(
                R.layout.activity_detalle_compra,
                null
            )

            dialog.setContentView(view)

            dialog.show()

        }

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