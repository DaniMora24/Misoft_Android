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

class DireccionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cliente_activity_direccion)

        val btnAgregarDireccion =
            findViewById<MaterialButton>(
                R.id.btnAgregarDireccion
            )

        val imgEditarCasa =
            findViewById<ImageView>(
                R.id.imgEditarCasa
            )

        val imgEliminarCasa =
            findViewById<ImageView>(
                R.id.imgEliminarCasa
            )

        val imgEditarTrabajo =
            findViewById<ImageView>(
                R.id.imgEditarTrabajo
            )

        val imgEliminarTrabajo =
            findViewById<ImageView>(
                R.id.imgEliminarTrabajo
            )


        //Flecha de retroceso

        val imgBack =
            findViewById<ImageView>(
                R.id.imgBack
            )

        imgBack.setOnClickListener {

            finish()

        }



        // Agregar Dirección

        btnAgregarDireccion.setOnClickListener {

            mostrarBottomSheet(
                "Agregar Dirección",
                ""
            )

        }





        // Editar Casa

        imgEditarCasa.setOnClickListener {

            mostrarBottomSheet(
                "Editar Dirección",
                "Cra 123 #45-67, Bogotá"
            )

        }

        // Editar Trabajo

        imgEditarTrabajo.setOnClickListener {

            mostrarBottomSheet(
                "Editar Dirección",
                "Av. El Dorado #20-15, Bogotá"
            )

        }

        // Eliminar Casa

        imgEliminarCasa.setOnClickListener {

            confirmarEliminacion(
                "Casa"
            )

        }

        // Eliminar Trabajo

        imgEliminarTrabajo.setOnClickListener {

            confirmarEliminacion(
                "Trabajo"
            )

        }
    }

    private fun mostrarBottomSheet(
        tituloCampo: String,
        valorActual: String
    ) {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(
            R.layout.bottom_edit_info,
            null
        )

        val txtTitulo =
            view.findViewById<TextView>(
                R.id.txtTituloEditar
            )

        val edtValor =
            view.findViewById<TextInputEditText>(
                R.id.edtValor
            )

        val btnGuardar =
            view.findViewById<MaterialButton>(
                R.id.btnGuardar
            )

        txtTitulo.text = tituloCampo

        edtValor.setText(valorActual)

        btnGuardar.setOnClickListener {

            // Aquí irá la lógica con Firebase o Backend

            dialog.dismiss()

        }

        dialog.setContentView(view)

        dialog.show()
    }

    private fun confirmarEliminacion(
        nombreDireccion: String
    ) {

        AlertDialog.Builder(this)
            .setTitle("Eliminar dirección")
            .setMessage(
                "¿Deseas eliminar la dirección $nombreDireccion?"
            )
            .setPositiveButton("Eliminar") { _, _ ->

                // Aquí irá la lógica de eliminación

            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



}