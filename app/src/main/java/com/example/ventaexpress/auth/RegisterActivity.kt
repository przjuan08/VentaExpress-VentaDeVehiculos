package com.example.ventaexpress.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ventaexpress.MainActivity
import com.example.ventaexpress.R
import com.example.ventaexpress.utils.LoadingDialog
import com.example.ventaexpress.utils.NetworkUtils
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var loadingDialog: LoadingDialog

    // Views
    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: TextView
    private lateinit var spinKit: SpinKitView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar Loading Dialog
        loadingDialog = LoadingDialog(this)

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        spinKit = findViewById(R.id.spinKit)

        // Configurar validaciones en tiempo real
        setupTextWatchers()

        // Configurar listeners
        btnRegister.setOnClickListener { registerUser() }
        btnLogin.setOnClickListener { goToLogin() }
    }

    private fun setupTextWatchers() {
        etNombre.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateNombre() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateEmail() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validatePassword() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateConfirmPassword() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validateNombre(): Boolean {
        val nombre = etNombre.text.toString().trim()
        return if (nombre.isEmpty()) {
            etNombre.error = "El nombre es requerido"
            false
        } else if (nombre.length < 2) {
            etNombre.error = "El nombre debe tener al menos 2 caracteres"
            false
        } else {
            etNombre.error = null
            true
        }
    }

    private fun validateEmail(): Boolean {
        val email = etEmail.text.toString().trim()
        return if (email.isEmpty()) {
            etEmail.error = "El correo electrónico es requerido"
            false
        } else if (!isValidEmail(email)) {
            etEmail.error = "Formato de correo electrónico inválido"
            false
        } else {
            etEmail.error = null
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = etPassword.text.toString().trim()
        return if (password.isEmpty()) {
            etPassword.error = "La contraseña es requerida"
            false
        } else if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            false
        } else {
            etPassword.error = null
            true
        }
    }

    private fun validateConfirmPassword(): Boolean {
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        return if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Debe confirmar la contraseña"
            false
        } else if (password != confirmPassword) {
            etConfirmPassword.error = "Las contraseñas no coinciden"
            false
        } else {
            etConfirmPassword.error = null
            true
        }
    }

    private fun registerUser() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateNombre() || !validateEmail() || !validatePassword() || !validateConfirmPassword()) {
            return
        }

        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        showLoading(true)

        // Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Usuario creado exitosamente
                    val user = auth.currentUser

                    // Actualizar perfil con nombre
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Guardar información adicional en Realtime Database
                                saveUserData(user.uid, nombre, email)

                                showLoading(false)
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                goToMainActivity()
                            } else {
                                showLoading(false)
                                Toast.makeText(this, "Error al actualizar perfil: ${profileTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    showLoading(false)
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData(uid: String, nombre: String, email: String) {
        val userRef = database.getReference("users").child(uid)

        val userData = HashMap<String, Any>()
        userData["nombre"] = nombre
        userData["email"] = email
        userData["fechaRegistro"] = System.currentTimeMillis()

        userRef.setValue(userData)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        return emailRegex.matches(email)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            loadingDialog.show()
            spinKit.visibility = android.view.View.VISIBLE
            btnRegister.isEnabled = false
        } else {
            loadingDialog.dismiss()
            spinKit.visibility = android.view.View.GONE
            btnRegister.isEnabled = true
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}