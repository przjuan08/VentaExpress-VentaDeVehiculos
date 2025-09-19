package com.example.ventaexpress.models

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val userId: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", 0.0, 0, "", 0)

    fun isValid(): Boolean {
        return nombre.isNotBlank() &&
                descripcion.isNotBlank() &&
                precio > 0 &&
                stock >= 0
    }

    fun getPrecioFormateado(): String {
        val formatter = java.text.DecimalFormat("$#,###.00")
        return formatter.format(precio)
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "descripcion" to descripcion,
            "precio" to precio,
            "stock" to stock,
            "userId" to userId,
            "fechaCreacion" to fechaCreacion
        )
    }
}