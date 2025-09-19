package com.example.ventaexpress.productos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Producto
import com.example.ventaexpress.utils.LoadingDialog
import com.example.ventaexpress.utils.NetworkUtils
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductosActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var productoAdapter: ProductoAdapter

    // Views
    private lateinit var rvProductos: RecyclerView
    private lateinit var fabAgregarProducto: FloatingActionButton
    private lateinit var tvEmptyState: TextView
    private lateinit var spinKit: SpinKitView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar Loading Dialog
        loadingDialog = LoadingDialog(this)

        // Inicializar vistas
        rvProductos = findViewById(R.id.rvProductos)
        fabAgregarProducto = findViewById(R.id.fabAgregarProducto)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        spinKit = findViewById(R.id.spinKit)

        // Configurar RecyclerView
        productoAdapter = ProductoAdapter(
            emptyList(),
            onEditClick = { producto -> editarProducto(producto) },
            onDeleteClick = { producto -> eliminarProducto(producto) }
        )

        rvProductos.layoutManager = LinearLayoutManager(this)
        rvProductos.adapter = productoAdapter

        // Configurar listeners
        fabAgregarProducto.setOnClickListener { agregarProducto() }

        // Cargar productos
        cargarProductos()
    }

    private fun cargarProductos() {
        val userId = auth.currentUser?.uid ?: return

        showLoading(true)

        val productosRef = database.getReference("users").child(userId).child("productos")

        productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productos = mutableListOf<Producto>()

                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(Producto::class.java)
                    producto?.let { productos.add(it) }
                }

                productoAdapter.updateData(productos)

                // Mostrar u ocultar empty state
                if (productos.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    rvProductos.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    rvProductos.visibility = View.VISIBLE
                }

                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                if (!NetworkUtils.isNetworkAvailable(this@ProductosActivity)) {
                    tvEmptyState.text = "Error de conexión. Verifique su internet"
                } else {
                    tvEmptyState.text = "Error al cargar los productos"
                }
                tvEmptyState.visibility = View.VISIBLE
                rvProductos.visibility = View.GONE
            }
        })
    }

    private fun agregarProducto() {
        val intent = Intent(this, EditarProductoActivity::class.java)
        startActivity(intent)
    }

    private fun editarProducto(producto: Producto) {
        val intent = Intent(this, EditarProductoActivity::class.java)
        intent.putExtra("producto_id", producto.id)
        intent.putExtra("producto_nombre", producto.nombre)
        intent.putExtra("producto_descripcion", producto.descripcion)
        intent.putExtra("producto_precio", producto.precio)
        intent.putExtra("producto_stock", producto.stock)
        intent.putExtra("producto_fechaCreacion", producto.fechaCreacion)
        startActivity(intent)
    }

    private fun eliminarProducto(producto: Producto) {
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que quieres eliminar \"${producto.nombre}\"?")
            .setPositiveButton("Eliminar") { dialog, which ->
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                val productoRef = database.getReference("users").child(userId).child("productos").child(producto.id)
                productoRef.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Producto eliminado exitosamente", Toast.LENGTH_SHORT).show()
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
            fabAgregarProducto.isEnabled = false
        } else {
            loadingDialog.dismiss()
            spinKit.visibility = View.GONE
            fabAgregarProducto.isEnabled = true
        }
    }
}