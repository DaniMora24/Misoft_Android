package com.misoft.misoftapp.estadisticas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.misoft.misoftapp.R
import com.misoft.misoftapp.home.HomeAdminActivity
import com.misoft.misoftapp.usuarios.UsuariosAdminActivity
import com.misoft.misoftapp.usuarios.VendedoresAdminActivity

class ReportesAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.admin_activity_reportes
        )

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationAdmin
            )

        bottomNavigation.selectedItemId =
            R.id.reportes

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> {

                    startActivity(
                        Intent(
                            this,
                            HomeAdminActivity::class.java
                        )
                    )

                    finish()

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

                    true

                }

                else -> false

            }

        }

    }

}