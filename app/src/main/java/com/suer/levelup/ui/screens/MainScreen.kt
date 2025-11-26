package com.suer.levelup.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalLibrary
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suer.levelup.data.model.Habit
import com.suer.levelup.ui.viewmodel.AuthViewModel
import com.suer.levelup.ui.viewmodel.HabitViewModel
import com.suer.levelup.ui.viewmodel.UserData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

// --- RENK PALETİ ---
private val CreamBg = Color(0xFFFDFDF6)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val PrimaryOrange = Color(0xFFFF6F61)
private val SecondaryBlue = Color(0xFF4A90E2)
private val TextDark = Color(0xFF2D3436)
private val TextLight = Color(0xFF636E72)
private val SuccessGreen = Color(0xFF2ECC71)

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToCreateHabit: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    habitViewModel: HabitViewModel = viewModel()
) {
    val userData by authViewModel.currentUserData.collectAsState()
    val habitList by habitViewModel.habitList.collectAsState()
    var selectedItem by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        habitViewModel.fetchHabits()
        authViewModel.fetchUserData()
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(selectedItem) { selectedItem = it } },
        containerColor = CreamBg
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedItem) {
                0 -> HomeContent(
                    userData = userData,
                    habitList = habitList,
                    onNavigateToCreateHabit = onNavigateToCreateHabit,
                    onProgressChange = { habit, newProgress ->
                        // --- PUAN HİLESİ ENGELLEME MANTIĞI ---
                        val wasCompleted = habit.progress >= 1f
                        val isCompletedNow = newProgress >= 1f

                        habitViewModel.updateHabitProgress(habit, newProgress)

                        if (!wasCompleted && isCompletedNow) {
                            // Tamamlanmamıştı, şimdi tamamlandı -> +10 Puan
                            authViewModel.updateUserPoints(10)
                        } else if (wasCompleted && !isCompletedNow) {
                            // Tamamlanmıştı, geri alındı -> -10 Puan
                            authViewModel.updateUserPoints(-10)
                        }
                    },
                    onDeleteHabit = { habit ->
                        habitViewModel.deleteHabit(habit.id)
                        // Silinince puanı geri almak istersen buraya logic ekleyebilirsin
                        // Şimdilik silinen alışkanlık puanı kullanıcıda kalsın :)
                    },
                    onEditHabit = { habit ->
                        // Buraya ileride düzenleme sayfasına gitme kodu gelecek
                    }
                )
                1 -> StatisticsScreenPlaceholder()
                2 -> ProfileScreenPlaceholder(onLogout, authViewModel)
            }
        }
    }
}

@Composable
fun HomeContent(
    userData: UserData,
    habitList: List<Habit>,
    onNavigateToCreateHabit: () -> Unit,
    onProgressChange: (Habit, Float) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onEditHabit: (Habit) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Üst Kısım
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            HeaderSection(userData)
            NewHabitButton(onNavigateToCreateHabit)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bugünkü Hedeflerin",
                color = TextDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. KAYDIRILABİLİR KARTLAR
        if (habitList.isEmpty()) {
            EmptyStateView()
        } else {
            val pagerState = rememberPagerState(pageCount = { habitList.size })

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.weight(1f)
            ) { page ->
                val habit = habitList[page]
                BigHabitCard(
                    habit = habit,
                    onProgressChange = onProgressChange,
                    onDeleteClick = onDeleteHabit,
                    onEditClick = onEditHabit
                )
            }

            // Pager Göstergesi
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) PrimaryOrange else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BigHabitCard(
    habit: Habit,
    onProgressChange: (Habit, Float) -> Unit,
    onDeleteClick: (Habit) -> Unit,
    onEditClick: (Habit) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.80f)
            .shadow(8.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        // Box kullanarak ikonları üst üste bindireceğiz
        Box(modifier = Modifier.fillMaxSize()) {

            // --- ANA İÇERİK ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Üst Kısım
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CategoryIconBox(habit.category, size = 56.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = habit.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1
                    )
                }

                // ORTA KISIM: CIRCULAR SLIDER
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    CircularSlider(
                        value = habit.progress,
                        onValueChange = { newValue ->
                            onProgressChange(habit, newValue)
                        },
                        modifier = Modifier.fillMaxSize(),
                        primaryColor = if(habit.progress >= 1f) SuccessGreen else SecondaryBlue,
                        secondaryColor = CreamBg
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(habit.progress * 100).toInt()}%",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = if(habit.progress >= 1f) "Tamamlandı" else "Devam Ediyor",
                            fontSize = 12.sp,
                            color = TextLight
                        )
                    }
                }

                // Alt Kısım
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, null, tint = PrimaryOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${habit.streak} Günlük Seri", color = TextLight, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = CreamBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = habit.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // --- SAĞ ÜST İKONLAR ---
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                // İkonlar arasındaki boşluğu artırdık: 8.dp -> 16.dp ✅
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SİLME BUTONU (Sadece %100 ise görünür)
                if (habit.progress >= 1f) {
                    IconButton(
                        onClick = { onDeleteClick(habit) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Sil",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // DÜZENLEME BUTONU (Her zaman görünür)
                IconButton(
                    onClick = { onEditClick(habit) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(CreamBg, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = TextLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// --- CIRCULAR SLIDER ---
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CircularSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color
) {
    var radius by remember { mutableFloatStateOf(0f) }
    var center by remember { mutableStateOf(Offset.Zero) }

    val strokeWidthDp = 14.dp
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidthDp.toPx() }

    Canvas(
        modifier = modifier
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        val x = event.x - center.x
                        val y = event.y - center.y
                        val angle = (Math.toDegrees(atan2(y.toDouble(), x.toDouble())) + 90 + 360) % 360
                        val newProgress = angle.toFloat() / 360f

                        if (abs(newProgress - value) > 0.5f) {
                            if (value > 0.5f) onValueChange(1f) else onValueChange(0f)
                        } else {
                            onValueChange(newProgress)
                        }
                        true
                    }
                    else -> false
                }
            }
            .onGloballyPositioned { coordinates ->
                val size = coordinates.size
                val minDimension = min(size.width, size.height).toFloat()
                radius = (minDimension - strokeWidthPx) / 2f
                center = Offset(size.width / 2f, size.height / 2f)
            }
    ) {
        val arcRadius = radius
        drawCircle(color = secondaryColor, radius = arcRadius, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round))
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = value * 360f,
            useCenter = false,
            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
        val angleInDegrees = (value * 360f) - 90f
        val angleInRadians = angleInDegrees * (PI / 180f).toFloat()
        val knobX = center.x + arcRadius * cos(angleInRadians)
        val knobY = center.y + arcRadius * sin(angleInRadians)
        drawCircle(color = Color.White, radius = strokeWidthPx * 0.7f, center = Offset(knobX, knobY))
        drawCircle(color = primaryColor, radius = strokeWidthPx * 0.7f, center = Offset(knobX, knobY), style = Stroke(width = 2.dp.toPx()))
    }
}

// ... (Diğer tüm yardımcı bileşenler aynı) ...
// HeaderSection, NewHabitButton, EmptyStateView, CategoryIconBox, BottomNavigationBar, Placeholders...
// Kodun fazla uzamaması için buraya tekrar kopyalamadım, önceki yanıttakilerle birebir aynıdır.
// Sadece yukarıdaki BigHabitCard ve HomeContent kısımları değişti.
// Eğer tam kodu istersen, alt kısım bileşenlerini de ekleyip tek parça halinde verebilirim.

@Composable
fun HeaderSection(userData: UserData) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "İyi Günler,", color = TextLight, fontSize = 16.sp)
            Text(
                text = if (userData.name.isNotEmpty()) userData.name else "Şampiyon",
                color = TextDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${userData.totalPoints}",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun NewHabitButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(70.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Yeni Alışkanlık", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Kendine bir iyilik yap", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            Box(
                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun CategoryIconBox(category: String, size: androidx.compose.ui.unit.Dp = 50.dp) {
    val (icon, color) = when (category) {
        "Sağlık" -> Icons.Rounded.Favorite to Color(0xFFFF6B6B)
        "Kişisel Gelişim" -> Icons.Rounded.LocalLibrary to Color(0xFF4ECDC4)
        "Finans" -> Icons.Rounded.MonetizationOn to Color(0xFFFFD93D)
        "Spor" -> Icons.Rounded.FitnessCenter to Color(0xFF6C5CE7)
        else -> Icons.Rounded.SelfImprovement to PrimaryOrange
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size / 2)
        )
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Henüz bir hedefin yok", color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Hadi, ilk adımını at! 🚀", color = TextLight, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(containerColor = SurfaceWhite, contentColor = PrimaryOrange, tonalElevation = 16.dp) {
        val items = listOf(
            Triple("Ana Sayfa", Icons.Default.Home, 0),
            Triple("İstatistikler", Icons.Default.List, 1),
            Triple("Profil", Icons.Default.Person, 2)
        )
        items.forEach { (label, icon, index) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontWeight = if(selectedItem==index) FontWeight.Bold else FontWeight.Normal) },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryOrange,
                    selectedTextColor = PrimaryOrange,
                    indicatorColor = PrimaryOrange.copy(alpha = 0.15f),
                    unselectedIconColor = TextLight,
                    unselectedTextColor = TextLight
                )
            )
        }
    }
}

@Composable
fun StatisticsScreenPlaceholder() {
    Box(modifier = Modifier.fillMaxSize().background(CreamBg), contentAlignment = Alignment.Center) {
        Text("İstatistikler", color = TextDark)
    }
}

@Composable
fun ProfileScreenPlaceholder(onLogout: () -> Unit, viewModel: AuthViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(CreamBg), contentAlignment = Alignment.Center) {
        Button(onClick = { viewModel.signOut(); onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)) {
            Text("Çıkış Yap")
        }
    }
}