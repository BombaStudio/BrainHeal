package com.example.ui.screens.tasks

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.EnergyLevel
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.ui.components.EnergyLevelChip
import com.example.ui.components.PriorityBadge
import com.example.ui.components.SatisfyingCheckbox
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TasksScreen(
    activeTasksCount: Int,
    filteredActiveTasks: List<TaskEntity>,
    completedTasks: List<TaskEntity>,
    selectedEnergy: EnergyLevel?,
    selectedPriority: TaskPriority?,
    selectedDay: Calendar?,
    onSelectEnergy: (EnergyLevel?) -> Unit,
    onSelectPriority: (TaskPriority?) -> Unit,
    onSelectDay: (Calendar?) -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onToggleSubtask: (TaskEntity, String) -> Unit,
    onStartFocus: (TaskEntity) -> Unit,
    onSaveTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    var showTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var showCompletedSection by remember { mutableStateOf(false) }

    // Generate 14 days around today
    val daysList = remember {
        val list = mutableListOf<Calendar>()
        val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }
        for (i in 0 until 14) {
            val c = start.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, i)
            list.add(c)
        }
        list
    }

    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val headerDateFormat = SimpleDateFormat("d MMMM yyyy, EEEE", Locale.getDefault())

    Scaffold(
        modifier = Modifier.testTag("tasks_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskToEdit = null
                    showTaskDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.tasks_add_title), modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item(contentType = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.tasks_calendar_day_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.tasks_header_today),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Visual Day-by-Day Calendar Strip Component
            item(contentType = "calendar_strip") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // "All" chip
                        item {
                            val isAllSelected = selectedDay == null
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(78.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onSelectDay(null) }
                                    .testTag("day_picker_all")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        Icons.Default.Event,
                                        contentDescription = null,
                                        tint = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.tasks_calendar_all_days),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "$activeTasksCount",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Daily Day Cards
                        items(daysList, key = { "day_${it.get(Calendar.DAY_OF_YEAR)}_${it.get(Calendar.YEAR)}" }) { cal ->
                            val isSelected = selectedDay != null && isSameDay(cal, selectedDay)
                            val isTodayCal = isSameDay(cal, Calendar.getInstance())
                            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time).uppercase()
                            val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(cal.time)
                            val calDateStr = dateFormat.format(cal.time)

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else if (isTodayCal) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    if (isSelected || isTodayCal) 1.5.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else if (isTodayCal) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .width(58.dp)
                                    .height(78.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onSelectDay(cal) }
                                    .testTag("day_picker_${calDateStr}")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 7.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isTodayCal && !isSelected) stringResource(R.string.tasks_calendar_today) else dayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isTodayCal || isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 9.sp
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else if (isTodayCal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = dayNum,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f) else Color.Transparent)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selected Date Info Bar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedDay != null) {
                                        headerDateFormat.format(selectedDay.time)
                                    } else {
                                        stringResource(R.string.tasks_title)
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.active_tasks_count, filteredActiveTasks.size),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                if (selectedDay != null && !isSameDay(selectedDay, Calendar.getInstance())) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onSelectDay(Calendar.getInstance()) }
                                    ) {
                                        Text(
                                            text = stringResource(R.string.tasks_calendar_today),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top Focus Hero Card
            val topTask = filteredActiveTasks.firstOrNull()
            if (topTask != null) {
                item(contentType = "hero_card") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hero_focus_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.tasks_hero_next_task),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = topTask.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${stringResource(R.string.task_minutes_format, topTask.estimatedMinutes)} • ${stringResource(topTask.energyLevel.shortStringRes)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { onStartFocus(topTask) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayCircleFilled,
                                    contentDescription = stringResource(R.string.tasks_hero_btn),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ADHD Energy & Low-Friction Filter Row
            item(contentType = "energy_filter") {
                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = stringResource(R.string.tasks_energy_filter_title),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedEnergy == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectEnergy(null) }
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_filter_all_count, activeTasksCount),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (selectedEnergy == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }

                        items(EnergyLevel.entries, key = { it.name }) { level ->
                            EnergyLevelChip(
                                energyLevel = level,
                                isSelected = selectedEnergy == level,
                                onClick = { onSelectEnergy(level) }
                            )
                        }
                    }
                }
            }

            // Task List
            if (filteredActiveTasks.isEmpty()) {
                item(contentType = "empty_state") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (activeTasksCount == 0) stringResource(R.string.tasks_all_completed) else if (selectedDay != null) stringResource(R.string.tasks_calendar_empty_day) else stringResource(R.string.tasks_empty_filter_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (activeTasksCount == 0) stringResource(R.string.tasks_empty_desc) else stringResource(R.string.tasks_empty_filter_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredActiveTasks,
                    key = { it.id },
                    contentType = { "task_item" }
                ) { task ->
                    TaskItemCard(
                        task = task,
                        onToggleComplete = { onToggleComplete(task) },
                        onToggleSubtask = { subId -> onToggleSubtask(task, subId) },
                        onStartFocus = { onStartFocus(task) },
                        onEdit = {
                            taskToEdit = task
                            showTaskDialog = true
                        },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }

            // Completed Tasks Dropdown / Section
            if (completedTasks.isNotEmpty()) {
                item(contentType = "completed_header") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCompletedSection = !showCompletedSection }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.tasks_completed_header, completedTasks.size),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (showCompletedSection) stringResource(R.string.tasks_hide_btn) else stringResource(R.string.tasks_show_btn),
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                if (showCompletedSection) {
                    items(
                        items = completedTasks,
                        key = { "comp_${it.id}" },
                        contentType = { "completed_task_item" }
                    ) { compTask ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = true,
                                    onCheckedChange = { onToggleComplete(compTask) },
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = compTask.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = TextDecoration.LineThrough,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteTask(compTask) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = stringResource(R.string.btn_delete),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showTaskDialog) {
        TaskDialog(
            taskToEdit = taskToEdit,
            onDismiss = {
                showTaskDialog = false
                taskToEdit = null
            },
            onSave = { saved ->
                onSaveTask(saved)
                showTaskDialog = false
                taskToEdit = null
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onStartFocus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val subtasks = task.getSubtasks()
    val completedSubtasksCount = subtasks.count { it.isCompleted }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Checkbox + Title / Description + Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SatisfyingCheckbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded }
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (expanded) 6 else 2
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onStartFocus, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.PlayCircleFilled,
                            contentDescription = stringResource(R.string.focus_start),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.tasks_edit_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.btn_delete),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges / Tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityBadge(priority = task.priority)
                EnergyLevelChip(energyLevel = task.energyLevel)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "⏱️ ${stringResource(R.string.task_minutes_format, task.estimatedMinutes)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                if (task.dueDate != null) {
                    val isDueToday = isSameDayMillis(task.dueDate, System.currentTimeMillis())
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDueToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = if (isDueToday) "📅 ${stringResource(R.string.date_today)}" else SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(task.dueDate)),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isDueToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Micro subtasks progress bar
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val progress = completedSubtasksCount.toFloat() / subtasks.size.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.tasks_steps_progress,
                            completedSubtasksCount,
                            subtasks.size,
                            (progress * 100).toInt()
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (expanded) "▲" else "▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Subtasks checklist
            if (expanded && subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
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
                                .clickable { onToggleSubtask(sub.id) }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SatisfyingCheckbox(
                                checked = sub.isCompleted,
                                onCheckedChange = { onToggleSubtask(sub.id) },
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(26.dp)
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
        }
    }
}

private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
           c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun isSameDayMillis(ms1: Long, ms2: Long): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return fmt.format(Date(ms1)) == fmt.format(Date(ms2))
}
