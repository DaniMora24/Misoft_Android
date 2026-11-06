package com.misoft.misoftapp.perfil_cliente

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.misoft.misoftapp.R
import com.misoft.misoftapp.catalogo.CatalogoActivity
import com.misoft.misoftapp.catalogo.FavoritosActivity
import com.misoft.misoftapp.compras.CarritoActivity
import com.misoft.misoftapp.compras.ComprasActivity
import com.misoft.misoftapp.home.HomeBuyerActivity

class PerfilClienteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Variable de Menu

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        // Opciones de  Perfil

        val cardPersonalInfo =
            findViewById<CardView>(
                R.id.cardPersonalInfo
            )

        val cardDirecciones =
            findViewById<CardView>(
                R.id.cardDirecciones
            )

        val cardMetodopago =
            findViewById<CardView>(
                R.id.cardMetodopago
            )

        val cardMiscompras =
            findViewById<CardView>(
                R.id.cardMiscompras
            )

        val cardMisfavoritos =
            findViewById<CardView>(
                R.id.cardMisfavoritos
            )

        // --------------------------------------
        // Navegación Perfil

        cardPersonalInfo.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    PersonalInfoActivity::class.java
                )
            )

        }

        cardDirecciones.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    DireccionActivity::class.java
                )
            )

        }

        cardMetodopago.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MetodoPagoActivity::class.java
                )
            )

        }

        cardMiscompras.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ComprasActivity::class.java
                )
            )

        }

        cardMisfavoritos.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    FavoritosActivity::class.java
                )
            )

        }

        // ------------------------------------
        // Navegacion Menu

        bottomNavigation.selectedItemId = R.id.perfil

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
                    true
                }

                else -> false
            }
        }
    }
}