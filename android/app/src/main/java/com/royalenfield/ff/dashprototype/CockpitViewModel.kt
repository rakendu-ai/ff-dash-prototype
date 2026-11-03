package com.royalenfield.ff.dashprototype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

data class LiveReadings(val kw: Double = 0.0, val regen: Double = 0.0, val torque: Int = 0)

// Holds the prototype state and every action the screens can take. Timers (connecting, face unlock,
// PIN lockout, toasts) run here, as they do in the design's script.
class CockpitViewModel : ViewModel() {
    private val _state = MutableStateFlow(CockpitState())
    val state: StateFlow<CockpitState> = _state.asStateFlow()

    private val _live = MutableStateFlow(LiveReadings())
    val live: StateFlow<LiveReadings> = _live.asStateFlow()

    private val jobs = HashMap<String, Job>()

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(500)
                tickLive()
            }
        }
    }

    private fun change(transform: (CockpitState) -> CockpitState) {
        _state.update { normalize(transform(it)) }
        syncTimers()
    }

    private fun ensure(key: String, active: Boolean, block: suspend CoroutineScope.() -> Unit) {
        if (!active) {
            jobs.remove(key)?.cancel()
            return
        }
        if (jobs[key]?.isActive == true) return
        jobs[key] = viewModelScope.launch(block = block)
    }

    private fun syncTimers() {
        val s = _state.value

        ensure("connect", s.overlay == "connecting") {
            delay(2200)
            change { t ->
                when {
                    t.overlay != "connecting" -> t
                    t.failNextConnection -> t.copy(overlay = "connfail", failNextConnection = false)
                    else -> t.copy(connected = true, overlay = null, toast = "Connected to Flying Flea N", toastBad = false)
                }
            }
        }

        ensure("face", s.screen == "CM-03" && s.overlay == null) {
            delay(1900)
            change { t ->
                when {
                    t.screen != "CM-03" || t.overlay != null -> t
                    t.faceFail -> t.copy(screen = "CM-04", toast = "Face not recognised. Enter your PIN.", toastBad = true)
                    else -> unlocked(t)
                }
            }
        }

        ensure("lock", s.screen == "CM-E3") {
            while (isActive) {
                delay(1000)
                val t = _state.value
                if (t.screen != "CM-E3") break
                if (t.lockLeft <= 1) {
                    change { it.copy(tries = 0, lockLeft = 30, screen = "CM-04") }
                    break
                }
                change { it.copy(lockLeft = it.lockLeft - 1) }
            }
        }

        ensure("toast", s.toast != null || s.overlay == "exitblocked") {
            delay(2600)
            change { t -> t.copy(toast = null, overlay = if (t.overlay == "exitblocked") null else t.overlay) }
        }
    }

    private fun unlocked(t: CockpitState): CockpitState {
        val screen = when (t.layoutPref) {
            "50" -> "CM-11"
            "70" -> "CM-12"
            else -> homeClusterId(t)
        }
        return t.copy(
            first = false,
            pin = "",
            tries = 0,
            overlay = null,
            screen = screen,
            lastApp = if (t.layoutPref == "70") "CM-12" else t.lastApp,
            toast = if (t.os == "ios") "Cluster mode on" else "Cluster mode on. Screen pinned.",
            toastBad = false,
        )
    }

    // Navigation
    fun go(id: String) = change { s ->
        s.copy(
            overlay = null,
            tab = if (id == "VD-00" || id == "VD-01") "status" else s.tab,
            screen = id,
            lastApp = if (id in APP_SCREENS) id else s.lastApp,
        )
    }

    fun setTab(tab: String) = change { it.copy(tab = tab) }
    fun openOverlay(name: String) = change { it.copy(overlay = name) }
    fun closeOverlay() = change { it.copy(overlay = null) }
    fun connect() = change { it.copy(overlay = "connecting") }

    fun refresh() {
        val connected = _state.value.connected
        showToast(if (connected) "Connected" else "Still not connected", !connected)
    }

    fun forget() {
        change { it.copy(overlay = null) }
        showToast("iPhone 15 forgotten")
    }

    fun showToast(message: String, bad: Boolean = false) {
        jobs.remove("toast")?.cancel()
        change { it.copy(toast = message, toastBad = bad) }
    }

    fun switchVehicle() {
        change { it.copy(overlay = null) }
        showToast("Switching to Flying Flea C6 opens that bike’s Home. Only N is in this prototype.")
    }

    fun enterCluster() {
        if (!_state.value.connected) {
            change { it.copy(overlay = "connfirst") }
            return
        }
        change { it.copy(screen = if (it.first) "CM-01" else if (it.os == "ios") "CM-02c" else "CM-03") }
    }

    // Set-up and unlock
    fun setLayout(layout: String) = change { it.copy(layoutPref = layout) }

    fun setupNext() = change {
        if (it.os == "ios") it.copy(screen = "CM-02a") else it.copy(overlay = "pindlg")
    }

    fun pinAllow() = change { it.copy(overlay = null, screen = "CM-03") }
    fun pinDeny() = change { it.copy(overlay = "pinreq") }
    fun toggleGuidedAccess() = change { it.copy(guidedAccessOn = !it.guidedAccessOn) }

    fun pinKey(digit: String) {
        if (_state.value.pin.length >= 4) return
        change { t ->
            val pin = t.pin + digit
            when {
                pin.length < 4 -> t.copy(pin = pin)
                pin == "1234" -> unlocked(t.copy(pin = pin))
                else -> {
                    val tries = t.tries + 1
                    if (tries >= 3) t.copy(pin = "", tries = tries, screen = "CM-E3", lockLeft = 30)
                    else t.copy(pin = "", tries = tries)
                }
            }
        }
    }

    fun pinDelete() = change { it.copy(pin = it.pin.dropLast(1)) }
    fun skipLock() = change { it.copy(tries = 0, lockLeft = 30, screen = "CM-04") }

    // Cluster
    fun clusterHome() = change {
        it.copy(overlay = null, screen = if (it.charging) "CM-14" else homeClusterId(it))
    }

    fun exitBlocked() = change { it.copy(overlay = "exitblocked") }

    fun exitCluster() = change {
        if (it.speed > 0) it.copy(overlay = "exitblocked") else it.copy(overlay = null, screen = "CM-15")
    }

    fun nextWarning() = change { it.copy(warningIndex = it.warningIndex + 1) }
    fun viewWarnings() = change { it.copy(overlay = null, screen = "CM-10", warningIndex = 0) }
    fun navigateToCharger() = change { it.copy(overlay = null, navToCharger = true, screen = "CM-11", lastApp = "CM-11") }
    fun togglePlay() = change { it.copy(playing = !it.playing) }

    fun swipeCluster(right: Boolean) {
        val s = _state.value
        if (!isCluster(s.screen) || s.overlay != null || s.screen == "CM-14") return
        val current = carIndex(s.screen)
        if (current < 0) return
        val next = if (right) minOf(CAR.lastIndex, current + 1) else maxOf(0, current - 1)
        change { t ->
            t.copy(
                screen = when (next) {
                    0 -> t.lastApp
                    1 -> homeClusterId(t)
                    else -> CAR[next]
                },
            )
        }
    }

    private fun carIndex(id: String): Int = when {
        id in APP_SCREENS -> 0
        id == "CM-05" || id == "CM-06" -> 1
        else -> CAR.indexOf(id)
    }

    // SOS
    fun sos(from: String) = change { it.copy(sosFrom = from, overlay = "sos") }

    fun sosCall() = change {
        if (it.sosFrom == "cluster") it.copy(overlay = "sosleave") else it.copy(overlay = null, screen = "SOS-03")
    }

    // Demo controls (the prototype's simulator)
    fun setConnected(v: Boolean) = change { it.copy(connected = v) }
    fun setCharging(v: Boolean) = change { it.copy(charging = v) }
    fun setChargerFault(v: Boolean) = change { it.copy(chargerFault = v) }
    fun setWarn(v: Boolean) = change { it.copy(warn = v) }
    fun setSoc(v: Int) = change { it.copy(soc = v) }
    fun setSpeed(v: Int) = change { it.copy(speed = v) }
    fun setMode(v: String) = change { it.copy(mode = v) }
    fun setStand(v: String) = change { it.copy(stand = v) }
    fun setKill(v: String) = change { it.copy(kill = v) }
    fun setIndicator(v: String) = change { it.copy(indicator = v) }
    fun setHighBeam(v: Boolean) = change { it.copy(highBeam = v) }
    fun setFailNext(v: Boolean) = change { it.copy(failNextConnection = v) }
    fun setFaceFail(v: Boolean) = change { it.copy(faceFail = v) }
    fun setOs(v: String) = change { it.copy(os = v) }

    fun resetDemo() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        _state.value = normalize(CockpitState())
        syncTimers()
    }

    // Powertrain readings, randomised while riding, as in the design.
    private fun tickLive() {
        val s = _state.value
        if (!isCluster(s.screen)) return
        if (s.speed > 0 && s.connected) {
            val power: Double
            val regen: Double
            if (Random.nextDouble() < 0.18) {
                power = 0.0
                regen = round1(Random.nextDouble() * 3.2)
            } else {
                regen = 0.0
                power = round1((s.speed * 0.11 + Random.nextDouble() * 3 - 1).coerceIn(0.0, 15.0))
            }
            _live.value = LiveReadings(power, regen, (power * 4.2).roundToInt())
        } else {
            _live.value = LiveReadings()
        }
    }
}
