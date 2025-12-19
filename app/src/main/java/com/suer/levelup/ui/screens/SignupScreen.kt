package com.suer.levelup.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suer.levelup.ui.viewmodel.AuthState
import com.suer.levelup.ui.viewmodel.AuthViewModel
import com.suer.levelup.ui.theme.* // <-- Renkler artık buradan geliyor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit, // Bu parametreyi tuttum ama authState içinde yöneteceğiz
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.VerificationSent -> {
                Toast.makeText(context, "Doğrulama maili gönderildi! Lütfen e-postanı kontrol et.", Toast.LENGTH_LONG).show()
                onNavigateToLogin() // Giriş ekranına yönlendir
                viewModel.resetAuthState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetAuthState()
            }
            else -> {}
        }
    }

    Scaffold(containerColor = CreamBg) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Aramıza Katıl ✨",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hemen ücretsiz hesabını oluştur",
                fontSize = 16.sp,
                color = TextLight // Gri yerine temadaki TextLight kullanıldı
            )

            Spacer(modifier = Modifier.height(40.dp))

            // E-posta
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-posta Adresi") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = PrimaryOrange) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryOrange,
                    focusedLabelColor = PrimaryOrange,
                    cursorColor = PrimaryOrange,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Şifre
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre (Min 6 Karakter)") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryOrange) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = TextLight
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryOrange,
                    focusedLabelColor = PrimaryOrange,
                    cursorColor = PrimaryOrange,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Şifre Tekrar
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Şifreyi Onayla") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryOrange) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = TextLight
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryOrange,
                    focusedLabelColor = PrimaryOrange,
                    cursorColor = PrimaryOrange,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Kayıt Ol Butonu
            Button(
                onClick = { viewModel.signup(email, password, confirmPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(24.dp))
                } else {
                    Text("Kayıt Ol", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SurfaceWhite)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Zaten hesabın var mı?", color = TextDark)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Giriş Yap", fontWeight = FontWeight.Bold, color = PrimaryOrange)
                }
            }
        }
    }
}