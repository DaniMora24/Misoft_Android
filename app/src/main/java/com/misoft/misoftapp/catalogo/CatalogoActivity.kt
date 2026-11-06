package com.misoft.misoftapp.catalogo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.misoft.misoftapp.compras.CarritoActivity
import com.misoft.misoftapp.home.HomeBuyerActivity
import com.misoft.misoftapp.perfil_cliente.PersonalInfoActivity
import com.misoft.misoftapp.R
import com.misoft.misoftapp.perfil_cliente.PerfilClienteActivity

class CatalogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)


        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNavigation.selectedItemId = R.id.categoria

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
                    true
                }

                R.id.carrito -> {

                    startActivity(
                        Intent(
                            this,
                            CarritoActivity::class.java
                        )
                    )

                    finish()

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