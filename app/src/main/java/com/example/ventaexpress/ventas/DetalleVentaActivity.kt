package com.example.ventaexpress.ventas

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Producto
import com.example.ventaexpress.models.Venta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleVentaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var productoAdapter: ProductoDetalleAdapter

    // Views
    private lateinit var tvCliente: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvHora: TextView
    private lateinit var tvTotal: TextView
    private lateinit var rvProductos: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_venta)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar vistas
        tvCliente = findViewById(R.id.tvCliente)
        tvFecha = findViewById(R.id.tvFecha)
        tvHora = findViewById(R.id.tvHora)
        tvTotal = findViewById(R.id.tvTotal)
        rvProductos = findViewById(R.id.rvProductos)

        // Configurar RecyclerView
        productoAdapter = ProductoDetalleAdapter(emptyList())
        rvProductos.layoutManager = LinearLayoutManager(this)
        rvProductos.adapter = productoAdapter

        // Obtener datos de la venta
        val ventaId = intent.getStringExtra("venta_id")
        if (ventaId != null) {
            cargarDetalleVenta(ventaId)
        } else {
            finish()
        }
    }

    private fun cargarDetalleVenta(ventaId: String) {
        val userId = auth.currentUser?.uid ?: return

        val ventaRef = database.getReference("users").child(userId).child("ventas").child(ventaId)

        ventaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val venta = snapshot.getValue(Venta::class.java)
                venta?.let {
                    mostrarDetallesVenta(it)
                    cargarProductosDetalle(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                finish()
            }
        })
    }

    private fun mostrarDetallesVenta(venta: Venta) {
        tvCliente.text = "Cliente: ${venta.clienteNombre}"

        // Formatear fecha y hora
        val date = Date(venta.fecha)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        tvFecha.text = "Fecha: ${dateFormat.format(date)}"
        tvHora.text = "Hora: ${timeFormat.format(date)}"

        // Formatear total con comas
        val formatter = java.text.DecimalFormat("$#,###.##")
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        tvTotal.text = "Total: ${formatter.format(venta.total)}"
    }

    private fun cargarProductosDetalle(venta: Venta) {
        val userId = auth.currentUser?.uid ?: return
        val productosRef = database.getReference("users").child(userId).child("productos")

        productosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productosDetalle = mutableListOf<ProductoDetalle>()

                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(Producto::class.java)
                    producto?.let {
                        val cantidad = venta.productos[it.id] ?: 0
                        if (cantidad > 0) {
                            productosDetalle.add(ProductoDetalle(it.nombre, it.precio, cantidad))
                        }
                    }
                }

                productoAdapter.updateData(productosDetalle)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error
            }
        })
    }
}

data class ProductoDetalle(val nombre: String, val precio: Double, val cantidad: Int)