package com.suer.levelup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.suer.levelup.data.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.min

// Renkler
private val CreamBg = Color(0xFFFDFDF6)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val PrimaryOrange = Color(0xFFFF6F61)
private val TextDark = Color(0xFF2D3436)
private val TextLight = Color(0xFF636E72)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(habitList: List<Habit>) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Haftalık", "Aylık", "Genel")
    var dateOffset by remember { mutableIntStateOf(0) }
    val chartScrollState = rememberChartScrollState()

    LaunchedEffect(selectedTab) { dateOffset = 0 }

    val today = LocalDate.now()
    val localeTR = Locale("tr", "TR")
    val dateFormatter = DateTimeFormatter.ISO_DATE

    val (chartData, xLabels, dateRangeLabel) = remember(selectedTab, habitList, dateOffset) {
        calculateChartDataWithNav(selectedTab, dateOffset, habitList, today, dateFormatter, localeTR)
    }

    val chartEntryModel = entryModelOf(*chartData.toTypedArray())

    LaunchedEffect(chartEntryModel) { chartScrollState.animateScrollBy(10000f) }

    val categoryCounts = habitList.groupingBy { it.category }.eachCount()
    val totalHabits = if (habitList.isNotEmpty()) habitList.size else 1
    val totalCompletedAllTime = habitList.sumOf { it.completedDates.size.toLong() }.toInt()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("İstatistikler", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CreamBg)
            )
        },
        containerColor = CreamBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Sekmeler
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceWhite,
                contentColor = PrimaryOrange,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryOrange
                        )
                    }
                },
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // 2. Özet Kartları
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard("Toplam", "$totalCompletedAllTime", "Tamamlanan", Modifier.weight(1f))
                val bestStreak = habitList.maxOfOrNull { it.streak } ?: 0
                InfoCard("En İyi Seri", "$bestStreak", "Gün", Modifier.weight(1f))
            }

            // 3. Grafik Kartı
            StatsCard(title = "${tabs[selectedTab]} Aktivite", icon = Icons.Rounded.BarChart) {
                Column {
                    // Navigasyon Butonları (Sadece Haftalık ve Aylık için göster)
                    if (selectedTab != 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { dateOffset-- }) {
                                Icon(Icons.Default.ChevronLeft, null, tint = TextLight)
                            }
                            Text(
                                text = dateRangeLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange
                            )
                            IconButton(onClick = { dateOffset++ }) {
                                Icon(Icons.Default.ChevronRight, null, tint = TextLight)
                            }
                        }
                    } else {
                        // Genel sekmesi için sadece başlık (Son 12 Ay)
                        Text(
                            text = dateRangeLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextLight,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // --- GRAFİK ALANI ---
                    ProvideChartStyle {
                        val column = LineComponent(
                            color = PrimaryOrange.hashCode(),
                            thicknessDp = 14f, // Çubuk kalınlığı
                            shape = Shapes.roundedCornerShape(allPercent = 40)
                        )

                        // Çubuklar arası boşluk ayarı (Genişlik için kritik)
                        // Genel sekmesinde (2) boşluğu artırıyoruz ki yana doğru uzasın
                        val spacing = if (selectedTab == 2) 32.dp else 12.dp

                        Chart(
                            chart = columnChart(
                                columns = listOf(column),
                                spacing = spacing // Burası grafiği genişletir
                            ),
                            model = chartEntryModel,
                            chartScrollState = chartScrollState, // Scroll özelliği

                            // DÜZELTME: Eksen Etiketlerinin Rengini Koyu Yapıyoruz
                            startAxis = rememberStartAxis(
                                title = "Adet",
                                titleComponent = textComponent(color = TextDark, textSize = 12.sp),
                                label = textComponent(color = TextDark, textSize = 10.sp)
                            ),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = AxisValueFormatter { value, _ ->
                                    xLabels.getOrElse(value.toInt()) { "" }
                                },
                                label = textComponent(color = TextDark, textSize = 10.sp),
                                guideline = null
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }

            // 4. Kategori Dağılımı
            StatsCard(title = "Kategori Dağılımı", icon = Icons.Rounded.PieChart) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (habitList.isEmpty()) {
                        Text("Henüz veri yok", color = TextLight, fontSize = 14.sp)
                    } else {
                        categoryCounts.forEach { (category, count) ->
                            val progress = count.toFloat() / totalHabits
                            val color = getColorForCategory(category)
                            CategoryProgressItem(category, progress, color)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun calculateChartDataWithNav(
    tabIndex: Int,
    offset: Int,
    habits: List<Habit>,
    referenceDate: LocalDate,
    formatter: DateTimeFormatter,
    locale: Locale
): Triple<List<Float>, List<String>, String> {

    val dataPoints = mutableListOf<Float>()
    val labels = mutableListOf<String>()
    var rangeLabel = ""

    when (tabIndex) {
        0 -> { // HAFTALIK
            val mondayOfTargetWeek = referenceDate
                .with(WeekFields.of(locale).firstDayOfWeek)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(offset.toLong())

            val endOfWeek = mondayOfTargetWeek.plusDays(6)
            val startStr = mondayOfTargetWeek.format(DateTimeFormatter.ofPattern("d MMM", locale))
            val endStr = endOfWeek.format(DateTimeFormatter.ofPattern("d MMM", locale))
            rangeLabel = "$startStr - $endStr"

            for (i in 0..6) {
                val date = mondayOfTargetWeek.plusDays(i.toLong())
                val dateStr = date.format(formatter)
                val count = habits.sumOf { habit -> if (habit.completedDates.contains(dateStr)) 1L else 0L }
                dataPoints.add(count.toFloat())
                labels.add(date.format(DateTimeFormatter.ofPattern("EEE", locale)))
            }
        }

        1 -> { // AYLIK
            val targetMonth = referenceDate.plusMonths(offset.toLong())
            rangeLabel = targetMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))

            val lengthOfMonth = targetMonth.lengthOfMonth()

            // Ayı 4 haftaya böl
            for (week in 0..3) {
                var weeklyCount = 0L
                val startDay = (week * 7) + 1

                if (startDay <= lengthOfMonth) {
                    val endDay = min((week * 7) + 7, lengthOfMonth)
                    for (day in startDay..endDay) {
                        val date = targetMonth.withDayOfMonth(day)
                        val dateStr = date.format(formatter)
                        weeklyCount += habits.sumOf { if (it.completedDates.contains(dateStr)) 1L else 0L }
                    }
                    dataPoints.add(weeklyCount.toFloat())
                    labels.add("${week + 1}.Hf")
                }
            }
        }

        2 -> { // GENEL (Son 12 Ay)
            rangeLabel = "Son 12 Ay"
            // Son 12 ayı hesapla
            for (i in 11 downTo 0) {
                val month = referenceDate.minusMonths(i.toLong())
                val monthPrefix = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))

                val count = habits.sumOf { habit ->
                    habit.completedDates.count { it.startsWith(monthPrefix) }.toLong()
                }
                dataPoints.add(count.toFloat())
                labels.add(month.format(DateTimeFormatter.ofPattern("MMM", locale)))
            }
        }
    }

    if (dataPoints.isEmpty()) return Triple(listOf(0f), listOf(""), rangeLabel)
    return Triple(dataPoints, labels, rangeLabel)
}

@Composable
fun InfoCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 12.sp, color = TextLight)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(subtitle, fontSize = 12.sp, color = PrimaryOrange, fontWeight = FontWeight.Medium)
        }
    }
}

fun getColorForCategory(category: String): Color {
    return when (category) {
        "Sağlık" -> Color(0xFFFF6B6B)
        "Spor" -> Color(0xFF6C5CE7)
        "Kişisel Gelişim", "Okuma" -> Color(0xFF4ECDC4)
        "Finans" -> Color(0xFFFFD93D)
        else -> Color(0xFFA29BFE)
    }
}

@Composable
fun StatsCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(PrimaryOrange.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = PrimaryOrange)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
            Spacer(modifier = Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
fun CategoryProgressItem(name: String, progress: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontWeight = FontWeight.Medium, color = TextDark)
            Text("${(progress * 100).toInt()}%", color = TextLight, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.3f),
        )
    }
}