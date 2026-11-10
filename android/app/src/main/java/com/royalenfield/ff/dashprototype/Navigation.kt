package com.royalenfield.ff.dashprototype

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RouteState(val destination: String?, val stops: List<String>, val etaMinutes: Int)

// Seam for the navigation engine. The FF app's Bosch navigation module (renavigation) plugs in here,
// so the destination and stops can change while Cluster mode runs (F-06). Until that module is
// connected, MockNavigation keeps the flow working for the prototype.
interface NavigationEngine {
    val route: StateFlow<RouteState>
    fun setDestination(name: String)
    fun addStop(name: String)
}

object MockNavigation : NavigationEngine {
    private val _route = MutableStateFlow(RouteState("MG Road", emptyList(), 18))
    override val route: StateFlow<RouteState> = _route.asStateFlow()

    override fun setDestination(name: String) {
        _route.value = RouteState(name, emptyList(), 18)
    }

    override fun addStop(name: String) {
        _route.value = _route.value.copy(stops = _route.value.stops + name)
    }
}

object Navigation {
    // Swap this for the Bosch-backed engine when the renavigation module is connected.
    var engine: NavigationEngine = MockNavigation
}

// Places a rider can pick from while riding. Typing is not offered in Cluster mode.
val RECENT_PLACES = listOf("Home", "Office", "MG Road", "Charging point, Ring Road")
