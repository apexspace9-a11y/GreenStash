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
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private val LocalMocQuyHazeState = staticCompositionLocalOf<HazeState?> { null }

@Composable
fun MocQuyBackground(content: @Composable BoxScope.() -> Unit) {
    val hazeState = rememberHazeState()
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val base = if (dark) Color(0xFF04130F) else Color(0xFFF5FCF9)
    val mid = if (dark) Color(0xFF0A2B24) else Color(0xFFE5F8F0)
    val end = if (dark) Color(0xFF0B2033) else Color(0xFFEAF2FF)
    val greenGlow = if (dark) Color(0xFF2CE7A4).copy(alpha = 0.30f) else Color(0xFF36DFA0).copy(alpha = 0.25f)
    val cyanGlow = if (dark) Color(0xFF35C9F5).copy(alpha = 0.22f) else Color(0xFF59CFF2).copy(alpha = 0.22f)
    val violetGlow = if (dark) Color(0xFF9C6CFF).copy(alpha = 0.16f) else Color(0xFFB48CFF).copy(alpha = 0.16f)
    val goldGlow = Color(0xFFF4C95D).copy(alpha = if (dark) 0.10f else 0.12f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val baseGradient = Brush.linearGradient(
                    colors = listOf(base, mid, end),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
                val glow1 = Brush.radialGradient(
                    colors = listOf(greenGlow, Color.Transparent),
                    center = Offset(size.width * 0.08f, size.height * 0.10f),
                    radius = size.maxDimension * 0.54f
                )
                val glow2 = Brush.radialGradient(
                    colors = listOf(cyanGlow, Color.Transparent),
                    center = Offset(size.width * 0.96f, size.height * 0.30f),
                    radius = size.maxDimension * 0.56f
                )
                val glow3 = Brush.radialGradient(
                    colors = listOf(violetGlow, Color.Transparent),
                    center = Offset(size.width * 0.42f, size.height * 0.98f),
                    radius = size.maxDimension * 0.50f
                )
                val glow4 = Brush.radialGradient(
                    colors = listOf(goldGlow, Color.Transparent),
                    center = Offset(size.width * 0.80f, size.height * 0.86f),
                    radius = size.maxDimension * 0.34f
                )
                onDrawBehind {
                    drawRect(baseGradient)
                    drawRect(glow1)
                    drawRect(glow2)
                    drawRect(glow3)
                    drawRect(glow4)
                }
            }
            .hazeSource(hazeState)
    ) {
        CompositionLocalProvider(LocalMocQuyHazeState provides hazeState) {
            content()
        }
    }
}

@Composable
fun Modifier.liquidGlass(
    radius: Dp = 28.dp,
    blurAmount: Dp = 26.dp,
): Modifier {
    val shape = RoundedCornerShape(radius)
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val hazeState = LocalMocQuyHazeState.current
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    val baseTint = if (dark) {
        Color(0xFF0B201B).copy(alpha = 0.23f)
    } else {
        Color.White.copy(alpha = 0.22f)
    }
    val topWash = Color.White.copy(alpha = if (dark) 0.075f else 0.23f)
    val bottomWash = primary.copy(alpha = if (dark) 0.045f else 0.065f)
    val rimBright = Color.White.copy(alpha = if (dark) 0.46f else 0.94f)
    val rimCool = secondary.copy(alpha = if (dark) 0.18f else 0.20f)
    val shadowColor = Color.Black.copy(alpha = if (dark) 0.27f else 0.12f)

    val blurLayer = if (hazeState != null) {
        Modifier.hazeEffect(state = hazeState) {
            drawContentBehind = true
            blurEffect {
                blurRadius = blurAmount
                noiseFactor = 0.055f
                colorEffects = listOf(HazeColorEffect.tint(baseTint))
            }
        }
    } else {
        Modifier.background(baseTint)
    }

    return this
        .shadow(
            elevation = 12.dp,
            shape = shape,
            clip = false,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .clip(shape)
        .then(blurLayer)
        .background(
            Brush.verticalGradient(
                colors = listOf(topWash, Color.Transparent, bottomWash)
            )
        )
        .border(
            BorderStroke(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        rimBright,
                        rimCool,
                        Color.White.copy(alpha = if (dark) 0.08f else 0.30f),
                        rimBright.copy(alpha = rimBright.alpha * 0.66f)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            ),
            shape
        )
        .drawWithCache {
            val topSpecular = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (dark) 0.38f else 0.60f),
                    Color.White.copy(alpha = if (dark) 0.08f else 0.12f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.10f, size.height * -0.06f),
                radius = size.maxDimension * 0.62f
            )
            val sideRefraction = Brush.linearGradient(
                colors = listOf(
                    secondary.copy(alpha = if (dark) 0.12f else 0.10f),
                    Color.Transparent,
                    primary.copy(alpha = if (dark) 0.10f else 0.08f)
                ),
                start = Offset(0f, size.height * 0.15f),
                end = Offset(size.width, size.height * 0.84f)
            )
            val bottomCaustic = Brush.radialGradient(
                colors = listOf(
                    primary.copy(alpha = if (dark) 0.16f else 0.12f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.72f, size.height * 1.12f),
                radius = size.maxDimension * 0.58f
            )
            val innerGlow = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (dark) 0.13f else 0.32f),
                    Color.Transparent,
                    secondary.copy(alpha = if (dark) 0.07f else 0.08f)
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )

            onDrawWithContent {
                drawContent()
                drawRect(sideRefraction)
                drawRect(topSpecular)
                drawRect(bottomCaustic)
                drawRoundRect(
                    brush = innerGlow,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius((radius - 1.dp).toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = if (dark) 0.10f else 0.20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx()),
                    style = Stroke(width = 0.55.dp.toPx())
                )
            }
        }
}

private fun Color.luminance(): Float =
    (red * 0.299f) + (green * 0.587f) + (blue * 0.114f)
