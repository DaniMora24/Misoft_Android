package com.misoft.misoftapp.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.misoft.misoftapp.R
import com.misoft.misoftapp.estadisticas.EstadisticasVendedorActivity
import com.misoft.misoftapp.pedidos.GestionPedidosActivity
import com.misoft.misoftapp.perfil_vendedor.PerfilVendedorActivity
import com.misoft.misoftapp.catalogo.ProductosVendedorActivity

class HomeVendedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.vendedor_activity_home
        )

        val cardMisProductos =
            findViewById<CardView>(
                R.id.cardMisProductos
            )

        val cardPedidos =
            findViewById<CardView>(
                R.id.cardPedidos
            )

        val cardCrearProducto =
            findViewById<CardView>(
                R.id.cardCrearProducto
            )

        val cardEstadisticas =
            findViewById<CardView>(
                R.id.cardEstadisticas
            )



        // Accesos rápidos

        cardMisProductos.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ProductosVendedorActivity::class.java
                )
            )

        }

        cardCrearProducto.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ProductosVendedorActivity::class.java
                )
            )

        }

        cardPedidos.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    GestionPedidosActivity::class.java
                )
            )

        }

        cardEstadisticas.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    EstadisticasVendedorActivity::class.java
                )
            )

        }

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationSeller
            )

        // HOME seleccionado

        bottomNavigation.selectedItemId =
            R.id.home

        // Navegación inferior

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> {

                    true

                }

                R.id.productos -> {

                    startActivity(
                        Intent(
                            this,
                            ProductosVendedorActivity::class.java
                        )
                    )


                    true

                }

                R.id.pedidos -> {

                    startActivity(
                        Intent(
                            this,
                            GestionPedidosActivity::class.java
                        )
                    )


                    true

                }

                R.id.perfil -> {

                    startActivity(
                        Intent(
                            this,
                            PerfilVendedorActivity::class.java
                        )
                    )


                    true

                }

                else -> false

            }

        }

        }

    }

