package com.suer.levelup.ui.screens

import android.widget.Toast
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.suer.levelup.R
import com.suer.levelup.data.model.Habit
import com.suer.levelup.ui.viewmodel.AuthViewModel
import com.suer.levelup.ui.viewmodel.HabitViewModel
import com.suer.levelup.ui.viewmodel.UserData
import com.suer.levelup.ui.theme.* // <-- TÜM RENKLER BURADAN GELİYOR
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

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
    val context = LocalContext.current

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
                0 -> {
                    val activeHabits = habitList.filter { !it.isArchived && it.id.isNotEmpty() }
                    HomeContent(
                        userData = userData,
                        habitList = activeHabits,
                        onNavigateToCreateHabit = onNavigateToCreateHabit,
                        onProgressChange = { habit, newProgress ->
                            val wasCompleted = habit.progress >= 1f
                            val isCompletedNow = newProgress >= 1f
                            habitViewModel.updateHabitProgress(habit, newProgress)
                            if (!wasCompleted && isCompletedNow) authViewModel.updateUserPoints(10)
                            else if (wasCompleted && !isCompletedNow) authViewModel.updateUserPoints(-10)
                        },
                        onDeleteHabit = { habit ->
                            if (habit.id.isNotEmpty()) {
                                habitViewModel.archiveHabit(habit.id)
                                Toast.makeText(context, "Silindi", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEditHabit = { habit ->
                            habitViewModel.prepareForEdit(habit)
                            onNavigateToCreateHabit()
                        },
                        habitViewModel = habitViewModel
                    )
                }
                1 -> StatisticsScreen(habitList = habitList)
                2 -> {
                    val gso = remember {
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail().build()
                    }
                    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

                    ProfileScreen(
                        userData = userData,
                        onLogoutClick = {
                            googleSignInClient.signOut().addOnCompleteListener {
                                authViewModel.signOut()
                                onLogout()
                            }
                        },
                        onDeleteAccountClick = {
                            authViewModel.deleteAccount(
                                onSuccess = {
                                    googleSignInClient.signOut().addOnCompleteListener { onLogout() }
                                },
                                onError = { }
                            )
                        },
                        viewModel = authViewModel
                    )
                }
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
    onEditHabit: (Habit) -> Unit,
    habitViewModel: HabitViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            HeaderSection(userData)
            NewHabitButton(onClick = {
                habitViewModel.resetForNewHabit()
                onNavigateToCreateHabit()
            })
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Bugünkü Hedeflerin",
                color = TextDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (habitList.isEmpty()) {
            EmptyStateView()
        } else {
            val pagerState = rememberPagerState(pageCount = { habitList.size })

            HorizontalPager(
                state = pagerState,
                key = { index -> if (index < habitList.size) habitList[index].id else index },
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.weight(1f)
            ) { page ->
                if (page < habitList.size) {
                    BigHabitCard(habitList[page], onProgressChange, onDeleteHabit, onEditHabit)
                }
            }

            Row(
                modifier = Modifier
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
            .fillMaxHeight(0.88f)
            .shadow(8.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // İkon ve Başlık
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CategoryIconBox(habit.category, size = 64.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = habit.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )
                }

                // İlerleme Çemberi
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                    CircularSlider(
                        value = habit.progress,
                        onValueChange = { newValue -> onProgressChange(habit, newValue) },
                        modifier = Modifier.fillMaxSize(),
                        primaryColor = if (habit.progress >= 1f) SuccessGreen else SecondaryBlue,
                        secondaryColor = CreamBg
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(habit.progress * 100).toInt()}%",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = if (habit.progress >= 1f) "Tamamlandı" else "Devam Ediyor",
                            fontSize = 14.sp,
                            color = TextLight
                        )
                    }
                }

                // Alt Bilgiler
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${habit.streak} Günlük Seri", color = TextLight, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(color = CreamBg, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = habit.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Düzenle ve Sil Butonları
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (habit.progress >= 1f) {
                    IconButton(
                        onClick = { onDeleteClick(habit) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Sil",
                            tint = Color.Red,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { onEditClick(habit) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(CreamBg, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = TextLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

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
    val strokeWidthDp = 16.dp
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
        drawCircle(
            color = secondaryColor,
            radius = arcRadius,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
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
        drawCircle(
            color = Color.White,
            radius = strokeWidthPx * 0.7f,
            center = Offset(knobX, knobY)
        )
        drawCircle(
            color = primaryColor,
            radius = strokeWidthPx * 0.7f,
            center = Offset(knobX, knobY),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun HeaderSection(userData: UserData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "İyi Günler,",
                color = TextLight,
                fontSize = 16.sp
            )
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
                    tint = Color(0xFFFFC107), // Yıldız sarısı özel kalabilir
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
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
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
                Text(
                    text = "Yeni Alışkanlık",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Kendine bir iyilik yap",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun CategoryIconBox(category: String, size: androidx.compose.ui.unit.Dp = 50.dp) {
    // Merkezi Color.kt dosyasından gelen renkleri kullanıyoruz
    val (icon, color) = when (category) {
        "Sağlık" -> Icons.Rounded.Favorite to ColorHealth
        "Spor" -> Icons.Rounded.FitnessCenter to ColorSport
        "Su İçmek" -> Icons.Rounded.WaterDrop to ColorWater
        "Okuma" -> Icons.Rounded.LocalLibrary to ColorReading
        "Finans" -> Icons.Rounded.MonetizationOn to ColorFinance
        "Kişisel" -> Icons.Rounded.Face to ColorPersonal
        "Yüzme" -> Icons.Rounded.Pool to ColorPool
        "Meditasyon" -> Icons.Rounded.SelfImprovement to ColorMeditation
        "Uyku" -> Icons.Rounded.Bedtime to ColorSleep
        "Yürüyüş" -> Icons.Rounded.DirectionsWalk to ColorWalk
        "Sosyal" -> Icons.Rounded.Groups to ColorSocial
        "Yaratıcılık" -> Icons.Rounded.Brush to ColorCreative
        "Kodlama" -> Icons.Rounded.Code to ColorCode
        "Müzik" -> Icons.Rounded.MusicNote to ColorMusic
        // Eski kayıtlar için destek
        "Kişisel Gelişim" -> Icons.Rounded.LocalLibrary to ColorReading
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Henüz bir hedefin yok",
            color = TextDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Hadi, ilk adımını at! 🚀",
            color = TextLight,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = SurfaceWhite,
        contentColor = PrimaryOrange,
        tonalElevation = 16.dp
    ) {
        val items = listOf(
            Triple("Ana Sayfa", Icons.Default.Home, 0),
            Triple("İstatistikler", Icons.Default.List, 1),
            Triple("Profil", Icons.Default.Person, 2)
        )

        items.forEach { (label, icon, index) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (selectedItem == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
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