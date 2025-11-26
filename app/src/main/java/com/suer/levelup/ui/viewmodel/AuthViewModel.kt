package com.suer.levelup.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.suer.levelup.data.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Kullanıcı verilerini tutacak model
data class UserData(
    val email: String = "",
    val name: String = "",
    val totalPoints: Int = 0
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Kullanıcı verilerini anlık takip etmek için
    private val _currentUserData = MutableStateFlow(UserData())
    val currentUserData: StateFlow<UserData> = _currentUserData

    init {
        // ViewModel başladığında eğer kullanıcı giriş yapmışsa verilerini çekmeye başla
        if (isUserLoggedIn()) {
            fetchUserData()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // --- VERİ ÇEKME FONKSİYONU ---
    fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return

        // addSnapshotListener: Veritabanında bir şey değiştiği an (puan arttığında) burası otomatik çalışır
        firestore.collection("users").document(userId).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            if (snapshot != null && snapshot.exists()) {
                val email = snapshot.getString("email") ?: ""
                val points = snapshot.getLong("totalPoints")?.toInt() ?: 0
                // Eğer veritabanında "name" yoksa, mail adresinin @ işaretinden önceki kısmını isim yapalım
                val name = snapshot.getString("name") ?: email.substringBefore("@")

                _currentUserData.value = UserData(email, name, points)
            }
        }
    }

    // ... (Diğer Login/Signup kodların aynen kalıyor, aşağıya ekliyorum) ...

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("E-posta ve şifre boş olamaz.")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                fetchUserData() // Giriş başarılı olunca veriyi çek
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Giriş başarısız.")
            }
    }

    fun signup(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("E-posta ve şifre boş olamaz.")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    initializeUserInFirestore(userId, email)
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Kayıt başarısız.")
            }
    }

    private fun initializeUserInFirestore(userId: String, email: String) {
        val nameFromEmail = email.substringBefore("@") // Varsayılan isim
        val userData = hashMapOf(
            "email" to email,
            "name" to nameFromEmail, // İsmi de kaydedelim
            "totalPoints" to 0,
            "dailyPoints" to 0,
            "level" to 1,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                fetchUserData() // Kayıt bitince veriyi çek
                _authState.value = AuthState.Success
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Veritabanı hatası: ${it.localizedMessage}")
            }
    }

    fun signInWithGoogleCredential(credential: AuthCredential) {
        _authState.value = AuthState.Loading
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                val isNewUser = result.additionalUserInfo?.isNewUser == true

                if (isNewUser && user != null) {
                    initializeUserInFirestore(user.uid, user.email ?: "")
                } else {
                    fetchUserData()
                    _authState.value = AuthState.Success
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.localizedMessage ?: "Google girişi hatası")
            }
    }

    fun updateUserPoints(pointsToAdd: Int) {
        val userId = auth.currentUser?.uid ?: return
        val currentPoints = _currentUserData.value.totalPoints
        val newPoints = currentPoints + pointsToAdd

        firestore.collection("users").document(userId)
            .update("totalPoints", newPoints)
            .addOnSuccessListener {
                // Başarılı olursa lokal veriyi de anında güncelle (UI hızlı tepki versin)
                _currentUserData.value = _currentUserData.value.copy(totalPoints = newPoints)
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
        _currentUserData.value = UserData() // Veriyi sıfırla
    }
}