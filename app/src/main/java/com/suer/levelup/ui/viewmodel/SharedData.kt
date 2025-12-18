package com.suer.levelup.ui.viewmodel

// Bu sınıflar artık tüm ViewModel'ler tarafından buradan kullanılacak
data class UserData(
    val email: String = "",
    val name: String = "",
    val totalPoints: Int = 0,
    val profileImageUrl: String = ""
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
    object VerificationSent : AuthState()
}