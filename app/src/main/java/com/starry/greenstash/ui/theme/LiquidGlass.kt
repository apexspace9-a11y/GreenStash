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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect

private val LocalMocQuyHazeState = staticCompositionLocalOf<HazeState?> { null }

@Composable
fun MocQuyBackground(content: @Composable BoxScope.() -> Unit) {
    val hazeState = rememberHazeState()
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val base = if (dark) Color(0xFF061512) else Color(0xFFF2FBF7)
    val mid = if (dark) Color(0xFF0B2A24) else Color(0xFFE2F7EF)
    val end = if (dark) Color(0xFF102238) else Color(0xFFE7F1FF)
    val glowA = if (dark) Color(0xFF36E6A1).copy(alpha = 0.22f) else Color(0xFF44D8A2).copy(alpha = 0.24f)
    val glowB = if (dark) Color(0xFF5B8DFF).copy(alpha = 0.20f) else Color(0xFF7CA8FF).copy(alpha = 0.22f)
    val glowC = if (dark) Color(0xFFB768FF).copy(alpha = 0.12f) else Color(0xFFC395FF).copy(alpha = 0.16f)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val baseGradient = Brush.linearGradient(
                        colors = listOf(base, mid, end),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                    val greenGlow = Brush.radialGradient(
                        colors = listOf(glowA, Color.Transparent),
                        center = Offset(size.width * 0.18f, size.height * 0.18f),
                        radius = size.maxDimension * 0.48f
                    )
                    val blueGlow = Brush.radialGradient(
                        colors = listOf(glowB, Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.28f),
                        radius = size.maxDimension * 0.52f
                    )
                    val violetGlow = Brush.radialGradient(
                        colors = listOf(glowC, Color.Transparent),
                        center = Offset(size.width * 0.45f, size.height * 0.92f),
                        radius = size.maxDimension * 0.48f
                    )
                    onDrawBehind {
                        drawRect(baseGradient)
                        drawRect(greenGlow)
                        drawRect(blueGlow)
                        drawRect(violetGlow)
                    }
                }
                .hazeSource(hazeState)
        )

        CompositionLocalProvider(LocalMocQuyHazeState provides hazeState) {
            Box(modifier = Modifier.fillMaxSize(), content = content)
        }
    }
}

@Composable
fun Modifier.liquidGlass(
    radius: Dp = 26.dp,
    blurAmount: Dp = 18.dp,
): Modifier {
    val shape = RoundedCornerShape(radius)
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val hazeState = LocalMocQuyHazeState.current
    val reflectedLightColor = MaterialTheme.colorScheme.primary.copy(
        alpha = if (dark) 0.10f else 0.08f
    )

    val tint = if (dark) {
        Color(0xFF10231F).copy(alpha = 0.34f)
    } else {
        Color.White.copy(alpha = 0.34f)
    }
    val tintTop = if (dark) Color.White.copy(alpha = 0.085f) else Color.White.copy(alpha = 0.34f)
    val tintBottom = if (dark) Color(0xFF58D5AD).copy(alpha = 0.035f) else Color(0xFFB9E9D9).copy(alpha = 0.16f)
    val rimStrong = if (dark) Color.White.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.90f)
    val rimSoft = if (dark) Color.White.copy(alpha = 0.08f) else Color(0xFF7A9B91).copy(alpha = 0.22f)
    val shadowColor = Color.Black.copy(alpha = if (dark) 0.34f else 0.16f)

    val glassLayer = if (hazeState != null) {
        Modifier.hazeEffect(state = hazeState) {
            drawContentBehind = true
            blurEffect {
                blurRadius = blurAmount
                noiseFactor = 0.035f
                colorEffects = listOf(HazeColorEffect.tint(tint))
            }
        }
    } else {
        Modifier.background(tint)
    }

    return this
        .shadow(
            elevation = 14.dp,
            shape = shape,
            clip = false,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .clip(shape)
        .then(glassLayer)
        .background(
            Brush.verticalGradient(
                colors = listOf(tintTop, Color.Transparent, tintBottom)
            )
        )
        .border(
            BorderStroke(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(rimStrong, rimSoft, rimStrong.copy(alpha = rimStrong.alpha * 0.55f)),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            ),
            shape
        )
        .drawWithCache {
            val specular = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (dark) 0.30f else 0.48f),
                    Color.White.copy(alpha = if (dark) 0.08f else 0.14f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.16f, size.height * 0.04f),
                radius = size.maxDimension * 0.62f
            )
            val lowerReflectedLight = Brush.radialGradient(
                colors = listOf(
                    reflectedLightColor,
                    Color.Transparent
                ),
                center = Offset(size.width * 0.78f, size.height * 1.06f),
                radius = size.maxDimension * 0.55f
            )
            onDrawWithContent {
                drawContent()
                drawRect(specular)
                drawRect(lowerReflectedLight)
                drawRoundRect(
                    color = Color.White.copy(alpha = if (dark) 0.11f else 0.26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx()),
                    style = Stroke(width = 0.75.dp.toPx())
                )
            }
        }
}

private fun Color.luminance(): Float =
    (red * 0.299f) + (green * 0.587f) + (blue * 0.114f)
