package com.emirozturk.brainheal.ui.screens.calendar

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirozturk.brainheal.R
import com.emirozturk.brainheal.data.model.CalendarEventEntity
import java.util.Calendar
import java.util.Locale

private data class EventCategoryOption(val id: String, @StringRes val labelRes: Int)

private val eventCategoryOptions = listOf(
    EventCategoryOption("work", R.string.category_work),
    EventCategoryOption("personal", R.string.category_personal),
    EventCategoryOption("health", R.string.category_health),
    EventCategoryOption("focus_block", R.string.category_focus_block),
    EventCategoryOption("appointment", R.string.category_appointment),
    EventCategoryOption("other", R.string.category_other)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventDialog(
    selectedDate: Calendar,
    eventToEdit: CalendarEventEntity? = null,
    onDismiss: () -> Unit,
    onSave: (CalendarEventEntity) -> Unit
) {
    var title by remember { mutableStateOf(eventToEdit?.title ?: "") }
    var description by remember { mutableStateOf(eventToEdit?.description ?: "") }
    var category by remember {
        mutableStateOf(
            eventToEdit?.category?.lowercase()?.let { cat ->
                when (cat) {
                    "work", "iş", "is" -> "work"
                    "personal", "kişisel", "kisisel" -> "personal"
                    "health", "sağlık", "saglik" -> "health"
                    "focus block", "odak bloğu", "odak blogu", "focus_block" -> "focus_block"
                    "appointment", "randevu" -> "appointment"
                    "other", "diğer", "diger" -> "other"
                    else -> cat
                }
            } ?: "work"
        )
    }
    var colorHex by remember { mutableStateOf(eventToEdit?.colorHex ?: 0xFF9E8BFC) }
    
    var startHour by remember { mutableIntStateOf(10) }
    var durationHours by remember { mutableIntStateOf(1) }

    val colorOptions = listOf(
        0xFF9E8BFC, // Minimal Lavender
        0xFF3B82F6, // Blue
        0xFFF59E0B, // Amber
        0xFFEF4444, // Coral
        0xFF10B981, // Emerald
        0xFFEC4899  // Pink
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("event_dialog"),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (eventToEdit == null) stringResource(R.string.calendar_dialog_add_title) else stringResource(R.string.calendar_dialog_edit_title),
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
                        label = { Text(stringResource(R.string.calendar_dialog_title_label)) },
                        placeholder = { Text(stringResource(R.string.calendar_dialog_title_hint)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_title_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.calendar_dialog_desc_label)) },
                        placeholder = { Text(stringResource(R.string.calendar_dialog_desc_hint)) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Start Hour Selector
                item {
                    Text(
                        text = stringResource(R.string.calendar_dialog_start_hour),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20).forEach { hour ->
                            FilterChip(
                                selected = startHour == hour,
                                onClick = { startHour = hour },
                                label = { Text(String.format(Locale.getDefault(), "%02d:00", hour), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Duration Selector
                item {
                    Text(
                        text = stringResource(R.string.calendar_dialog_duration),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3).forEach { h ->
                            FilterChip(
                                selected = durationHours == h,
                                onClick = { durationHours = h },
                                label = { Text(stringResource(R.string.calendar_duration_hours, h), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Color Palette
                item {
                    Text(
                        text = stringResource(R.string.calendar_dialog_color),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colorOptions.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .clickable { colorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (colorHex == hex) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Chips
                item {
                    Text(
                        text = stringResource(R.string.calendar_dialog_category),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        eventCategoryOptions.forEach { opt ->
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val calStart = (selectedDate.clone() as Calendar).apply {
                            set(Calendar.HOUR_OF_DAY, startHour)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }
                        val calEnd = (calStart.clone() as Calendar).apply {
                            add(Calendar.HOUR_OF_DAY, durationHours)
                        }

                        val event = CalendarEventEntity(
                            id = eventToEdit?.id ?: 0L,
                            title = title.trim(),
                            description = description.trim(),
                            startTimestamp = calStart.timeInMillis,
                            endTimestamp = calEnd.timeInMillis,
                            colorHex = colorHex,
                            category = category
                        )
                        onSave(event)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("save_event_btn")
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

