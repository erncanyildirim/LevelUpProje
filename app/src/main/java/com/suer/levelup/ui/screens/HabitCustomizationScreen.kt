package com.suer.levelup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.* // Tüm yuvarlak ikonları al
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suer.levelup.ui.viewmodel.HabitViewModel

private val CreamBg = Color(0xFFFDFDF6)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val PrimaryOrange = Color(0xFFFF6F61)
private val TextDark = Color(0xFF2D3436)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCustomizationScreen(
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: HabitViewModel
) {
    var selectedCategory by remember { mutableStateOf("Genel") }

    // YENİ VE GENİŞ KATEGORİ LİSTESİ
    val categories = listOf(
        CategoryItem("Sağlık", Icons.Rounded.Favorite, Color(0xFFFF6B6B)),
        CategoryItem("Spor", Icons.Rounded.FitnessCenter, Color(0xFF6C5CE7)),
        CategoryItem("Su İçmek", Icons.Rounded.WaterDrop, Color(0xFF00CEC9)),
        CategoryItem("Okuma", Icons.Rounded.LocalLibrary, Color(0xFFFAB1A0)),
        CategoryItem("Finans", Icons.Rounded.MonetizationOn, Color(0xFFFFD93D)),
        CategoryItem("Kişisel", Icons.Rounded.Face, Color(0xFFA29BFE)),
        CategoryItem("Yüzme", Icons.Rounded.Pool, Color(0xFF74B9FF)),
        CategoryItem("Meditasyon", Icons.Rounded.SelfImprovement, Color(0xFF55E6C1)),
        CategoryItem("Uyku", Icons.Rounded.Bedtime, Color(0xFF2d3436)), // Yeni
        CategoryItem("Yürüyüş", Icons.Rounded.DirectionsWalk, Color(0xFF27AE60)), // Yeni
        CategoryItem("Sosyal", Icons.Rounded.Groups, Color(0xFFFF7675)), // Yeni
        CategoryItem("Yaratıcılık", Icons.Rounded.Brush, Color(0xFFFD79A8)), // Yeni
        CategoryItem("Kodlama", Icons.Rounded.Code, Color(0xFF0984E3)), // Yeni
        CategoryItem("Müzik", Icons.Rounded.MusicNote, Color(0xFFE84393)) // Yeni
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Adım 3/3", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Bir Kategori Seç 🏷️",
                style = MaterialTheme.typography.headlineMedium,
                color = TextDark,
                fontWeight = FontWeight.ExtraBold
            )

            // Kategori Izgarası
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categories) { item ->
                    CategoryCard(
                        item = item,
                        isSelected = selectedCategory == item.name,
                        onSelect = { selectedCategory = item.name }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = PrimaryOrange,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.updateCategoryAndSave(selectedCategory) {
                            onSave()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text("Başlat 🚀", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class CategoryItem(val name: String, val icon: ImageVector, val color: Color)

@Composable
fun CategoryCard(item: CategoryItem, isSelected: Boolean, onSelect: () -> Unit) {
    val backgroundColor = if (isSelected) item.color.copy(alpha = 0.15f) else SurfaceWhite
    val borderColor = if (isSelected) item.color else Color.Transparent
    val elevation = if (isSelected) 0.dp else 2.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = item.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                fontSize = 14.sp
            )
        }
    }
}