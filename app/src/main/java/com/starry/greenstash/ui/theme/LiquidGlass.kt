package com.starry.greenstash.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(26.dp),
    elevation: Dp = 12.dp,
    tint: Color = MaterialTheme.colorScheme.surface,
): Modifier {
    val dark = isSystemInDarkTheme()
    val baseAlpha = if (dark) 0.56f else 0.68f
    val highlightAlpha = if (dark) 0.18f else 0.52f
    val edgeAlpha = if (dark) 0.24f else 0.60f
    val primary = MaterialTheme.colorScheme.primary

    val glassBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = highlightAlpha),
            tint.copy(alpha = baseAlpha),
            primary.copy(alpha = if (dark) 0.10f else 0.08f),
            tint.copy(alpha = baseAlpha * 0.82f),
        )
    )
    val edgeBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = edgeAlpha),
            Color.White.copy(alpha = 0.10f),
            primary.copy(alpha = 0.18f),
            Color.White.copy(alpha = edgeAlpha * 0.72f),
        )
    )

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.16f),
            spotColor = Color.Black.copy(alpha = 0.22f),
        )
        .clip(shape)
        .background(glassBrush, shape)
        .border(BorderStroke(1.dp, edgeBrush), shape)
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    elevation: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .liquidGlass(shape = shape, elevation = elevation)
            .padding(contentPadding),
        content = content,
    )
}
