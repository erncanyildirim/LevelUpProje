package com.suer.levelup.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.compose.ui.platform.LocalContext
import com.suer.levelup.R
import com.suer.levelup.ui.viewmodel.AuthViewModel

private val CreamBg = Color(0xFFFDFDF6)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF2D3436)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail().build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ayarlar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CreamBg)
            )
        },
        containerColor = CreamBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("Hesap İşlemleri", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(16.dp))

            // Çıkış Yap
            SettingsItem(Icons.Default.Logout, "Çıkış Yap", TextDark) {
                googleSignInClient.signOut().addOnCompleteListener {
                    authViewModel.signOut()
                    onLogout()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hesap Sil
            SettingsItem(Icons.Default.Delete, "Hesabımı Sil", Color.Red) {
                // İleride buraya silme onayı eklenebilir
            }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, color: Color, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}