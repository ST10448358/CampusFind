package com.segotlo.campusfindapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Static Dark Base Colors
val DeepNavyDark = Color(0xFF0B0F19)
val SurfaceNavyDark = Color(0xFF111827)
val CardNavyDark = Color(0xFF1F2937)

// Static Light Base Colors
val DeepNavyLight = Color(0xFFF3F4F6)
val SurfaceNavyLight = Color(0xFFFFFFFF)
val CardNavyLight = Color(0xFFFFFFFF)

// Accent Colors
val TealPrimary = Color(0xFF2D7D71)
val TealAccent = Color(0xFF3BA092)

val AccentPurple = Color(0xFF4F46E5)
val AccentGreen = Color(0xFF10B981)
val AccentRed = Color(0xFFF43F5E)
val AccentAmber = Color(0xFFF59E0B)
val AccentSkyBlue = Color(0xFF0EA5E9)

// Text & Border Dark Colors
val TextPrimaryDark = Color(0xFFF9FAFB)
val TextSecondaryDark = Color(0xFF9CA3AF)
val BorderColorDark = Color(0xFF374151)

// Text & Border Light Colors
val TextPrimaryLight = Color(0xFF111827)
val TextSecondaryLight = Color(0xFF4B5563)
val BorderColorLight = Color(0xFFE5E7EB)

// Dynamic Composable Color Getters Reacting To LocalIsDarkMode
val DeepNavy: Color
    @Composable get() = if (LocalIsDarkMode.current) DeepNavyDark else DeepNavyLight

val SurfaceNavy: Color
    @Composable get() = if (LocalIsDarkMode.current) SurfaceNavyDark else SurfaceNavyLight

val CardNavy: Color
    @Composable get() = if (LocalIsDarkMode.current) CardNavyDark else CardNavyLight

val TextPrimary: Color
    @Composable get() = if (LocalIsDarkMode.current) TextPrimaryDark else TextPrimaryLight

val TextSecondary: Color
    @Composable get() = if (LocalIsDarkMode.current) TextSecondaryDark else TextSecondaryLight

val BorderColor: Color
    @Composable get() = if (LocalIsDarkMode.current) BorderColorDark else BorderColorLight
