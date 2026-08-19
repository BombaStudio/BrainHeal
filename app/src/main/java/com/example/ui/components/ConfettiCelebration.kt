package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class ParticleShape {
    RECTANGLE,
    CIRCLE,
    STAR,
    RIBBON
}

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var vRot: Float,
    val color: Color,
    val size: Float,
    val shape: ParticleShape,
    var alpha: Float = 1f,
    var wobble: Float = 0f,
    var wobbleSpeed: Float = 0f
)

private val ConfettiColors = listOf(
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber Gold
    Color(0xFFEC4899), // Pink
    Color(0xFF3B82F6), // Blue
    Color(0xFF8B5CF6), // Purple
    Color(0xFF06B6D4), // Cyan
    Color(0xFFEF4444), // Coral Red
    Color(0xFF84CC16)  // Lime
)

/**
 * High-performance, ADHD-friendly satisfying Canvas confetti explosion.
 * Triggers a burst of multi-colored ribbons, stars and particles whenever [triggerTimestamp] changes (> 0).
 */
@Composable
fun ConfettiCelebrationOverlay(
    triggerTimestamp: Long,
    modifier: Modifier = Modifier,
    particleCount: Int = 90
) {
    var isSimulating by remember { mutableStateOf(false) }
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(triggerTimestamp) {
        if (triggerTimestamp > 0L) {
            val random = Random(triggerTimestamp)
            particles = List(particleCount) {
                val shape = ParticleShape.entries[random.nextInt(ParticleShape.entries.size)]
                val color = ConfettiColors[random.nextInt(ConfettiColors.size)]
                
                // Explode from top-center or upper-third
                val startX = 0.5f + (random.nextFloat() - 0.5f) * 0.4f
                val startY = 0.25f + (random.nextFloat() - 0.5f) * 0.2f
                
                // Varied initial burst velocities
                val angle = random.nextFloat() * 2f * PI.toFloat()
                val speed = random.nextFloat() * 0.9f + 0.3f
                
                ConfettiParticle(
                    x = startX,
                    y = startY,
                    vx = (cos(angle) * speed * 0.4f).coerceIn(-0.6f, 0.6f),
                    vy = (sin(angle) * speed * 0.5f - 0.5f).coerceIn(-0.9f, 0.1f), // initial upward bias
                    rotation = random.nextFloat() * 360f,
                    vRot = (random.nextFloat() - 0.5f) * 720f,
                    color = color,
                    size = random.nextFloat() * 12f + 8f,
                    shape = shape,
                    alpha = 1f,
                    wobble = random.nextFloat() * 2f * PI.toFloat(),
                    wobbleSpeed = random.nextFloat() * 8f + 4f
                )
            }

            isSimulating = true
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
            )
            isSimulating = false
        }
    }

    if (isSimulating && particles.isNotEmpty()) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = progress.value

            particles.forEach { p ->
                // Physics update
                val currentX = (p.x + p.vx * t + sin(p.wobble + t * p.wobbleSpeed) * 0.04f) * w
                val gravity = 0.95f * t * t
                val currentY = (p.y + p.vy * t + gravity) * h
                val currentRot = p.rotation + p.vRot * t
                val currentAlpha = ((1f - t) * 1.4f).coerceIn(0f, 1f)
                val currentScale = (1f - (t * 0.3f)).coerceIn(0.4f, 1.2f)

                if (currentY <= h + 50 && currentAlpha > 0f) {
                    val drawColor = p.color.copy(alpha = currentAlpha)

                    rotate(degrees = currentRot, pivot = Offset(currentX, currentY)) {
                        when (p.shape) {
                            ParticleShape.RECTANGLE -> {
                                val wobbleScaleX = cos(p.wobble + t * p.wobbleSpeed).coerceIn(-1f, 1f)
                                drawRect(
                                    color = drawColor,
                                    topLeft = Offset(currentX - (p.size * wobbleScaleX) / 2f, currentY - p.size / 2f),
                                    size = Size(p.size * currentScale * wobbleScaleX.coerceAtLeast(0.2f), p.size * 1.5f * currentScale)
                                )
                            }
                            ParticleShape.CIRCLE -> {
                                drawCircle(
                                    color = drawColor,
                                    radius = (p.size / 2f) * currentScale,
                                    center = Offset(currentX, currentY)
                                )
                            }
                            ParticleShape.STAR -> {
                                drawStar(
                                    center = Offset(currentX, currentY),
                                    radius = (p.size / 1.5f) * currentScale,
                                    color = drawColor
                                )
                            }
                            ParticleShape.RIBBON -> {
                                val ribbonW = p.size * 0.6f * currentScale
                                val ribbonH = p.size * 2.2f * currentScale
                                drawRoundRect(
                                    color = drawColor,
                                    topLeft = Offset(currentX - ribbonW / 2f, currentY - ribbonH / 2f),
                                    size = Size(ribbonW, ribbonH),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.45f
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = (i * PI / 5.0 - PI / 2.0).toFloat()
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

/**
 * Satisfying Animated ADHD Checkbox with bouncy tactile spring feedback & pulse ring.
 */
@Composable
fun SatisfyingCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    var animateTrigger by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (animateTrigger) 1.25f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = {
            animateTrigger = false
        },
        label = "checkbox_scale"
    )

    Box(
        modifier = modifier
            .size(36.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                animateTrigger = true
                onCheckedChange()
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (checked) checkedColor else Color.Transparent,
            border = if (!checked) androidx.compose.foundation.BorderStroke(2.dp, uncheckedColor) else null,
            modifier = Modifier.size(22.dp)
        ) {
            if (checked) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
