package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.BlendMode

// =========================================================================
// =================== LIVING PASTEL SKY & CORE THEMING ====================
// =========================================================================

@Composable
fun LivingPastelSky(
    viewModel: ZenoViewModel,
    modifier: Modifier = Modifier
) {
    val weather by viewModel.weatherState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val overrideTime by viewModel.skyTimeOverride.collectAsState()
    val overrideWeather by viewModel.skyWeatherOverride.collectAsState()

    val currentHour = remember(overrideTime) {
        if (overrideTime >= 0) overrideTime else Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }
    val currentDesc = remember(weather, overrideWeather) {
        if (overrideWeather.isNotEmpty()) overrideWeather.lowercase() else weather.weatherDescription.lowercase()
    }

    // Sunrise: 5-7, Midday: 8-16, Sunset: 17-18, Dusk: 19-20, Night: 21-4
    val skyState = remember(currentHour) {
        when {
            currentHour in 5..7 -> "sunrise"
            currentHour in 8..16 -> "midday"
            currentHour in 17..18 -> "sunset"
            currentHour in 19..20 -> "dusk"
            else -> "night"
        }
    }

    val colorTop = animateColorAsState(
        targetValue = when {
            isDark -> Color(0xFF161617) // Calm dark
            skyState == "sunrise" -> Color(0xFFFFDFD0) // soft peach
            skyState == "midday" -> Color(0xFFB3E5FC) // bright sky blue
            skyState == "sunset" -> Color(0xFFFF8A80) // warm coral
            skyState == "dusk" -> Color(0xFF4A148C) // mauve
            else -> Color(0xFF1E1035) // deep lavender night
        },
        animationSpec = tween(2000),
        label = "SkyTopAnim"
    )

    val colorBottom = animateColorAsState(
        targetValue = when {
            isDark -> Color(0xFF101011)
            skyState == "sunrise" -> Color(0xFFFFC0A0) // apricot
            skyState == "midday" -> Color(0xFFE1F5FE) // lazy white-blue
            skyState == "sunset" -> Color(0xFFFFD180) // rose gold
            skyState == "dusk" -> Color(0xFF8E24AA) // dusky indigo
            else -> Color(0xFF0F081D) // midnight base
        },
        animationSpec = tween(2000),
        label = "SkyBottomAnim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colorTop.value, colorBottom.value)))
    ) {
        if (!isDark) {
            when {
                currentDesc.contains("rain") || currentDesc.contains("shower") || currentDesc.contains("drizzle") -> {
                    FallingRainAnimation()
                }
                currentDesc.contains("snow") || currentDesc.contains("ice") || currentDesc.contains("flurry") -> {
                    FallingSnowAnimation()
                }
                skyState == "sunrise" -> {
                    SunriseRaysAnimation()
                }
                skyState == "midday" -> {
                    DriftingCloudsAnimation()
                }
                skyState == "sunset" -> {
                    SunsetGlowAnimation()
                }
                skyState == "dusk" -> {
                    DuskLapseAnimation()
                }
                skyState == "night" -> {
                    TwinklingStarsAnimation()
                }
            }
        } else {
            // Dark mode always features subtle night stars for cozy ambient
            TwinklingStarsAnimation()
        }

        // Sleeping or Happy Sky Character
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 44.dp, end = 24.dp)
        ) {
            SkyCharacter(skyState = skyState, isDark = isDark)
        }
    }
}

@Composable
fun SkyCharacter(skyState: String, isDark: Boolean) {
    Canvas(modifier = Modifier.size(46.dp)) {
        val w = size.width
        val h = size.height
        val s = 1.6f.dp.toPx() // Stroke width calibration

        if (isDark || skyState == "night" || skyState == "dusk") {
            // Crescent Moon
            val moonColor = Color(0xFFFFFDE7)
            drawCircle(moonColor, radius = w * 0.35f, center = Offset(w * 0.45f, h * 0.5f))
            drawCircle(Color.Transparent, radius = w * 0.35f, center = Offset(w * 0.28f, h * 0.42f), blendMode = BlendMode.Clear)

            // Sleepy crescent face line
            val faceColor = Color(0xFF5D4037)
            drawArc(
                color = faceColor.copy(alpha = 0.5f),
                startAngle = 10f,
                sweepAngle = 150f,
                useCenter = false,
                style = Stroke(s, cap = StrokeCap.Round),
                size = Size(w * 0.15f, h * 0.12f),
                topLeft = Offset(w * 0.44f, h * 0.44f)
            )
            // tranquil curve smile
            drawArc(
                color = faceColor.copy(alpha = 0.5f),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(s, cap = StrokeCap.Round),
                size = Size(w * 0.1f, h * 0.1f),
                topLeft = Offset(w * 0.48f, h * 0.56f)
            )
        } else {
            // Sun for sunrise/midday/sunset
            drawCircle(color = Color(0xFFFFD54F), radius = w * 0.32f)
            val rayColor = Color(0xFFFFCA28)
            val rayCount = 8
            for (i in 0 until rayCount) {
                val angle = i * (360f / rayCount)
                val rad = Math.toRadians(angle.toDouble())
                val startX = (w / 2) + Math.cos(rad).toFloat() * (w * 0.34f)
                val startY = (h / 2) + Math.sin(rad).toFloat() * (h * 0.34f)
                val endX = (w / 2) + Math.cos(rad).toFloat() * (w * 0.46f)
                val endY = (h / 2) + Math.sin(rad).toFloat() * (h * 0.46f)
                drawLine(color = rayColor, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = s * 1.5f)
            }

            val faceColor = Color(0xFF5D4037)
            if (skyState == "sunrise") {
                // Sleepy Waking Sun
                drawLine(color = faceColor, start = Offset(w * 0.36f, h * 0.45f), end = Offset(w * 0.46f, h * 0.45f), strokeWidth = s)
                drawLine(color = faceColor, start = Offset(w * 0.54f, h * 0.45f), end = Offset(w * 0.64f, h * 0.45f), strokeWidth = s)
                drawCircle(color = faceColor, radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.62f))
            } else if (skyState == "midday") {
                // Bright happy Sun
                drawCircle(color = faceColor, radius = s * 1.2f, center = Offset(w * 0.38f, h * 0.46f))
                drawCircle(color = faceColor, radius = s * 1.2f, center = Offset(w * 0.62f, h * 0.46f))
                drawArc(
                    color = faceColor,
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    style = Stroke(s, cap = StrokeCap.Round),
                    size = Size(w * 0.22f, h * 0.22f),
                    topLeft = Offset(w * 0.39f, h * 0.45f)
                )
            } else {
                // Sunset content/happy Sun
                drawArc(
                    color = faceColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(s, cap = StrokeCap.Round),
                    size = Size(w * 0.12f, h * 0.12f),
                    topLeft = Offset(w * 0.35f, h * 0.42f)
                )
                drawArc(
                    color = faceColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(s, cap = StrokeCap.Round),
                    size = Size(w * 0.12f, h * 0.12f),
                    topLeft = Offset(w * 0.53f, h * 0.42f)
                )
                drawArc(
                    color = faceColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(s, cap = StrokeCap.Round),
                    size = Size(w * 0.2f, h * 0.15f),
                    topLeft = Offset(w * 0.4f, h * 0.55f)
                )
            }
        }
    }
}

@Composable
fun FallingRainAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "RainTrans")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RainProg"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val linesCount = 30
        for (i in 0 until linesCount) {
            val personalProgress = (progress + (i / linesCount.toFloat())) % 1f
            val yStart = personalProgress * h * 1.2f - 100f
            val xStart = ((w / linesCount) * i + (personalProgress * 180f)) % w
            val length = 28.dp.toPx()
            drawLine(
                color = Color.White.copy(alpha = 0.28f),
                start = Offset(xStart, yStart),
                end = Offset(xStart - length * 0.22f, yStart + length),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun FallingSnowAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "SnowTrans")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SnowProg"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val flakesCount = 24
        for (i in 0 until flakesCount) {
            val personalProgress = (progress + (i / flakesCount.toFloat())) % 1f
            val y = personalProgress * h
            val xOffset = Math.sin(personalProgress * Math.PI * 6 + i).toFloat() * 18.dp.toPx()
            val x = ((w / flakesCount) * i + xOffset + w) % w
            drawCircle(
                color = Color.White.copy(alpha = 0.40f),
                radius = (2.5f + i % 3.5f).dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun SunriseRaysAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "RaysTrans")
    val angleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(80000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RaysRotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = 0f
        val cy = size.height
        val rayLines = 8
        for (i in 0 until rayLines) {
            val angle = i * (180f / rayLines) + (angleRotation % 20f)
            val rad = Math.toRadians(angle.toDouble())
            val rx = cx + Math.cos(rad).toFloat() * size.width * 1.6f
            val ry = cy - Math.sin(rad).toFloat() * size.height * 1.6f
            drawLine(
                color = Color(0xFFFFE0B2).copy(alpha = 0.08f),
                start = Offset(cx, cy),
                end = Offset(rx, ry),
                strokeWidth = 40.dp.toPx()
            )
        }
    }
}

@Composable
fun DriftingCloudsAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "CloudsTrans")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(34000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudsProg"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val seedClouds = listOf(
            Triple(0.18f, 0.22f, 48.dp.toPx()),
            Triple(0.55f, 0.14f, 68.dp.toPx()),
            Triple(0.82f, 0.32f, 42.dp.toPx())
        )

        seedClouds.forEach { (cloudX, cloudY, r) ->
            val initialX = w * cloudX
            val x = (initialX + (progress * w)) % w
            val y = h * cloudY

            drawCircle(color = Color.White.copy(alpha = 0.22f), radius = r, center = Offset(x, y))
            drawCircle(color = Color.White.copy(alpha = 0.22f), radius = r * 0.78f, center = Offset(x - r * 0.55f, y))
            drawCircle(color = Color.White.copy(alpha = 0.22f), radius = r * 0.78f, center = Offset(x + r * 0.55f, y))
        }
    }
}

@Composable
fun SunsetGlowAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "SunsetGlowTrans")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SunsetGlowPulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.78f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFD180).copy(alpha = 0.12f), Color.Transparent),
                center = Offset(cx, cy),
                radius = 240.dp.toPx() * scaleFactor
            ),
            radius = 340.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}

@Composable
fun DuskLapseAnimation() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xFF311B92).copy(alpha = 0.08f), Color(0xFF4A148C).copy(alpha = 0.12f))
            ),
            size = size
        )
    }
}

@Composable
fun TwinklingStarsAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "StarsTrans")
    val alphaA by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarA"
    )
    val alphaB by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarB"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val coordinates = listOf(
            Offset(w * 0.12f, h * 0.12f) to alphaA,
            Offset(w * 0.38f, h * 0.24f) to alphaB,
            Offset(w * 0.72f, h * 0.16f) to alphaA,
            Offset(w * 0.88f, h * 0.34f) to alphaB,
            Offset(w * 0.52f, h * 0.20f) to alphaA,
            Offset(w * 0.25f, h * 0.44f) to alphaB,
            Offset(w * 0.64f, h * 0.42f) to alphaA
        )

        coordinates.forEach { (pos, alpha) ->
            val dim = 5.dp.toPx()
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(pos.x - dim, pos.y),
                end = Offset(pos.x + dim, pos.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(pos.x, pos.y - dim),
                end = Offset(pos.x, pos.y + dim),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

// =========================================================================
// ==================== FROSTED GLASS CARDS & WIDGETS =====================
// =========================================================================

@Composable
fun ZenoFrostedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.34f)
    val borderLight = if (isDark) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.38f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, borderLight, RoundedCornerShape(20.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun ZenoWeatherWidget(weather: WeatherState, isDark: Boolean, onClick: () -> Unit) {
    ZenoFrostedCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(32.dp).border(1.dp, (if (isDark) Color.White else Color.Black).copy(alpha = 0.12f), CircleShape))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(ZenoSageGreen))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = weather.temperature.ifEmpty { "68°F" }, fontSize = 20.sp, fontWeight = FontWeight.Light, color = if (isDark) ZenoTextDark else ZenoTextLight)
                    Text(text = weather.weatherDescription.ifEmpty { "Clear" }.uppercase(), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, letterSpacing = 2.sp, color = (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.7f))
                }
                val firstDaily = weather.dailyForecast.firstOrNull()
                Text(text = if (firstDaily != null) "HIGH ${firstDaily.maxTemp} / LOW ${firstDaily.minTemp}" else "HIGH 72° / LOW 54°", fontSize = 10.sp, fontWeight = FontWeight.Light, color = (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun ZenoTasksWidget(tasks: List<Task>, isDark: Boolean, onToggle: (Task) -> Unit) {
    ZenoFrostedCard {
        Text("TODAY'S GLANCE", style = MaterialTheme.typography.labelSmall, color = ZenoSageGreen, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
        if (tasks.isEmpty()) {
            Text("Your schedule is beautifully clear. Enjoy the empty space.", style = MaterialTheme.typography.bodyMedium, color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary, fontWeight = FontWeight.Light)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tasks.take(3).forEach { task ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val dotColor = when (task.priority) {
                            1 -> ZenoPriorityHigh
                            2 -> ZenoPriorityMedium
                            else -> ZenoPriorityLow
                        }
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(dotColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(dotColor))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = task.title, fontSize = 14.sp, fontWeight = FontWeight.Light, color = if (task.completed) (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.4f) else if (isDark) ZenoTextDark else ZenoTextLight, modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = task.completed,
                            onCheckedChange = { onToggle(task) },
                            colors = CheckboxDefaults.colors(checkedColor = ZenoSageGreen, uncheckedColor = ZenoSageGreen.copy(alpha = 0.4f), checkmarkColor = if (isDark) ZenoBackgroundDark else ZenoBackgroundLight),
                            modifier = Modifier.scale(0.80f).size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZenoCalendarWidget(isDark: Boolean) {
    val events = listOf(
        "10:00 AM" to "Sipping Tea Mindful Reflection",
        "04:15 PM" to "Breathe & Walk Zen Rhythm"
    )
    ZenoFrostedCard {
        Text("CALENDAR EVENTS", style = MaterialTheme.typography.labelSmall, color = ZenoSageGreen, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            events.forEach { (time, name) ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(time, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = ZenoSageGreen, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(name, fontSize = 13.sp, fontWeight = FontWeight.Light, color = if (isDark) ZenoTextDark else ZenoTextLight)
                }
            }
        }
    }
}

@Composable
fun ZenoStocksWidget(isDark: Boolean) {
    ZenoFrostedCard {
        Text("CALM STOCKS TICKER", style = MaterialTheme.typography.labelSmall, color = ZenoSageGreen, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val indexes = listOf(
                "SLOW" to "+2.4%",
                "COZY" to "+3.1%",
                "BREATH" to "+1.5%"
            )
            indexes.forEach { (symbol, change) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(symbol, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) ZenoTextDark else ZenoTextLight)
                    Text(change, fontSize = 10.sp, color = ZenoSageGreen, fontWeight = FontWeight.Light)
                }
            }
        }
    }
}

@Composable
fun ZenoBatteryWidget(isDark: Boolean) {
    ZenoFrostedCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ZenoIcon(name = "battery", tint = ZenoSageGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("ZERO POWER ENGINE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = ZenoSageGreen, letterSpacing = 1.sp)
                    Text("Drawing 0.04W (High Efficiency)", fontSize = 11.sp, color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary, fontWeight = FontWeight.Light)
                }
            }
            Text("88%", style = MaterialTheme.typography.bodyLarge, fontSize = 18.sp, fontWeight = FontWeight.Light, color = if (isDark) ZenoTextDark else ZenoTextLight)
        }
    }
}

@Composable
fun ZenoHabitGlanceWidget(habits: List<Habit>, isDark: Boolean, onToggle: (Habit) -> Unit) {
    ZenoFrostedCard {
        Text("HABITS STREAKS", style = MaterialTheme.typography.labelSmall, color = ZenoSageGreen, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
        if (habits.isEmpty()) {
            Text("No habits scheduled. Deep simple living.", style = MaterialTheme.typography.bodyMedium, color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                habits.take(2).forEach { habit ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(habit.name, fontSize = 14.sp, color = if (isDark) ZenoTextDark else ZenoTextLight, fontWeight = FontWeight.Light)
                            Text("Streak: ${habit.streak} days", fontSize = 10.sp, color = ZenoSageGreen, fontWeight = FontWeight.Light)
                        }
                        val isCompletedToday = habit.completedDates.contains(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isCompletedToday) ZenoSageGreen else (if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)))
                                .clickable { onToggle(habit) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZenoQuoteWidget(quote: String, isDark: Boolean) {
    ZenoFrostedCard {
        Text(
            text = quote,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            color = (if (isDark) ZenoTextDark else ZenoTextLight).copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
            lineHeight = 20.sp
        )
    }
}

// --- POWER-BASED CUSTOM LINE ICON DRAWINGS (ZERO EXTRA DEPENDENCIES) ---
@Composable
fun ZenoIcon(
    name: String,
    modifier: Modifier = Modifier,
    tint: Color = ZenoSageGreen
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.35.dp.toPx()

        when (name.lowercase()) {
            "glance" -> {
                // Circle concentric ring with a small lock dots inside
                drawCircle(color = tint, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.56f), style = Stroke(strokeWidth))
                // Lock arch
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(strokeWidth),
                    size = Size(w * 0.36f, h * 0.36f),
                    topLeft = Offset(w * 0.32f, h * 0.22f)
                )
            }
            "planner" -> {
                // Dynamic line art representing schedule lines
                drawLine(color = tint, start = Offset(w * 0.2f, h * 0.35f), end = Offset(w * 0.8f, h * 0.35f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.2f, h * 0.55f), end = Offset(w * 0.65f, h * 0.55f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.2f, h * 0.75f), end = Offset(w * 0.5f, h * 0.75f), strokeWidth = strokeWidth)
                // Small pin track dot
                drawCircle(color = tint, radius = strokeWidth * 1.5f, center = Offset(w * 0.78f, h * 0.55f))
            }
            "weather" -> {
                // Sun cloud line drawings
                drawCircle(color = tint, radius = w * 0.15f, center = Offset(w * 0.35f, h * 0.5f), style = Stroke(strokeWidth))
                drawCircle(color = tint, radius = w * 0.2f, center = Offset(w * 0.53f, h * 0.44f), style = Stroke(strokeWidth))
                drawCircle(color = tint, radius = w * 0.15f, center = Offset(w * 0.68f, h * 0.55f), style = Stroke(strokeWidth))
                drawLine(color = tint, start = Offset(w * 0.24f, h * 0.62f), end = Offset(w * 0.76f, h * 0.62f), strokeWidth = strokeWidth)
            }
            "habits" -> {
                // Dot streak connectors representing habits grid
                drawCircle(color = tint, radius = w * 0.1f, center = Offset(w * 0.25f, h * 0.5f), style = Stroke(strokeWidth))
                drawCircle(color = tint, radius = w * 0.1f, center = Offset(w * 0.5f, h * 0.5f))
                drawCircle(color = tint, radius = w * 0.1f, center = Offset(w * 0.75f, h * 0.5f), style = Stroke(strokeWidth))
                drawLine(color = tint, start = Offset(w * 0.35f, h * 0.5f), end = Offset(w * 0.4f, h * 0.5f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.6f, h * 0.5f), end = Offset(w * 0.65f, h * 0.5f), strokeWidth = strokeWidth)
            }
            "sun" -> {
                // Clean thin circle + 8 light strokes
                drawCircle(color = tint, radius = w * 0.25f, style = Stroke(strokeWidth))
                for (i in 0 until 8) {
                    val angle = i * Math.PI / 4
                    val startX = (w / 2) + Math.cos(angle).toFloat() * (w * 0.32f)
                    val startY = (h / 2) + Math.sin(angle).toFloat() * (h * 0.32f)
                    val endX = (w / 2) + Math.cos(angle).toFloat() * (w * 0.46f)
                    val endY = (h / 2) + Math.sin(angle).toFloat() * (h * 0.46f)
                    drawLine(color = tint, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = strokeWidth)
                }
            }
            "cloud" -> {
                // Cloud outline
                drawCircle(color = tint, radius = w * 0.15f, center = Offset(w * 0.35f, h * 0.5f), style = Stroke(strokeWidth))
                drawCircle(color = tint, radius = w * 0.2f, center = Offset(w * 0.53f, h * 0.45f), style = Stroke(strokeWidth))
                drawCircle(color = tint, radius = w * 0.15f, center = Offset(w * 0.68f, h * 0.55f), style = Stroke(strokeWidth))
                drawLine(color = tint, start = Offset(w * 0.24f, h * 0.62f), end = Offset(w * 0.76f, h * 0.62f), strokeWidth = strokeWidth)
            }
            "rain" -> {
                // Cloud with three rain lines
                drawCircle(color = tint, radius = w * 0.15f, center = Offset(w * 0.35f, h * 0.45f), style = Stroke(strokeWidth))
                drawCircle(color = tint, radius = w * 0.2f, center = Offset(w * 0.53f, h * 0.4f), style = Stroke(strokeWidth))
                drawCircle(color = tint, radius = w * 0.15f, center = Offset(w * 0.68f, h * 0.5f), style = Stroke(strokeWidth))
                drawLine(color = tint, start = Offset(w * 0.24f, h * 0.56f), end = Offset(w * 0.76f, h * 0.56f), strokeWidth = strokeWidth)
                // Rain droplets
                drawLine(color = tint, start = Offset(w * 0.38f, h * 0.66f), end = Offset(w * 0.34f, h * 0.78f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.53f, h * 0.66f), end = Offset(w * 0.49f, h * 0.78f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.68f, h * 0.66f), end = Offset(w * 0.64f, h * 0.78f), strokeWidth = strokeWidth)
            }
            "morning" -> {
                // Rising sun over horizontal shelf
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(strokeWidth),
                    size = Size(w * 0.4f, h * 0.4f),
                    topLeft = Offset(w * 0.3f, h * 0.28f)
                )
                drawLine(color = tint, start = Offset(w * 0.15f, h * 0.68f), end = Offset(w * 0.85f, h * 0.68f), strokeWidth = strokeWidth)
                // Rays
                drawLine(color = tint, start = Offset(w * 0.5f, h * 0.25f), end = Offset(w * 0.5f, h * 0.15f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.32f, h * 0.35f), end = Offset(w * 0.24f, h * 0.27f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.68f, h * 0.35f), end = Offset(w * 0.76f, h * 0.27f), strokeWidth = strokeWidth)
            }
            "afternoon" -> {
                // Bold sun with double circles for zen atmosphere
                drawCircle(color = tint, radius = w * 0.22f, style = Stroke(strokeWidth))
                drawCircle(color = tint, radius = w * 0.1f, style = Stroke(strokeWidth / 2))
                for (i in 0 until 4) {
                    val angle = i * Math.PI / 2
                    val startX = (w / 2) + Math.cos(angle).toFloat() * (w * 0.28f)
                    val startY = (h / 2) + Math.sin(angle).toFloat() * (h * 0.28f)
                    val endX = (w / 2) + Math.cos(angle).toFloat() * (w * 0.42f)
                    val endY = (h / 2) + Math.sin(angle).toFloat() * (h * 0.42f)
                    drawLine(color = tint, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = strokeWidth)
                }
            }
            "evening" -> {
                // Stylized thin sickle moon
                drawArc(
                    color = tint,
                    startAngle = -45f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(strokeWidth),
                    size = Size(w * 0.5f, h * 0.5f),
                    topLeft = Offset(w * 0.25f, h * 0.25f)
                )
                drawArc(
                    color = tint,
                    startAngle = -22f,
                    sweepAngle = 135f,
                    useCenter = false,
                    style = Stroke(strokeWidth * 0.8f),
                    size = Size(w * 0.38f, h * 0.38f),
                    topLeft = Offset(w * 0.34f, h * 0.26f)
                )
            }
            "air" -> {
                // Stream wind vectors
                drawLine(color = tint, start = Offset(w * 0.2f, h * 0.35f), end = Offset(w * 0.7f, h * 0.35f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.3f, h * 0.5f), end = Offset(w * 0.8f, h * 0.5f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.15f, h * 0.65f), end = Offset(w * 0.6f, h * 0.65f), strokeWidth = strokeWidth)
            }
            "uv" -> {
                // Double eye outline with middle dot
                drawArc(
                    color = tint,
                    startAngle = 30f,
                    sweepAngle = 120f,
                    useCenter = false,
                    style = Stroke(strokeWidth),
                    size = Size(w * 0.7f, h * 0.5f),
                    topLeft = Offset(w * 0.15f, h * 0.15f)
                )
                drawArc(
                    color = tint,
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    style = Stroke(strokeWidth),
                    size = Size(w * 0.7f, h * 0.5f),
                    topLeft = Offset(w * 0.15f, h * 0.35f)
                )
                drawCircle(color = tint, radius = strokeWidth * 2f)
            }
            "humidity" -> {
                // Drop shape line art: curved base with a sharp point
                drawCircle(color = tint, radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.62f), style = Stroke(strokeWidth))
                drawLine(color = tint, start = Offset(w * 0.32f, h * 0.6f), end = Offset(w * 0.50f, h * 0.28f), strokeWidth = strokeWidth)
                drawLine(color = tint, start = Offset(w * 0.68f, h * 0.6f), end = Offset(w * 0.50f, h * 0.28f), strokeWidth = strokeWidth)
            }
            "battery" -> {
                // Battery outline + fill level segment
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.2f, h * 0.35f),
                    size = Size(w * 0.55f, h * 0.3f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(strokeWidth)
                )
                // Battery cap
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.75f, h * 0.43f),
                    size = Size(w * 0.08f, h * 0.14f),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
                // Battery charge fill segment
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.27f, h * 0.41f),
                    size = Size(w * 0.25f, h * 0.18f),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
            "focus" -> {
                // Target core concentric ring
                drawCircle(color = tint, radius = w * 0.38f, style = Stroke(strokeWidth))
                drawCircle(color = tint, radius = w * 0.2f, style = Stroke(strokeWidth / 1.5f))
                drawCircle(color = tint, radius = w * 0.06f)
            }
            else -> {
                // Default placeholder zen dot
                drawCircle(color = tint, radius = w * 0.1f)
            }
        }
    }
}

// --- TAB DATA STRUTURE ---
data class TabInfo(val id: String, val iconName: String, val label: String)
data class PeriodInfo(val name: String, val iconName: String)
data class BadgeInfo(val title: String, val value: String, val iconName: String)

// --- MAIN BOTTOM NAV CONTAINER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenoApp(viewModel: ZenoViewModel) {
    val isFocusActive by viewModel.isFocusActive.collectAsState()
    
    var currentTab by remember { mutableStateOf("Glance") }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LivingPastelSky(viewModel = viewModel)
        if (isFocusActive) {
            FocusSanctuaryScreen(viewModel = viewModel)
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    NavigationBar(
                        containerColor = if (isSystemInDarkTheme()) ZenoBackgroundDark.copy(alpha = 0.95f) else ZenoBackgroundLight.copy(alpha = 0.95f),
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("zeno_bottom_nav")
                    ) {
                        val navItems = listOf(
                            TabInfo("Glance", "glance", "Glance"),
                            TabInfo("Planner", "planner", "Planner"),
                            TabInfo("Weather", "weather", "Weather"),
                            TabInfo("Habits", "habits", "Habits")
                        )

                        navItems.forEach { item ->
                            val isSelected = currentTab == item.id
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = item.id },
                                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                                icon = {
                                    ZenoIcon(
                                        name = item.iconName,
                                        tint = if (isSelected) ZenoSageGreen else (if (isSystemInDarkTheme()) ZenoTextDarkSecondary else ZenoTextLightSecondary),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ZenoSageGreen,
                                    selectedTextColor = ZenoSageGreen,
                                    indicatorColor = if (isSystemInDarkTheme()) ZenoSurfaceDark else ZenoSurfaceLight,
                                    unselectedIconColor = if (isSystemInDarkTheme()) ZenoTextDarkSecondary else ZenoTextLightSecondary,
                                    unselectedTextColor = if (isSystemInDarkTheme()) ZenoTextDarkSecondary else ZenoTextLightSecondary
                                )
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Crossfade(
                        targetState = currentTab,
                        animationSpec = tween(500, easing = LinearOutSlowInEasing),
                        label = "ZenoTabCrossfade"
                    ) { tab ->
                        when (tab) {
                            "Glance" -> GlanceScreen(
                                viewModel = viewModel,
                                onNavigateToWeather = { currentTab = "Weather" },
                                onNavigateToFocus = { viewModel.enterFocusMode() }
                            )
                            "Planner" -> PlannerScreen(
                                viewModel = viewModel,
                                onOpenAddTask = { showAddTaskDialog = true }
                            )
                            "Weather" -> WeatherScreen(viewModel = viewModel)
                            "Habits" -> HabitsScreen(
                                viewModel = viewModel,
                                onOpenAddHabit = { showAddHabitDialog = true }
                            )
                        }
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAdd = { title, timeOfDay, priority ->
                    viewModel.addTask(title, timeOfDay, priority)
                    showAddTaskDialog = false
                }
            )
        }

        // Add Habit Dialog
        if (showAddHabitDialog) {
            AddHabitDialog(
                onDismiss = { showAddHabitDialog = false },
                onAdd = { name ->
                    viewModel.addHabit(name)
                    showAddHabitDialog = false
                }
            )
        }
    }
}


// --- 1. GLANCE SCREEN (Simulated Lock Screen) ---
@Composable
fun GlanceScreen(
    viewModel: ZenoViewModel,
    onNavigateToWeather: () -> Unit,
    onNavigateToFocus: () -> Unit
) {
    val topTasks by viewModel.top3TasksForToday.collectAsState()
    val weather by viewModel.weatherState.collectAsState()
    val quote by viewModel.currentQuote.collectAsState()
    val habits by viewModel.habits.collectAsState()

    val widgetsOrder by viewModel.widgetsOrder.collectAsState()
    val widgetsEnabled by viewModel.widgetsEnabled.collectAsState()

    // Dialog trigger states
    var showWidgetCustomizeDialog by remember { mutableStateOf(false) }
    var showFocusSetupDialog by remember { mutableStateOf(false) }

    // Real-time ticking time state
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDateString = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now).uppercase()
            delay(1000)
        }
    }

    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row - Minimal Zen Status Bar with widgets customizing trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ZENO SERVICE",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                letterSpacing = 2.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) ZenoTextDark else ZenoTextLight,
                modifier = Modifier.alpha(0.4f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Settings button for Widgets arrangement & Sky overrides
                IconButton(
                    onClick = { showWidgetCustomizeDialog = true },
                    modifier = Modifier.size(24.dp).testTag("widgets_customize_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Customize Screen Widgets",
                        tint = if (isDark) ZenoTextDark else ZenoTextLight,
                        modifier = Modifier.size(16.dp).alpha(0.5f)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.alpha(0.4f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isDark) ZenoTextDark else ZenoTextLight)
                    )
                    Text(
                        text = "88%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) ZenoTextDark else ZenoTextLight
                    )
                }
            }
        }

        // Clock Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = currentTimeString.ifEmpty { "10:48" },
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                fontSize = 82.sp,
                lineHeight = 76.sp,
                color = if (isDark) ZenoTextDark else ZenoTextLight,
                letterSpacing = (-2).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentDateString.ifEmpty { "TUESDAY, OCTOBER 24" },
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.alpha(0.6f)
            )
        }

        // Glance Area (Weather & Tasks grouped inside a scrollable column)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            widgetsOrder.forEach { id ->
                if (widgetsEnabled[id] == true) {
                    when (id) {
                        "weather" -> ZenoWeatherWidget(weather = weather, isDark = isDark, onClick = onNavigateToWeather)
                        "tasks" -> ZenoTasksWidget(tasks = topTasks, isDark = isDark, onToggle = { viewModel.toggleTaskCompletion(it) })
                        "calendar" -> ZenoCalendarWidget(isDark = isDark)
                        "habits" -> ZenoHabitGlanceWidget(habits = habits, isDark = isDark, onToggle = { viewModel.toggleHabitForToday(it) })
                        "stocks" -> ZenoStocksWidget(isDark = isDark)
                        "battery" -> ZenoBatteryWidget(isDark = isDark)
                        "quote" -> ZenoQuoteWidget(quote = quote, isDark = isDark)
                    }
                }
            }
        }

        // Bottom Controls & Affirmation (Centered block)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            // Solid Classy Minimal Enter Focus Button
            Button(
                onClick = { showFocusSetupDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color.White else ZenoTextLight
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 240.dp)
                    .testTag("enter_focus_button")
            ) {
                Text(
                    text = "ENTER FOCUS MODE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = if (isDark) ZenoBackgroundDark else ZenoBackgroundLight,
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Deco pill
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background((if (isDark) Color.White else Color.Black).copy(alpha = 0.1f))
            )
        }
    }

    // Modal dialog overlays
    if (showWidgetCustomizeDialog) {
        WidgetCustomizeDialog(
            viewModel = viewModel,
            onDismiss = { showWidgetCustomizeDialog = false }
        )
    }

    if (showFocusSetupDialog) {
        FocusSetupDialog(
            viewModel = viewModel,
            onDismiss = { showFocusSetupDialog = false }
        )
    }
}


// --- 2. PLANNER SCREEN ---
@Composable
fun PlannerScreen(
    viewModel: ZenoViewModel,
    onOpenAddTask: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasksForSelectedDate.collectAsState()

    val weeklyDays = remember {
        val list = mutableListOf<Pair<String, String>>()
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2)

        for (i in 0 until 7) {
            val label = sdfDay.format(cal.time).uppercase()
            val dateStr = sdfDate.format(cal.time)
            list.add(Pair(label, dateStr))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PLANNER",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenoSageGreen,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Weekly Order",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSystemInDarkTheme()) ZenoTextDark else ZenoTextLight
                )
            }

            IconButton(
                onClick = onOpenAddTask,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ZenoSageGreen)
                    .size(40.dp)
                    .testTag("add_task_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = if (isSystemInDarkTheme()) ZenoBackgroundDark else ZenoBackgroundLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly Strip Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items(weeklyDays) { (dayLabel, dateStr) ->
                val isSelected = dateStr == selectedDate
                val dayNum = dateStr.takeLast(2)

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) ZenoSageGreen else Color.Transparent
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (isSelected) Color.Transparent else (if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setDate(dateStr) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (isSelected) {
                            if (isSystemInDarkTheme()) ZenoBackgroundDark else ZenoBackgroundLight
                        } else {
                            if (isSystemInDarkTheme()) ZenoTextDarkSecondary else ZenoTextLightSecondary
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dayNum,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) {
                            if (isSystemInDarkTheme()) ZenoBackgroundDark else ZenoBackgroundLight
                        } else {
                            if (isSystemInDarkTheme()) ZenoTextDark else ZenoTextLight
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grouped Tasks by Morning / Afternoon / Evening
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val periods = listOf(
                PeriodInfo("Morning", "morning"),
                PeriodInfo("Afternoon", "afternoon"),
                PeriodInfo("Evening", "evening")
            )

            periods.forEach { period ->
                val periodTasks = tasks.filter { it.timeOfDay.equals(period.name, ignoreCase = true) }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            ZenoIcon(
                                name = period.iconName,
                                tint = ZenoSageGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = period.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSystemInDarkTheme()) ZenoTextDarkSecondary else ZenoTextLightSecondary,
                                letterSpacing = 1.sp
                            )
                        }

                        if (periodTasks.isEmpty()) {
                            Text(
                                text = "Empty slot. Breath space here.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraLight,
                                color = if (isSystemInDarkTheme()) ZenoTextDarkSecondary.copy(alpha = 0.5f) else ZenoTextLightSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                            )
                        } else {
                            periodTasks.forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSystemInDarkTheme()) ZenoSurfaceDark.copy(alpha = 0.3f) else ZenoSurfaceLight.copy(alpha = 0.4f))
                                        .clickable { viewModel.toggleTaskCompletion(task) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dotColor = when (task.priority) {
                                        1 -> ZenoPriorityHigh
                                        2 -> ZenoPriorityMedium
                                        else -> ZenoPriorityLow
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Light,
                                        color = if (task.completed) {
                                            if (isSystemInDarkTheme()) ZenoTextDarkSecondary.copy(alpha = 0.5f) else ZenoTextLightSecondary.copy(alpha = 0.5f)
                                        } else {
                                            if (isSystemInDarkTheme()) ZenoTextDark else ZenoTextLight
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (task.completed) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = ZenoSageGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Quick Delete trigger (Uses core verified Delete icon)
                                    IconButton(
                                        onClick = { viewModel.deleteTask(task.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete task",
                                            tint = (if (isSystemInDarkTheme()) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- 3. WEATHER SCREEN ---
@Composable
fun WeatherScreen(viewModel: ZenoViewModel) {
    val weather by viewModel.weatherState.collectAsState()

    val isDark = isSystemInDarkTheme()
    val weatherThemeBg = if (isDark) {
        ZenoBackgroundDark
    } else {
        when {
            weather.isRainy -> Color(0xFFE3E5EB)
            weather.isCloudy -> Color(0xFFEBECF0)
            else -> Color(0xFFF7F4EC)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Weather Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ATMOSPHERE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenoSageGreen,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Open-Meteo API",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDark) ZenoTextDark else ZenoTextLight
                )
            }

            IconButton(
                onClick = { viewModel.refreshWeatherForce() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDark) ZenoSurfaceDark else ZenoSurfaceLight)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh cached weather",
                    tint = ZenoSageGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Weather Indicator Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ZenoIcon(
                name = when {
                    weather.isRainy -> "rain"
                    weather.isCloudy -> "cloud"
                    else -> "sun"
                },
                tint = ZenoSageGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = weather.temperature,
                style = MaterialTheme.typography.displayLarge,
                fontSize = 52.sp,
                fontWeight = FontWeight.Light,
                color = if (isDark) ZenoTextDark else ZenoTextLight
            )
            Text(
                text = weather.weatherDescription.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary),
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Small Pill Badges Row (Explicit Triple Types defined so compiler is happy)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val badges = listOf(
                BadgeInfo("AIR QUALITY", weather.airQualityIndex, "air"),
                BadgeInfo("UV INDEX", weather.uvIndex, "sun"),
                BadgeInfo("HUMIDITY", weather.relativeHumidity, "humidity")
            )

            badges.forEach { badge ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) ZenoSurfaceDark.copy(alpha = 0.5f) else ZenoSurfaceLight.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ZenoIcon(
                            name = badge.iconName,
                            tint = ZenoSageGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = badge.value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) ZenoTextDark else ZenoTextLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hourly scrolling ribbon title
        Text(
            text = "HOURLY OVERVIEW",
            style = MaterialTheme.typography.labelSmall,
            color = ZenoSageGreen,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Hourly graph scrolling ribbon
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(weather.hourlyForecast) { item ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) ZenoSurfaceDark.copy(alpha = 0.3f) else ZenoSurfaceLight.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.hour,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ZenoIcon(
                        name = when {
                            WeatherUtils.isRainy(item.code) -> "rain"
                            WeatherUtils.isCloudy(item.code) -> "cloud"
                            else -> "sun"
                        },
                        tint = ZenoSageGreen.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.temp,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) ZenoTextDark else ZenoTextLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 7-Day outlook Below
        Text(
            text = "7-DAY OUTLOOK",
            style = MaterialTheme.typography.labelSmall,
            color = ZenoSageGreen,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        weather.dailyForecast.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) ZenoSurfaceDark.copy(alpha = 0.2f) else ZenoSurfaceLight.copy(alpha = 0.2f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.dayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) ZenoTextDark else ZenoTextLight,
                    modifier = Modifier.weight(1.5f)
                )

                ZenoIcon(
                    name = when {
                        WeatherUtils.isRainy(item.code) -> "rain"
                        WeatherUtils.isCloudy(item.code) -> "cloud"
                        else -> "sun"
                    },
                    tint = ZenoSageGreen,
                    modifier = Modifier
                        .size(16.dp)
                        .weight(1f)
                )

                Row(
                    modifier = Modifier.weight(2f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.minTemp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) ZenoTextDarkSecondary.copy(alpha = 0.5f) else ZenoTextLightSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.maxTemp,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) ZenoTextDark else ZenoTextLight
                    )
                }
            }
        }
    }
}


// --- 4. HABIT TRACKER SCREEN ---
@Composable
fun HabitsScreen(
    viewModel: ZenoViewModel,
    onOpenAddHabit: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Habits Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HABITS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenoSageGreen,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Natural Rhythm",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDark) ZenoTextDark else ZenoTextLight
                )
            }

            IconButton(
                onClick = onOpenAddHabit,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ZenoSageGreen)
                    .size(40.dp)
                    .testTag("add_habit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Habit",
                    tint = if (isDark) ZenoBackgroundDark else ZenoBackgroundLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No custom habits active. Create up to 6 routines to track with simple structural dots.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraLight,
                    color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits) { habit ->
                    HabitItemRow(
                        habit = habit, 
                        viewModel = viewModel,
                        onDelete = { viewModel.deleteHabit(habit.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitItemRow(
    habit: Habit,
    viewModel: ZenoViewModel,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val dateList = remember {
        val list = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0 until 7) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) ZenoSurfaceDark.copy(alpha = 0.4f) else ZenoSurfaceLight.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            0.5.dp, 
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) ZenoTextDark else ZenoTextLight
                    )
                    Text(
                        text = "${habit.streak}-day streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZenoSageGreen
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val completedCount = dateList.count { habit.completedDates.contains(it) }
            val progressFraction = completedCount / 7f
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp)),
                color = ZenoSageGreen,
                trackColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 7 minimal dot grid row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dateList.forEachIndexed { index, dateStr ->
                    val isToday = dateStr == viewModel.getTodayDateString()
                    val completed = habit.completedDates.contains(dateStr)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isToday) "T" else dateStr.takeLast(2),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Light,
                            color = if (isToday) ZenoSageGreen else (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (completed) ZenoSageGreen else Color.Transparent
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (completed) Color.Transparent else (if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f)),
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.toggleHabitForToday(habit)
                                }
                        )
                    }
                }
            }
        }
    }
}


// --- 5. FOCUS/LOCKSCREEN SANCTUARY SCREEN ---
@Composable
fun FocusSanctuaryScreen(viewModel: ZenoViewModel) {
    val isZen by viewModel.isZenMode.collectAsState()
    val isIncomingCallActive by viewModel.isIncomingCallActive.collectAsState()
    val callerName by viewModel.callerName.collectAsState()

    if (isZen) {
        ZenModeFocusScreen(
            viewModel = viewModel,
            isIncomingCallActive = isIncomingCallActive,
            callerName = callerName
        )
        return
    }

    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val timeRemaining by viewModel.focusTimeRemaining.collectAsState()
    val breatheState by viewModel.breatheState.collectAsState()
    val breatheProgress by viewModel.breatheProgress.collectAsState()
    val selectedSound by viewModel.selectedFocusSound.collectAsState()

    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val ambientPulseScale by animateFloatAsState(
        targetValue = 1f + (breatheProgress * 0.4f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "BreathePulseCircle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Sanctuary Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Text(
                    text = "ZENO DEEP COHERENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Distraction-Free Sanctuary Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }

            // Central Breathing Pulse circle helper
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = size.minDimension / 2 * ambientPulseScale
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.02f),
                            radius = size.minDimension / 1.5f * ambientPulseScale
                        )
                        drawCircle(
                            color = ZenoSageGreen.copy(alpha = 0.15f * breatheProgress),
                            radius = size.minDimension / 2.2f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = breatheState.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color.White
                    )
                }
            }

            // Lower controller block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CURRENT INTENTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Presence and Pure Focus Block",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Sound select ribbons
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sounds = listOf("Rain", "Forest", "Off")
                    sounds.forEach { sName ->
                        val isSelected = sName == selectedSound
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { viewModel.setFocusSound(sName) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Standard Toggle Play/Pause trigger
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleFocusTimer() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .size(56.dp)
                    ) {
                        // Standard guaranteed play and stop check
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "Trigger Flow",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Slide To Unlock / Disconnect lock panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                        .clickable { viewModel.exitFocusMode() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Unlock blocker",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PRESS TO EXIT SANCTUARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}


// --- TASK ADD DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, timeOfDay: String, priority: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var timeOfDay by remember { mutableStateOf("Morning") }
    var priority by remember { mutableStateOf(2) }

    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) ZenoSurfaceDark else ZenoBackgroundLight
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                0.5.dp, 
                if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "NEW ATTENTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenoSageGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task description", style = MaterialTheme.typography.bodyMedium) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZenoSageGreen,
                        unfocusedBorderColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.15f),
                        focusedLabelColor = ZenoSageGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("task_input_field")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TIME OF DAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val times = listOf("Morning", "Afternoon", "Evening")
                    times.forEach { t ->
                        val selected = t == timeOfDay
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) ZenoSageGreen else (if (isDark) ZenoSurfaceDark else ZenoSurfaceLight)
                                )
                                .clickable { timeOfDay = t }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Light,
                                color = if (selected) {
                                    if (isDark) ZenoBackgroundDark else ZenoBackgroundLight
                                } else {
                                    if (isDark) ZenoTextDark else ZenoTextLight
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PRIORITY RHYTHM",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val priorities = listOf(
                        Triple(1, "High", ZenoPriorityHigh),
                        Triple(2, "Medium", ZenoPriorityMedium),
                        Triple(3, "Low", ZenoPriorityLow)
                    )
                    priorities.forEach { (pNum, pName, dotColor) ->
                        val selected = pNum == priority
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) ZenoSageGreen.copy(alpha = 0.2f) else Color.Transparent
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) ZenoSageGreen else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { priority = pNum }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 12.sp,
                                    color = if (isDark) ZenoTextDark else ZenoTextLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = ZenoSageGreen, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(title, timeOfDay, priority)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenoSageGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ADD", color = if (isDark) ZenoBackgroundDark else ZenoBackgroundLight, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}


// --- HABIT ADD DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) ZenoSurfaceDark else ZenoBackgroundLight
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                0.5.dp, 
                if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "NEW HABIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenoSageGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit name", style = MaterialTheme.typography.bodyMedium) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZenoSageGreen,
                        unfocusedBorderColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.15f),
                        focusedLabelColor = ZenoSageGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("habit_input_field")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = ZenoSageGreen, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(name)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenoSageGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CREATE", color = if (isDark) ZenoBackgroundDark else ZenoBackgroundLight, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// =========================================================================
// ==================== LOCK SCREEN WIDGET CUSTOMIZER =====================
// =========================================================================

@Composable
fun WidgetCustomizeDialog(
    viewModel: ZenoViewModel,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val widgetsOrder by viewModel.widgetsOrder.collectAsState()
    val widgetsEnabled by viewModel.widgetsEnabled.collectAsState()
    val skyTimeOverride by viewModel.skyTimeOverride.collectAsState()
    val skyWeatherOverride by viewModel.skyWeatherOverride.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) ZenoSurfaceDark else ZenoSurfaceLight
            ),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                0.5.dp,
                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "LOCK SCREEN GRAPHICS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenoSageGreen,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Part 1: Widget Ordering & Toggle
                    Text(
                        "WIDGETS LAYOUT & REORDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )

                    widgetsOrder.forEachIndexed { index, id ->
                        val isEnabled = widgetsEnabled[id] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background((if (isDark) Color.White else Color.Black).copy(alpha = 0.04f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Up/down arrow ordering controls
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val newList = widgetsOrder.toMutableList()
                                            // Swap
                                            newList[index] = newList[index - 1]
                                            newList[index - 1] = id
                                            viewModel.updateWidgetConfig(newList, widgetsEnabled)
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Move widget up",
                                        tint = if (isDark) ZenoTextDark else ZenoTextLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (index < widgetsOrder.size - 1) {
                                            val newList = widgetsOrder.toMutableList()
                                            newList[index] = newList[index + 1]
                                            newList[index + 1] = id
                                            viewModel.updateWidgetConfig(newList, widgetsEnabled)
                                        }
                                    },
                                    enabled = index < widgetsOrder.size - 1,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Move widget down",
                                        tint = if (isDark) ZenoTextDark else ZenoTextLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = id.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light,
                                    color = if (isDark) ZenoTextDark else ZenoTextLight
                                )
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    val newMap = widgetsEnabled.toMutableMap()
                                    newMap[id] = checked
                                    viewModel.updateWidgetConfig(widgetsOrder, newMap)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ZenoSageGreen,
                                    checkedTrackColor = ZenoSageGreen.copy(alpha = 0.3f),
                                    uncheckedThumbColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f),
                                    uncheckedTrackColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Part 2: Living Sky Overrides (Morning, Midday, Sunset, Dusk, Night)
                    Text(
                        "LIVING SKY CLIMATE CONTROLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Select any state to instantly trigger the imperceptibly slow, reactive transition & wakes the corner sky character.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        color = (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.6f),
                        lineHeight = 14.sp
                    )

                    // Time rows
                    val times = listOf(
                        "Automatic" to -1,
                        "Sunrise (Peach)" to 6,
                        "Midday (Azure)" to 12,
                        "Sunset (Rose)" to 18,
                        "Dusk (Mauve)" to 20,
                        "Night (Indigo)" to 23
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(times) { (label, hrValue) ->
                            val isSelected = skyTimeOverride == hrValue
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSkyTimeOverride(hrValue) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Light) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ZenoSageGreen,
                                    selectedLabelColor = if (isDark) ZenoBackgroundDark else ZenoBackgroundLight
                                )
                            )
                        }
                    }

                    // Weather weather rows
                    val weatherOptions = listOf(
                        "Automatic" to "",
                        "Gentle Rain" to "Rain",
                        "Silent Flurries" to "Snow",
                        "Tranquil Clear" to "Clear"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(weatherOptions) { (label, descVal) ->
                            val isSelected = skyWeatherOverride == descVal
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSkyWeatherOverride(descVal) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Light) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ZenoSageGreen,
                                    selectedLabelColor = if (isDark) ZenoBackgroundDark else ZenoBackgroundLight
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "CLOSE",
                            color = ZenoSageGreen,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// ==================== FOCUS MODE HARMONY SELECTION ======================
// =========================================================================

@Composable
fun FocusSetupDialog(
    viewModel: ZenoViewModel,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var selectedMinutes by remember { mutableStateOf(25) }
    var selectedSound by remember { mutableStateOf("Silent Cosmic Space") }
    var isZenModeSelected by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) ZenoSurfaceDark else ZenoSurfaceLight
            ),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                0.5.dp,
                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "CHOOSE FOCUS HARMONY",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenoSageGreen,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                // Minutes selection rows
                Text(
                    "DURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 25, 50).forEach { mins ->
                        val selected = selectedMinutes == mins
                        Button(
                            onClick = { selectedMinutes = mins },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) ZenoSageGreen else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
                                contentColor = if (selected) (if (isDark) ZenoBackgroundDark else ZenoBackgroundLight) else (if (isDark) ZenoTextDark else ZenoTextLight)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text("${mins}m", fontSize = 12.sp, fontWeight = FontWeight.Light)
                        }
                    }
                }

                // Sounds selection row
                Text(
                    "AMBIENT NOISE SCAPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = (if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary).copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Silent Sky", "Pine Forest", "Cosmic Wave").forEach { sound ->
                        val selected = selectedSound == sound
                        Button(
                            onClick = { selectedSound = sound },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) ZenoSageGreen else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
                                contentColor = if (selected) (if (isDark) ZenoBackgroundDark else ZenoBackgroundLight) else (if (isDark) ZenoTextDark else ZenoTextLight)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text(sound, fontSize = 9.sp, fontWeight = FontWeight.Light)
                        }
                    }
                }

                // Zen focus toggler
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background((if (isDark) Color.White else Color.Black).copy(alpha = 0.04f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "ZEN MODE COUNTDOWN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) ZenoTextDark else ZenoTextLight
                        )
                        Text(
                            "Greyscale UI. Locks interactions except phone calls.",
                            fontSize = 9.sp,
                            color = if (isDark) ZenoTextDarkSecondary else ZenoTextLightSecondary,
                            fontWeight = FontWeight.Light
                        )
                    }
                    Switch(
                        checked = isZenModeSelected,
                        onCheckedChange = { isZenModeSelected = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ZenoSageGreen,
                            checkedTrackColor = ZenoSageGreen.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = ZenoSageGreen, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.setFocusSound(selectedSound)
                            viewModel.enterFocusMode(task = null, durationMinutes = selectedMinutes, isZen = isZenModeSelected)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenoSageGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("START SESSION", color = if (isDark) ZenoBackgroundDark else ZenoBackgroundLight, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// =========================================================================
// ==================== GREYSCALE ZEN TIMER SANCTUARY =====================
// =========================================================================

@Composable
fun ZenModeFocusScreen(
    viewModel: ZenoViewModel,
    isIncomingCallActive: Boolean,
    callerName: String
) {
    val timeRemaining by viewModel.focusTimeRemaining.collectAsState()
    val topTasks by viewModel.top3TasksForToday.collectAsState()

    val mins = timeRemaining / 60
    val secs = timeRemaining % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    val currentTaskTitle = topTasks.firstOrNull { !it.completed }?.title ?: "Deep Simple Breathing"

    // Pure greyscale background for complete non-distraction focus
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0E))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text(
                    text = "ZENO FOCUS SANCTUARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                    Text(
                        text = "Zen Mode Active (All distractions blocked)",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Light
                    )
                }
            }

            // Central Pure greyscale Focus Widget
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Focus Task
                Text(
                    text = "CURRENT INTENTION",
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    color = Color.White.copy(alpha = 0.35f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentTaskTitle.uppercase(),
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(44.dp))

                // Timer Display in classic display typography
                Text(
                    text = timeFormatted,
                    fontSize = 90.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = Color.White,
                    letterSpacing = (-2).sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Call simulator triggers for easy verification
                Button(
                    onClick = { viewModel.simulateIncomingCall("Emergency: Mom") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Simulate call exception",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Simulate Emergency Call",
                            fontSize = 10.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Bottom Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.exitFocusMode() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 220.dp)
                ) {
                    Text(
                        text = "EXIT ZEN SESSION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Overlay simulated Call filter
        if (isIncomingCallActive) {
            val isCallAnswered = callerName.startsWith("Active:")
            val displayName = callerName.removePrefix("Active: ")

            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E20)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // High Contrast Minimal Screen
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCallAnswered) Icons.Default.Phone else Icons.Default.Person,
                                contentDescription = "Active caller icon",
                                tint = if (isCallAnswered) Color.Green else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isCallAnswered) {
                            Text(
                                text = "CALL IN PROGRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Green,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Muted • Spk Active • 0:14",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Light
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = { viewModel.declineIncomingCall() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "DISCONNECT PHONE",
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                text = "FILTERING CALL EXCEPTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.declineIncomingCall() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("DECLINE", fontSize = 11.sp, color = Color.White)
                                }
                                Button(
                                    onClick = { viewModel.answerIncomingCall() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("ANSWER", fontSize = 11.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

