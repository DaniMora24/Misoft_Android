package com.misoft.misoftapp.pedidos

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.misoft.misoftapp.R
import com.misoft.misoftapp.catalogo.ProductosVendedorActivity
import com.misoft.misoftapp.home.HomeVendedorActivity
import com.misoft.misoftapp.perfil_vendedor.PerfilVendedorActivity

class GestionPedidosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.actuvity_pedidos_vendedor
        )

        val imgDetallePedido124 =
            findViewById<ImageView>(
                R.id.imgflechapedido
            )

        val imgDetallePedido123 =
            findViewById<ImageView>(
                R.id.imgflechapedido2
            )

        val imgDetallePedido122 =
            findViewById<ImageView>(
                R.id.imgflechapedido3
            )

        // ------------------------------------
        // Navegación Menú

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationSeller
            )

        bottomNavigation.selectedItemId =
            R.id.pedidos

        bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {

                    startActivity(
                        Intent(
                            this,
                            HomeVendedorActivity::class.java
                        )
                    )
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


        // Pedido Pendiente

        imgDetallePedido124.setOnClickListener {

            mostrarDetallePedido(
                "MCO124",
                "María Fernanda",
                "3001234567",
                "Cra 10 #20-30 Bogotá",
                "Naruto Amigurumi",
                "Nequi",
                "$45.000",
                "Pendiente"
            )

        }

        // Pedido Enviado

        imgDetallePedido123.setOnClickListener {

            mostrarDetallePedido(
                "MCO123",
                "Juan Pérez",
                "3119988776",
                "Av 68 #80-20 Bogotá",
                "Goku Amigurumi",
                "PSE",
                "$70.000",
                "Enviado"
            )

        }

        // Pedido Entregado

        imgDetallePedido122.setOnClickListener {

            mostrarDetallePedido(
                "MCO122",
                "Laura Gómez",
                "3204445566",
                "Calle 45 #12-90 Bogotá",
                "Conejito Rosa",
                "Tarjeta Visa",
                "$60.000",
                "Entregado"
            )

        }

    }

    private fun mostrarDetallePedido(
        pedido: String,
        cliente: String,
        telefono: String,
        direccion: String,
        producto: String,
        metodoPago: String,
        total: String,
        estado: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.bottom_detalle_pedido,
            null
        )

        val txtPedido =
            view.findViewById<TextView>(
                R.id.txtPedido
            )

        val txtCliente =
            view.findViewById<TextView>(
                R.id.txtCliente
            )

        val txtTelefono =
            view.findViewById<TextView>(
                R.id.txtTelefono
            )

        val txtDireccion =
            view.findViewById<TextView>(
                R.id.txtDireccion
            )

        val txtNombreProducto =
            view.findViewById<TextView>(
                R.id.txtNombreProducto
            )

        val txtMetodoPago =
            view.findViewById<TextView>(
                R.id.txtMetodoPago
            )

        val txtTotal =
            view.findViewById<TextView>(
                R.id.txtTotal
            )

        val actEstadoPedido =
            view.findViewById<AutoCompleteTextView>(
                R.id.actEstadoPedido
            )

        val btnActualizarEstado =
            view.findViewById<MaterialButton>(
                R.id.btnActualizarEstado
            )

        // Cargar datos

        txtPedido.text =
            "Pedido #$pedido"

        txtCliente.text = cliente

        txtTelefono.text = telefono

        txtDireccion.text = direccion

        txtNombreProducto.text = producto

        txtMetodoPago.text = metodoPago

        txtTotal.text = total

        // Estados

        val estados = listOf(
            "Pendiente",
            "En preparación",
            "Enviado",
            "Entregado"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            estados
        )

        actEstadoPedido.setAdapter(adapter)

        actEstadoPedido.setText(
            estado,
            false
        )

        // Actualizar (Frontend)

        btnActualizarEstado.setOnClickListener {

            dialog.dismiss()

        }

        dialog.setContentView(view)

        dialog.show()




        }
    }


