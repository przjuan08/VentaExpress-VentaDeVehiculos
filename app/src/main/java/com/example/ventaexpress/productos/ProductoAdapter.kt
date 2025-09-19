package com.example.ventaexpress.productos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Producto

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onEditClick: (Producto) -> Unit,
    private val onDeleteClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvNombre.text = producto.nombre
        holder.tvDescripcion.text = producto.descripcion
        holder.tvPrecio.text = producto.getPrecioFormateado()

        // Cambiar color del stock según la cantidad
        if (producto.stock == 0) {
            holder.tvStock.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
            holder.tvStock.text = "Sin stock"
        } else if (producto.stock < 5) {
            holder.tvStock.setTextColor(holder.itemView.context.getColor(android.R.color.holo_orange_dark))
            holder.tvStock.text = "Stock: ${producto.stock} (Bajo)"
        } else {
            holder.tvStock.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            holder.tvStock.text = "Stock: ${producto.stock}"
        }

        holder.btnEdit.setOnClickListener { onEditClick(producto) }
        holder.btnDelete.setOnClickListener { onDeleteClick(producto) }

        // Click en el item para ver detalles
        holder.itemView.setOnClickListener {
            onEditClick(producto)
        }
    }

    override fun getItemCount(): Int = productos.size

    fun updateData(newProductos: List<Producto>) {
        productos = newProductos
        notifyDataSetChanged()
    }
}