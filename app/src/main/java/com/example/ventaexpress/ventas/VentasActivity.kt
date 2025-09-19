package com.example.ventaexpress.ventas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Venta
import com.example.ventaexpress.utils.LoadingDialog
import com.example.ventaexpress.utils.NetworkUtils
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class VentasActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var ventaAdapter: VentaAdapter

    // Views
    private lateinit var rvVentas: RecyclerView
    private lateinit var fabNuevaVenta: FloatingActionButton
    private lateinit var tvEmptyState: TextView
    private lateinit var tvTotalVentas: TextView
    private lateinit var spinKit: SpinKitView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar Loading Dialog
        loadingDialog = LoadingDialog(this)

        // Inicializar vistas
        rvVentas = findViewById(R.id.rvVentas)
        fabNuevaVenta = findViewById(R.id.fabNuevaVenta)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvTotalVentas = findViewById(R.id.tvTotalVentas)
        spinKit = findViewById(R.id.spinKit)

        // Configurar RecyclerView
        ventaAdapter = VentaAdapter(emptyList())

        rvVentas.layoutManager = LinearLayoutManager(this)
        rvVentas.adapter = ventaAdapter

        // Configurar listeners - SOLO para el FAB
        fabNuevaVenta.setOnClickListener {
            nuevaVenta()
        }

        // Asegurar que el FAB no intercepte otros clics
        fabNuevaVenta.isClickable = true
        fabNuevaVenta.isFocusable = true

        // Cargar ventas
        cargarVentas()
    }

    private fun cargarVentas() {
        val userId = auth.currentUser?.uid ?: return

        showLoading(true)

        val ventasRef = database.getReference("users").child(userId).child("ventas")

        ventasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ventas = mutableListOf<Venta>()
                var totalVentas = 0.0

                for (ventaSnapshot in snapshot.children) {
                    val venta = ventaSnapshot.getValue(Venta::class.java)
                    venta?.let {
                        ventas.add(it)
                        totalVentas += it.total
                    }
                }

                // Ordenar ventas por fecha (más recientes primero)
                ventas.sortByDescending { it.fecha }

                ventaAdapter.updateData(ventas)

                // Formatear el total con comas
                val formatter = java.text.DecimalFormat("$#,###.##")
                formatter.minimumFractionDigits = 2
                formatter.maximumFractionDigits = 2

                // Obtener nombre del usuario actual
                val userName = auth.currentUser?.displayName ?: "Vendedor"

                tvTotalVentas.text = "$userName, has vendido: ${formatter.format(totalVentas)}"

                // Mostrar u ocultar empty state
                if (ventas.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    rvVentas.visibility = View.GONE
                    tvTotalVentas.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    rvVentas.visibility = View.VISIBLE
                    tvTotalVentas.visibility = View.VISIBLE
                }

                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                if (!NetworkUtils.isNetworkAvailable(this@VentasActivity)) {
                    tvEmptyState.text = "Error de conexión. Verifique su internet"
                } else {
                    tvEmptyState.text = "Error al cargar las ventas"
                }
                tvEmptyState.visibility = View.VISIBLE
                rvVentas.visibility = View.GONE
                tvTotalVentas.visibility = View.GONE
            }
        })
    }

    private fun nuevaVenta() {
        val intent = Intent(this, NuevaVentaActivity::class.java)
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            loadingDialog.show()
            spinKit.visibility = View.VISIBLE
            fabNuevaVenta.isEnabled = false
        } else {
            loadingDialog.dismiss()
            spinKit.visibility = View.GONE
            fabNuevaVenta.isEnabled = true
        }
    }
}