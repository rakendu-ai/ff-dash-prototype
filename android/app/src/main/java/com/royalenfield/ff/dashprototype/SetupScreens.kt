package com.royalenfield.ff.dashprototype

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val DarkBg = Color.Black

// Cluster mode set-up (CM-01): layout choice and the rider's first-run checks.
@Composable
fun SetupScreen(s: CockpitState, vm: CockpitViewModel) {
    Column(Modifier.fillMaxSize().background(DarkBg).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = { vm.go("VD-00") }) { Text("‹ Back", color = Color.White) }
        Text("Cluster mode", color = Color.White, fontFamily = AntonFamily, fontSize = 34.sp)
        Text("Use your phone as the dashboard for Flying Flea N.", color = Re.N04, fontSize = 14.sp)
        FeatureLine("Mount your phone", "Portrait only, on the handlebar mount.")
        FeatureLine("The dashboard stays on screen", if (s.os == "ios") "Uses iPhone Guided Access." else "Uses Android app pinning.")
        FeatureLine("Unlock with Face ID or PIN", "")
        FeatureLine("Exit only when stopped", "Swipe to exit works at 0 km/h.")
        Text("Default layout", color = Re.N04, fontSize = 12.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LayoutOption("Cluster only", s.layoutPref == "full", Modifier.weight(1f)) { vm.setLayout("full") }
            LayoutOption("50:50 + map", s.layoutPref == "50", Modifier.weight(1f)) { vm.setLayout("50") }
            LayoutOption("70:30 + map", s.layoutPref == "70", Modifier.weight(1f)) { vm.setLayout("70") }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { vm.setupNext() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Re.Teal, contentColor = Re.N07),
        ) { Text("Continue", fontWeight = FontWeight.Bold) }
        TextButton(onClick = { vm.go("VD-00") }, modifier = Modifier.fillMaxWidth()) { Text("Not now", color = Re.Teal) }
    }
}

@Composable
private fun FeatureLine(title: String, body: String) {
    Column {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        if (body.isNotEmpty()) Text(body, color = Re.N04, fontSize = 13.sp)
    }
}

@Composable
private fun LayoutOption(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF121515))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected) Re.Teal.copy(alpha = 0.3f) else Color(0xFF1E2323)),
        )
        Text(label, color = if (selected) Re.Teal else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

// iOS path, step 1 (CM-02a): explains Guided Access before Cluster mode can run.
@Composable
fun IosSetupScreen(vm: CockpitViewModel) {
    Column(Modifier.fillMaxSize().background(DarkBg).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = { vm.go("CM-01") }) { Text("‹ Back", color = Color.White) }
        Text("Turn on Guided Access", color = Color.White, fontFamily = AntonFamily, fontSize = 30.sp, lineHeight = 34.sp)
        Text(
            "iPhone needs Guided Access to keep Flying Flea on screen while you ride. You only set this up once.",
            color = Re.N04,
            fontSize = 14.sp,
        )
        NumberedStep(1, "Open Settings › Accessibility")
        NumberedStep(2, "Tap Guided Access and turn it on")
        NumberedStep(3, "Turn on Accessibility Shortcut")
        NumberedStep(4, "Come back to Flying Flea")
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { vm.go("CM-02b") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Re.Teal, contentColor = Re.N07),
        ) { Text("Open Settings", fontWeight = FontWeight.Bold) }
        TextButton(onClick = { vm.go("CM-02c") }, modifier = Modifier.fillMaxWidth()) { Text("I’ve already turned it on", color = Re.Teal) }
    }
}

// iOS Settings mock (CM-02b): turn Guided Access on.
@Composable
fun IosSettingsScreen(s: CockpitState, vm: CockpitViewModel) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF2F2F7)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = { if (s.guidedAccessOn) vm.go("CM-02c") else vm.showToast("Turn on Guided Access first.") }) {
            Text(if (s.guidedAccessOn) "‹ Flying Flea" else "‹ Accessibility", color = Color(0xFF007AFF))
        }
        Text("Guided Access", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Guided Access", color = Color.Black, fontSize = 16.sp)
            Switch(checked = s.guidedAccessOn, onCheckedChange = { vm.toggleGuidedAccess() })
        }
        Text(
            "Guided Access keeps the iPhone in a single app. Triple-click the side button in an app to start it.",
            color = Color(0xFF6D6D72),
            fontSize = 13.sp,
        )
        if (s.guidedAccessOn) {
            Text("Done. Tap ‹ Flying Flea at the top to go back.", color = Color(0xFF00706C), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0F1010))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("FLYING FLEA GUIDE", color = Re.Teal, fontFamily = AntonFamily, fontSize = 11.sp)
            Text("Step 1 · Open Accessibility", color = Color.White, fontSize = 13.sp)
            Text("Step 2 · Turn on Guided Access", color = Color.White, fontSize = 13.sp)
            Text("Step 3 · Turn on the shortcut", color = Color.White, fontSize = 13.sp)
            Text("Step 4 · Go back to Flying Flea", color = Color.White, fontSize = 13.sp)
        }
    }
}

// iOS start (CM-02c): triple-click the side button, then Start.
@Composable
fun IosStartScreen(vm: CockpitViewModel) {
    Column(Modifier.fillMaxSize().background(DarkBg).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("Start Guided Access", color = Color.White, fontFamily = AntonFamily, fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(
            "Triple-click the side button, then tap Start at the top right.",
            color = Re.N04,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f))
        Text("Do this each time you start Cluster mode on iPhone.", color = Re.N04, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { vm.go("CM-03") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Re.Teal, contentColor = Re.N07),
        ) { Text("Guided Access is on", fontWeight = FontWeight.Bold) }
        TextButton(onClick = { vm.go("CM-02a") }, modifier = Modifier.fillMaxWidth()) { Text("Show me the setup again", color = Re.Teal) }
    }
}

// Face unlock (CM-03). The design simulates the match after about two seconds.
@Composable
fun FaceScreen(vm: CockpitViewModel) {
    Column(
        Modifier.fillMaxSize().background(DarkBg).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Text("Unlock Cluster mode", color = Color.White, fontFamily = AntonFamily, fontSize = 30.sp)
        Text("Look at your phone", color = Re.N04, fontSize = 14.sp)
        Box(
            Modifier
                .padding(top = 30.dp)
                .size(150.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(Color(0xFF0E1414)),
            contentAlignment = Alignment.Center,
        ) {
            Text("☺", color = Re.Teal, fontSize = 72.sp)
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = { vm.go("CM-04") }) { Text("Use PIN instead", color = Re.Teal) }
    }
}

// PIN unlock (CM-04). Demo PIN is 1234.
@Composable
fun PinScreen(s: CockpitState, vm: CockpitViewModel) {
    val wrong = s.tries > 0
    Column(
        Modifier.fillMaxSize().background(DarkBg).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Enter PIN", color = Color.White, fontFamily = AntonFamily, fontSize = 30.sp)
        Text(
            if (wrong) "Wrong PIN. ${3 - s.tries} ${if (3 - s.tries == 1) "attempt" else "attempts"} left." else "Enter your 4-digit Cluster mode PIN",
            color = if (wrong) Color(0xFFFF6B6B) else Color(0xFFA0A3A3),
            fontSize = 14.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            repeat(4) { i ->
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (i < s.pin.length) Color.White else Color.Transparent)
                        .padding(0.dp),
                )
            }
        }
        Text("Demo PIN: 1234", color = Color(0xFF5E6262), fontSize = 12.sp)
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "FACE", "0", "DEL")
        keys.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { key ->
                    when (key) {
                        "FACE" -> TextButton(onClick = { vm.go("CM-03") }) { Text("Face ID", color = Re.Teal, fontSize = 12.sp) }
                        "DEL" -> TextButton(onClick = { vm.pinDelete() }) { Text("Delete", color = Color(0xFFA0A3A3), fontSize = 12.sp) }
                        else -> Button(
                            onClick = { vm.pinKey(key) },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF151818), contentColor = Color.White),
                        ) { Text(key, fontFamily = AntonFamily, fontSize = 26.sp) }
                    }
                }
            }
        }
    }
}

// PIN lockout (CM-E3): counts down from 30 seconds.
@Composable
fun LockedScreen(s: CockpitState, vm: CockpitViewModel) {
    Column(
        Modifier.fillMaxSize().background(DarkBg).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(100.dp))
        Text("🔒", fontSize = 56.sp, color = Re.Red)
        Text("Too many attempts", color = Color.White, fontFamily = AntonFamily, fontSize = 30.sp)
        Text("Try again in ${s.lockLeft} s, or unlock with Face ID.", color = Re.N04, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { vm.go("CM-03") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2323), contentColor = Color.White),
        ) { Text("Use Face ID", fontWeight = FontWeight.Bold) }
        TextButton(onClick = { vm.skipLock() }) { Text("Skip wait (prototype only)", color = Re.Teal) }
    }
}

// Calling the emergency contact (SOS-03). Cluster mode has ended.
@Composable
fun CallingScreen(vm: CockpitViewModel) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFF0A1A19)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(Modifier.height(60.dp))
        Text("Calling…", color = Re.N04, fontSize = 14.sp)
        Text("Priya", color = Color.White, fontFamily = AntonFamily, fontSize = 36.sp)
        Text("Emergency contact", color = Re.N04, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Text("Cluster mode has ended. Swipe to start it again after the call.", color = Re.N04, fontSize = 12.sp, textAlign = TextAlign.Center)
        Button(
            onClick = { vm.go("VD-00") },
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30), contentColor = Color.White),
        ) { Text("End", fontWeight = FontWeight.Bold) }
    }
}

// Overlays drawn above the current screen: trays, dialogs and alerts.
@Composable
fun OverlayFor(s: CockpitState, vm: CockpitViewModel) {
    when (s.overlay) {
        "connecting" -> Tray("Connecting to Flying Flea N", onClose = null) {
            Text("✓ Vehicle found", color = Re.N05, fontSize = 14.sp)
            Text("• Verifying this phone", color = Re.N05, fontSize = 14.sp)
            Text("• Syncing vehicle data", color = Re.N04, fontSize = 14.sp)
            OutlinedButton(onClick = { vm.closeOverlay() }, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
        "connfail" -> Tray("Couldn’t connect to Flying Flea N", onClose = { vm.closeOverlay() }) {
            Text(CONN_MSG, color = Re.N05, fontSize = 14.sp)
            Button(onClick = { vm.connect() }, modifier = Modifier.fillMaxWidth()) { Text("Connect now") }
            OutlinedButton(onClick = { vm.closeOverlay() }, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
        "connfirst" -> Tray("Connect to Flying Flea N first", onClose = { vm.closeOverlay() }) {
            Text("Cluster mode shows live data from your vehicle. $CONN_MSG", color = Re.N05, fontSize = 14.sp)
            Button(onClick = { vm.connect() }, modifier = Modifier.fillMaxWidth()) { Text("Connect now") }
            OutlinedButton(onClick = { vm.closeOverlay() }, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
        "vehicles" -> Tray("Your vehicles", onClose = { vm.closeOverlay() }) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF4F4F2))
                    .clickable { vm.closeOverlay() }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Flying Flea N", color = Re.N07, fontWeight = FontWeight.Bold)
                    Text("Connected · ${s.soc} % · 1,234 km", color = Re.N04, fontSize = 12.sp)
                }
                Text("✓", color = Re.Teal, fontFamily = AntonFamily)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF4F4F2))
                    .clickable { vm.switchVehicle() }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Flying Flea C6", color = Re.N07, fontWeight = FontWeight.Bold)
                    Text("Not nearby · seen 2 days ago", color = Re.N04, fontSize = 12.sp)
                }
            }
            OutlinedButton(onClick = { vm.showToast("Adding a vehicle isn’t part of this prototype.") }, modifier = Modifier.fillMaxWidth()) {
                Text("+ Add a vehicle")
            }
        }
        "forget" -> Tray("Forget iPhone 15?", onClose = { vm.closeOverlay() }) {
            Text("This phone won’t connect to Flying Flea N or start Cluster mode until it’s paired again.", color = Re.N05, fontSize = 14.sp)
            Button(
                onClick = { vm.forget() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Re.Red, contentColor = Color.White),
            ) { Text("Forget phone") }
            OutlinedButton(onClick = { vm.closeOverlay() }, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
        "pindlg" -> CentredDialog {
            Text("Pin Flying Flea?", color = Re.N07, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("The app stays on screen until you unpin it. You can unpin it from Cluster mode when the bike is stopped.", color = Re.N05, fontSize = 14.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { vm.pinDeny() }) { Text("No thanks", color = Re.Teal) }
                TextButton(onClick = { vm.pinAllow() }) { Text("Pin", color = Re.Teal, fontWeight = FontWeight.Bold) }
            }
        }
        "pinreq" -> Tray("Cluster mode needs pinning", onClose = null) {
            Text("Without pinning, other apps could cover the dashboard while you ride.", color = Re.N05, fontSize = 14.sp)
            Button(onClick = { vm.openOverlay("pindlg") }, modifier = Modifier.fillMaxWidth()) { Text("Try again") }
            OutlinedButton(onClick = { vm.go("VD-00") }, modifier = Modifier.fillMaxWidth()) { Text("Not now") }
        }
        "warn" -> AlertBox(Re.Red) {
            Text("Battery fault", color = Color.White, fontFamily = AntonFamily, fontSize = 30.sp)
            Text("Stop when it’s safe and contact Royal Enfield service.", color = Color(0xFFD6D6D6), fontSize = 15.sp)
            Button(onClick = { vm.closeOverlay() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Re.N07)) { Text("OK") }
            Button(onClick = { vm.viewWarnings() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2323), contentColor = Color.White)) { Text("View warnings") }
        }
        "lowbatt" -> AlertBox(Re.Amber) {
            Text("Battery low · ${s.soc} %", color = Color.White, fontFamily = AntonFamily, fontSize = 30.sp)
            Text("About ${rangeKm(s.soc)} km left. The nearest charging point is 3.2 km away.", color = Color(0xFFD6D6D6), fontSize = 15.sp)
            Button(onClick = { vm.navigateToCharger() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Re.Lime, contentColor = Re.N07)) { Text("Navigate to charger") }
            Button(onClick = { vm.closeOverlay() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2323), contentColor = Color.White)) { Text("Dismiss") }
        }
        "sos" -> Tray("SOS sent", onClose = { vm.closeOverlay() }) {
            Text("An SMS with your location went to Priya (emergency contact) at 10:44.", color = Re.N05, fontSize = 14.sp)
            Text("Lat 12.9716, Long 77.5946", color = Re.N04, fontSize = 12.sp)
            Text("Do you want to call Priya now?", color = Re.N07, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
                onClick = { vm.sosCall() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Re.Red, contentColor = Color.White),
            ) { Text("Call Priya") }
            OutlinedButton(onClick = { vm.closeOverlay() }, modifier = Modifier.fillMaxWidth()) { Text("Not now") }
        }
        "sosleave" -> Tray("Calling will take you out of Cluster mode", onClose = { vm.closeOverlay() }) {
            Text("The phone app opens for the call and the dashboard closes. You can start Cluster mode again after the call.", color = Re.N05, fontSize = 14.sp)
            Button(
                onClick = { vm.go("SOS-03") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Re.Red, contentColor = Color.White),
            ) { Text("Call and exit Cluster mode") }
            OutlinedButton(onClick = { vm.closeOverlay() }, modifier = Modifier.fillMaxWidth()) { Text("Stay in Cluster mode") }
        }
    }
}

@Composable
private fun CentredDialog(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0x8C000000)).padding(20.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF2F2F2))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun AlertBox(accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xB3000000)).padding(20.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF121414))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(width = 56.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(accent))
            content()
        }
    }
}

// Demo controls: the prototype's simulator panel, opened from the Home screen's Demo button.
@Composable
fun DemoPanel(s: CockpitState, vm: CockpitViewModel, onDismiss: () -> Unit) {
    Tray("Demo controls", onClose = onDismiss) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DemoSwitch("Vehicle link connected", s.connected) { vm.setConnected(it) }
            DemoSwitch("Charging", s.charging) { vm.setCharging(it) }
            DemoSwitch("Charger fault", s.chargerFault) { vm.setChargerFault(it) }
            DemoSwitch("Warnings active", s.warn) { vm.setWarn(it) }
            DemoSwitch("High beam", s.highBeam) { vm.setHighBeam(it) }
            DemoSwitch("Next connection fails", s.failNextConnection) { vm.setFailNext(it) }
            DemoSwitch("Face ID fails", s.faceFail) { vm.setFaceFail(it) }

            Text("Speed: ${s.speed} km/h", color = Re.N07, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Slider(
                value = s.speed.toFloat(),
                onValueChange = { vm.setSpeed(it.roundToInt()) },
                valueRange = 0f..80f,
            )
            Text("State of charge: ${s.soc} %", color = Re.N07, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Slider(
                value = s.soc.toFloat(),
                onValueChange = { vm.setSoc(it.roundToInt()) },
                valueRange = 0f..100f,
            )

            Text("Ride mode", color = Re.N04, fontSize = 12.sp)
            ChoiceRow(RIDE_MODES, s.mode) { vm.setMode(it) }
            Text("Side stand", color = Re.N04, fontSize = 12.sp)
            ChoiceRow(listOf("Applied", "Removed"), s.stand) { vm.setStand(it) }
            Text("Kill switch", color = Re.N04, fontSize = 12.sp)
            ChoiceRow(listOf("Run", "Off"), s.kill) { vm.setKill(it) }
            Text("Indicator", color = Re.N04, fontSize = 12.sp)
            ChoiceRow(listOf("off", "left", "right", "hazard"), s.indicator) { vm.setIndicator(it) }
            Text("Platform", color = Re.N04, fontSize = 12.sp)
            ChoiceRow(listOf("android", "ios"), s.os) { vm.setOs(it) }

            Button(
                onClick = { vm.resetDemo() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Re.N07, contentColor = Color.White),
            ) { Text("Reset prototype") }
        }
    }
}

@Composable
private fun DemoSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Re.N07, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

// A numbered line for the iOS Guided Access steps.
@Composable
private fun NumberedStep(number: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Re.Teal),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", color = Re.N07, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Text(text, color = Color.White, fontSize = 15.sp)
    }
}
