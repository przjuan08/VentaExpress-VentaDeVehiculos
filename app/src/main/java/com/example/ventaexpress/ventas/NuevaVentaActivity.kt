package com.example.ventaexpress.ventas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Cliente
import com.example.ventaexpress.models.Producto
import com.example.ventaexpress.models.Venta
import com.example.ventaexpress.utils.LoadingDialog
import com.example.ventaexpress.utils.NetworkUtils
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class NuevaVentaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var productoAdapter: ProductoVentaAdapter

    // Views
    private lateinit var spClientes: Spinner
    private lateinit var rvProductos: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnFinalizar: Button
    private lateinit var btnCancelar: Button
    private lateinit var spinKit: SpinKitView

    private var clientes: List<Cliente> = emptyList()
    private var productos: List<Producto> = emptyList()
    private var productosSeleccionados: MutableMap<String, Int> = mutableMapOf() // productoId -> cantidad
    private var clienteSeleccionado: Cliente? = null
    private var total: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_venta)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar Loading Dialog
        loadingDialog = LoadingDialog(this)

        // Inicializar vistas
        spClientes = findViewById(R.id.spClientes)
        rvProductos = findViewById(R.id.rvProductos)
        tvTotal = findViewById(R.id.tvTotal)
        btnFinalizar = findViewById(R.id.btnFinalizar)
        btnCancelar = findViewById(R.id.btnCancelar)
        spinKit = findViewById(R.id.spinKit)

        // Configurar RecyclerView
        productoAdapter = ProductoVentaAdapter(emptyList()) { producto, cantidad ->
            actualizarProductoSeleccionado(producto, cantidad)
        }

        rvProductos.layoutManager = LinearLayoutManager(this)
        rvProductos.adapter = productoAdapter

        // Configurar listeners
        spClientes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    clienteSeleccionado = clientes[position - 1]
                } else {
                    clienteSeleccionado = null
                }
                actualizarBotonFinalizar()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                clienteSeleccionado = null
                actualizarBotonFinalizar()
            }
        }

        btnFinalizar.setOnClickListener { finalizarVenta() }
        btnCancelar.setOnClickListener { finish() }

        // Cargar datos
        cargarClientes()
        cargarProductos()
    }

    private fun cargarClientes() {
        val userId = auth.currentUser?.uid ?: return

        showLoading(true)

        val clientesRef = database.getReference("users").child(userId).child("clientes")

        clientesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clientesList = mutableListOf<Cliente>()

                for (clienteSnapshot in snapshot.children) {
                    val cliente = clienteSnapshot.getValue(Cliente::class.java)
                    cliente?.let { clientesList.add(it) }
                }

                clientes = clientesList

                // Configurar spinner
                val adapter = ArrayAdapter(
                    this@NuevaVentaActivity,
                    android.R.layout.simple_spinner_item,
                    listOf("Seleccionar cliente") + clientesList.map { it.nombre }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spClientes.adapter = adapter

                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Toast.makeText(this@NuevaVentaActivity, "Error al cargar clientes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarProductos() {
        val userId = auth.currentUser?.uid ?: return

        showLoading(true)

        val productosRef = database.getReference("users").child(userId).child("productos")

        productosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productosList = mutableListOf<Producto>()

                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(Producto::class.java)
                    // Solo mostrar productos con stock disponible
                    if (producto != null && producto.stock > 0) {
                        productosList.add(producto)
                    }
                }

                productos = productosList
                productoAdapter.updateData(productosList)

                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Toast.makeText(this@NuevaVentaActivity, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarProductoSeleccionado(producto: Producto, cantidad: Int) {
        if (cantidad > 0) {
            productosSeleccionados[producto.id] = cantidad
        } else {
            productosSeleccionados.remove(producto.id)
        }

        // Calcular total
        total = 0.0
        for ((productoId, cant) in productosSeleccionados) {
            val prod = productos.find { it.id == productoId }
            prod?.let { total += it.precio * cant }
        }

        // Formatear el total con comas
        val formatter = java.text.DecimalFormat("$#,###.##")
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        tvTotal.text = "Total: ${formatter.format(total)}"

        actualizarBotonFinalizar()
    }

    private fun actualizarBotonFinalizar() {
        btnFinalizar.isEnabled = clienteSeleccionado != null && productosSeleccionados.isNotEmpty() && total > 0
    }

    private fun finalizarVenta() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        if (clienteSeleccionado == null || productosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Seleccione un cliente y al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar stock disponible
        for ((productoId, cantidad) in productosSeleccionados) {
            val producto = productos.find { it.id == productoId }
            if (producto == null || producto.stock < cantidad) {
                Toast.makeText(this, "Stock insuficiente para ${producto?.nombre}", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Mostrar diálogo de confirmación
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Confirmar Venta")
            .setMessage("¿Está seguro que quiere finalizar la venta?")
            .setPositiveButton("Finalizar") { dialog, which ->
                procesarVenta()
            }
            .setNegativeButton("Cancelar", null)

        val dialog = dialogBuilder.create()
        dialog.show()

        // Cambiar colores de los botones
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(resources.getColor(R.color.white, theme))
            setBackgroundColor(resources.getColor(R.color.success, theme))
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
            setTextColor(resources.getColor(R.color.white, theme))
            setBackgroundColor(resources.getColor(R.color.text_secondary, theme))
        }
    }

    private fun procesarVenta() {
        showLoading(true)

        val userId = auth.currentUser?.uid ?: return
        val ventaId = UUID.randomUUID().toString()

        val venta = Venta(
            id = ventaId,
            clienteId = clienteSeleccionado!!.id,
            clienteNombre = clienteSeleccionado!!.nombre,
            productos = productosSeleccionados,
            total = total,
            fecha = System.currentTimeMillis(),
            userId = userId
        )

        if (!venta.isValid()) {
            showLoading(false)
            Toast.makeText(this, "Datos de venta inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar venta y actualizar stock
        guardarVentaYActualizarStock(venta)

    }

    private fun guardarVentaYActualizarStock(venta: Venta) {
        val userId = auth.currentUser?.uid ?: return

        val ventaRef = database.getReference("users").child(userId).child("ventas").child(venta.id)
        val updates = mutableMapOf<String, Any>()

        // Agregar venta
        updates["/ventas/${venta.id}"] = venta.toMap()

        // Actualizar stock de productos
        for ((productoId, cantidadVendida) in venta.productos) {
            updates["/productos/${productoId}/stock"] =
                productos.find { it.id == productoId }!!.stock - cantidadVendida
        }

        database.getReference("users").child(userId).updateChildren(updates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Venta realizada exitosamente", Toast.LENGTH_SHORT).show()
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
            btnFinalizar.isEnabled = false
            btnCancelar.isEnabled = false
            spClientes.isEnabled = false
        } else {
            loadingDialog.dismiss()
            spinKit.visibility = View.GONE
            actualizarBotonFinalizar()
            btnCancelar.isEnabled = true
            spClientes.isEnabled = true
        }
    }
}