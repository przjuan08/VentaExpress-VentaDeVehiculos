package com.example.ventaexpress.clientes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ventaexpress.R
import com.example.ventaexpress.models.Cliente

class ClienteAdapter(
    private var clientes: List<Cliente>,
    private val onEditClick: (Cliente) -> Unit,
    private val onDeleteClick: (Cliente) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCorreo: TextView = itemView.findViewById(R.id.tvCorreo)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefono)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]

        holder.tvNombre.text = cliente.nombre
        holder.tvCorreo.text = cliente.correo
        holder.tvTelefono.text = cliente.telefono

        holder.btnEdit.setOnClickListener { onEditClick(cliente) }
        holder.btnDelete.setOnClickListener { onDeleteClick(cliente) }

        // Click en el item para ver detalles
        holder.itemView.setOnClickListener {
            onEditClick(cliente)
        }
    }

    override fun getItemCount(): Int = clientes.size

    fun updateData(newClientes: List<Cliente>) {
        clientes = newClientes
        notifyDataSetChanged()
    }
}