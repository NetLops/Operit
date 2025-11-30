package com.ai.assistance.operit.ui.features.chat.components.style

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.lerp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MagicMotionEffect(
    modifier: Modifier = Modifier,
    isGenerating: Boolean = true
) {
    if (!isGenerating) return

    Box(modifier = modifier) {
        // Sparkles
        SparklesEffect(modifier = Modifier.fillMaxSize())
        
        // Breathing/Pulse Effect
        BreathingEffect(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun SparklesEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "Sparkles")
    
    // Create multiple sparkles with different phases
    val sparkles = remember { List(15) { SparkleState() } }
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    Canvas(modifier = modifier) {
        sparkles.forEach { sparkle ->
            val currentProgress = (time + sparkle.offset) % 1f
            val alpha = if (currentProgress < 0.5f) {
                currentProgress * 2
            } else {
                (1f - currentProgress) * 2
            }
            
            val scale = 0.5f + alpha * 0.5f
            
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.8f),
                radius = sparkle.size * scale,
                center = Offset(
                    x = size.width * sparkle.x,
                    y = size.height * sparkle.y
                )
            )
        }
    }
}

data class SparkleState(
    val x: Float = Random.nextFloat(),
    val y: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 4f + 2f,
    val offset: Float = Random.nextFloat()
)

@Composable
fun BreathingEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "Breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * scale
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6200EE).copy(alpha = alpha), // Primary color
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }
}
