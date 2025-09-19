package com.example.ventaexpress.ventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import java.text.DecimalFormat

class ProductoDetalleAdapter(
    private var productos: List<ProductoDetalle>
) : RecyclerView.Adapter<ProductoDetalleAdapter.ProductoDetalleViewHolder>() {

    inner class ProductoDetalleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoDetalleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_detalle, parent, false)
        return ProductoDetalleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoDetalleViewHolder, position: Int) {
        val producto = productos[position]
        val subtotal = producto.precio * producto.cantidad

        val formatter = DecimalFormat("$#,###.##")
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2

        holder.tvNombre.text = producto.nombre
        holder.tvPrecio.text = "Precio: ${formatter.format(producto.precio)}"
        holder.tvCantidad.text = "Cantidad: ${producto.cantidad}"
        holder.tvSubtotal.text = "Subtotal: ${formatter.format(subtotal)}"
    }

    override fun getItemCount(): Int = productos.size

    fun updateData(newProductos: List<ProductoDetalle>) {
        productos = newProductos
        notifyDataSetChanged()
    }
}