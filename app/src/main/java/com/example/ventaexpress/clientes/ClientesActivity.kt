package com.example.ventaexpress.clientes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Cliente
import com.example.ventaexpress.utils.LoadingDialog
import com.example.ventaexpress.utils.NetworkUtils
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClientesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var clienteAdapter: ClienteAdapter

    // Views
    private lateinit var rvClientes: RecyclerView
    private lateinit var fabAgregarCliente: FloatingActionButton
    private lateinit var tvEmptyState: TextView
    private lateinit var spinKit: SpinKitView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar Loading Dialog
        loadingDialog = LoadingDialog(this)

        // Inicializar vistas
        rvClientes = findViewById(R.id.rvClientes)
        fabAgregarCliente = findViewById(R.id.fabAgregarCliente)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        spinKit = findViewById(R.id.spinKit)

        // Configurar RecyclerView
        clienteAdapter = ClienteAdapter(
            emptyList(),
            onEditClick = { cliente -> editarCliente(cliente) },
            onDeleteClick = { cliente -> eliminarCliente(cliente) }
        )

        rvClientes.layoutManager = LinearLayoutManager(this)
        rvClientes.adapter = clienteAdapter

        // Configurar listeners
        fabAgregarCliente.setOnClickListener { agregarCliente() }

        // Cargar clientes
        cargarClientes()
    }

    private fun cargarClientes() {
        val userId = auth.currentUser?.uid ?: return

        showLoading(true)

        val clientesRef = database.getReference("users").child(userId).child("clientes")

        clientesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clientes = mutableListOf<Cliente>()

                for (clienteSnapshot in snapshot.children) {
                    val cliente = clienteSnapshot.getValue(Cliente::class.java)
                    cliente?.let { clientes.add(it) }
                }

                clienteAdapter.updateData(clientes)

                // Mostrar u ocultar empty state
                if (clientes.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    rvClientes.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    rvClientes.visibility = View.VISIBLE
                }

                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                if (!NetworkUtils.isNetworkAvailable(this@ClientesActivity)) {
                    tvEmptyState.text = "Error de conexión. Verifique su internet"
                } else {
                    tvEmptyState.text = "Error al cargar los clientes"
                }
                tvEmptyState.visibility = View.VISIBLE
                rvClientes.visibility = View.GONE
            }
        })
    }

    private fun agregarCliente() {
        val intent = Intent(this, EditarClienteActivity::class.java)
        startActivity(intent)
    }

    private fun editarCliente(cliente: Cliente) {
        val intent = Intent(this, EditarClienteActivity::class.java)
        intent.putExtra("cliente_id", cliente.id)
        intent.putExtra("cliente_nombre", cliente.nombre)
        intent.putExtra("cliente_correo", cliente.correo)
        intent.putExtra("cliente_telefono", cliente.telefono)
        startActivity(intent)
    }

    private fun eliminarCliente(cliente: Cliente) {
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Eliminar Cliente")
            .setMessage("¿Estás seguro de que quieres eliminar \"${cliente.nombre}\"?")
            .setPositiveButton("Eliminar") { dialog, which ->
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                val clienteRef = database.getReference("users").child(userId).child("clientes").child(cliente.id)
                clienteRef.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cliente eliminado exitosamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Cambiar colores de los botones
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(resources.getColor(R.color.white, theme))
            setBackgroundColor(resources.getColor(R.color.error, theme))
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
            setTextColor(resources.getColor(R.color.white, theme))
            setBackgroundColor(resources.getColor(R.color.text_secondary, theme))
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            loadingDialog.show()
            spinKit.visibility = View.VISIBLE
            fabAgregarCliente.isEnabled = false
        } else {
            loadingDialog.dismiss()
            spinKit.visibility = View.GONE
            fabAgregarCliente.isEnabled = true
        }
    }
}