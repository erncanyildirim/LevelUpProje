package com.suer.levelup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
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
import com.suer.levelup.ui.theme.* // <-- Renkler artık buradan geliyor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitSchedulingScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: HabitViewModel
) {
    val currentHabit = viewModel.newHabitState.collectAsState().value
    var frequency by remember { mutableStateOf(currentHabit.frequency.ifEmpty { "Her Gün" }) }
    val selectedDays = remember { mutableStateListOf<String>().apply { addAll(currentHabit.selectedDays) } }
    var reminderEnabled by remember { mutableStateOf(currentHabit.reminderEnabled) }
    val reminderTimes = remember { mutableStateListOf<String>().apply { addAll(currentHabit.reminderTimes) } }
    var isRepeatEnabled by remember { mutableStateOf(currentHabit.isRepeatEnabled) }
    var repeatIntervalHours by remember { mutableFloatStateOf(if(currentHabit.repeatIntervalHours > 0) currentHabit.repeatIntervalHours.toFloat() else 1f) }

    val daysMap = listOf(
        "Pzt" to "P", "Sal" to "S", "Çar" to "Ç",
        "Per" to "P", "Cum" to "C", "Cmt" to "C", "Paz" to "P"
    )

    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                val hour = timePickerState.hour.toString().padStart(2, '0')
                val minute = timePickerState.minute.toString().padStart(2, '0')
                val newTime = "$hour:$minute"
                if (!reminderTimes.contains(newTime)) {
                    reminderTimes.add(newTime)
                    reminderTimes.sort()
                }
                showTimePicker = false
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = CreamBg,
                    selectorColor = PrimaryOrange,
                    containerColor = SurfaceWhite,
                    timeSelectorSelectedContentColor = Color.White,
                    timeSelectorUnselectedContentColor = TextDark,
                    timeSelectorSelectedContainerColor = PrimaryOrange,
                    timeSelectorUnselectedContainerColor = CreamBg,
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContentColor = TextDark,
                    periodSelectorSelectedContainerColor = PrimaryOrange,
                    periodSelectorUnselectedContainerColor = Color.Transparent
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Zamanlamayı Ayarla ⏰",
                style = MaterialTheme.typography.headlineMedium,
                color = TextDark,
                fontWeight = FontWeight.ExtraBold
            )

            // KART 1: Sıklık Seçimi
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ne sıklıkla?", fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Her Gün", "Haftalık").forEach { freq ->
                            FilterChip(
                                selected = frequency == freq,
                                onClick = { frequency = freq },
                                label = { Text(freq) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryOrange.copy(alpha = 0.2f),
                                    selectedLabelColor = PrimaryOrange,
                                    labelColor = TextDark
                                )
                            )
                        }
                    }
                }
            }

            // KART 2: Gün Seçimi (Sadece Haftalık seçiliyse görünür)
            if (frequency == "Haftalık") {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hangi günler?", fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            daysMap.forEach { (fullName, shortName) ->
                                DayChip(shortName, fullName, selectedDays)
                            }
                        }
                    }
                }
            }

            // KART 3: Hatırlatıcılar
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
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
                            Text("Hatırlatıcılar", fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SurfaceWhite,
                                checkedTrackColor = PrimaryOrange
                            )
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (reminderTimes.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(reminderTimes) { time ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(time, fontWeight = FontWeight.Bold) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Sil",
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable { reminderTimes.remove(time) }
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = CreamBg,
                                            labelColor = PrimaryOrange
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange)
                        ) {
                            Icon(Icons.Default.Add, null, tint = PrimaryOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saat Ekle", color = PrimaryOrange)
                        }
                    }
                }
            }

            // KART 4: Aralıklı Tekrar
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Repeat, null, tint = ColorSport) // ColorSport = Mor
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aralıklı Tekrar", fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        Switch(
                            checked = isRepeatEnabled,
                            onCheckedChange = { isRepeatEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SurfaceWhite,
                                checkedTrackColor = ColorSport
                            )
                        )
                    }
                    if (isRepeatEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${repeatIntervalHours.toInt()} Saatte Bir",
                            color = TextDark,
                            fontSize = 14.sp
                        )
                        Slider(
                            value = repeatIntervalHours,
                            onValueChange = { repeatIntervalHours = it },
                            valueRange = 1f..12f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = ColorSport,
                                activeTrackColor = ColorSport
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.updateSchedule(
                        frequency,
                        selectedDays,
                        reminderEnabled,
                        reminderTimes,
                        isRepeatEnabled,
                        repeatIntervalHours.toInt()
                    )
                    onNext()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                Text("Devam Et", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

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
                Text("Ekle", color = PrimaryOrange, fontWeight = FontWeight.Bold)
            }
        },
        text = { content() },
        containerColor = SurfaceWhite
    )
}

@Composable
fun DayChip(
    shortName: String,
    fullName: String,
    selectedDays: MutableList<String>
) {
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
        Text(
            text = shortName,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}