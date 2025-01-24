package com.example.crudrealtime240125.models

import java.io.Serializable

// Modelo para los artículos de la tienda
data class Articulo(
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Float = 0f
) : Serializable
