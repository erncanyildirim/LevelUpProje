package com.suer.levelup.data.model

import com.google.firebase.Timestamp

data class Habit(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val frequency: String = "Her Gün", // Her Gün, Haftalık vb.
    val selectedDays: List<String> = emptyList(), // Pzt, Sal...
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "",
    val category: String = "Genel", // Sağlık, Finans...
    val progress: Float = 0f, // 0.0 - 1.0 arası
    val streak: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)