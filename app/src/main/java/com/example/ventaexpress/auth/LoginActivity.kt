package com.example.ventaexpress.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ventaexpress.MainActivity
import com.example.ventaexpress.R
import com.example.ventaexpress.utils.LoadingDialog
import com.example.ventaexpress.utils.NetworkUtils
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import java.util.Arrays

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var loadingDialog: LoadingDialog

    // Views
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: TextView
    private lateinit var btnFacebook: Button
    private lateinit var btnGitHub: Button
    private lateinit var spinKit: SpinKitView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inicializar Facebook Callback Manager
        callbackManager = CallbackManager.Factory.create()

        // Inicializar Loading Dialog
        loadingDialog = LoadingDialog(this)

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnFacebook = findViewById(R.id.btnFacebook)
        btnGitHub = findViewById(R.id.btnGitHub)
        spinKit = findViewById(R.id.spinKit)

        // Configurar validaciones en tiempo real
        setupTextWatchers()

        // Configurar listeners
        btnLogin.setOnClickListener { loginWithEmail() }
        btnRegister.setOnClickListener { goToRegister() }
        btnFacebook.setOnClickListener { loginWithFacebook() }
        btnGitHub.setOnClickListener { loginWithGitHub() }

        // Verificar si ya hay una sesión activa
        checkCurrentUser()
    }

    private fun setupTextWatchers() {
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

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            goToMainActivity()
        }
    }

    private fun loginWithEmail() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateEmail() || !validatePassword()) {
            return
        }

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        showLoading(true)

        // Autenticar con Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginWithFacebook() {
        showLoading(true)
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList())
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                showLoading(false)
                Toast.makeText(this@LoginActivity, "Inicio de sesión con Facebook cancelado", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                showLoading(false)
                Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión con Facebook exitoso", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun loginWithGitHub() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        val provider = OAuthProvider.newBuilder("github.com")
        provider.addCustomParameter("prompt", "consent")

        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask
                .addOnSuccessListener { authResult ->
                    showLoading(false)
                    Toast.makeText(this, "Inicio de sesión con GitHub exitoso", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener { authResult ->
                    showLoading(false)
                    Toast.makeText(this, "Inicio de sesión con GitHub exitoso", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        return emailRegex.matches(email)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            loadingDialog.show()
            spinKit.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            btnFacebook.isEnabled = false
            btnGitHub.isEnabled = false
        } else {
            loadingDialog.dismiss()
            spinKit.visibility = View.GONE
            btnLogin.isEnabled = true
            btnFacebook.isEnabled = true
            btnGitHub.isEnabled = true
        }
    }

    private fun goToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}