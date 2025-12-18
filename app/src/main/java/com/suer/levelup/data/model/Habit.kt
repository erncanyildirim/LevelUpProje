package com.suer.levelup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Habit(
    // ID İşlemleri
    val id: String = "",

    // Temel Bilgiler
    val title: String = "",
    val description: String = "",
    val startDate: String = "",

    // Sıklık (Her gün, Haftalık)
    val frequency: String = "Her Gün",
    val selectedDays: List<String> = emptyList(),

    // --- YENİ HATIRLATICI SİSTEMİ ---
    val reminderEnabled: Boolean = false,

    // Tek bir saat yerine artık bir liste tutuyoruz (Örn: ["09:00", "14:30", "21:00"])
    val reminderTimes: List<String> = emptyList(),

    // Tekrar Özelliği (Örn: 2 saatte bir)
    val isRepeatEnabled: Boolean = false,
    val repeatIntervalHours: Int = 0, // Kaç saatte bir?

    // İlerleme ve Kategori
    val category: String = "Genel",
    val progress: Float = 0f,
    val streak: Int = 0,
    val completedDates: List<String> = emptyList(),

    // Arşivleme (Silme için)
    @get:PropertyName("isArchived")
    @set:PropertyName("isArchived")
    var isArchived: Boolean = false,

    val createdAt: Timestamp = Timestamp.now()
)