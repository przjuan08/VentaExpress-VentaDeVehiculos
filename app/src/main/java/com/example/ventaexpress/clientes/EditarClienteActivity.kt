package com.example.ventaexpress.clientes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Cliente
import com.example.ventaexpress.utils.LoadingDialog
import com.example.ventaexpress.utils.NetworkUtils
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class EditarClienteActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var loadingDialog: LoadingDialog

    // Views
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etTelefono: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var spinKit: SpinKitView

    private var clienteId: String? = null
    private var isEditing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_cliente)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar Loading Dialog
        loadingDialog = LoadingDialog(this)

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etTelefono = findViewById(R.id.etTelefono)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
        spinKit = findViewById(R.id.spinKit)

        // Configurar validaciones en tiempo real
        setupTextWatchers()

        // Verificar si es edición o creación
        clienteId = intent.getStringExtra("cliente_id")
        isEditing = clienteId != null

        if (isEditing) {
            // Cargar datos existentes
            cargarDatosCliente()
        }

        // Configurar listeners
        btnGuardar.setOnClickListener { guardarCliente() }
        btnCancelar.setOnClickListener { finish() }
    }

    private fun setupTextWatchers() {
        etNombre.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateNombre() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etCorreo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateCorreo() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etTelefono.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateTelefono() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun cargarDatosCliente() {
        val nombre = intent.getStringExtra("cliente_nombre") ?: ""
        val correo = intent.getStringExtra("cliente_correo") ?: ""
        val telefono = intent.getStringExtra("cliente_telefono") ?: ""

        etNombre.setText(nombre)
        etCorreo.setText(correo)
        etTelefono.setText(telefono)
    }

    private fun validateNombre(): Boolean {
        val nombre = etNombre.text.toString().trim()
        return if (nombre.isEmpty()) {
            etNombre.error = "El nombre es requerido"
            false
        } else if (nombre.length < 2) {
            etNombre.error = "Mínimo 2 caracteres"
            false
        } else if (nombre.length > 100) {
            etNombre.error = "Máximo 100 caracteres"
            false
        } else {
            etNombre.error = null
            true
        }
    }

    private fun validateCorreo(): Boolean {
        val correo = etCorreo.text.toString().trim()
        return if (correo.isEmpty()) {
            etCorreo.error = "El correo es requerido"
            false
        } else if (!isValidEmail(correo)) {
            etCorreo.error = "Formato de correo inválido"
            false
        } else {
            etCorreo.error = null
            true
        }
    }

    private fun validateTelefono(): Boolean {
        val telefono = etTelefono.text.toString().trim()
        return if (telefono.isEmpty()) {
            etTelefono.error = "El teléfono es requerido"
            false
        } else if (!isValidPhone(telefono)) {
            etTelefono.error = "Formato inválido. Use: ####-####"
            false
        } else {
            etTelefono.error = null
            true
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        return emailRegex.matches(email)
    }

    private fun isValidPhone(phone: String): Boolean {
        val phoneRegex = Regex("^[2|6|7][0-9]{3}-[0-9]{4}$")
        return phoneRegex.matches(phone)
    }

    private fun guardarCliente() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateNombre() || !validateCorreo() || !validateTelefono()) {
            Toast.makeText(this, "Por favor, complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        val cliente = if (isEditing) {
            Cliente(
                id = clienteId!!,
                nombre = nombre,
                correo = correo,
                telefono = telefono,
                userId = userId
            )
        } else {
            Cliente(
                id = UUID.randomUUID().toString(),
                nombre = nombre,
                correo = correo,
                telefono = telefono,
                userId = userId
            )
        }

        if (!cliente.isValid()) {
            Toast.makeText(this, "Datos del cliente inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val clienteRef = database.getReference("users").child(userId).child("clientes").child(cliente.id)

        clienteRef.setValue(cliente.toMap())
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Cliente ${if (isEditing) "actualizado" else "creado"} exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            loadingDialog.show()
            spinKit.visibility = View.VISIBLE
            btnGuardar.isEnabled = false
            btnCancelar.isEnabled = false
        } else {
            loadingDialog.dismiss()
            spinKit.visibility = View.GONE
            btnGuardar.isEnabled = true
            btnCancelar.isEnabled = true
        }
    }
}