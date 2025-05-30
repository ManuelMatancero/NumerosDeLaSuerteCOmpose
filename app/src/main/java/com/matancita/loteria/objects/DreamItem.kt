package com.matancita.loteria.objects

data class DreamItem(
    val number: Int,
    val text: String,
    val keywords: List<String> = emptyList() // Opcional, para un filtrado más avanzado
)