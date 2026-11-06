package com.misoft.misoftapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.misoft.misoftapp.R
import android.widget.TextView

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val txtIrLogin =
            findViewById<TextView>(
                R.id.txtIrLogin
            )


        // Ir a Inicio de sesopm

        txtIrLogin.setOnClickListener {

            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            startActivity(intent)

        }




    }
}