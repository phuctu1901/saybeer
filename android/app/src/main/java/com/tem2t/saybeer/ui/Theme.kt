package com.tem2t.saybeer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

// Tokens extracted from the user's Stitch project, 28 October 2026.
val Amber = Color(0xFFF5A623)
val Gold = Color(0xFFFBBF24)
val Muted = Color(0xFFB8A793)
val Green = Color(0xFF6ED6AD)
val Red = Color(0xFFFF8395)
val Ink = Color(0xFF1C1204)
val Page = Color(0xFF0F0B08)
val Panel = Color(0xFF1D1914)
val CardBorder = Color(0xFF3A2D1B)
val PageGradient = Brush.verticalGradient(listOf(Color(0xFF120E0A), Color(0xFF0B0907)))
val ActionGradient = Brush.linearGradient(listOf(Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFE58000)))
val CardGradient = Brush.linearGradient(listOf(Color(0xFF241D15), Color(0xFF15110D)))

fun Modifier.amberGlow(strength: Float = .14f) = drawBehind {
    val radius = size.maxDimension * 1.1f
    drawCircle(Brush.radialGradient(listOf(Amber.copy(alpha = strength), Color.Transparent),
        center = center, radius = radius), radius = radius)
}

@Composable fun SayBeerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Amber, onPrimary = Ink,
        primaryContainer = Color(0xFF39270D), onPrimaryContainer = Gold,
        secondary = Gold, secondaryContainer = Color(0xFF2B2115), onSecondaryContainer = Gold,
        background = Page, surface = Color(0xFF181512), outline = Color(0xFF64513B),
        surfaceVariant = Panel, onSurface = Color(0xFFF7F4F0), onSurfaceVariant = Muted, error = Red),
        shapes = Shapes(extraSmall = RoundedCornerShape(12.dp), small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(28.dp))) {
        Surface(Modifier.fillMaxSize(), color = Page, contentColor = MaterialTheme.colorScheme.onSurface) {
            Box(Modifier.background(PageGradient).drawBehind {
                drawRect(Brush.radialGradient(listOf(Amber.copy(alpha = .08f), Color.Transparent),
                    center = Offset(size.width * .5f, size.height * .32f), radius = size.height * .6f))
            }) { content() }
        }
    }
}

/** Scrolling content and an independent action area keep actions reachable with the keyboard open. */
@Composable fun Screen(title: String, back: () -> Unit, action: (@Composable () -> Unit)? = null,
                      footer: (@Composable ColumnScope.() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back, modifier = Modifier.offset(x = (-8).dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Quay lại", tint = Gold, modifier = Modifier.size(25.dp))
            }
            Text(title, Modifier.weight(1f), fontSize = if (title.length > 16) 20.sp else 22.sp,
                lineHeight = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.5).sp)
            action?.invoke()
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp), content = content)
        if (footer != null) Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), content = footer)
    }
}

@Composable fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().background(CardGradient, RoundedCornerShape(18.dp))
        .border(1.dp, CardBorder, RoundedCornerShape(18.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
}

@Composable fun Section(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label.uppercase(), color = Muted, fontSize = 11.sp, lineHeight = 16.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        GlassCard(content = content)
    }
}

@Composable fun HeroIcon(icon: ImageVector, description: String? = null, color: Color = Amber, size: Dp = 96.dp) {
    Box(Modifier.size(size).amberGlow(.20f)
        .background(Brush.verticalGradient(listOf(color.copy(alpha = .19f), color.copy(alpha = .05f))), RoundedCornerShape(size * .27f))
        .border(1.dp, color.copy(alpha = .24f), RoundedCornerShape(size * .27f)), contentAlignment = Alignment.Center) {
        Icon(icon, description, Modifier.size(size * .46f), tint = color)
    }
}

@Composable fun PrimaryButton(text: String, enabled: Boolean = true, icon: ImageVector? = null, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .98f else 1f, label = "button press")
    val shape = RoundedCornerShape(16.dp)
    Button(onClick = onClick, enabled = enabled, interactionSource = interaction,
        modifier = Modifier.fillMaxWidth().scale(scale).heightIn(min = 58.dp)
            .then(if (enabled) Modifier.shadow(10.dp, shape, ambientColor = Amber.copy(alpha = .1f), spotColor = Amber.copy(alpha = .25f)) else Modifier)
            .background(if (enabled) ActionGradient else Brush.linearGradient(listOf(Color(0xFF2B2723), Color(0xFF2B2723))), shape),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Ink,
            disabledContainerColor = Color.Transparent, disabledContentColor = Color(0xFF9F968C)),
        shape = shape, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        if (icon != null) { Icon(icon, null, Modifier.size(21.dp)); Spacer(Modifier.width(10.dp)) }
        Text(text, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable fun SecondaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, if (enabled) CardBorder else CardBorder.copy(alpha = .4f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF171410), contentColor = Color(0xFFE0D5C7)),
        contentPadding = PaddingValues(16.dp)) {
        Text(text, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
    }
}

@Composable fun StatusPill(text: String, icon: ImageVector? = null) {
    Row(Modifier.background(Amber.copy(alpha = .1f), RoundedCornerShape(50))
        .border(1.dp, Amber.copy(alpha = .24f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (icon != null) Icon(icon, null, Modifier.size(14.dp), tint = Amber)
        Text(text, color = Amber, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable fun Note(text: String, color: Color = Muted) {
    Text(text, color = color, style = MaterialTheme.typography.bodySmall, lineHeight = 20.sp)
}

@Composable fun ToggleRow(text: String, checked: Boolean, changed: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, Modifier.weight(1f).padding(end = 12.dp), fontWeight = FontWeight.SemiBold)
        Switch(checked = checked, onCheckedChange = changed,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Amber,
                checkedBorderColor = Amber, uncheckedTrackColor = Color(0xFF30271F), uncheckedBorderColor = CardBorder))
    }
}
