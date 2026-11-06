package com.misoft.misoftapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.misoft.misoftapp.R
import android.widget.Button
import com.misoft.misoftapp.home.HomeBuyerActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val textViewRegister =
            findViewById<TextView>(
                R.id.txtRegistro
            )

        val txtOlvide =
            findViewById<TextView>(
                R.id.txtOlvide
            )

        val btnLogin =
            findViewById<Button>(
                R.id.btnLogin
            )

        // Ir a Registro

        textViewRegister.setOnClickListener {

            val intent = Intent(
                this,
                RegisterActivity::class.java
            )

            startActivity(intent)

        }

        // Ir a Home

        btnLogin.setOnClickListener {

            val intent = Intent(
                this,
                HomeBuyerActivity::class.java
            )

            startActivity(intent)

            finish()

        }

        // Ir a Olvide Contraseña
        txtOlvide.setOnClickListener {

            val intent = Intent(
                this,
                ForgetPassword::class.java
            )

            startActivity(intent)


        }

    }
}