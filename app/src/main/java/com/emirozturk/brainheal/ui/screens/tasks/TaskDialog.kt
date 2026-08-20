package com.emirozturk.brainheal.ui.screens.tasks

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirozturk.brainheal.R
import com.emirozturk.brainheal.data.model.EnergyLevel
import com.emirozturk.brainheal.data.model.SubTask
import com.emirozturk.brainheal.data.model.TaskEntity
import com.emirozturk.brainheal.data.model.TaskPriority
import java.util.UUID

private data class TaskCategoryOption(val id: String, @StringRes val labelRes: Int)

private val taskCategoryOptions = listOf(
    TaskCategoryOption("general", R.string.category_general),
    TaskCategoryOption("work", R.string.category_work),
    TaskCategoryOption("personal", R.string.category_personal),
    TaskCategoryOption("health", R.string.category_health),
    TaskCategoryOption("study", R.string.category_study)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDialog(
    taskToEdit: TaskEntity? = null,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: TaskPriority.IMPORTANT) }
    var energyLevel by remember { mutableStateOf(taskToEdit?.energyLevel ?: EnergyLevel.MEDIUM) }
    var estimatedMinutes by remember { mutableIntStateOf(taskToEdit?.estimatedMinutes ?: 25) }
    var category by remember {
        mutableStateOf(
            taskToEdit?.category?.lowercase()?.let { cat ->
                when (cat) {
                    "general", "genel" -> "general"
                    "work", "iş", "is" -> "work"
                    "personal", "kişisel", "kisisel" -> "personal"
                    "health", "sağlık", "saglik" -> "health"
                    "study", "ders", "çalışma", "calisma" -> "study"
                    else -> cat
                }
            } ?: "general"
        )
    }
    var dueDateMillis by remember { mutableStateOf<Long?>(taskToEdit?.dueDate ?: System.currentTimeMillis()) }

    val subtasks = remember {
        mutableStateListOf<SubTask>().apply {
            addAll(taskToEdit?.getSubtasks() ?: emptyList())
        }
    }
    var newSubtaskText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("task_dialog"),
        title = {
            Text(
                text = if (taskToEdit == null) stringResource(R.string.tasks_add_title) else stringResource(R.string.tasks_edit_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.task_title_hint)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.task_desc_hint)) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Category Selection
                item {
                    Text(
                        text = stringResource(R.string.task_category_label) + ":",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        taskCategoryOptions.forEach { opt ->
                            FilterChip(
                                selected = category == opt.id,
                                onClick = { category = opt.id },
                                label = { Text(stringResource(opt.labelRes), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Energy Level (ADHD Vital)
                item {
                    Text(
                        text = stringResource(R.string.task_energy_label),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EnergyLevel.entries.forEach { level ->
                            FilterChip(
                                selected = energyLevel == level,
                                onClick = { energyLevel = level },
                                label = { Text(text = "${level.emoji} ${stringResource(level.shortStringRes)}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Priority
                item {
                    Text(
                        text = stringResource(R.string.task_priority_label),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TaskPriority.entries.forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(stringResource(p.stringRes), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Estimated Duration (Focus Blocks)
                item {
                    Text(
                        text = stringResource(R.string.task_est_minutes),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 15, 25, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = estimatedMinutes == mins,
                                onClick = { estimatedMinutes = mins },
                                label = { Text(stringResource(R.string.task_minutes_format, mins), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Scheduled Day / Due Date Selector
                item {
                    Text(
                        text = stringResource(R.string.task_scheduled_day_label) + ":",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val todayCal = java.util.Calendar.getInstance()
                    val tomorrowCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 1) }
                    val dayAfterCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 2) }

                    val isTodaySelected = dueDateMillis != null && isSameDayMillis(dueDateMillis!!, todayCal.timeInMillis)
                    val isTomorrowSelected = dueDateMillis != null && isSameDayMillis(dueDateMillis!!, tomorrowCal.timeInMillis)
                    val isDayAfterSelected = dueDateMillis != null && isSameDayMillis(dueDateMillis!!, dayAfterCal.timeInMillis)
                    val isNoDateSelected = dueDateMillis == null

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = isTodaySelected,
                            onClick = { dueDateMillis = todayCal.timeInMillis },
                            label = { Text(stringResource(R.string.task_scheduled_today), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        FilterChip(
                            selected = isTomorrowSelected,
                            onClick = { dueDateMillis = tomorrowCal.timeInMillis },
                            label = { Text(stringResource(R.string.task_scheduled_tomorrow), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        FilterChip(
                            selected = isDayAfterSelected,
                            onClick = { dueDateMillis = dayAfterCal.timeInMillis },
                            label = { Text(stringResource(R.string.task_scheduled_2days), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        FilterChip(
                            selected = isNoDateSelected,
                            onClick = { dueDateMillis = null },
                            label = { Text(stringResource(R.string.task_scheduled_none), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Subtasks / Micro-steps (Break down into small bite-sized steps)
                item {
                    Text(
                        text = stringResource(R.string.task_subtasks_section),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.task_subtasks_subtext),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubtaskText,
                            onValueChange = { newSubtaskText = it },
                            placeholder = { Text(stringResource(R.string.task_new_subtask_hint)) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("subtask_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newSubtaskText.isNotBlank()) {
                                    subtasks.add(
                                        SubTask(
                                            id = UUID.randomUUID().toString(),
                                            title = newSubtaskText.trim(),
                                            isCompleted = false
                                        )
                                    )
                                    newSubtaskText = ""
                                }
                            },
                            modifier = Modifier.testTag("add_subtask_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.task_add_subtask_btn), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                items(subtasks, key = { it.id }) { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = sub.isCompleted,
                            onCheckedChange = { checked ->
                                val index = subtasks.indexOf(sub)
                                if (index != -1) {
                                    subtasks[index] = sub.copy(isCompleted = checked)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        Text(
                            text = sub.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { subtasks.remove(sub) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.btn_delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val task = TaskEntity(
                            id = taskToEdit?.id ?: 0L,
                            title = title.trim(),
                            description = description.trim(),
                            priority = priority,
                            energyLevel = energyLevel,
                            estimatedMinutes = estimatedMinutes,
                            category = category,
                            dueDate = dueDateMillis,
                            subtasksRaw = TaskEntity.encodeSubtasks(subtasks),
                            isCompleted = taskToEdit?.isCompleted ?: false,
                            createdAt = taskToEdit?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(task)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("save_task_btn")
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

private fun isSameDayMillis(ms1: Long, ms2: Long): Boolean {
    val fmt = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
    return fmt.format(java.util.Date(ms1)) == fmt.format(java.util.Date(ms2))
}
