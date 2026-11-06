package com.misoft.misoftapp.home
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.misoft.misoftapp.catalogo.CatalogoActivity
import com.misoft.misoftapp.catalogo.DetalleProductoActivity
import com.misoft.misoftapp.compras.CarritoActivity
import com.misoft.misoftapp.perfil_cliente.PersonalInfoActivity
import com.misoft.misoftapp.R
import com.misoft.misoftapp.perfil_cliente.PerfilClienteActivity

class HomeBuyerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_buyer)

        val cardNaruto =
            findViewById<CardView>(
                R.id.cardNaruto
            )

        val txtVerTodos =
            findViewById<TextView>(
                R.id.txtVerTodos
            )
        val ctgAnimales =
            findViewById<ImageView>(
                R.id.ctgAnimales
            )
        val ctgFlores =
            findViewById<ImageView>(
                R.id.ctgFlores
            )
        val ctgPersonajes =
            findViewById<ImageView>(
                R.id.ctgPersonajes
            )

        cardNaruto.setOnClickListener {

            val intent = Intent(
                this,
                DetalleProductoActivity::class.java
            )

            startActivity(intent)

        }

        txtVerTodos.setOnClickListener {

            val intent = Intent(
                this,
                CatalogoActivity::class.java
            )

            startActivity(intent)

        }

        ctgAnimales.setOnClickListener {

            val intent = Intent (
                this,
                CatalogoActivity :: class.java
            )

            startActivity(intent)
        }

        ctgFlores.setOnClickListener {

            val intent = Intent (
                this,
                CatalogoActivity :: class.java
            )

            startActivity(intent)
        }

        ctgPersonajes.setOnClickListener {

            val intent = Intent (
                this,
                CatalogoActivity :: class.java
            )

            startActivity(intent)
        }


        //Menu
        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNavigation.selectedItemId = R.id.home

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> {
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