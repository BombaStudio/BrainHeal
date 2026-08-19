package com.example.ui.screens.notes

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.R
import com.example.data.model.NoteChecklistItem
import com.example.data.model.NoteEntity
import java.util.UUID

private data class NoteCategoryOption(val id: String, @StringRes val labelRes: Int)

private val noteCategoryOptions = listOf(
    NoteCategoryOption("ideas", R.string.category_ideas),
    NoteCategoryOption("personal", R.string.category_personal),
    NoteCategoryOption("work", R.string.category_work),
    NoteCategoryOption("reminder", R.string.category_reminder),
    NoteCategoryOption("projects", R.string.category_projects),
    NoteCategoryOption("shopping", R.string.category_shopping)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteDialog(
    noteToEdit: NoteEntity? = null,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit
) {
    var title by remember { mutableStateOf(noteToEdit?.title ?: "") }
    var content by remember { mutableStateOf(noteToEdit?.content ?: "") }
    var category by remember {
        mutableStateOf(
            noteToEdit?.category?.lowercase()?.let { cat ->
                when (cat) {
                    "ideas", "fikirler", "fikir" -> "ideas"
                    "personal", "kişisel", "kisisel" -> "personal"
                    "work", "iş", "is" -> "work"
                    "reminder", "hatırlatıcı", "hatirlatici" -> "reminder"
                    "projects", "projeler", "proje" -> "projects"
                    "shopping", "alışveriş", "alisveris" -> "shopping"
                    else -> cat
                }
            } ?: "ideas"
        )
    }
    var colorHex by remember { mutableStateOf(noteToEdit?.colorHex ?: 0xFF9E8BFC) }
    var isPinned by remember { mutableStateOf(noteToEdit?.isPinned ?: false) }
    var isChecklist by remember { mutableStateOf(noteToEdit?.isChecklist ?: false) }

    val checklistItems = remember {
        mutableStateListOf<NoteChecklistItem>().apply {
            addAll(noteToEdit?.getChecklistItems() ?: emptyList())
        }
    }
    var newChecklistText by remember { mutableStateOf("") }

    val colorOptions = listOf(
        0xFF9E8BFC, // Minimal Lavender
        0xFF3B82F6, // Blue
        0xFFF59E0B, // Amber
        0xFFEF4444, // Coral
        0xFF10B981, // Emerald
        0xFF1E293B  // Slate
    )

    val fallbackNote = stringResource(R.string.note_fallback_title)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("note_dialog"),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (noteToEdit == null) stringResource(R.string.notes_dialog_add_title) else stringResource(R.string.notes_dialog_edit_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { isPinned = !isPinned }) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = stringResource(R.string.notes_pin),
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
                        label = { Text(stringResource(R.string.notes_dialog_title_label)) },
                        placeholder = { Text(stringResource(R.string.notes_dialog_title_hint)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input")
                    )
                }

                // Toggle Checklist Mode
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.notes_dialog_checklist_mode),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = isChecklist,
                            onCheckedChange = { isChecklist = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                if (!isChecklist) {
                    item {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text(stringResource(R.string.notes_dialog_content_label)) },
                            placeholder = { Text(stringResource(R.string.notes_dialog_content_hint)) },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newChecklistText,
                                onValueChange = { newChecklistText = it },
                                placeholder = { Text(stringResource(R.string.notes_dialog_add_item_hint)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (newChecklistText.isNotBlank()) {
                                        checklistItems.add(
                                            NoteChecklistItem(
                                                id = UUID.randomUUID().toString(),
                                                text = newChecklistText.trim(),
                                                isChecked = false
                                            )
                                        )
                                        newChecklistText = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    items(checklistItems, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { checked ->
                                    val index = checklistItems.indexOf(item)
                                    if (index != -1) {
                                        checklistItems[index] = item.copy(isChecked = checked)
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { checklistItems.remove(item) },
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

                // Category selection
                item {
                    Text(
                        text = stringResource(R.string.notes_dialog_category),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        noteCategoryOptions.forEach { opt ->
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

                // Color selection
                item {
                    Text(
                        text = stringResource(R.string.notes_dialog_color),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colorOptions.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank() || checklistItems.isNotEmpty()) {
                        val finalTitle = if (title.isBlank()) {
                            content.take(25) + if (content.length > 25) "..." else fallbackNote
                        } else title.trim()

                        val note = NoteEntity(
                            id = noteToEdit?.id ?: 0L,
                            title = finalTitle,
                            content = content.trim(),
                            category = category,
                            colorHex = colorHex,
                            isPinned = isPinned,
                            isChecklist = isChecklist,
                            checklistRaw = if (isChecklist) NoteEntity.encodeChecklist(checklistItems) else "",
                            createdAt = noteToEdit?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(note)
                    }
                },
                enabled = title.isNotBlank() || content.isNotBlank() || checklistItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("save_note_btn")
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

