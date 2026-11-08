package com.misoft.misoftapp.compras

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.misoft.misoftapp.catalogo.CatalogoActivity
import com.misoft.misoftapp.home.HomeBuyerActivity
import com.misoft.misoftapp.R
import com.misoft.misoftapp.perfil_cliente.PerfilClienteActivity

class CarritoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cliente_activity_carrito)

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNavigation.selectedItemId = R.id.carrito

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> {

                    startActivity(
                        Intent(
                            this,
                            HomeBuyerActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                R.id.categoria -> {

                    startActivity(
                        Intent(
                            this,
                            CatalogoActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                R.id.carrito -> {
                    true
                }

                R.id.perfil -> {

                    startActivity(
                        Intent(
                            this,
                            PerfilClienteActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                else -> false
            }
        }


    }
}