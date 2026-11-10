package com.royalenfield.ff.dashprototype

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

// Royal Enfield design-system colours, as used in the design prototype.
object Re {
    val Teal = Color(0xFF00A8A3)
    val Lime = Color(0xFFD0FD0A)
    val N01 = Color(0xFFDDDDDD)
    val N02 = Color(0xFFBDBDBD)
    val N04 = Color(0xFF7E7E7E)
    val N05 = Color(0xFF212121)
    val N07 = Color(0xFF0F1010)
    val Red = Color(0xFFC62828)
    val RedDark = Color(0xFF8E1B1B)
    val Amber = Color(0xFFF2A900)
    val Blue = Color(0xFF3D7BFF)
    val Panel = Color(0xFF0B0E0E)
    val Tile = Color(0xFF171B1B)
}

@Composable
fun TellTale(label: String, on: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (on) color.copy(alpha = 0.16f) else Color(0xFF151A1A)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (on) color else Color(0xFF3A4343),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun TellTaleRow(s: CockpitState) {
    val off = !s.connected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Re.Panel)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TellTale("L", !off && (s.indicator == "left" || s.indicator == "hazard"), Re.Lime)
        TellTale("LOW", !off && !s.highBeam, Re.Lime)
        TellTale("HIGH", !off && s.highBeam, Re.Blue)
        TellTale("STAND", !off && s.stand == "Applied", Re.Amber)
        TellTale("KILL", !off && s.kill == "Off", Re.Red)
        TellTale("FAULT", !off && s.warn, Re.Red)
        TellTale("BT", true, if (off) Re.Red else Re.Teal)
        TellTale("R", !off && (s.indicator == "right" || s.indicator == "hazard"), Re.Lime)
    }
}

@Composable
private fun PowerTile(label: String, value: String, color: Color?, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Re.Tile)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = color ?: Re.N04, fontFamily = AntonFamily, fontSize = 15.sp)
        Text(label, color = Re.N04, fontSize = 10.sp)
    }
}

// Ignition, side stand and kill switch as coloured tiles.
@Composable
fun PowertrainWidget(s: CockpitState) {
    val off = !s.connected
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Re.N07)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Powertrain status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                if (off) "Not connected" else "Connected",
                color = if (off) Re.N04 else Color.White,
                fontSize = 12.sp,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PowerTile(
                label = "IGNITION",
                value = if (off) "—" else s.ignition.uppercase(),
                color = if (off) null else if (s.ignition == "On") Re.Lime else Re.N04,
                modifier = Modifier.weight(1f),
            )
            PowerTile(
                label = "SIDE STAND",
                value = if (off) "—" else if (s.stand == "Applied") "DOWN" else "UP",
                color = if (off) null else if (s.stand == "Applied") Re.Amber else Re.Lime,
                modifier = Modifier.weight(1f),
            )
            PowerTile(
                label = "KILL SWITCH",
                value = if (off) "—" else if (s.kill == "Run") "RUN" else "OFF",
                color = if (off) null else if (s.kill == "Run") Re.Lime else Re.Red,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

enum class SwipeTone { Teal, Mute, Red }

// Drag the knob across to act. Releasing early springs it back, as the design does.
@Composable
fun SwipeAction(label: String, tone: SwipeTone, onComplete: () -> Unit) {
    val knobWidth = if (tone == SwipeTone.Red) 60.dp else 132.dp
    val background = when (tone) {
        SwipeTone.Teal -> Color.White.copy(alpha = 0.92f)
        SwipeTone.Mute -> Color(0xFFEDEDED)
        SwipeTone.Red -> Color(0xFF1A1B1B)
    }
    val knobColor = when (tone) {
        SwipeTone.Teal -> Re.Teal
        SwipeTone.Mute -> Re.N02
        SwipeTone.Red -> Re.Red
    }
    val density = LocalDensity.current
    var offsetPx by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(background),
    ) {
        val maxPx = with(density) { (maxWidth - knobWidth).toPx() }
        Box(Modifier.fillMaxSize().padding(start = knobWidth), contentAlignment = Alignment.Center) {
            Text(label, color = Re.N05, fontSize = 14.sp)
        }
        Box(
            Modifier
                .offset { IntOffset(offsetPx.roundToInt(), 0) }
                .width(knobWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(30.dp))
                .background(knobColor)
                .pointerInput(maxPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetPx >= maxPx * 0.85f) onComplete()
                            offsetPx = 0f
                        },
                        onDragCancel = { offsetPx = 0f },
                    ) { _, dragAmount ->
                        offsetPx = (offsetPx + dragAmount).coerceIn(0f, maxPx)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (tone == SwipeTone.Red) Color.White else Re.N07,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

// Bottom sheet over a dimmed screen. Used for the design's trays.
@Composable
fun Tray(title: String, onClose: (() -> Unit)?, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0x8C000000))) {
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color.White)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 34.dp),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Re.N02),
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(
                    title,
                    modifier = Modifier.weight(1f),
                    color = Re.N07,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                if (onClose != null) {
                    TextButton(onClick = onClose) { Text("Close", color = Re.N05) }
                }
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun ToastBar(message: String, bad: Boolean) {
    Text(
        text = message,
        color = Color.White,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (bad) Color(0xFF3A0E0E) else Color(0xFF212121))
            .padding(13.dp),
    )
}

@Composable
fun Compass() {
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0xFF111414)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(40.dp)) {
            drawCircle(color = Color(0xFF2A3030), radius = size.minDimension / 2f, style = Stroke(width = 2f))
        }
        Text("NW", color = Re.N02, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BatteryTrack(fraction: Float, low: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFECECEC)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(if (low) Re.Red else Re.Teal),
        )
    }
}

@Composable
fun SosButton(size: Dp, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = CircleShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Re.Red, contentColor = Color.White),
    ) {
        Text("SOS", fontFamily = AntonFamily, fontSize = if (size >= 56.dp) 16.sp else 12.sp)
    }
}

@Composable
fun WhiteCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

// The Flying Flea N photo with its background removed on the device, as the design does.
@Composable
fun BikeImage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, context) {
        value = withContext(Dispatchers.Default) { loadBikeCutout(context) }
    }
    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = "Flying Flea N",
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
fun ChoiceRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { option ->
            val on = option == selected
            Button(
                onClick = { onSelect(option) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (on) Re.Teal else Color(0xFFEDEDED),
                    contentColor = if (on) Re.N07 else Re.N05,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
            ) {
                Text(option, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

// Anton, the Royal Enfield display face used for numbers and headings.
val AntonFamily = FontFamily(Font(R.font.anton))
