package com.suer.levelup.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suer.levelup.ui.viewmodel.HabitViewModel
import com.suer.levelup.ui.theme.* // <-- TÜM RENKLERİ BURADAN ALIYORUZ
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCreationScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: HabitViewModel
) {
    val currentHabit = viewModel.newHabitState.collectAsState().value

    var title by remember { mutableStateOf(currentHabit.title) }
    var description by remember { mutableStateOf(currentHabit.description) }

    // Tarih İşlemleri
    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val initialDateMillis = try {
        if (currentHabit.startDate.isNotEmpty())
            dateFormatter.parse(currentHabit.startDate)?.time ?: calendar.timeInMillis
        else calendar.timeInMillis
    } catch (e: Exception) {
        calendar.timeInMillis
    }

    var selectedDate by remember { mutableStateOf(initialDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    // Tarih Seçici Diyalog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) {
                    Text("Seç", color = PrimaryOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("İptal", color = TextDark)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Adım 1/3", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
                text = if(currentHabit.id.isEmpty()) "Hayalindeki Alışkanlığı\nOluşturalım ✨" else "Alışkanlığını\nDüzenle ✏️",
                style = MaterialTheme.typography.headlineMedium,
                color = TextDark,
                fontWeight = FontWeight.ExtraBold
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- BAŞLIK ALANI ---
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Alışkanlık Adı") },
                        placeholder = { Text("Örn: Kitap okumak") },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = PrimaryOrange) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,

                        // KLAVYE AYARLARI
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrect = true,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),

                        // RENKLER
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = PrimaryOrange,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = PrimaryOrange
                        )
                    )

                    // --- AÇIKLAMA ALANI ---
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Motivasyon Notun") },
                        placeholder = { Text("Neden istiyorsun?") },
                        leadingIcon = { Icon(Icons.Default.Description, null, tint = PrimaryOrange) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 5,

                        // KLAVYE AYARLARI
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),

                        // RENKLER
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = PrimaryOrange,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = PrimaryOrange
                        )
                    )

                    // --- TARİH SEÇİMİ ---
                    Box {
                        OutlinedTextField(
                            value = dateFormatter.format(Date(selectedDate)),
                            onValueChange = { },
                            label = { Text("Başlangıç Tarihi") },
                            leadingIcon = { Icon(Icons.Default.Event, null, tint = PrimaryOrange) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = PrimaryOrange,
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    onClick = { showDatePicker = true },
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // İlerleme Çubuğu ve Buton
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = { 0.33f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimaryOrange,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.updateTitleDescDate(title, description, dateFormatter.format(Date(selectedDate)))
                        onNext()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
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