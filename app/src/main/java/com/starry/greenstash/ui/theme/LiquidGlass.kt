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

    val base = if (dark) Color(0xFF18261F) else Color(0xFFF7F5EF)
    val mid = if (dark) Color(0xFF24392F) else Color(0xFFEEF3EC)
    val end = if (dark) Color(0xFF32392E) else Color(0xFFF5EFE2)
    val jadeGlow = if (dark) {
        Color(0xFF69B58D).copy(alpha = 0.18f)
    } else {
        Color(0xFF79B596).copy(alpha = 0.13f)
    }
    val mossGlow = if (dark) {
        Color(0xFFA0B588).copy(alpha = 0.12f)
    } else {
        Color(0xFFA5B692).copy(alpha = 0.10f)
    }
    val brassGlow = Color(0xFFC5A86A).copy(alpha = if (dark) 0.10f else 0.09f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val baseGradient = Brush.linearGradient(
                    colors = listOf(base, mid, end),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
                val jade = Brush.radialGradient(
                    colors = listOf(jadeGlow, Color.Transparent),
                    center = Offset(size.width * 0.10f, size.height * 0.08f),
                    radius = size.maxDimension * 0.52f
                )
                val moss = Brush.radialGradient(
                    colors = listOf(mossGlow, Color.Transparent),
                    center = Offset(size.width * 0.92f, size.height * 0.55f),
                    radius = size.maxDimension * 0.50f
                )
                val brass = Brush.radialGradient(
                    colors = listOf(brassGlow, Color.Transparent),
                    center = Offset(size.width * 0.58f, size.height * 0.98f),
                    radius = size.maxDimension * 0.34f
                )
                onDrawBehind {
                    drawRect(baseGradient)
                    drawRect(jade)
                    drawRect(moss)
                    drawRect(brass)
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
    radius: Dp = 24.dp,
    blurAmount: Dp = 18.dp,
): Modifier {
    val shape = RoundedCornerShape(radius)
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val hazeState = LocalMocQuyHazeState.current
    val primary = MaterialTheme.colorScheme.primary

    val baseTint = if (dark) {
        Color(0xFF31443A).copy(alpha = 0.58f)
    } else {
        Color(0xFFFFFEFA).copy(alpha = 0.48f)
    }
    val topWash = Color.White.copy(alpha = if (dark) 0.10f else 0.16f)
    val bottomWash = primary.copy(alpha = if (dark) 0.055f else 0.035f)
    val rimBright = Color.White.copy(alpha = if (dark) 0.28f else 0.56f)
    val rimTint = primary.copy(alpha = if (dark) 0.14f else 0.10f)
    val shadowColor = Color.Black.copy(alpha = if (dark) 0.16f else 0.08f)

    val blurLayer = if (hazeState != null) {
        Modifier.hazeEffect(state = hazeState) {
            drawContentBehind = true
            blurEffect {
                blurRadius = blurAmount
                noiseFactor = 0.025f
                colorEffects = listOf(HazeColorEffect.tint(baseTint))
            }
        }
    } else {
        Modifier.background(baseTint)
    }

    return this
        .shadow(
            elevation = 8.dp,
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
                    colors = listOf(rimBright, rimTint, rimBright.copy(alpha = rimBright.alpha * 0.55f)),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            ),
            shape
        )
        .drawWithCache {
            val topSpecular = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (dark) 0.22f else 0.30f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.14f, size.height * -0.08f),
                radius = size.maxDimension * 0.52f
            )
            onDrawWithContent {
                drawContent()
                drawRect(topSpecular)
                drawRoundRect(
                    color = Color.White.copy(alpha = if (dark) 0.10f else 0.13f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius((radius - 1.dp).toPx()),
                    style = Stroke(width = 0.8.dp.toPx())
                )
            }
        }
}

private fun Color.luminance(): Float =
    (red * 0.299f) + (green * 0.587f) + (blue * 0.114f)
