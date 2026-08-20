package com.emirozturk.brainheal.ui.screens.minimalist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirozturk.brainheal.R
import com.emirozturk.brainheal.data.model.PomodoroMode
import com.emirozturk.brainheal.data.model.TaskEntity
import com.emirozturk.brainheal.ui.components.EnergyLevelChip
import com.emirozturk.brainheal.ui.components.PriorityBadge
import com.emirozturk.brainheal.ui.components.SatisfyingCheckbox
import com.emirozturk.brainheal.ui.theme.AmberImportant
import com.emirozturk.brainheal.ui.theme.EmeraldSuccess
import com.emirozturk.brainheal.ui.theme.TealPrimary

@Composable
fun MinimalistModeScreen(
    activeTasks: List<TaskEntity>,
    pomodoroMode: PomodoroMode,
    pomodoroRound: Int,
    remainingSeconds: Int,
    initialSeconds: Int,
    isTimerRunning: Boolean,
    customFocusMinutes: Int,
    customShortBreakMinutes: Int,
    customLongBreakMinutes: Int,
    onSelectMode: (PomodoroMode) -> Unit,
    onSetCustomDuration: (PomodoroMode, Int) -> Unit,
    onAdjustMinutes: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onToggleSubtask: ((TaskEntity, String) -> Unit)? = null,
    onQuickBrainDump: (String) -> Unit,
    onExitMinimalist: () -> Unit
) {
    val topTask = activeTasks.firstOrNull()
    var quickNoteText by remember { mutableStateOf("") }
    var showDurationDialog by remember { mutableStateOf(false) }

    val progress = if (initialSeconds > 0) {
        remainingSeconds.toFloat() / initialSeconds.toFloat()
    } else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "pomodoroProgress"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("minimalist_mode_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.zen_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onExitMinimalist() }
                        .testTag("exit_minimalist_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.zen_exit_btn),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.zen_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Pomodoro Timer Main Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pomodoro_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Pomodoro Mode Segmented Switcher (Focus / Short Break / Long Break)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PomodoroMode.entries.forEach { mode ->
                                val isSelected = pomodoroMode == mode
                                val modeDuration = when (mode) {
                                    PomodoroMode.FOCUS -> customFocusMinutes
                                    PomodoroMode.SHORT_BREAK -> customShortBreakMinutes
                                    PomodoroMode.LONG_BREAK -> customLongBreakMinutes
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                        )
                                        .clickable { onSelectMode(mode) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${stringResource(mode.labelRes)} ($modeDuration)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Round info badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (pomodoroMode) {
                                PomodoroMode.FOCUS -> MaterialTheme.colorScheme.primaryContainer
                                PomodoroMode.SHORT_BREAK -> AmberImportant.copy(alpha = 0.2f)
                                PomodoroMode.LONG_BREAK -> EmeraldSuccess.copy(alpha = 0.2f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (pomodoroMode) {
                                        PomodoroMode.FOCUS -> Icons.Default.HourglassEmpty
                                        PomodoroMode.SHORT_BREAK -> Icons.Default.Coffee
                                        PomodoroMode.LONG_BREAK -> Icons.Default.SelfImprovement
                                    },
                                    contentDescription = null,
                                    tint = when (pomodoroMode) {
                                        PomodoroMode.FOCUS -> MaterialTheme.colorScheme.primary
                                        PomodoroMode.SHORT_BREAK -> AmberImportant
                                        PomodoroMode.LONG_BREAK -> EmeraldSuccess
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(
                                        R.string.pomodoro_round_badge,
                                        pomodoroRound,
                                        stringResource(pomodoroMode.labelRes)
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (pomodoroMode) {
                                        PomodoroMode.FOCUS -> MaterialTheme.colorScheme.onPrimaryContainer
                                        PomodoroMode.SHORT_BREAK -> AmberImportant
                                        PomodoroMode.LONG_BREAK -> EmeraldSuccess
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Circular Countdown Display
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(220.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            // Track
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.size(210.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 10.dp
                            )
                            // Countdown Progress
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(210.dp),
                                color = when {
                                    remainingSeconds < 60 -> MaterialTheme.colorScheme.error
                                    pomodoroMode == PomodoroMode.SHORT_BREAK -> AmberImportant
                                    pomodoroMode == PomodoroMode.LONG_BREAK -> EmeraldSuccess
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                strokeWidth = 10.dp
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isTimerRunning) {
                                        when (pomodoroMode) {
                                            PomodoroMode.FOCUS -> stringResource(R.string.pomodoro_status_focus)
                                            PomodoroMode.SHORT_BREAK -> stringResource(R.string.pomodoro_status_short_break)
                                            PomodoroMode.LONG_BREAK -> stringResource(R.string.pomodoro_status_long_break)
                                        }
                                    } else stringResource(R.string.focus_pause),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Micro-Adjustment & Customization Row (-5m, Adjust Duration, +5m)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onAdjustMinutes(-5) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(36.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(stringResource(R.string.pomodoro_sub_5m), style = MaterialTheme.typography.labelSmall)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = { showDurationDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("pomodoro_adjust_btn")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.pomodoro_customize_btn),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedButton(
                                onClick = { onAdjustMinutes(5) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(36.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(stringResource(R.string.pomodoro_add_5m), style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Timer Controls (Reset / Start / Pause)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onResetTimer,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("pomodoro_reset_btn")
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.focus_reset),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = if (isTimerRunning) onPauseTimer else onStartTimer,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTimerRunning) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                    contentColor = if (isTimerRunning) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .height(54.dp)
                                    .width(170.dp)
                                    .testTag("pomodoro_start_pause_btn")
                            ) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTimerRunning) stringResource(R.string.focus_pause) else stringResource(R.string.focus_start),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Top Single Task
            item {
                if (topTask != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("minimalist_top_task_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_hero_badge),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                PriorityBadge(priority = topTask.priority)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = topTask.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (topTask.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = topTask.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val subtasks = topTask.getSubtasks()
                            if (subtasks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(8.dp)
                                ) {
                                    subtasks.forEach { sub ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onToggleSubtask?.invoke(topTask, sub.id) }
                                                .padding(horizontal = 6.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SatisfyingCheckbox(
                                                checked = sub.isCompleted,
                                                onCheckedChange = { onToggleSubtask?.invoke(topTask, sub.id) },
                                                checkedColor = MaterialTheme.colorScheme.primary,
                                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = sub.title,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                                    color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onToggleTaskComplete(topTask) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("minimalist_complete_task_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.focus_complete_btn),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.tasks_empty_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.zen_no_task),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Quick Brain Dump Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = quickNoteText,
                            onValueChange = { quickNoteText = it },
                            placeholder = {
                                Text(
                                    stringResource(R.string.notes_brain_dump_hint),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (quickNoteText.isNotBlank()) {
                                    onQuickBrainDump(quickNoteText.trim())
                                    quickNoteText = ""
                                }
                            },
                            enabled = quickNoteText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = stringResource(R.string.btn_add),
                                tint = if (quickNoteText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Custom Duration Customization Dialog
    if (showDurationDialog) {
        PomodoroCustomDurationDialog(
            currentMode = pomodoroMode,
            currentFocusMinutes = customFocusMinutes,
            currentShortBreakMinutes = customShortBreakMinutes,
            currentLongBreakMinutes = customLongBreakMinutes,
            onDismiss = { showDurationDialog = false },
            onConfirm = { mode, minutesValue ->
                onSetCustomDuration(mode, minutesValue)
                showDurationDialog = false
            }
        )
    }
}

@Composable
fun PomodoroCustomDurationDialog(
    currentMode: PomodoroMode,
    currentFocusMinutes: Int,
    currentShortBreakMinutes: Int,
    currentLongBreakMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (PomodoroMode, Int) -> Unit
) {
    var selectedTargetMode by remember { mutableStateOf(currentMode) }
    var selectedMinutes by remember(selectedTargetMode) {
        mutableFloatStateOf(
            when (selectedTargetMode) {
                PomodoroMode.FOCUS -> currentFocusMinutes.toFloat()
                PomodoroMode.SHORT_BREAK -> currentShortBreakMinutes.toFloat()
                PomodoroMode.LONG_BREAK -> currentLongBreakMinutes.toFloat()
            }
        )
    }

    val presets = when (selectedTargetMode) {
        PomodoroMode.FOCUS -> listOf(15, 20, 25, 30, 45, 50, 60, 90)
        PomodoroMode.SHORT_BREAK -> listOf(3, 5, 8, 10, 15)
        PomodoroMode.LONG_BREAK -> listOf(10, 15, 20, 25, 30)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.pomodoro_custom_dialog_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mode selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PomodoroMode.entries.forEach { mode ->
                        val isSel = selectedTargetMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable {
                                    selectedTargetMode = mode
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(mode.labelRes),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display selected duration
                Text(
                    text = "${selectedMinutes.toInt()} ${stringResource(R.string.pomodoro_minutes_label)}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Slider
                val maxMinutes = if (selectedTargetMode == PomodoroMode.FOCUS) 120f else 45f
                val minMinutes = 1f
                Slider(
                    value = selectedMinutes,
                    onValueChange = { selectedMinutes = it },
                    valueRange = minMinutes..maxMinutes,
                    steps = (maxMinutes - minMinutes).toInt() - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Preset quick chips
                Text(
                    text = stringResource(R.string.pomodoro_custom_time),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(4).forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedMinutes.toInt() == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedMinutes = preset.toFloat() }
                        ) {
                            Text(
                                text = "$preset m",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedMinutes.toInt() == preset) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedMinutes.toInt() == preset) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedTargetMode, selectedMinutes.toInt()) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
