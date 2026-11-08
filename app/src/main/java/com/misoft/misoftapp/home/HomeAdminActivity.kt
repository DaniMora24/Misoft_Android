package com.misoft.misoftapp.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.misoft.misoftapp.R
import com.misoft.misoftapp.estadisticas.ReportesAdminActivity
import com.misoft.misoftapp.usuarios.UsuariosAdminActivity
import com.misoft.misoftapp.usuarios.VendedoresAdminActivity

class HomeAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.admin_activity_home
        )



        // ------------------------------------
        // Bottom Navigation

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationAdmin
            )

        bottomNavigation.selectedItemId =
            R.id.home

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> {

                    true

                }

                R.id.usuarios -> {

                    startActivity(
                        Intent(
                            this,
                            UsuariosAdminActivity::class.java
                        )
                    )

                    finish()

                    true

                }

                R.id.vendedores -> {

                    startActivity(
                        Intent(
                            this,
                            VendedoresAdminActivity::class.java
                        )
                    )

                    finish()

                    true

                }

                R.id.reportes -> {

                    startActivity(
                        Intent(
                            this,
                            ReportesAdminActivity::class.java
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