package com.ai.assistance.operit.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Brand Colors (Electric Violet / Neon Blue)
val PrimaryViolet = Color(0xFF7F5AF0)
val PrimaryBlue = Color(0xFF2CB67D)
val SecondaryCyan = Color(0xFF2CB67D)

// Dark Theme Colors (Nebula/Midnight)
val NebulaBackground = Color(0xFF0F0E17) // Deep Onyx
val NebulaSurface = Color(0xFF16161A) // Slightly lighter for cards
val NebulaSurfaceHighlight = Color(0xFF242629)
val TextPrimary = Color(0xFFFFFFFE)
val TextSecondary = Color(0xFF94A1B2)

// Light Theme Colors (Clean/Crisp)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF2F4F6)
val LightTextPrimary = Color(0xFF2D3436)
val LightTextSecondary = Color(0xFF636E72)

// Gradients
val NebulaGradient = Brush.verticalGradient(colors = listOf(Color(0xFF0F0E17), Color(0xFF1A1A2E)))

val PrimaryGradient =
        Brush.horizontalGradient(colors = listOf(Color(0xFF7F5AF0), Color(0xFF6246EA)))

// Glassmorphism
val GlassBlack = Color(0xCC0F0E17)
val GlassWhite = Color(0xCCFFFFFF)
val GlassBorder = Color(0x1AFFFFFF)

// Legacy/Material Colors (Mapped)
val Purple80 = PrimaryViolet
val PurpleGrey80 = TextSecondary
val Pink80 = SecondaryCyan

val Purple40 = PrimaryViolet
val PurpleGrey40 = TextSecondary
val Pink40 = SecondaryCyan
