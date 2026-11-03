package com.royalenfield.ff.dashprototype

import kotlin.math.roundToInt

// Mirrors the design prototype's state object. Screen ids (VD-00, CM-06, ...) match the design's flow index.
data class CockpitState(
    val screen: String = "VD-00",
    val tab: String = "status",
    val connected: Boolean = true,
    val charging: Boolean = false,
    val chargerFault: Boolean = false,
    val warn: Boolean = false,
    val soc: Int = 70,
    val speed: Int = 0,
    val mode: String = "ECO",
    val stand: String = "Applied",
    val kill: String = "Run",
    val ignition: String = "On",
    val indicator: String = "off",
    val highBeam: Boolean = false,
    val first: Boolean = true,
    val os: String = "android",
    val layoutPref: String = "full",
    val overlay: String? = null,
    val toast: String? = null,
    val toastBad: Boolean = false,
    val warningIndex: Int = 0,
    val pin: String = "",
    val tries: Int = 0,
    val lockLeft: Int = 30,
    val failNextConnection: Boolean = false,
    val faceFail: Boolean = false,
    val navToCharger: Boolean = false,
    val playing: Boolean = true,
    val lastApp: String = "CM-11",
    val guidedAccessOn: Boolean = false,
    val sosFrom: String = "app",
)

data class Warning(
    val severity: String,
    val title: String,
    val body: String,
    val whenText: String,
)

const val CONN_MSG = "Ensure the vehicle is turned on and the vehicle and phone are connected via Bluetooth."
const val EXIT_LABEL = "Stop the vehicle and swipe to exit Cluster mode"

val WARNINGS = listOf(
    Warning("crit", "Battery fault", "The battery reported an under-voltage fault. Stop when it’s safe and contact Royal Enfield service.", "Now"),
    Warning("warn", "Indicator lamp fault", "A turn indicator lamp has a fault. Get it checked soon.", "6 min ago"),
    Warning("warn", "DC-DC converter fault", "The DC-DC output voltage is too high. Get the vehicle checked soon.", "12 min ago"),
)

val HOME_SCREENS = setOf("VD-00", "VD-03", "VD-06", "VD-08")
val CLUSTER_SCREENS = setOf("CM-05", "CM-06", "CM-07", "CM-08", "CM-09", "CM-10", "CM-11", "CM-12", "CM-13", "CM-14", "CM-16")
val APP_SCREENS = setOf("CM-11", "CM-12", "CM-13", "CM-16")
val CAR = listOf("APPS", "HOME", "CM-07", "CM-08", "CM-09", "CM-10")

val OVERLAY_SCREEN = mapOf(
    "vehicles" to "VD-11",
    "connecting" to "VD-04",
    "connfail" to "VD-05",
    "forget" to "VD-10",
    "pindlg" to "CM-02",
    "connfirst" to "CM-E1",
    "pinreq" to "CM-E2",
    "warn" to "CM-N1",
    "lowbatt" to "CM-N2",
    "exitblocked" to "CM-N4",
    "sos" to "SOS-01",
    "sosleave" to "SOS-02",
)

fun isCluster(id: String): Boolean = id in CLUSTER_SCREENS

fun rangeKm(soc: Int): Int = (soc * 1.4).roundToInt()

fun timeToFull(soc: Int): String {
    val minutes = ((100 - soc) * 2.2).roundToInt()
    return if (minutes >= 60) "${minutes / 60} h ${minutes % 60} min" else "$minutes min"
}

fun round1(value: Double): Double = (value * 10).roundToInt() / 10.0

fun homeClusterId(s: CockpitState): String = if (s.speed > 0) "CM-06" else "CM-05"

fun landingId(s: CockpitState): String = when {
    !s.connected -> "VD-03"
    s.charging -> "VD-08"
    s.warn -> "VD-06"
    else -> "VD-00"
}

// Keeps the screen id valid for the current state, as the design's normalize() does.
fun normalize(s: CockpitState): CockpitState {
    var n = s
    if (n.screen in HOME_SCREENS) n = n.copy(screen = landingId(n))
    if (n.screen == "VD-02") n = n.copy(screen = "VD-01")
    if (n.screen == "CM-05" || n.screen == "CM-06") n = n.copy(screen = if (n.charging) "CM-14" else homeClusterId(n))
    if (!n.charging && n.screen == "CM-14") n = n.copy(screen = homeClusterId(n))
    return n
}

// The flow id a designer would label this state with. Used for the flow index, not for rendering.
fun effectiveId(s: CockpitState): String {
    s.overlay?.let { name -> OVERLAY_SCREEN[name]?.let { return it } }
    if (isCluster(s.screen) && s.screen != "CM-14" && !s.connected) return "CM-N3"
    if (s.screen == "CM-14" && s.chargerFault) return "CM-N5"
    if (s.screen == "VD-01" && s.tab == "info") return "VD-02"
    return s.screen
}

// Ride modes come from the vehicle API (not integrated yet). PARK is a vehicle state, not a ride mode.
val RIDE_MODES = listOf("ECO", "CITY", "RAPID")
