package com.misoft.misoftapp.catalogo

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.misoft.misoftapp.R
import com.misoft.misoftapp.home.HomeVendedorActivity
import com.misoft.misoftapp.pedidos.GestionPedidosActivity
import com.misoft.misoftapp.perfil_vendedor.PerfilVendedorActivity

class ProductosVendedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos_vendedor)


        // ------------------------------------
        // Navegación Menú

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationSeller
            )

        bottomNavigation.selectedItemId =
            R.id.productos

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home -> {

                    startActivity(
                        Intent(
                            this,
                            HomeVendedorActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                R.id.productos -> {

                    true

                }

                R.id.pedidos -> {

                    startActivity(
                        Intent(
                            this,
                            GestionPedidosActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                R.id.perfil -> {

                    startActivity(
                        Intent(
                            this,
                            PerfilVendedorActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                else -> false

            }

        }


        val btnNuevo =
            findViewById<MaterialButton>(
                R.id.btnNuevo
            )

        val imgEditarConejo =
            findViewById<ImageView>(
                R.id.imgEditarConejo
            )

        val imgEliminarConejo =
            findViewById<ImageView>(
                R.id.imgEliminarConejo
            )

        val imgEditarGirasol =
            findViewById<ImageView>(
                R.id.imgEditarGirasol
            )

        val imgEliminarGirasol =
            findViewById<ImageView>(
                R.id.imgEliminarGirasol
            )

        val imgEditarTotoro =
            findViewById<ImageView>(
                R.id.imgEditarTotoro
            )

        val imgEliminarTotoro =
            findViewById<ImageView>(
                R.id.imgEliminarTotoro
            )

        // NUEVO

        btnNuevo.setOnClickListener {

            mostrarProducto(
                "Nuevo Producto",
                "",
                "",
                "",
                "",
                "Guardar producto"
            )

        }

        // EDITAR CONEJO

        imgEditarConejo.setOnClickListener {

            mostrarProducto(
                "Editar Producto",
                "Conejito Rosa",
                "Amigurumi tejido a mano",
                "45000",
                "8",
                "Actualizar producto"
            )

        }

        // EDITAR GIRASOL

        imgEditarGirasol.setOnClickListener {

            mostrarProducto(
                "Editar Producto",
                "Girasol",
                "Flor tejida",
                "35000",
                "6",
                "Actualizar producto"
            )

        }

        // EDITAR TOTORO

        imgEditarTotoro.setOnClickListener {

            mostrarProducto(
                "Editar Producto",
                "Totoro",
                "Personaje anime",
                "60000",
                "0",
                "Actualizar producto"
            )

        }

        // ELIMINAR

        imgEliminarConejo.setOnClickListener {
            confirmarEliminacion("Conejito Rosa")
        }

        imgEliminarGirasol.setOnClickListener {
            confirmarEliminacion("Girasol")
        }

        imgEliminarTotoro.setOnClickListener {
            confirmarEliminacion("Totoro")
        }

    }

    private fun mostrarProducto(
        titulo: String,
        nombre: String,
        descripcion: String,
        precio: String,
        stock: String,
        textoBoton: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.bottom_crear_producto,
            null
        )

        dialog.setContentView(view)

        dialog.show()

    }

    private fun confirmarEliminacion(
        nombreProducto: String
    ) {

        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage(
                "¿Deseas eliminar $nombreProducto?"
            )
            .setPositiveButton(
                "Eliminar",
                null
            )
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()

    }


        }


