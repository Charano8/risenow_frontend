package com.simats.risenow.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.theme.*

@Composable
fun AppBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AppBackgroundTop, AppBackgroundBottom)
                )
            )
    ) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 20,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (enabled && !isLoading) {
                        listOf(PrimaryGradientStart, PrimaryGradientEnd)
                    } else {
                        listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.2f))
                    }
                )
            )
            .drawBehind {
                if (enabled && !isLoading) {
                    drawRect(
                        color = PrimaryGlow,
                        alpha = 0.3f
                    )
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}
