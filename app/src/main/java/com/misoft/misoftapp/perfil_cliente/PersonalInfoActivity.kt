package com.misoft.misoftapp.perfil_cliente

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.misoft.misoftapp.R

class PersonalInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cliente_activity_personal_info)

        val imgEditarNombre =
            findViewById<ImageView>(R.id.imgEditarNombre)

        val imgEditarApellido =
            findViewById<ImageView>(R.id.imgEditarApellido)

        val imgEditarCorreo =
            findViewById<ImageView>(R.id.imgEditarCorreo)

        val imgEditarTelefono =
            findViewById<ImageView>(R.id.imgEditarTelefono)

        val imgEditarDireccion =
            findViewById<ImageView>(R.id.imgEditarDireccion)

        val imgEditarFechaNacimiento =
            findViewById<ImageView>(R.id.imgEditarFechaNacimiento)

        //Flecha de retroceso

        val imgBack =
            findViewById<ImageView>(
                R.id.imgBack
            )

        imgBack.setOnClickListener {

            finish()

        }


        //Menu


        imgEditarNombre.setOnClickListener {
            mostrarBottomSheet(
                "Editar Nombre",
                "Cristian"
            )
        }

        imgEditarApellido.setOnClickListener {
            mostrarBottomSheet(
                "Editar Apellido",
                "Mora"
            )
        }

        imgEditarCorreo.setOnClickListener {
            mostrarBottomSheet(
                "Editar Correo Electrónico",
                "cristian@email.com"
            )
        }

        imgEditarTelefono.setOnClickListener {
            mostrarBottomSheet(
                "Editar Teléfono",
                "3001234567"
            )
        }

        imgEditarDireccion.setOnClickListener {
            mostrarBottomSheet(
                "Editar Dirección",
                "Cra 123 #45-67, Bogotá"
            )
        }

        imgEditarFechaNacimiento.setOnClickListener {
            mostrarBottomSheet(
                "Editar Fecha de Nacimiento",
                "01/01/2000"
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

        txtTitulo.text = tituloCampo

        edtValor.setText(valorActual)

        dialog.setContentView(view)

        dialog.show()







    }



}