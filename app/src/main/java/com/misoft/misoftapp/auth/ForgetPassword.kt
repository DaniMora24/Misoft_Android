package com.misoft.misoftapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.misoft.misoftapp.R

class ForgetPassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)


        val txtVolver =
            findViewById<TextView>(
                R.id.txtVolver
            )

        // Ir a Login

        txtVolver.setOnClickListener {

            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            startActivity(intent)

        }


    }



}