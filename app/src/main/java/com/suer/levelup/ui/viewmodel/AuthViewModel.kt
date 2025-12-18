package com.suer.levelup.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUserData = MutableStateFlow(UserData())
    val currentUserData: StateFlow<UserData> = _currentUserData

    init {
        if (isUserLoggedIn()) {
            fetchUserData()
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // --- RESİM YÜKLEME ---
    fun uploadProfileImage(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profile_images/$userId.jpg")
        _authState.value = AuthState.Loading
        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateUserProfileImage(downloadUri.toString())
                }
            }
            .addOnFailureListener { _authState.value = AuthState.Error("Hata: ${it.localizedMessage}") }
    }

    private fun updateUserProfileImage(url: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).update("profileImageUrl", url)
            .addOnSuccessListener {
                _currentUserData.value = _currentUserData.value.copy(profileImageUrl = url)
                _authState.value = AuthState.Success
            }
    }

    // --- KRİTİK: HESAP SİLME FONKSİYONU ---
    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser ?: return
        val userId = user.uid

        _authState.value = AuthState.Loading

        // 1. Önce kullanıcının alışkanlıklarını sil (Subcollection)
        firestore.collection("users").document(userId).collection("habits").get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                // 2. Kullanıcı dokümanını sil
                batch.delete(firestore.collection("users").document(userId))

                batch.commit().addOnSuccessListener {
                    // 3. Profil resmini sil (Varsa)
                    storage.reference.child("profile_images/$userId.jpg").delete().addOnCompleteListener {
                        // 4. En son Authentication hesabını sil
                        user.delete().addOnSuccessListener {
                            _authState.value = AuthState.Idle
                            _currentUserData.value = UserData() // Veriyi sıfırla
                            onSuccess()
                        }.addOnFailureListener { e ->
                            onError("Hesap silinemedi: ${e.localizedMessage} (Lütfen çıkış yapıp tekrar girin)")
                            _authState.value = AuthState.Error(e.localizedMessage ?: "")
                        }
                    }
                }.addOnFailureListener { e ->
                    onError("Veri silinemedi: ${e.localizedMessage}")
                }
            }
    }

    // --- DİĞER FONKSİYONLAR (AYNEN KALIYOR) ---
    fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val email = snapshot.getString("email") ?: ""
                val points = snapshot.getLong("totalPoints")?.toInt() ?: 0
                val name = snapshot.getString("name") ?: email.substringBefore("@")
                val imgUrl = snapshot.getString("profileImageUrl") ?: ""
                _currentUserData.value = UserData(email, name, points, imgUrl)
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) { _authState.value = AuthState.Error("Boş alan bırakmayınız."); return }
        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener { fetchUserData(); _authState.value = AuthState.Success }.addOnFailureListener { _authState.value = AuthState.Error(it.localizedMessage ?: "Hata") }
    }

    fun signup(email: String, pass: String, confirmPass: String) {
        if (pass != confirmPass) { _authState.value = AuthState.Error("Şifreler uyuşmuyor"); return }
        auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener {
            val user = it.user
            user?.sendEmailVerification()
            initializeUserInFirestore(user!!.uid, email)
            auth.signOut()
            _authState.value = AuthState.VerificationSent
        }.addOnFailureListener { _authState.value = AuthState.Error(it.localizedMessage ?: "Hata") }
    }

    private fun initializeUserInFirestore(userId: String, email: String) {
        val nameFromEmail = email.substringBefore("@")
        val userData = hashMapOf("email" to email, "name" to nameFromEmail, "totalPoints" to 0, "profileImageUrl" to "", "createdAt" to com.google.firebase.Timestamp.now())
        firestore.collection("users").document(userId).set(userData)
    }

    fun signInWithGoogleCredential(credential: AuthCredential) {
        auth.signInWithCredential(credential).addOnSuccessListener {
            val isNew = it.additionalUserInfo?.isNewUser == true
            if(isNew) initializeUserInFirestore(it.user!!.uid, it.user!!.email!!)
            fetchUserData()
            _authState.value = AuthState.Success
        }
    }

    fun updateUserPoints(points: Int) {
        val uid = auth.currentUser?.uid ?: return
        val current = _currentUserData.value.totalPoints
        firestore.collection("users").document(uid).update("totalPoints", current + points)
    }

    fun signOut() { auth.signOut(); _authState.value = AuthState.Idle; _currentUserData.value = UserData() }
    fun resetAuthState() { _authState.value = AuthState.Idle }
}