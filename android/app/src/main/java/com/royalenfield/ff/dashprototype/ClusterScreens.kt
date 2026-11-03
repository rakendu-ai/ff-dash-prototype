package com.royalenfield.ff.dashprototype

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable

// Shared chrome for every Cluster mode screen: black surface, connection line, footer controls.
@Composable
fun ClusterFrame(
    s: CockpitState,
    vm: CockpitViewModel,
    showLayouts: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    val off = !s.connected
    Column(Modifier.fillMaxSize().background(Color.Black).padding(horizontal = 18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("10:42", color = Re.N02, fontSize = 12.sp)
            Text(if (off) "—" else "912 m", color = Re.N02, fontSize = 12.sp)
            Text(
                if (off) "Not connected" else "Connected",
                color = if (off) Re.Red else Re.Teal,
                fontSize = 12.sp,
            )
        }
        if (off) {
            Row(
                Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF3A0E0E))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Connection to vehicle lost", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Reconnecting…", color = Re.N02, fontSize = 12.sp)
                }
                TextButton(onClick = { vm.showToast("Trying to reconnect…") }) { Text("Retry", color = Re.Red) }
            }
        }
        Column(Modifier.weight(1f).padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
        ClusterFooter(s, vm, showLayouts)
    }
}

@Composable
private fun ClusterFooter(s: CockpitState, vm: CockpitViewModel, showLayouts: Boolean) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showLayouts) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF121515))
                    .padding(3.dp),
            ) {
                LayoutChip("Cluster", s.screen !in APP_SCREENS, Modifier.weight(1f)) { vm.clusterHome() }
                LayoutChip("50:50", s.screen == "CM-11", Modifier.weight(1f)) { vm.go("CM-11") }
                LayoutChip("70:30", s.screen == "CM-12", Modifier.weight(1f)) { vm.go("CM-12") }
                LayoutChip("Nav+Music", s.screen == "CM-16", Modifier.weight(1f)) { vm.go("CM-16") }
                LayoutChip("Music", s.screen == "CM-13", Modifier.weight(1f)) { vm.go("CM-13") }
            }
        }
        if (!s.connected) {
            // Exit is never offered while disconnected.
        } else if (s.speed > 0) {
            OutlinedButton(
                onClick = { vm.exitBlocked() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(EXIT_LABEL, color = Color.White, fontSize = 12.sp) }
        } else {
            SwipeAction("Swipe to exit Cluster mode", SwipeTone.Red) { vm.exitCluster() }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { vm.clusterHome() },
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161919), contentColor = Color.White),
            ) { Text("⌂", fontSize = 18.sp) }
            Text(if (showLayouts) "" else "•  •  •", color = Re.N04, fontSize = 12.sp)
            SosButton(44.dp) { vm.sos("cluster") }
        }
    }
}

@Composable
private fun LayoutChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Re.Teal else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) Re.N07 else Re.N02,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Compact(s: CockpitState, live: LiveReadings) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(if (!s.connected) "--" else "${s.speed}", color = if (!s.connected) Re.N04 else Color.White, fontFamily = AntonFamily, fontSize = 40.sp)
        Text("KM/H", color = Re.N02, fontSize = 11.sp)
        Column(horizontalAlignment = Alignment.End) {
            Text(if (!s.connected) "—" else s.mode, color = modeColor(s.mode), fontFamily = AntonFamily, fontSize = 22.sp)
            Text(if (!s.connected) "—" else "${s.soc} % · ${rangeKm(s.soc)} km", color = Re.N02, fontSize = 12.sp)
        }
        Compass()
    }
}

private fun modeColor(mode: String): Color = when (mode) {
    "ECO" -> Re.Lime
    "CITY" -> Re.Teal
    "RAPID" -> Color.White
    else -> Re.N02
}

@Composable
private fun PowerReadout(s: CockpitState, live: LiveReadings) {
    val off = !s.connected
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth().height(12.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(3.dp)).background(Color(0xFF151818))) {
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .fillMaxWidth(if (off) 0f else (live.regen / 5.0).toFloat().coerceIn(0f, 1f))
                        .background(Re.Lime),
                )
            }
            Box(Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(3.dp)).background(Color(0xFF151818))) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .fillMaxWidth(if (off) 0f else (live.kw / 15.0).toFloat().coerceIn(0f, 1f))
                        .background(Re.Teal),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("REGEN ${if (off) "—" else String.format("%.1f", live.regen)} kW", color = Re.N04, fontSize = 11.sp)
            Text("TORQUE ${if (off) "—" else live.torque.toString()} Nm", color = Re.N04, fontSize = 11.sp)
            Text("POWER ${if (off) "—" else String.format("%.1f", live.kw)} kW", color = Re.N04, fontSize = 11.sp)
        }
    }
}

// Cluster home: parked (CM-05) or riding (CM-06).
@Composable
fun ClusterHomeScreen(s: CockpitState, live: LiveReadings, vm: CockpitViewModel) {
    val off = !s.connected
    val hint = when {
        !off && s.kill == "Off" -> "Kill switch is off. Switch to RUN to ride."
        !off && s.speed == 0 && s.stand == "Applied" -> "Raise the side stand to ride."
        else -> ""
    }
    ClusterFrame(s, vm, showLayouts = false) {
        TellTaleRow(s)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (off) "—" else s.mode,
                    color = modeColor(s.mode),
                    fontFamily = AntonFamily,
                    fontSize = 30.sp,
                )
                Text(
                    if (s.speed > 0) "READY" else "PARKED",
                    color = Re.N02,
                    fontSize = 11.sp,
                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.Transparent).padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            Compass()
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (off) "--" else "${s.speed}",
                color = if (off) Re.N04 else Color.White,
                fontFamily = AntonFamily,
                fontSize = 150.sp,
                lineHeight = 140.sp,
            )
            Text("KM/H", color = Re.N02, fontSize = 13.sp)
        }
        PowerReadout(s, live)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF111414))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BatteryTrack(if (off) 0f else s.soc / 100f, s.soc <= 15, Modifier.fillMaxWidth())
            }
            Text(if (off) "—" else "${s.soc}%", color = Color.White, fontFamily = AntonFamily, fontSize = 26.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KeyValue("RANGE", if (off) "—" else "${rangeKm(s.soc)} km", Modifier.weight(1f))
            KeyValue("ODO", if (off) "—" else "1,234 km", Modifier.weight(1f))
        }
        if (hint.isNotEmpty()) {
            Text(hint, color = if (hint.startsWith("Kill")) Color(0xFFFF6B6B) else Re.Amber, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun KeyValue(label: String, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF111414)).padding(10.dp)) {
        Text(label, color = Re.N04, fontSize = 10.sp)
        Text(value, color = Color.White, fontFamily = AntonFamily, fontSize = 20.sp)
    }
}

// Trip A (CM-07) and Trip B (CM-08).
@Composable
fun TripScreen(s: CockpitState, vm: CockpitViewModel, trip: String) {
    val a = trip == "A"
    ClusterFrame(s, vm, showLayouts = false) {
        TellTaleRow(s)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text("Trip $trip", color = Color.White, fontFamily = AntonFamily, fontSize = 34.sp)
            Text(if (a) "This journey" else "Long-term", color = Re.N04, fontSize = 12.sp)
        }
        val rows = if (a) listOf("Distance" to "54.4 km", "Time" to "1 h 12 min", "Average speed" to "38 km/h", "Efficiency" to "41 Wh/km")
        else listOf("Distance" to "320.0 km", "Time" to "9 h 40 min", "Average speed" to "33 km/h", "Efficiency" to "44 Wh/km")
        rows.forEach { (label, value) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, color = Color(0xFFA0A3A3), fontSize = 14.sp)
                Text(value, color = Color.White, fontFamily = AntonFamily, fontSize = 22.sp)
            }
        }
    }
}

// Energy (CM-09): state of charge against distance since Trip B reset.
@Composable
fun EnergyScreen(s: CockpitState, vm: CockpitViewModel) {
    val points = listOf(
        0f to 100f, 38f to 74f, 71f to 49f, 96f to 30f, 96f to 92f, 140f to 62f,
        171f to 40f, 171f to 96f, 214f to 68f, 262f to 38f, 294f to 19f, 294f to 88f, 320f to 70f,
    )
    ClusterFrame(s, vm, showLayouts = false) {
        TellTaleRow(s)
        Text("Energy", color = Color.White, fontFamily = AntonFamily, fontSize = 34.sp)
        Text("Trip B · SOC % against km", color = Re.N04, fontSize = 12.sp)
        Canvas(Modifier.fillMaxWidth().height(200.dp)) {
            val plotLeft = 30f
            val plotBottom = size.height - 22f
            val plotTop = 8f
            val plotWidth = size.width - plotLeft - 8f
            val plotHeight = plotBottom - plotTop
            fun px(k: Float) = plotLeft + k / 320f * plotWidth
            fun py(v: Float) = plotTop + (100f - v) / 100f * plotHeight
            listOf(0f, 25f, 50f, 75f, 100f).forEach { v ->
                drawLine(Color(0xFF1E2424), Offset(plotLeft, py(v)), Offset(size.width - 8f, py(v)), strokeWidth = 1f)
            }
            val path = Path()
            points.forEachIndexed { i, (x, y) ->
                if (i == 0) path.moveTo(px(x), py(y)) else path.lineTo(px(x), py(y))
            }
            val area = Path().apply {
                addPath(path)
                lineTo(px(320f), plotBottom)
                lineTo(px(0f), plotBottom)
                close()
            }
            drawPath(area, Brush.verticalGradient(listOf(Re.Teal.copy(alpha = 0.35f), Re.Teal.copy(alpha = 0f))))
            drawPath(path, Re.Teal, style = Stroke(width = 4f))
            val last = points.last()
            drawCircle(Re.Lime, radius = 6f, center = Offset(px(last.first), py(last.second)))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KeyValue("ENERGY", "44 Wh/km", Modifier.weight(1f))
            KeyValue("REGEN", "12 %", Modifier.weight(1f))
        }
    }
}

// Warnings in the cluster (CM-10): one at a time, Next cycles.
@Composable
fun ClusterWarningsScreen(s: CockpitState, vm: CockpitViewModel) {
    ClusterFrame(s, vm, showLayouts = false) {
        TellTaleRow(s)
        if (!s.warn) {
            Text("Warnings", color = Color.White, fontFamily = AntonFamily, fontSize = 34.sp)
            Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(Re.Lime), contentAlignment = Alignment.Center) {
                    Text("✓", color = Re.N07, fontFamily = AntonFamily, fontSize = 28.sp)
                }
                Text("No active warnings", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(top = 14.dp))
            }
        } else {
            val index = s.warningIndex % WARNINGS.size
            val w = WARNINGS[index]
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("Warnings", color = Color.White, fontFamily = AntonFamily, fontSize = 34.sp)
                Text("${index + 1} of ${WARNINGS.size}", color = Re.N04, fontSize = 12.sp)
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF140C0C))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    if (w.severity == "crit") "Critical" else "Check soon",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (w.severity == "crit") Re.Red else Re.Amber)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
                Text(w.title, color = Color.White, fontFamily = AntonFamily, fontSize = 28.sp, lineHeight = 30.sp)
                Text(w.body, color = Color(0xFFD6D6D6), fontSize = 15.sp, lineHeight = 21.sp)
                Text(w.whenText, color = Re.N04, fontSize = 12.sp)
            }
            Button(
                onClick = { vm.nextWarning() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2323), contentColor = Color.White),
            ) { Text("Next warning ›", fontWeight = FontWeight.Bold) }
        }
    }
}

// Split layouts (CM-11 50:50, CM-12 70:30, CM-13 music 80:20, CM-16 navigation + music).
@Composable
fun SplitScreen(s: CockpitState, live: LiveReadings, vm: CockpitViewModel, screen: String) {
    val off = !s.connected
    val weights = when (screen) {
        "CM-11" -> 50f to 50f
        "CM-12" -> 70f to 30f
        "CM-16" -> 33f to 67f
        else -> 80f to 20f
    }
    ClusterFrame(s, vm, showLayouts = true) {
        Column(Modifier.weight(weights.first), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TellTaleRow(s)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    if (off) "--" else "${s.speed}",
                    color = if (off) Re.N04 else Color.White,
                    fontFamily = AntonFamily,
                    fontSize = if (weights.first >= 70f) 96.sp else 72.sp,
                )
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Compass()
                    Text(if (off) "—" else s.mode, color = modeColor(s.mode), fontFamily = AntonFamily, fontSize = 22.sp)
                }
            }
            PowerReadout(s, live)
        }
        Column(
            Modifier
                .weight(weights.second)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0E1212)),
        ) {
            if (screen == "CM-13") {
                MusicPlayer(s, vm, strip = false)
            } else {
                MapPanel(s, vm, screen == "CM-16")
            }
        }
    }
}

@Composable
private fun MapPanel(s: CockpitState, vm: CockpitViewModel, withMusic: Boolean) {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color(0xFF0E1212))
                val grid = Color(0xFF1E2626)
                listOf(
                    Offset(-10f, size.height * 0.77f) to Offset(size.width * 0.35f, size.height * 0.67f),
                    Offset(size.width * 0.35f, size.height * 0.67f) to Offset(size.width * 0.59f, size.height * 0.4f),
                    Offset(size.width * 0.59f, size.height * 0.4f) to Offset(size.width + 10f, size.height * 0.3f),
                ).forEach { (a, b) -> drawLine(grid, a, b, strokeWidth = 10f) }
                val route = Path().apply {
                    moveTo(size.width * 0.3f, size.height)
                    lineTo(size.width * 0.35f, size.height * 0.67f)
                    lineTo(size.width * 0.59f, size.height * 0.4f)
                    lineTo(size.width * 0.68f, size.height * 0.13f)
                }
                drawPath(route, Re.Teal, style = Stroke(width = 7f))
                drawCircle(Re.Teal, radius = 11f, center = Offset(size.width * 0.35f, size.height * 0.67f))
                if (s.navToCharger) drawCircle(Re.Lime, radius = 10f, center = Offset(size.width * 0.68f, size.height * 0.13f))
            }
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Re.N07.copy(alpha = 0.9f))
                    .padding(8.dp),
            ) {
                Text(
                    if (s.navToCharger) "600 m" else "300 m",
                    color = Color.White,
                    fontFamily = AntonFamily,
                    fontSize = 24.sp,
                )
                Text(
                    if (s.navToCharger) "Turn right · charging point on left" else "Turn right onto MG Road",
                    color = Color(0xFFD0D0D0),
                    fontSize = 12.sp,
                )
            }
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Re.N07.copy(alpha = 0.94f))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("ETA ${if (s.navToCharger) "10:51" else "11:08"}", color = Color(0xFFD0D0D0), fontSize = 12.sp)
                Text("${if (s.navToCharger) "3.2" else "18"} km", color = Color(0xFFD0D0D0), fontSize = 12.sp)
                Text("Arrive with ${if (s.navToCharger) "12" else "52"} %", color = Color(0xFFD0D0D0), fontSize = 12.sp)
            }
        }
        if (withMusic) MusicPlayer(s, vm, strip = true)
    }
}

@Composable
private fun MusicPlayer(s: CockpitState, vm: CockpitViewModel, strip: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (strip) Color(0xFF0F1212) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = if (strip) 10.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(if (strip) 44.dp else 56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.linearGradient(listOf(Re.Teal, Color(0xFF0F1010)))),
        )
        Column(Modifier.weight(1f)) {
            Text("Long Way Home", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
            Text("The Highway Set", color = Color(0xFFA0A3A3), fontSize = 12.sp, maxLines = 1)
        }
        TextButton(onClick = { vm.showToast("Previous track") }) { Text("‹‹", color = Color.White) }
        Button(
            onClick = { vm.togglePlay() },
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Re.N07),
        ) { Text(if (s.playing) "II" else "▶", fontFamily = AntonFamily) }
        TextButton(onClick = { vm.showToast("Next track: Open Roads") }) { Text("››", color = Color.White) }
    }
}

// Charging in the cluster (CM-14) and the charger-fault state (CM-N5).
@Composable
fun ChargingScreen(s: CockpitState, vm: CockpitViewModel) {
    val gained = maxOf(0, s.soc - 42)
    ClusterFrame(s, vm, showLayouts = false) {
        TellTaleRow(s)
        Text(
            if (s.chargerFault) "Charging stopped" else "Charging",
            color = if (s.chargerFault) Color(0xFFFF6B6B) else Re.Teal,
            fontFamily = AntonFamily,
            fontSize = 34.sp,
        )
        Text("AC · Standard", color = Re.N04, fontSize = 12.sp)
        if (s.chargerFault) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF140C0C))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Charger fault", color = Color.White, fontWeight = FontWeight.Bold)
                Text("The charger reported an output over-voltage. Unplug it and check the charger.", color = Color(0xFFD6D6D6), fontSize = 14.sp)
            }
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${s.soc}%", color = Color.White, fontFamily = AntonFamily, fontSize = if (s.chargerFault) 96.sp else 120.sp)
            Text(
                if (s.chargerFault) "PAUSED" else timeToFull(s.soc).uppercase() + " TO 100 %",
                color = Re.N02,
                fontSize = 13.sp,
            )
        }
        BatteryTrack(s.soc / 100f, false, Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KeyValue("SOC GAINED", "+$gained %", Modifier.weight(1f))
            KeyValue("RANGE GAINED", "+${(gained * 1.4).toInt()} km", Modifier.weight(1f))
        }
        Text("Unplug the charger to ride.", color = Re.Amber, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

// Ride summary (CM-15), shown after Cluster mode ends.
@Composable
fun SummaryScreen(vm: CockpitViewModel) {
    Column(Modifier.fillMaxSize().background(Re.N01).padding(16.dp)) {
        Text("Ride summary", color = Re.N07, fontFamily = AntonFamily, fontSize = 30.sp)
        Text("Today · 9:30 – 10:42 · Cluster mode ended", color = Re.N04, fontSize = 12.sp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 14.dp)) {
            WhiteCard {
                SummaryRow("Distance travelled", "54.4 km")
                SummaryRow("Time taken", "1 h 12 min")
                SummaryRow("Average speed", "38 km/h")
                SummaryRow("Energy consumed", "2.2 kWh")
                SummaryRow("Efficiency", "41 Wh/km")
            }
        }
        Button(
            onClick = { vm.go("VD-00") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Re.Teal, contentColor = Re.N07),
        ) { Text("Done", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Re.N05, fontSize = 14.sp)
        Text(value, color = Re.N07, fontFamily = AntonFamily, fontSize = 18.sp)
    }
}
