package com.example.ventaexpress.models

data class Cliente(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val userId: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", 0)

    fun isValid(): Boolean {
        return nombre.isNotBlank() &&
                correo.isNotBlank() &&
                isValidEmail(correo) &&
                isValidPhone(telefono)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        return emailRegex.matches(email)
    }

    private fun isValidPhone(phone: String): Boolean {
        val phoneRegex = Regex("^[2|6|7][0-9]{3}-[0-9]{4}$")
        return phoneRegex.matches(phone)
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "correo" to correo,
            "telefono" to telefono,
            "userId" to userId,
            "fechaCreacion" to fechaCreacion
        )
    }
}