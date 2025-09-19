package com.example.ventaexpress

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.ventaexpress.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario está autenticado después de 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthStatus()
        }, 2000)
    }

    private fun checkAuthStatus() {
        val currentUser = auth.currentUser
        val intent = if (currentUser != null) {
            // Usuario autenticado, ir al MainActivity
            Intent(this, MainActivity::class.java)
        } else {
            // Usuario no autenticado, ir al Login
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}