
package com.misoft.misoftapp.perfil_cliente

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.misoft.misoftapp.R

class MetodoPagoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cliente_activity_metodos_pago)

        val btnAgregarMetodo =
            findViewById<MaterialButton>(
                R.id.btnAgregarMetodo
            )

        val imgEditarVisa =
            findViewById<ImageView>(
                R.id.imgEditarVisa
            )

        val imgEliminarVisa =
            findViewById<ImageView>(
                R.id.imgEliminarVisa
            )

        val imgEditarMaster =
            findViewById<ImageView>(
                R.id.imgEditarMaster
            )

        val imgEliminarMaster =
            findViewById<ImageView>(
                R.id.imgEliminarMaster
            )


        //Flecha de retroceso

        val imgBack =
            findViewById<ImageView>(
                R.id.imgBack
            )

        imgBack.setOnClickListener {

            finish()

        }

        // AGREGAR

        btnAgregarMetodo.setOnClickListener {

            mostrarBottomSheetAgregar()

        }

        // EDITAR VISA

        imgEditarVisa.setOnClickListener {

            mostrarBottomSheetEditar(
                "Visa",
                "Cristian Mora",
                "1234",
                "12/30",
                "Visa Principal"
            )

        }

        // EDITAR MASTERCARD

        imgEditarMaster.setOnClickListener {

            mostrarBottomSheetEditar(
                "Mastercard",
                "Cristian Mora",
                "5678",
                "11/29",
                "Master Principal"
            )

        }

        // ELIMINAR VISA

        imgEliminarVisa.setOnClickListener {

            confirmarEliminacion(
                "Visa terminada en 1234"
            )

        }

        // ELIMINAR MASTER

        imgEliminarMaster.setOnClickListener {

            confirmarEliminacion(
                "Mastercard terminada en 5678"
            )

        }
    }

    private fun mostrarBottomSheetAgregar() {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.cliente_bottom_selector_pago,
            null
        )

        dialog.setContentView(view)

        dialog.show()

        val cardDebito =
            view.findViewById<androidx.cardview.widget.CardView>(
                R.id.cardDebito
            )

        val cardCredito =
            view.findViewById<androidx.cardview.widget.CardView>(
                R.id.cardCredito
            )

        val cardPse =
            view.findViewById<androidx.cardview.widget.CardView>(
                R.id.cardPse
            )

        val cardNequi =
            view.findViewById<androidx.cardview.widget.CardView>(
                R.id.cardNequi
            )

        cardDebito.setOnClickListener {

            dialog.dismiss()

            abrirFormularioTarjeta(
                "Tarjeta Débito"
            )

        }

        cardCredito.setOnClickListener {

            dialog.dismiss()

            abrirFormularioTarjeta(
                "Tarjeta Crédito"
            )

        }

        cardPse.setOnClickListener {

            dialog.dismiss()

            abrirFormularioPse()

        }

        cardNequi.setOnClickListener {

            dialog.dismiss()

            abrirFormularioNequi()

        }

    }

    private fun abrirFormularioTarjeta(
        tituloFormulario: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.cliente_bottom_tarjeta_pago,
            null
        )

        val titulo =
            view.findViewById<TextView>(
                R.id.txtTituloMetodoPago
            )

        titulo.text = tituloFormulario

        dialog.setContentView(view)

        dialog.show()
    }

    private fun abrirFormularioPse() {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.cliente_bottom_pse_pago,
            null
        )

        dialog.setContentView(view)

        dialog.show()

    }

    private fun abrirFormularioNequi() {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.cliente_bottom_nequi_pago,
            null
        )

        dialog.setContentView(view)

        dialog.show()

    }

    private fun mostrarBottomSheetEditar(
        tipo: String,
        titular: String,
        numeroFinal: String,
        vencimiento: String,
        alias: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.cliente_bottom_tarjeta_pago,
            null
        )

        val txtTitulo =
            view.findViewById<TextView>(
                R.id.txtTituloMetodoPago
            )

        val edtTitular =
            view.findViewById<TextInputEditText>(
                R.id.edtTitular
            )

        val edtNumero =
            view.findViewById<TextInputEditText>(
                R.id.edtNumeroTarjeta
            )

        val edtFecha =
            view.findViewById<TextInputEditText>(
                R.id.edtFechaVencimiento
            )


        txtTitulo.text = "Editar $tipo"

        edtTitular.setText(titular)

        edtNumero.setText(
            "**** **** **** $numeroFinal"
        )

        edtFecha.setText(vencimiento)


        dialog.setContentView(view)

        dialog.show()
    }


    private fun confirmarEliminacion(
        descripcion: String
    ) {

        AlertDialog.Builder(this)
            .setTitle("Eliminar método de pago")
            .setMessage(
                "¿Deseas eliminar $descripcion?"
            )
            .setPositiveButton("Eliminar") { _, _ ->

                // Backend después

            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



}