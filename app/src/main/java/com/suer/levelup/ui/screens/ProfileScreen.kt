package com.suer.levelup.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.suer.levelup.R
import com.suer.levelup.ui.viewmodel.AuthViewModel
import com.suer.levelup.ui.viewmodel.UserData
import com.suer.levelup.ui.theme.* // <-- Renkler artık buradan geliyor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userData: UserData,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    viewModel: AuthViewModel
) {
    val scrollState = rememberScrollState()

    // UYARI KUTUSU DURUMU
    var showDeleteDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.uploadProfileImage(it) } }

    val level = (userData.totalPoints / 100) + 1
    val pointsToNextLevel = 100 - (userData.totalPoints % 100)
    val progressFloat = (userData.totalPoints % 100) / 100f

    // Özel altın rengini Color.kt'ye eklemediysek burada geçici tanımlayabiliriz
    // Veya tema dosyasında ColorGold varsa oradan çekeriz.
    val goldColor = Color(0xFFFFC107)

    val allBadges = listOf(
        Badge("Yeni Başlayan", 0, Icons.Rounded.Star),
        Badge("Azimli", 50, Icons.Rounded.MilitaryTech),
        Badge("Usta", 150, Icons.Rounded.EmojiEvents),
        Badge("Efsane", 300, Icons.Rounded.Star)
    )

    // --- HESAP SİLME UYARI KUTUSU ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Hesabımı Sil",
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed // Color.kt'den
                )
            },
            text = {
                Text(
                    "Hesabınızın silinmesini onaylarsanız tüm verileriniz bir daha geri getirilmemek üzere silinecektir.",
                    textAlign = TextAlign.Center
                )
            },
            containerColor = SurfaceWhite,
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccountClick() // Silme işlemini başlat
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Onaylıyorum", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hayır", color = TextDark)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profilim", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CreamBg)
            )
        },
        containerColor = CreamBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profil Resmi
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(120.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            if (userData.profileImageUrl.isNotEmpty()) userData.profileImageUrl
                            else R.drawable.ic_launcher_background
                        )
                        .crossfade(true).build(),
                    contentDescription = "Profil Resmi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(4.dp, SurfaceWhite, CircleShape)
                        .clickable { galleryLauncher.launch("image/*") }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryOrange)
                        .border(2.dp, SurfaceWhite, CircleShape)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        null,
                        tint = SurfaceWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userData.name.ifEmpty { "Kullanıcı" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = userData.email,
                fontSize = 14.sp,
                color = TextLight
            )

            Spacer(modifier = Modifier.height(24.dp))

            // İstatistikler
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    Icons.Rounded.Star,
                    "Toplam Puan",
                    "${userData.totalPoints}",
                    goldColor,
                    Modifier.weight(1f)
                )
                StatCard(
                    Icons.Rounded.MilitaryTech,
                    "Seviye",
                    "$level",
                    PrimaryOrange,
                    Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seviye Çubuğu
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Sonraki Seviye: ${level + 1}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        "$pointsToNextLevel puan kaldı",
                        fontSize = 12.sp,
                        color = TextLight
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = PrimaryOrange,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Rozetler
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text(
                    "Rozetlerim 🏆",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(allBadges) { badge ->
                        val isUnlocked = userData.totalPoints >= badge.requiredPoints
                        BadgeCard(badge, isUnlocked, goldColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // BUTONLAR
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFECEB)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Logout, null, tint = ErrorRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Çıkış Yap",
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // HESABI SİL BUTONU (Tıklanınca Dialog Açılır)
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        null,
                        tint = TextLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hesabımı Sil", color = TextLight, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Veri Sınıfı
data class Badge(val name: String, val requiredPoints: Int, val icon: ImageVector)

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(title, fontSize = 12.sp, color = TextLight)
        }
    }
}

@Composable
fun BadgeCard(badge: Badge, isUnlocked: Boolean, goldColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(if (isUnlocked) 1f else 0.4f)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) goldColor else Color.Gray)
                .border(2.dp, SurfaceWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                badge.icon,
                null,
                tint = SurfaceWhite,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            badge.name,
            fontSize = 12.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium
        )
        if (!isUnlocked) {
            Text("${badge.requiredPoints}p", fontSize = 10.sp, color = TextLight)
        }
    }
}