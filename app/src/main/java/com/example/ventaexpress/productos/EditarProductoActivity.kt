package com.example.ventaexpress.productos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Producto
import com.example.ventaexpress.utils.LoadingDialog
import com.example.ventaexpress.utils.NetworkUtils
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class EditarProductoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var loadingDialog: LoadingDialog

    // Views
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etStock: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var spinKit: SpinKitView

    private var productoId: String? = null
    private var isEditing: Boolean = false
    private var fechaCreacion: Long = System.currentTimeMillis()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_producto)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar Loading Dialog
        loadingDialog = LoadingDialog(this)

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPrecio = findViewById(R.id.etPrecio)
        etStock = findViewById(R.id.etStock)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
        spinKit = findViewById(R.id.spinKit)

        // Configurar validaciones en tiempo real
        setupTextWatchers()

        // Verificar si es edición o creación
        productoId = intent.getStringExtra("producto_id")
        isEditing = productoId != null

        if (isEditing) {
            // Cargar datos existentes
            cargarDatosProducto()
        }

        // Configurar listeners
        btnGuardar.setOnClickListener { guardarProducto() }
        btnCancelar.setOnClickListener { finish() }
    }

    private fun setupTextWatchers() {
        etNombre.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateNombre() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etDescripcion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateDescripcion() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etPrecio.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var previousText = ""

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                val currentText = s.toString()
                if (currentText == previousText) return

                isFormatting = true

                // Remover el listener temporalmente para evitar loops infinitos
                val cleanText = currentText.replace("[^\\d.]".toRegex(), "")

                if (cleanText.isNotEmpty()) {
                    try {
                        val parsed = cleanText.toDouble()
                        val formatter = java.text.DecimalFormat("#,###.##")
                        val formatted = formatter.format(parsed)

                        if (formatted != currentText) {
                            etPrecio.setText(formatted)
                            etPrecio.setSelection(formatted.length)
                        }
                        previousText = formatted
                    } catch (e: Exception) {
                        // Si hay error, mantener el texto actual
                        previousText = currentText
                    }
                } else {
                    previousText = ""
                }

                validatePrecio()
                isFormatting = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etStock.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateStock() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun cargarDatosProducto() {
        val nombre = intent.getStringExtra("producto_nombre") ?: ""
        val descripcion = intent.getStringExtra("producto_descripcion") ?: ""
        val precio = intent.getDoubleExtra("producto_precio", 0.0)
        val stock = intent.getIntExtra("producto_stock", 0)
        fechaCreacion = intent.getLongExtra("producto_fechaCreacion", System.currentTimeMillis())

        etNombre.setText(nombre)
        etDescripcion.setText(descripcion)
        etPrecio.setText(precio.toString())
        etStock.setText(stock.toString())
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

    private fun validateDescripcion(): Boolean {
        val descripcion = etDescripcion.text.toString().trim()
        return if (descripcion.isEmpty()) {
            etDescripcion.error = "La descripción es requerida"
            false
        } else if (descripcion.length < 10) {
            etDescripcion.error = "Mínimo 10 caracteres"
            false
        } else if (descripcion.length > 500) {
            etDescripcion.error = "Máximo 500 caracteres"
            false
        } else {
            etDescripcion.error = null
            true
        }
    }

    private fun validatePrecio(): Boolean {
        val precioStr = etPrecio.text.toString().replace(",", "").trim()
        return if (precioStr.isEmpty()) {
            etPrecio.error = "El precio es requerido"
            false
        } else {
            try {
                val precio = precioStr.toDouble()
                if (precio <= 0) {
                    etPrecio.error = "El precio debe ser mayor a 0"
                    false
                } else if (precio > 1000000) {
                    etPrecio.error = "Precio demasiado alto"
                    false
                } else {
                    etPrecio.error = null
                    true
                }
            } catch (e: NumberFormatException) {
                etPrecio.error = "Precio inválido"
                false
            }
        }
    }

    private fun validateStock(): Boolean {
        val stockStr = etStock.text.toString().trim()
        return if (stockStr.isEmpty()) {
            etStock.error = "El stock es requerido"
            false
        } else {
            try {
                val stock = stockStr.toInt()
                if (stock < 0) {
                    etStock.error = "El stock no puede ser negativo"
                    false
                } else if (stock > 10000) {
                    etStock.error = "Stock demasiado alto"
                    false
                } else {
                    etStock.error = null
                    true
                }
            } catch (e: NumberFormatException) {
                etStock.error = "Stock inválido"
                false
            }
        }
    }

    private fun guardarProducto() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateNombre() || !validateDescripcion() || !validatePrecio() || !validateStock()) {
            Toast.makeText(this, "Por favor, complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        val precio = etPrecio.text.toString().replace(",", "").toDouble()
        val nombre = etNombre.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val stock = etStock.text.toString().toInt()
        val userId = auth.currentUser?.uid ?: return

        val producto = if (isEditing) {
            Producto(
                id = productoId!!,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                userId = userId,
                fechaCreacion = fechaCreacion // Usar la fecha original al editar
            )
        } else {
            Producto(
                id = UUID.randomUUID().toString(),
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                userId = userId,
                fechaCreacion = System.currentTimeMillis() // Nueva fecha al crear
            )
        }

        if (!producto.isValid()) {
            Toast.makeText(this, "Datos del producto inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val productoRef = database.getReference("users").child(userId).child("productos").child(producto.id)

        productoRef.setValue(producto.toMap())
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Producto ${if (isEditing) "actualizado" else "creado"} exitosamente", Toast.LENGTH_SHORT).show()
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

private fun formatPrice(input: String): String {
    if (input.isEmpty()) return ""

    // Remover comas existentes y caracteres no numéricos excepto el punto decimal
    val cleanString = input.replace("[^\\d.]".toRegex(), "")

    if (cleanString.isEmpty()) return ""

    try {
        // Convertir a número
        val parsed = cleanString.toDouble()

        // Formatear con comas y 2 decimales
        val formatter = java.text.DecimalFormat("#,###.##")
        return formatter.format(parsed)
    } catch (e: Exception) {
        return input
    }
}