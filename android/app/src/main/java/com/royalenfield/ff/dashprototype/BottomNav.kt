package com.royalenfield.ff.dashprototype

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Bottom navigation on Home (F-03). Home works; the other tabs are placeholders in this prototype.
@Composable
fun BottomNav(vm: CockpitViewModel) {
    val items = listOf(
        Triple("Home", Icons.Filled.Home, true),
        Triple("Pin", Icons.Filled.Lock, false),
        Triple("Flea", Icons.Filled.Build, false),
        Triple("Alerts", Icons.Filled.Notifications, false),
        Triple("Profile", Icons.Filled.Person, false),
    )
    Row(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { (label, icon, active) ->
            NavItem(label, icon, active) {
                if (active) vm.go("VD-00") else vm.showToast("$label is not part of this prototype yet.")
            }
        }
    }
}

@Composable
private fun NavItem(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    val color = if (active) Re.Teal else Re.N05
    Column(
        Modifier.clickable(onClick = onClick).padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Text(label, color = color, fontSize = 11.sp)
    }
}
