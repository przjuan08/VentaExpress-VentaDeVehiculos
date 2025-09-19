package com.example.ventaexpress

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ventaexpress.auth.LoginActivity
import com.example.ventaexpress.clientes.ClientesActivity
import com.example.ventaexpress.productos.ProductosActivity
import com.example.ventaexpress.ventas.VentasActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Views
    private lateinit var tvWelcome: TextView
    private lateinit var btnProductos: Button
    private lateinit var btnClientes: Button
    private lateinit var btnVentas: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Verificar autenticación
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        // Configurar toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Inicializar vistas
        tvWelcome = findViewById(R.id.tvWelcome)
        btnProductos = findViewById(R.id.btnProductos)
        btnClientes = findViewById(R.id.btnClientes)
        btnVentas = findViewById(R.id.btnVentas)

        // Configurar welcome message
        val user = auth.currentUser
        val welcomeMessage = "Bienvenido, ${user?.displayName ?: user?.email ?: "Usuario"}"
        tvWelcome.text = welcomeMessage

        // Configurar listeners
        btnProductos.setOnClickListener { goToProductos() }
        btnClientes.setOnClickListener { goToClientes() }
        btnVentas.setOnClickListener { goToVentas() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToProductos() {
        val intent = Intent(this, ProductosActivity::class.java)
        startActivity(intent)
    }

    private fun goToClientes() {
        val intent = Intent(this, ClientesActivity::class.java)
        startActivity(intent)
    }

    private fun goToVentas() {
        val intent = Intent(this, VentasActivity::class.java)
        startActivity(intent)
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun logout() {
        auth.signOut()
        goToLogin()
    }
}