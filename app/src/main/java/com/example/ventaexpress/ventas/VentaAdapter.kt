package com.example.ventaexpress.ventas

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Venta

class VentaAdapter(
    private var ventas: List<Venta>
) : RecyclerView.Adapter<VentaAdapter.VentaViewHolder>() {

    inner class VentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCliente: TextView = itemView.findViewById(R.id.tvCliente)
        val tvProductos: TextView = itemView.findViewById(R.id.tvProductos)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)

        init {
            // Hacer toda la tarjeta clickeable, no solo los textos
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val venta = ventas[position]
                    val context = itemView.context
                    val intent = Intent(context, DetalleVentaActivity::class.java)
                    intent.putExtra("venta_id", venta.id)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val venta = ventas[position]

        holder.tvCliente.text = venta.clienteNombre
        holder.tvTotal.text = venta.getTotalFormateado()
        holder.tvFecha.text = venta.getFechaFormateada()

        // Formatear productos
        val productosText = if (venta.productos.size == 1) {
            "1 producto"
        } else {
            "${venta.productos.size} productos"
        }
        holder.tvProductos.text = productosText
    }

    override fun getItemCount(): Int = ventas.size

    fun updateData(newVentas: List<Venta>) {
        ventas = newVentas
        notifyDataSetChanged()
    }
}