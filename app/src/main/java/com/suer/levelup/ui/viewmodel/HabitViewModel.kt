package com.suer.levelup.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.suer.levelup.data.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _newHabitState = MutableStateFlow(Habit())
    val newHabitState: StateFlow<Habit> = _newHabitState

    private val _habitList = MutableStateFlow<List<Habit>>(emptyList())
    val habitList: StateFlow<List<Habit>> = _habitList

    // --- HAZIRLIK ---
    fun prepareForEdit(habit: Habit) { _newHabitState.value = habit }
    fun resetForNewHabit() { _newHabitState.value = Habit() }

    // --- ADIM 1: İsim ve Açıklama ---
    fun updateTitleDescDate(title: String, desc: String, date: String) {
        _newHabitState.value = _newHabitState.value.copy(title = title, description = desc, startDate = date)
    }

    // --- ADIM 2: Zamanlama (GÜNCELLENDİ) ---
    // Artık tek bir saat değil, saat listesi ve tekrar aralığı alıyor
    fun updateSchedule(
        frequency: String,
        days: List<String>,
        reminderEnabled: Boolean,
        reminderTimes: List<String>,
        isRepeatEnabled: Boolean,
        repeatIntervalHours: Int
    ) {
        _newHabitState.value = _newHabitState.value.copy(
            frequency = frequency,
            selectedDays = days,
            reminderEnabled = reminderEnabled,
            reminderTimes = reminderTimes, // Liste olarak kaydediyoruz
            isRepeatEnabled = isRepeatEnabled,
            repeatIntervalHours = repeatIntervalHours
        )
    }

    // --- ADIM 3: Kategori ve Kayıt ---
    fun updateCategoryAndSave(category: String, onSuccess: () -> Unit) {
        _newHabitState.value = _newHabitState.value.copy(category = category)
        saveHabitToFirestore(onSuccess)
    }

    private fun saveHabitToFirestore(onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val currentHabitState = _newHabitState.value

        val habitRef = if (currentHabitState.id.isNotEmpty()) {
            firestore.collection("users").document(userId).collection("habits").document(currentHabitState.id)
        } else {
            firestore.collection("users").document(userId).collection("habits").document()
        }

        val finalHabit = currentHabitState.copy(
            id = habitRef.id,
            createdAt = if(currentHabitState.id.isEmpty()) com.google.firebase.Timestamp.now() else currentHabitState.createdAt
        )

        habitRef.set(finalHabit).addOnSuccessListener {
            _newHabitState.value = Habit()
            onSuccess()
        }
    }

    // --- VERİ ÇEKME ---
    fun fetchHabits() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).collection("habits")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    val habits = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Habit::class.java)?.copy(id = doc.id)
                    }
                    _habitList.value = habits
                }
            }
    }

    // --- SİLME / ARŞİVLEME ---
    fun archiveHabit(habitId: String) {
        val userId = auth.currentUser?.uid ?: return
        if (habitId.isEmpty()) return

        val currentList = _habitList.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == habitId }
        if (index != -1) {
            val updatedHabit = currentList[index].copy(isArchived = true)
            currentList[index] = updatedHabit
            _habitList.value = currentList
        }

        firestore.collection("users").document(userId)
            .collection("habits").document(habitId)
            .update("isArchived", true)
            .addOnFailureListener { fetchHabits() }
    }

    fun updateHabitProgress(habit: Habit, progressValue: Float) {
        val userId = auth.currentUser?.uid ?: return
        if (habit.id.isEmpty()) return

        val isCompletedNow = progressValue >= 1f
        val wasCompletedBefore = habit.progress >= 1f
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

        var newStreak = habit.streak
        val updates = mutableMapOf<String, Any>("progress" to progressValue)

        if (isCompletedNow && !wasCompletedBefore) {
            newStreak += 1
            updates["completedDates"] = FieldValue.arrayUnion(todayStr)
        } else if (!isCompletedNow && wasCompletedBefore) {
            if (newStreak > 0) newStreak -= 1
            updates["completedDates"] = FieldValue.arrayRemove(todayStr)
        }

        updates["streak"] = newStreak
        firestore.collection("users").document(userId)
            .collection("habits").document(habit.id)
            .update(updates)
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser ?: return
        val userId = user.uid
        firestore.collection("users").document(userId).collection("habits").get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) { batch.delete(doc.reference) }
                batch.delete(firestore.collection("users").document(userId))
                batch.commit().addOnSuccessListener {
                    storage.reference.child("profile_images/$userId.jpg").delete().addOnCompleteListener {
                        user.delete().addOnSuccessListener { onSuccess() }
                    }
                }
            }
    }
}