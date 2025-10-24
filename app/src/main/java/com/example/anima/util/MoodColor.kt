package com.example.anima.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

object MoodColor {
    private val blue = Color(0xFF2196F3)
    private val green = Color(0xFF4CAF50)

    /**
     * Map mood 1..10 to a color between blue (1) and green (10). Null returns neutral gray.
     */
    fun colorFor(mood: Int?): Color {
        if (mood == null) return Color(0xFF9E9E9E)
        val clamped = mood.coerceIn(1, 10)
        val t = (clamped - 1) / 9f
        return lerp(blue, green, t)
    }
}

