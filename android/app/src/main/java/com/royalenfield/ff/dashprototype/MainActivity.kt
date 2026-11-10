package com.royalenfield.ff.dashprototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box as LayoutBox

class MainActivity : ComponentActivity() {
    private val viewModel: CockpitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = Color.White) {
                    CockpitApp(viewModel)
                }
            }
        }
    }

    // The compass listens only while the screen is in front.
    override fun onResume() {
        super.onResume()
        Heading.start(this)
    }

    override fun onPause() {
        Heading.stop()
        super.onPause()
    }
}

private val BackScreens = setOf("VD-01", "VD-07", "VD-09", "CM-01", "CM-02a", "CM-02b", "CM-02c", "CM-03", "CM-04")

@Composable
fun CockpitApp(vm: CockpitViewModel) {
    val s by vm.state.collectAsState()
    val live by vm.live.collectAsState()
    var showDemo by remember { mutableStateOf(false) }

    BackHandler(enabled = s.overlay != null || s.screen in BackScreens) {
        if (s.overlay != null) vm.closeOverlay() else vm.go("VD-00")
    }

    Box(Modifier.fillMaxSize()) {
        ScreenRouter(s, live, vm, onDemo = { showDemo = true })
        OverlayFor(s, vm)
        s.toast?.let { message ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Box(Modifier.padding(bottom = 120.dp)) {
                    ToastBar(message, s.toastBad)
                }
            }
        }
        if (showDemo) {
            DemoPanel(s, vm, onDismiss = { showDemo = false })
        }
    }
}

@Composable
private fun ScreenRouter(s: CockpitState, live: LiveReadings, vm: CockpitViewModel, onDemo: () -> Unit) {
    when (s.screen) {
        "VD-01", "VD-02" -> VehicleDashboardScreen(s, vm)
        "VD-07" -> WarningsScreen(s, vm)
        "VD-09" -> ConnectionScreen(s, vm)
        "CM-01" -> SetupScreen(s, vm)
        "CM-02a" -> IosSetupScreen(vm)
        "CM-02b" -> IosSettingsScreen(s, vm)
        "CM-02c" -> IosStartScreen(vm)
        "CM-03" -> FaceScreen(vm)
        "CM-04" -> PinScreen(s, vm)
        "CM-E3" -> LockedScreen(s, vm)
        "CM-05", "CM-06" -> ClusterHomeScreen(s, live, vm)
        "CM-07" -> TripScreen(s, vm, "A")
        "CM-08" -> TripScreen(s, vm, "B")
        "CM-09" -> EnergyScreen(s, vm)
        "CM-10" -> ClusterWarningsScreen(s, vm)
        "CM-11", "CM-12", "CM-13", "CM-16" -> SplitScreen(s, live, vm, s.screen)
        "CM-14" -> ChargingScreen(s, vm)
        "CM-15" -> SummaryScreen(vm)
        "SOS-03" -> CallingScreen(vm)
        else -> Column(Modifier.fillMaxSize()) {
            LayoutBox(Modifier.weight(1f)) {
                HomeScreen(s, vm, onDemo)
            }
            BottomNav(vm)
        }
    }
}
