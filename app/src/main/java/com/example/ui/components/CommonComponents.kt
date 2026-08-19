package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import com.example.R
import com.example.data.model.EnergyLevel
import com.example.data.model.TaskPriority
import com.example.ui.theme.AmberImportant
import com.example.ui.theme.CoralUrgent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.delay

@Composable
fun CelebrationBanner(
    text: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(text) {
        if (text != null) {
            delay(3200)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = text != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f)
        ) + fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.9f),
        exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.95f),
        modifier = modifier
    ) {
        if (text != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("celebration_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.btn_cancel),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnergyLevelChip(
    energyLevel: EnergyLevel,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val (bg, contentColor) = when (energyLevel) {
        EnergyLevel.LOW -> {
            if (isDark) Color(0xFF064E3B) to Color(0xFFA7F3D0)
            else Color(0xFFD1E7DD) to Color(0xFF0F5132)
        }
        EnergyLevel.MEDIUM -> {
            if (isDark) Color(0xFF78350F) to Color(0xFFFDE68A)
            else Color(0xFFFFF3CD) to Color(0xFF664D03)
        }
        EnergyLevel.HIGH -> {
            if (isDark) Color(0xFF881337) to Color(0xFFFECDD3)
            else Color(0xFFF8D7DA) to Color(0xFF842029)
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) bg else bg.copy(alpha = if (isDark) 0.5f else 0.7f),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = energyLevel.emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(energyLevel.stringRes),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}

@Composable
fun PriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val (bgColor, textColor) = when (priority) {
        TaskPriority.URGENT -> {
            if (isDark) CoralUrgent.copy(alpha = 0.2f) to CoralUrgent
            else Color(0xFFFFDAD6) to Color(0xFFBA1A1A)
        }
        TaskPriority.IMPORTANT -> {
            if (isDark) AmberImportant.copy(alpha = 0.2f) to AmberImportant
            else Color(0xFFFFDEA8) to Color(0xFF7D5700)
        }
        TaskPriority.CASUAL -> {
            if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) to MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = stringResource(priority.stringRes),
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
