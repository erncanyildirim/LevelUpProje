package com.suer.levelup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suer.levelup.ui.viewmodel.HabitViewModel

// Renkler
private val CreamBg = Color(0xFFFDFDF6)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val PrimaryOrange = Color(0xFFFF6F61)
private val TextDark = Color(0xFF2D3436)
private val SecondaryBlue = Color(0xFF4A90E2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitSchedulingScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: HabitViewModel
) {
    var frequency by remember { mutableStateOf("Her Gün") }
    val daysMap = listOf(
        "Pzt" to "P", "Sal" to "S", "Çar" to "Ç",
        "Per" to "P", "Cum" to "C", "Cmt" to "C", "Paz" to "P"
    )
    val selectedDays = remember { mutableStateListOf<String>() }
    var reminderEnabled by remember { mutableStateOf(true) }

    // Saat İşlemleri
    var time by remember { mutableStateOf("09:00") }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)

    // --- MODERN SAAT SEÇİCİ DİYALOĞU ---
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                val hour = timePickerState.hour.toString().padStart(2, '0')
                val minute = timePickerState.minute.toString().padStart(2, '0')
                time = "$hour:$minute"
                showTimePicker = false
            }
        ) {
            // Burası Saat Seçici Bileşeni (Analog Görünüm)
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = CreamBg, // Arka plan
                    selectorColor = PrimaryOrange, // Seçici kol rengi
                    containerColor = SurfaceWhite, // Genel kutu rengi
                    periodSelectorBorderColor = PrimaryOrange,
                    periodSelectorSelectedContainerColor = PrimaryOrange.copy(alpha = 0.2f),
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorSelectedContentColor = PrimaryOrange,
                    periodSelectorUnselectedContentColor = TextDark,
                    timeSelectorSelectedContainerColor = PrimaryOrange.copy(alpha = 0.2f),
                    timeSelectorUnselectedContainerColor = CreamBg,
                    timeSelectorSelectedContentColor = PrimaryOrange,
                    timeSelectorUnselectedContentColor = TextDark
                )
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Adım 2/3", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
                text = "Zamanlamayı Ayarla ⏰",
                style = MaterialTheme.typography.headlineMedium,
                color = TextDark,
                fontWeight = FontWeight.ExtraBold
            )

            // Sıklık Seçimi
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ne sıklıkla?", fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Her Gün", "Haftalık").forEach { freq ->
                            val isSelected = frequency == freq
                            FilterChip(
                                selected = isSelected,
                                onClick = { frequency = freq },
                                label = { Text(freq) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryOrange.copy(alpha = 0.2f),
                                    selectedLabelColor = PrimaryOrange,
                                    labelColor = TextDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.LightGray,
                                    selectedBorderColor = PrimaryOrange
                                )
                            )
                        }
                    }
                }
            }

            // Gün Seçimi
            if (frequency == "Haftalık") {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hangi günler?", fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            daysMap.forEach { (fullName, shortName) ->
                                DayChip(
                                    shortName = shortName,
                                    fullName = fullName,
                                    selectedDays = selectedDays
                                )
                            }
                        }
                    }
                }
            }

            // Hatırlatıcı
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, null, tint = SecondaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hatırlatıcı", fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SurfaceWhite,
                                checkedTrackColor = PrimaryOrange,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange)
                        ) {
                            Icon(Icons.Default.AccessTime, null, tint = PrimaryOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(time, fontSize = 18.sp, color = PrimaryOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = { 0.66f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = PrimaryOrange,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.updateSchedule(frequency, selectedDays, reminderEnabled, time)
                        onNext()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text("Devam Et", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- ÖZEL TIME PICKER DIALOG ---
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextDark)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Tamam", color = PrimaryOrange, fontWeight = FontWeight.Bold)
            }
        },
        text = { content() },
        containerColor = SurfaceWhite, // Arka planı beyaz yaptık
        tonalElevation = 6.dp
    )
}

@Composable
fun DayChip(shortName: String, fullName: String, selectedDays: MutableList<String>) {
    val isSelected = selectedDays.contains(fullName)
    val bgColor = if (isSelected) PrimaryOrange else Color.Transparent
    val textColor = if (isSelected) Color.White else TextDark
    val borderColor = if (isSelected) PrimaryOrange else Color.LightGray

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable {
                if (isSelected) selectedDays.remove(fullName) else selectedDays.add(fullName)
            }
    ) {
        Text(text = shortName, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}