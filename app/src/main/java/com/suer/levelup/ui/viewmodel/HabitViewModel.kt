package com.suer.levelup.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.suer.levelup.data.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HabitViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- GEÇİCİ ALIŞKANLIK VERİSİ (Ekleme Sihirbazı İçin) ---
    private val _newHabitState = MutableStateFlow(Habit())
    val newHabitState: StateFlow<Habit> = _newHabitState

    // --- ANA EKRAN İÇİN LİSTE (Firestore'dan gelenler) ---
    private val _habitList = MutableStateFlow<List<Habit>>(emptyList())
    val habitList: StateFlow<List<Habit>> = _habitList

    // --- 1. ADIM: İsim, Açıklama, Tarih ---
    fun updateTitleDescDate(title: String, desc: String, date: String) {
        _newHabitState.value = _newHabitState.value.copy(
            title = title,
            description = desc,
            startDate = date
        )
    }

    // --- 2. ADIM: Sıklık, Günler, Hatırlatıcı ---
    fun updateSchedule(frequency: String, days: List<String>, reminderEnabled: Boolean, time: String) {
        _newHabitState.value = _newHabitState.value.copy(
            frequency = frequency,
            selectedDays = days,
            reminderEnabled = reminderEnabled,
            reminderTime = time
        )
    }

    // --- 3. ADIM: Kategori ve Kayıt İşlemi ---
    fun updateCategoryAndSave(category: String, onSuccess: () -> Unit) {
        _newHabitState.value = _newHabitState.value.copy(category = category)
        saveHabitToFirestore(onSuccess)
    }

    // --- FIRESTORE'A KAYDETME ---
    private fun saveHabitToFirestore(onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val newHabit = _newHabitState.value

        // Users -> {userId} -> habits -> {habitId}
        val habitRef = firestore.collection("users").document(userId).collection("habits").document()

        // ID'yi ve oluşturulma zamanını ekleyerek son halini al
        val finalHabit = newHabit.copy(
            id = habitRef.id,
            createdAt = com.google.firebase.Timestamp.now()
        )

        habitRef.set(finalHabit)
            .addOnSuccessListener {
                // Kayıt başarılı, formu temizle
                _newHabitState.value = Habit()
                onSuccess()
            }
            .addOnFailureListener {
                // Hata durumu loglanabilir
            }
    }

    // --- ALIŞKANLIKLARI ÇEKME (Canlı Takip) ---
    fun fetchHabits() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            // Kullanıcı yoksa veya çıkış yapılmışsa listeyi boşalt
            _habitList.value = emptyList()
            return
        }

        // Verileri "createdAt" alanına göre tersten (En yeni en üstte) sıralayarak çekiyoruz
        firestore.collection("users").document(userId).collection("habits")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val habits = snapshot.toObjects(Habit::class.java)
                    _habitList.value = habits
                }
            }
    }

    // --- ESKİ TIKLAYARAK TAMAMLAMA (Yedek olarak kalabilir) ---
    fun toggleHabitCompletion(habit: Habit, onCompleted: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        // Eğer %100 ise %0 yap (Geri al), değilse %100 yap (Tamamla)
        val newProgress = if (habit.progress >= 1f) 0f else 1f
        val newStreak = if (newProgress == 1f) habit.streak + 1 else if (habit.streak > 0) habit.streak - 1 else 0

        val updates = mapOf(
            "progress" to newProgress,
            "streak" to newStreak
        )

        firestore.collection("users").document(userId)
            .collection("habits").document(habit.id)
            .update(updates)
            .addOnSuccessListener {
                // Eğer tamamlandıysa (progress 1 olduysa) true döndür
                if (newProgress == 1f) {
                    onCompleted(true)
                } else {
                    onCompleted(false)
                }
            }
    }

    // --- SLIDER İLE İLERLEME GÜNCELLEME ---
    fun updateHabitProgress(habit: Habit, progressValue: Float) {
        val userId = auth.currentUser?.uid ?: return

        // Yeni değer %100 ise streak artır, değilse ve önceden %100 idiyse streak azalt
        val isCompletedNow = progressValue >= 1f
        val wasCompletedBefore = habit.progress >= 1f

        var newStreak = habit.streak
        if (isCompletedNow && !wasCompletedBefore) {
            newStreak += 1
        } else if (!isCompletedNow && wasCompletedBefore && newStreak > 0) {
            newStreak -= 1
        }

        val updates = mapOf(
            "progress" to progressValue,
            "streak" to newStreak
        )

        firestore.collection("users").document(userId)
            .collection("habits").document(habit.id)
            .update(updates)
    }

    // --- YENİ EKLENEN: ALIŞKANLIK SİLME ---
    fun deleteHabit(habitId: String) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("habits").document(habitId)
            .delete()
            .addOnSuccessListener {
                // Silme başarılı olduğunda yapılacak işlemler (gerekirse)
            }
            .addOnFailureListener {
                // Hata durumu
            }
    }
}