package com.example.ventaexpress.models

data class Venta(
    val id: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val productos: Map<String, Int> = emptyMap(), // productoId -> cantidad
    val total: Double = 0.0,
    val fecha: Long = System.currentTimeMillis(),
    val userId: String = ""
) {
    constructor() : this("", "", "", emptyMap(), 0.0, 0, "")

    fun isValid(): Boolean {
        return clienteId.isNotBlank() &&
                productos.isNotEmpty() &&
                total > 0
    }

    fun getTotalFormateado(): String {
        val formatter = java.text.DecimalFormat("$#,###.##")
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.format(total)
    }

    fun getFechaFormateada(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(fecha))
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "clienteId" to clienteId,
            "clienteNombre" to clienteNombre,
            "productos" to productos,
            "total" to total,
            "fecha" to fecha,
            "userId" to userId
        )
    }
}