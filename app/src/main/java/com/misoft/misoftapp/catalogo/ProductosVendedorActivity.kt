package com.misoft.misoftapp.catalogo

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.misoft.misoftapp.R
import com.misoft.misoftapp.home.HomeVendedorActivity
import com.misoft.misoftapp.pedidos.GestionPedidosActivity
import com.misoft.misoftapp.perfil_vendedor.PerfilVendedorActivity

class ProductosVendedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_productos_vendedor
        )

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

        // ------------------------------------
        // Views

        val btnNuevo =
            findViewById<MaterialButton>(
                R.id.btnNuevo
            )

        val imgEditarConejo =
            findViewById<ImageView>(
                R.id.imgEditarConejo
            )

        val imgEditarGirasol =
            findViewById<ImageView>(
                R.id.imgEditarGirasol
            )

        val imgEditarTotoro =
            findViewById<ImageView>(
                R.id.imgEditarTotoro
            )

        val imgEliminarConejo =
            findViewById<ImageView>(
                R.id.imgEliminarConejo
            )

        val imgEliminarGirasol =
            findViewById<ImageView>(
                R.id.imgEliminarGirasol
            )

        val imgEliminarTotoro =
            findViewById<ImageView>(
                R.id.imgEliminarTotoro
            )

        // ------------------------------------
        // Crear

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

        // ------------------------------------
        // Detalle Producto

        imgEditarConejo.setOnClickListener {

            mostrarDetalleProducto(
                "Snoopy",
                "$45.000",
                "Amigurumi tejido a mano inspirado en Snoopy.",
                "20 cm",
                "Hilo de algodón",
                "5"
            )

        }

        imgEditarGirasol.setOnClickListener {

            mostrarDetalleProducto(
                "Gerbera Rosa",
                "$35.000",
                "Flor tejida artesanalmente.",
                "18 cm",
                "Hilo premium",
                "10"
            )

        }

        imgEditarTotoro.setOnClickListener {

            mostrarDetalleProducto(
                "Goku",
                "$55.000",
                "Personaje anime tejido a mano.",
                "25 cm",
                "Hilo de algodón",
                "0"
            )

        }

        // ------------------------------------
        // Eliminar

        imgEliminarConejo.setOnClickListener {

            confirmarEliminacion(
                "Snoopy"
            )

        }

        imgEliminarGirasol.setOnClickListener {

            confirmarEliminacion(
                "Gerbera Rosa"
            )

        }

        imgEliminarTotoro.setOnClickListener {

            confirmarEliminacion(
                "Goku"
            )

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

        val txtTitulo =
            view.findViewById<TextView>(
                R.id.txtTituloProducto
            )

        val edtNombre =
            view.findViewById<TextInputEditText>(
                R.id.edtNombreProducto
            )

        val edtDescripcion =
            view.findViewById<TextInputEditText>(
                R.id.edtDescripcion
            )

        val edtPrecio =
            view.findViewById<TextInputEditText>(
                R.id.edtPrecio
            )

        val edtStock =
            view.findViewById<TextInputEditText>(
                R.id.edtStock
            )

        val btnGuardar =
            view.findViewById<MaterialButton>(
                R.id.btnGuardarProducto
            )

        txtTitulo.text = titulo

        edtNombre.setText(nombre)
        edtDescripcion.setText(descripcion)
        edtPrecio.setText(precio)
        edtStock.setText(stock)

        btnGuardar.text = textoBoton

        btnGuardar.setOnClickListener {

            dialog.dismiss()

        }

        dialog.setContentView(view)

        dialog.show()

    }

    private fun mostrarDetalleProducto(
        nombre: String,
        precio: String,
        descripcion: String,
        altura: String,
        material: String,
        stock: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.bottom_detalle_producto_vendedor,
            null
        )

        view.findViewById<TextView>(
            R.id.txtNombreProducto
        ).text = nombre

        view.findViewById<TextView>(
            R.id.txtPrecioProducto
        ).text = precio

        view.findViewById<TextView>(
            R.id.txtDescripcion
        ).text = descripcion

        view.findViewById<TextView>(
            R.id.txtAltura
        ).text = "📏 Altura: $altura"

        view.findViewById<TextView>(
            R.id.txtMaterial
        ).text = "🧶 Material: $material"

        view.findViewById<TextView>(
            R.id.txtStock
        ).text = "📦 Stock: $stock"

        val btnEditar =
            view.findViewById<MaterialButton>(
                R.id.btnEditarProducto
            )

        val btnEliminar =
            view.findViewById<MaterialButton>(
                R.id.btnEliminarProducto
            )

        btnEditar.setOnClickListener {

            dialog.dismiss()

            mostrarProducto(
                "Editar Producto",
                nombre,
                descripcion,
                precio.replace("$", "")
                    .replace(".", ""),
                stock,
                "Actualizar producto"
            )

        }

        btnEliminar.setOnClickListener {

            dialog.dismiss()

            confirmarEliminacion(
                nombre
            )

        }

        dialog.setContentView(view)

        dialog.show()

    }

    private fun confirmarEliminacion(
        nombreProducto: String
    ) {

        AlertDialog.Builder(this)
            .setTitle(
                "Eliminar producto"
            )
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