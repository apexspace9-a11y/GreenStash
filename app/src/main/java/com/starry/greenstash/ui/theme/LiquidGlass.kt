package com.starry.greenstash.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MocQuyBackground(content: @Composable BoxScope.() -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val base = if (dark) Color(0xFF071A16) else Color(0xFFF1FBF7)
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        base,
                        if (dark) Color(0xFF0D2B25) else Color(0xFFE4F7F0),
                        if (dark) Color(0xFF10263A) else Color(0xFFEAF3FF)
                    )
                )
            ),
        content = content
    )
}

@Composable
fun Modifier.liquidGlass(radius: Dp = 26.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val tint = if (dark) Color.White.copy(alpha = 0.075f) else Color.White.copy(alpha = 0.58f)
    val rim = if (dark) Color.White.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.92f)
    return this
        .clip(shape)
        .background(tint)
        .border(BorderStroke(1.dp, rim), shape)
        .drawWithCache {
            val shine = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.18f, size.height * 0.08f),
                radius = size.maxDimension * 0.85f
            )
            onDrawWithContent {
                drawContent()
                drawRect(shine)
                drawRoundRect(
                    color = Color.White.copy(alpha = if (dark) 0.10f else 0.28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
}

private fun Color.luminance(): Float =
    (red * 0.299f) + (green * 0.587f) + (blue * 0.114f)
