package com.example.ventaexpress.ventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Producto

class ProductoVentaAdapter(
    private var productos: List<Producto>,
    private val onCantidadChange: (Producto, Int) -> Unit
) : RecyclerView.Adapter<ProductoVentaAdapter.ProductoVentaViewHolder>() {

    private val cantidades: MutableMap<String, Int> = mutableMapOf()

    inner class ProductoVentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        val btnMenos: Button = itemView.findViewById(R.id.btnMenos)
        val btnMas: Button = itemView.findViewById(R.id.btnMas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoVentaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_venta, parent, false)
        return ProductoVentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoVentaViewHolder, position: Int) {
        val producto = productos[position]
        val cantidad = cantidades[producto.id] ?: 0

        holder.tvNombre.text = producto.nombre
        holder.tvPrecio.text = producto.getPrecioFormateado()
        holder.tvStock.text = "Stock: ${producto.stock}"
        holder.tvCantidad.text = cantidad.toString()

        holder.btnMenos.isEnabled = cantidad > 0
        holder.btnMas.isEnabled = cantidad < producto.stock

        holder.btnMenos.setOnClickListener {
            val nuevaCantidad = cantidad - 1
            cantidades[producto.id] = nuevaCantidad
            notifyItemChanged(position)
            onCantidadChange(producto, nuevaCantidad)
        }

        holder.btnMas.setOnClickListener {
            val nuevaCantidad = cantidad + 1
            cantidades[producto.id] = nuevaCantidad
            notifyItemChanged(position)
            onCantidadChange(producto, nuevaCantidad)
        }
    }

    override fun getItemCount(): Int = productos.size

    fun updateData(newProductos: List<Producto>) {
        productos = newProductos
        cantidades.clear()
        notifyDataSetChanged()
    }
}