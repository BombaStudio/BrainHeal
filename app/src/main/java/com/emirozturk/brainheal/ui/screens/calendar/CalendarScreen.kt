package com.emirozturk.brainheal.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirozturk.brainheal.R
import com.emirozturk.brainheal.data.model.CalendarEventEntity
import com.emirozturk.brainheal.data.model.TaskEntity
import com.emirozturk.brainheal.ui.components.SatisfyingCheckbox
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CalendarViewMode {
    MONTH_GRID, // 30-day rows and columns grid
    WEEK_STRIP  // 14-day horizontal strip
}

data class DayCellInfo(
    val calendar: Calendar,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val dayNumber: Int,
    val taskCount: Int,
    val eventCount: Int
)

@Composable
fun CalendarScreen(
    allEvents: List<CalendarEventEntity>,
    allTasks: List<TaskEntity>,
    selectedDate: Calendar,
    onSelectDate: (Calendar) -> Unit,
    onSaveEvent: (CalendarEventEntity) -> Unit,
    onDeleteEvent: (CalendarEventEntity) -> Unit,
    onToggleCompleteTask: (TaskEntity) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<CalendarEventEntity?>(null) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH_GRID) }

    // Month currently displayed in the 30-day grid
    var displayedMonth by remember {
        val cal = selectedDate.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        mutableStateOf(cal)
    }

    val selectedDateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(selectedDate.time)
    val dayEvents = allEvents.filter {
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.startTimestamp)) == selectedDateStr
    }.sortedBy { it.startTimestamp }

    val dayTasks = allTasks.filter { task ->
        task.dueDate != null && SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(task.dueDate)) == selectedDateStr
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val headerDateFormat = SimpleDateFormat("d MMMM yyyy, EEEE", Locale.getDefault())

    // 14 days strip centered around today for alternate view
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

    Scaffold(
        modifier = Modifier.testTag("calendar_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    eventToEdit = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_event_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add), modifier = Modifier.size(28.dp))
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
            // Header: Title & View Mode Toggle (Stacked vertically)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.calendar_hero_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.calendar_hero_title),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // View Mode Toggle (30-Day Grid vs Strip) - Stacked underneath
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(11.dp),
                                color = if (viewMode == CalendarViewMode.MONTH_GRID) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(11.dp))
                                    .clickable { viewMode = CalendarViewMode.MONTH_GRID }
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.GridView,
                                        contentDescription = stringResource(R.string.calendar_view_month),
                                        tint = if (viewMode == CalendarViewMode.MONTH_GRID) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.calendar_view_month),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (viewMode == CalendarViewMode.MONTH_GRID) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(11.dp),
                                color = if (viewMode == CalendarViewMode.WEEK_STRIP) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(11.dp))
                                    .clickable { viewMode = CalendarViewMode.WEEK_STRIP }
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ViewWeek,
                                        contentDescription = stringResource(R.string.calendar_view_week),
                                        tint = if (viewMode == CalendarViewMode.WEEK_STRIP) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.calendar_view_week),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (viewMode == CalendarViewMode.WEEK_STRIP) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 30-Day Grid or 14-Day Strip View
            item {
                if (viewMode == CalendarViewMode.MONTH_GRID) {
                    MonthGridCalendarCard(
                        displayedMonth = displayedMonth,
                        selectedDate = selectedDate,
                        allTasks = allTasks,
                        allEvents = allEvents,
                        onSelectDate = { cal ->
                            onSelectDate(cal)
                            // Keep displayedMonth in sync if selecting day outside current month
                            if (cal.get(Calendar.MONTH) != displayedMonth.get(Calendar.MONTH) ||
                                cal.get(Calendar.YEAR) != displayedMonth.get(Calendar.YEAR)) {
                                val nextM = cal.clone() as Calendar
                                nextM.set(Calendar.DAY_OF_MONTH, 1)
                                displayedMonth = nextM
                            }
                        },
                        onPrevMonth = {
                            val prev = displayedMonth.clone() as Calendar
                            prev.add(Calendar.MONTH, -1)
                            displayedMonth = prev
                        },
                        onNextMonth = {
                            val next = displayedMonth.clone() as Calendar
                            next.add(Calendar.MONTH, 1)
                            displayedMonth = next
                        },
                        onToday = {
                            val today = Calendar.getInstance()
                            onSelectDate(today)
                            val thisMonth = today.clone() as Calendar
                            thisMonth.set(Calendar.DAY_OF_MONTH, 1)
                            displayedMonth = thisMonth
                        }
                    )
                } else {
                    // 14-day horizontal strip
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(daysList, key = { "cal_strip_${it.get(Calendar.DAY_OF_YEAR)}_${it.get(Calendar.YEAR)}" }) { cal ->
                            val isSelected = isSameDay(cal, selectedDate)
                            val isTodayCal = isToday(cal.timeInMillis)
                            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time).uppercase()
                            val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(cal.time)
                            val calDateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
                            val tasksForThisDay = allTasks.count {
                                it.dueDate != null && SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.dueDate)) == calDateStr
                            }
                            val eventsForThisDay = allEvents.count {
                                SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.startTimestamp)) == calDateStr
                            }

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
                                    .clickable { onSelectDate(cal) }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isTodayCal || isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else if (isTodayCal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dayNum,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (tasksForThisDay > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                        if (eventsForThisDay > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary)
                                            )
                                        }
                                        if (tasksForThisDay == 0 && eventsForThisDay == 0) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Date Summary Bar
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = headerDateFormat.format(selectedDate.time),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = stringResource(R.string.calendar_day_summary, dayTasks.size, dayEvents.size),
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Tasks due on this date
            if (dayTasks.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.calendar_due_tasks_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(dayTasks, key = { "cal_task_${it.id}" }) { t ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SatisfyingCheckbox(
                                checked = t.isCompleted,
                                onCheckedChange = { onToggleCompleteTask(t) },
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = t.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (t.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                                    ),
                                    color = if (t.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${stringResource(R.string.task_minutes_format, t.estimatedMinutes)} • ${stringResource(t.energyLevel.stringRes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Time-blocked Events of the Day
            item {
                Text(
                    text = stringResource(R.string.calendar_events_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (dayEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.calendar_empty_events_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.calendar_empty_events_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(dayEvents, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_item_${event.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color indicator strip
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(event.colorHex))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(event.colorHex).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = com.emirozturk.brainheal.ui.util.CategoryUtils.getLocalizedCategoryName(event.category),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(event.colorHex),
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "${timeFormat.format(Date(event.startTimestamp))} - ${timeFormat.format(Date(event.endTimestamp))}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (event.description.isNotBlank()) {
                                    Text(
                                        text = event.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(onClick = { onDeleteEvent(event) }) {
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

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showDialog) {
        EventDialog(
            selectedDate = selectedDate,
            eventToEdit = eventToEdit,
            onDismiss = {
                showDialog = false
                eventToEdit = null
            },
            onSave = { saved ->
                onSaveEvent(saved)
                showDialog = false
                eventToEdit = null
            }
        )
    }
}

/**
 * 30-day Month Grid Calendar Card (7 Columns x 5-6 Rows)
 */
@Composable
fun MonthGridCalendarCard(
    displayedMonth: Calendar,
    selectedDate: Calendar,
    allTasks: List<TaskEntity>,
    allEvents: List<CalendarEventEntity>,
    onSelectDate: (Calendar) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit
) {
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val rawMonthTitle = monthYearFormat.format(displayedMonth.time)
    val monthTitle = rawMonthTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    // Column days (Monday to Sunday)
    val symbols = remember { java.text.DateFormatSymbols.getInstance(Locale.getDefault()) }
    val weekDayHeaders = remember(symbols) {
        listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        ).map { dayOfWeek ->
            symbols.shortWeekdays[dayOfWeek].take(3)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    // Build the grid cells for 30/31 days of the month with proper offsets
    val gridCells = remember(displayedMonth.get(Calendar.YEAR), displayedMonth.get(Calendar.MONTH), selectedDate, allTasks, allEvents) {
        calculateMonthGridCells(displayedMonth, selectedDate, allTasks, allEvents)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("month_grid_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = stringResource(R.string.calendar_prev_month),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToday() }
                    ) {
                        Text(
                            text = stringResource(R.string.calendar_today_btn),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.calendar_next_month),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7-Column Day Names Header (Pzt, Sal, Çar, Per, Cum, Cmt, Paz)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDayHeaders.forEach { header ->
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rows and Columns (7 columns x N rows)
            val rows = gridCells.chunked(7)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rows.forEach { rowCells ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        rowCells.forEach { cell ->
                            MonthGridDayCell(
                                cell = cell,
                                onClick = { onSelectDate(cell.calendar) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend dots (Tasks & Events)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.nav_tasks),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE65100)) // Amber event dot
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.nav_calendar),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MonthGridDayCell(
    cell: DayCellInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = cell.isSelected
    val isToday = cell.isToday
    val isCurrentMonth = cell.isCurrentMonth

    val cellBackground = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else -> Color.Transparent
    }

    val cellBorder = when {
        isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        else -> null
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cellBackground,
        border = cellBorder,
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${cell.dayNumber}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )

            // Indicators row for task and event
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cell.taskCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                    )
                }
                if (cell.eventCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFFE65100))
                    )
                }
                if (cell.taskCount == 0 && cell.eventCount == 0) {
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}

private fun calculateMonthGridCells(
    displayedMonth: Calendar,
    selectedDate: Calendar,
    allTasks: List<TaskEntity>,
    allEvents: List<CalendarEventEntity>
): List<DayCellInfo> {
    val cells = mutableListOf<DayCellInfo>()
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    val workCal = displayedMonth.clone() as Calendar
    workCal.set(Calendar.DAY_OF_MONTH, 1)

    // In Java Calendar, Sunday = 1, Monday = 2, ..., Saturday = 7
    // Monday-start index: Monday = 0, ..., Sunday = 6
    val firstDayOfWeek = workCal.get(Calendar.DAY_OF_WEEK)
    val mondayOffset = (firstDayOfWeek + 5) % 7

    // Trailing days from previous month
    val prevMonthCal = workCal.clone() as Calendar
    prevMonthCal.add(Calendar.MONTH, -1)
    val maxDaysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    for (i in (maxDaysInPrevMonth - mondayOffset + 1)..maxDaysInPrevMonth) {
        val cellCal = prevMonthCal.clone() as Calendar
        cellCal.set(Calendar.DAY_OF_MONTH, i)
        val dateStr = dateFormat.format(cellCal.time)
        val taskCount = allTasks.count { it.dueDate != null && dateFormat.format(Date(it.dueDate)) == dateStr }
        val eventCount = allEvents.count { dateFormat.format(Date(it.startTimestamp)) == dateStr }

        cells.add(
            DayCellInfo(
                calendar = cellCal,
                isCurrentMonth = false,
                isToday = isToday(cellCal.timeInMillis),
                isSelected = isSameDay(cellCal, selectedDate),
                dayNumber = i,
                taskCount = taskCount,
                eventCount = eventCount
            )
        )
    }

    // Days of current month (1..30/31)
    val maxDaysInCurrentMonth = workCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 1..maxDaysInCurrentMonth) {
        val cellCal = workCal.clone() as Calendar
        cellCal.set(Calendar.DAY_OF_MONTH, i)
        val dateStr = dateFormat.format(cellCal.time)
        val taskCount = allTasks.count { it.dueDate != null && dateFormat.format(Date(it.dueDate)) == dateStr }
        val eventCount = allEvents.count { dateFormat.format(Date(it.startTimestamp)) == dateStr }

        cells.add(
            DayCellInfo(
                calendar = cellCal,
                isCurrentMonth = true,
                isToday = isToday(cellCal.timeInMillis),
                isSelected = isSameDay(cellCal, selectedDate),
                dayNumber = i,
                taskCount = taskCount,
                eventCount = eventCount
            )
        )
    }

    // Leading days from next month to complete the last row
    val totalCells = cells.size
    val remainingCells = if (totalCells % 7 != 0) 7 - (totalCells % 7) else 0
    val nextMonthCal = workCal.clone() as Calendar
    nextMonthCal.add(Calendar.MONTH, 1)

    for (i in 1..remainingCells) {
        val cellCal = nextMonthCal.clone() as Calendar
        cellCal.set(Calendar.DAY_OF_MONTH, i)
        val dateStr = dateFormat.format(cellCal.time)
        val taskCount = allTasks.count { it.dueDate != null && dateFormat.format(Date(it.dueDate)) == dateStr }
        val eventCount = allEvents.count { dateFormat.format(Date(it.startTimestamp)) == dateStr }

        cells.add(
            DayCellInfo(
                calendar = cellCal,
                isCurrentMonth = false,
                isToday = isToday(cellCal.timeInMillis),
                isSelected = isSameDay(cellCal, selectedDate),
                dayNumber = i,
                taskCount = taskCount,
                eventCount = eventCount
            )
        )
    }

    return cells
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isToday(timestamp: Long): Boolean {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()
    return isSameDay(cal, today)
}
