package com.simats.risenow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import com.simats.risenow.ui.components.AppBackground
import com.simats.risenow.ui.theme.*

@Composable
fun SplashScreen() {
    val shimmerColors = listOf(
        PrimaryGradientStart.copy(alpha = 0.6f),
        PrimaryGradientEnd,
        PrimaryGradientStart.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    AppBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Composition
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = PrimaryGradientStart,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.BottomCenter)
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                                drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
                            }
                        }
                )
                 Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = PrimaryGradientEnd,
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.TopCenter)
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                                drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
                            }
                        }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "RiseNow",
                color = TextPrimary,
                style = MaterialTheme.typography.displayLarge,
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Wake With Purpose",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    RiseNowTheme {
        SplashScreen()
    }
}
