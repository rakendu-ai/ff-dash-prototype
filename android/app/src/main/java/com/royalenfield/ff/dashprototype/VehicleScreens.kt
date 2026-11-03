package com.royalenfield.ff.dashprototype

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LandGreen = listOf(Color(0xFF5E8A76), Color(0xFF86AD9A), Color(0xFFBCD5C8))
private val LandGrey = listOf(Color(0xFF8A8D8C), Color(0xFFB4B7B6), Color(0xFFDCDDDC))

// Home (VD-00), not connected (VD-03), warning (VD-06) and charging (VD-08).
@Composable
fun HomeScreen(s: CockpitState, vm: CockpitViewModel, onDemo: () -> Unit) {
    val off = !s.connected
    val charging = s.charging && !off
    val warning = s.warn && !off && !charging
    val grey = off || warning
    val word = when {
        off -> "Not connected"
        charging -> "Charging"
        warning -> "Warning"
        else -> "All good"
    }

    Column(Modifier.fillMaxSize().background(Re.N01).verticalScroll(rememberScrollState())) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(if (grey) LandGrey else LandGreen))
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable { vm.openOverlay("vehicles") }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text("My vehicle", color = Color.White, fontSize = 10.sp)
                    Text("Flying Flea N ▾", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                TextButton(onClick = onDemo) { Text("Demo", color = Color.White) }
            }

            Text(
                word,
                color = if (warning) Re.RedDark else Color.White,
                fontFamily = AntonFamily,
                fontSize = 52.sp,
                lineHeight = 54.sp,
                modifier = Modifier.padding(top = 12.dp),
            )

            when {
                off -> Column(
                    Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Your phone isn’t connected to Flying Flea N. $CONN_MSG", color = Re.N05, fontSize = 14.sp)
                    Button(
                        onClick = { vm.connect() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Re.Teal, contentColor = Re.N07),
                    ) { Text("Connect now", fontWeight = FontWeight.Bold) }
                }
                warning -> Text(
                    "Battery fault · Contact service ›",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Re.RedDark)
                        .clickable { vm.go("VD-07") }
                        .padding(14.dp),
                )
                else -> Text(
                    (if (charging) "AC charging · ${timeToFull(s.soc)} to 100 %" else "Parked & secured") + " · Updated just now ›",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { vm.go("VD-01") },
                )
            }

            BikeImage(Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp))

            SwipeAction(
                label = if (off) "Connect to start Cluster mode" else "Swipe to start Cluster mode",
                tone = if (off) SwipeTone.Mute else SwipeTone.Teal,
                onComplete = { vm.enterCluster() },
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Re.N01)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BatteryCard(s)
            if (charging) ChargingCard(s)
            PowertrainWidget(s)
            WhiteCard(Modifier.clickable { vm.go("VD-01") }) {
                Text("Flying Flea N", color = Re.N05, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Vehicle dashboard", color = Re.N07, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("›", color = Re.N07, fontSize = 22.sp)
                }
            }
            SosCard { vm.sos("app") }
        }
    }
}

@Composable
private fun BatteryCard(s: CockpitState) {
    val off = !s.connected
    val low = s.soc <= 15
    val status = when {
        off -> "—"
        s.charging -> "Charging"
        low -> "Low"
        else -> "All good"
    }
    WhiteCard {
        Text("Battery status", color = Re.N05, fontSize = 12.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(
                status,
                color = if (low && !off) Re.Red else Re.N07,
                fontFamily = AntonFamily,
                fontSize = 24.sp,
            )
            Text(
                if (off) "—" else "${rangeKm(s.soc)} km",
                color = Re.N07,
                fontFamily = AntonFamily,
                fontSize = 20.sp,
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BatteryTrack(if (off) 0f else s.soc / 100f, low, Modifier.weight(1f))
            Text(if (off) "—" else "${s.soc} %", color = Re.N07, fontFamily = AntonFamily, fontSize = 22.sp)
        }
    }
}

@Composable
private fun ChargingCard(s: CockpitState) {
    val gained = maxOf(0, s.soc - 42)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Re.N07)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("AC · Standard charging", color = Color.White, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("+$gained%", "SOC gained", Modifier.weight(1f))
            StatTile("+${(gained * 1.4).toInt()} km", "Range gained", Modifier.weight(1f))
            StatTile(timeToFull(s.soc), "To 100 %", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF1A1F1F)).padding(8.dp)) {
        Text(value, color = Color.White, fontFamily = AntonFamily, fontSize = 15.sp)
        Text(label, color = Re.N02, fontSize = 11.sp)
    }
}

@Composable
fun SosCard(onSos: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SosButton(56.dp, onSos)
        Column(Modifier.weight(1f)) {
            Text("Emergency SOS", color = Re.N07, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Sends your location by SMS to your emergency contact.", color = Re.N05, fontSize = 13.sp)
        }
    }
}

// Vehicle dashboard (VD-01 status, VD-02 information).
@Composable
fun VehicleDashboardScreen(s: CockpitState, vm: CockpitViewModel) {
    val off = !s.connected
    val charging = s.charging && !off
    val warning = s.warn && !off
    val title = when {
        off -> "Not connected"
        warning -> "1 warning"
        charging -> "Charging · AC"
        else -> "All Good"
    }
    val subtitle = when {
        off -> "Connect to see live data"
        warning -> "Battery fault"
        charging -> "${timeToFull(s.soc)} to 100 %"
        else -> "Parked & secured"
    }
    val info = s.tab == "info"

    Column(Modifier.fillMaxSize().background(Re.N01)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF34431F), Color(0xFF141813), Color(0xFF0B0C0B))))
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { vm.go("VD-00") }) { Text("‹ Back", color = Color.White) }
                Text(
                    if (off) "Not connected" else "Connected",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Text("Vehicle dashboard", color = Color.White, fontFamily = AntonFamily, fontSize = 26.sp)
            Text(
                if (off) "Last updated 2 h ago" else "Updated just now",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
            )
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontFamily = AntonFamily, fontSize = 22.sp)
                    Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                BikeImage(Modifier.size(width = 150.dp, height = 90.dp))
            }
            Text(
                "Estimated range: ${if (off) "—" else "${rangeKm(s.soc)} km"}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            BatteryTrack(if (off) 0f else s.soc / 100f, s.soc <= 15, Modifier.fillMaxWidth().padding(top = 6.dp))
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDADAD8))
                    .padding(3.dp),
            ) {
                TabChip("Status", selected = !info, Modifier.weight(1f)) { vm.setTab("status") }
                TabChip("Information", selected = info, Modifier.weight(1f)) { vm.setTab("info") }
            }

            if (!info) {
                PowertrainWidget(s)
                WhiteCard {
                    Text("Riding", color = Re.N05, fontSize = 12.sp)
                    InfoRow("Ride mode", if (off) "—" else s.mode)
                    InfoRow("Charging", if (off) "—" else if (charging) "AC · ${timeToFull(s.soc)} to 100 %" else "Not plugged in")
                    Row(
                        Modifier.fillMaxWidth().clickable { vm.go("VD-07") }.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Warnings", color = Re.N07, fontWeight = FontWeight.Bold)
                        Text(
                            if (off) "—" else if (warning) "1 active" else "None",
                            color = if (warning) Re.Red else Re.N05,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else {
                WhiteCard {
                    Text("Odometer", color = Re.N05, fontSize = 12.sp)
                    InfoRow("Odometer", if (off) "—" else "1,234 km")
                    InfoRow("Trip A", if (off) "—" else "54.4 km")
                    InfoRow("Trip B", if (off) "—" else "320.0 km")
                }
            }

            WhiteCard(Modifier.clickable { vm.go("VD-09") }) {
                Text("Connection", color = Re.N05, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Bluetooth", color = Re.N07, fontWeight = FontWeight.Bold)
                    Text(
                        if (off) "Not connected" else "Connected",
                        color = if (off) Re.Red else Re.N05,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            SosCard { vm.sos("app") }
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Re.Teal else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Re.N07, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Re.N07, fontWeight = FontWeight.Bold)
        Text(value, color = Re.N05)
    }
}

// Warnings list (VD-07).
@Composable
fun WarningsScreen(s: CockpitState, vm: CockpitViewModel) {
    Column(Modifier.fillMaxSize().background(Re.N01).padding(16.dp)) {
        TextButton(onClick = { vm.go("VD-00") }) { Text("‹ Back", color = Re.N07) }
        Text("Warnings", color = Re.Red, fontFamily = AntonFamily, fontSize = 30.sp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 12.dp)) {
            if (s.warn) {
                WARNINGS.forEach { w ->
                    WhiteCard(Modifier.padding(bottom = 10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(w.title, color = Re.N07, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(w.whenText, color = Re.N04, fontSize = 12.sp)
                        }
                        Text(w.body, color = Re.N05, fontSize = 14.sp)
                        Text(
                            if (w.severity == "crit") "Critical" else "Check soon",
                            color = if (w.severity == "crit") Color.White else Re.N07,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (w.severity == "crit") Re.Red else Re.Amber)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            } else {
                Text("No active warnings.", color = Re.N05, fontSize = 15.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text("Warnings clear on their own once the fault is fixed.", color = Re.N04, fontSize = 12.sp)
        }
    }
}

// Connection settings (VD-09). Forget (VD-10) opens as a tray over this screen.
@Composable
fun ConnectionScreen(s: CockpitState, vm: CockpitViewModel) {
    val off = !s.connected
    Column(Modifier.fillMaxSize().background(Re.N01).padding(16.dp)) {
        TextButton(onClick = { vm.go("VD-01") }) { Text("‹ Back", color = Re.N07) }
        Text("Connection", color = Re.N07, fontFamily = AntonFamily, fontSize = 30.sp)
        Text("Settings › Vehicle details › Connection status", color = Re.N04, fontSize = 12.sp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            WhiteCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Flying Flea N", color = Re.N07, fontWeight = FontWeight.Bold)
                    Text(if (off) "Not connected" else "Connected", color = if (off) Re.Red else Re.N05)
                }
                TextButton(onClick = { vm.refresh() }) { Text("Refresh", color = Re.Teal, fontWeight = FontWeight.Bold) }
            }
            WhiteCard {
                Text("Paired phones", color = Re.N05, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("This phone", color = Re.N07, fontWeight = FontWeight.Bold)
                    Text("In use", color = Re.N04)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("iPhone 15", color = Re.N07, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { vm.openOverlay("forget") }) { Text("Forget", color = Re.Red, fontWeight = FontWeight.Bold) }
                }
            }
            Button(
                onClick = { vm.showToast("Adding a phone isn’t part of this prototype.") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Re.N07),
            ) { Text("Add a phone") }
            Text("Connection status is an indicator only: Connected or Not connected.", color = Re.N04, fontSize = 12.sp)
        }
    }
}
