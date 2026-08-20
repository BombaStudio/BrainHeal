package com.emirozturk.brainheal.util

import android.content.Context
import android.net.Uri
import com.emirozturk.brainheal.data.model.CalendarEventEntity
import com.emirozturk.brainheal.data.model.EnergyLevel
import com.emirozturk.brainheal.data.model.ExpenseEntity
import com.emirozturk.brainheal.data.model.NoteChecklistItem
import com.emirozturk.brainheal.data.model.NoteEntity
import com.emirozturk.brainheal.data.model.SubTask
import com.emirozturk.brainheal.data.model.TaskEntity
import com.emirozturk.brainheal.data.model.TaskPriority
import com.emirozturk.brainheal.data.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

data class ParsedBackupData(
    val appName: String = "Brain Heal",
    val version: Int = 2,
    val exportDate: String = "",
    val tasks: List<TaskEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val calendarEvents: List<CalendarEventEntity> = emptyList()
) {
    val totalCount: Int
        get() = tasks.size + notes.size + expenses.size + calendarEvents.size
}

object BackupJsonImporter {

    /**
     * Reads the text content from a selected content Uri (e.g. from File Picker)
     */
    fun readTextFromUri(context: Context, uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open file stream"))
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
            reader.close()
            inputStream.close()
            Result.success(stringBuilder.toString().trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses and validates JSON backup content
     */
    fun parseBackupJson(rawJson: String): Result<ParsedBackupData> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("JSON metni boş"))
        }

        return try {
            val root = if (trimmed.startsWith("{")) {
                JSONObject(trimmed)
            } else if (trimmed.startsWith("[")) {
                // If an array was provided directly, determine root structure
                val arr = JSONArray(trimmed)
                JSONObject().apply { put("tasks", arr) }
            } else {
                return Result.failure(IllegalArgumentException("Geçersiz JSON formatı. '{...}' şeklinde bir JSON nesnesi bekleniyor."))
            }

            val appName = root.optString("appName", "Brain Heal")
            val version = root.optInt("version", 1)
            val exportDate = root.optString("exportDate", "")

            // 1. Parse Tasks
            val parsedTasks = mutableListOf<TaskEntity>()
            if (root.has("tasks")) {
                val tasksArray = root.optJSONArray("tasks")
                if (tasksArray != null) {
                    for (i in 0 until tasksArray.length()) {
                        val obj = tasksArray.optJSONObject(i) ?: continue
                        val title = obj.optString("title", "").trim()
                        if (title.isEmpty()) continue

                        val priority = try {
                            TaskPriority.valueOf(obj.optString("priority", TaskPriority.IMPORTANT.name))
                        } catch (e: Exception) {
                            TaskPriority.IMPORTANT
                        }

                        val energyLevel = try {
                            EnergyLevel.valueOf(obj.optString("energyLevel", EnergyLevel.MEDIUM.name))
                        } catch (e: Exception) {
                            EnergyLevel.MEDIUM
                        }

                        val subtasksList = mutableListOf<SubTask>()
                        if (obj.has("subtasks")) {
                            val subArr = obj.optJSONArray("subtasks")
                            if (subArr != null) {
                                for (j in 0 until subArr.length()) {
                                    val subObj = subArr.optJSONObject(j) ?: continue
                                    val subTitle = subObj.optString("title", "").trim()
                                    if (subTitle.isNotEmpty()) {
                                        subtasksList.add(
                                            SubTask(
                                                id = subObj.optString("id", UUID.randomUUID().toString()),
                                                title = subTitle,
                                                isCompleted = subObj.optBoolean("isCompleted", false)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        val subtasksRaw = if (subtasksList.isNotEmpty()) {
                            TaskEntity.encodeSubtasks(subtasksList)
                        } else {
                            obj.optString("subtasksRaw", "")
                        }

                        val task = TaskEntity(
                            id = obj.optLong("id", 0),
                            title = title,
                            description = obj.optString("description", ""),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            priority = priority,
                            energyLevel = energyLevel,
                            estimatedMinutes = obj.optInt("estimatedMinutes", 25),
                            category = obj.optString("category", "Genel"),
                            subtasksRaw = subtasksRaw,
                            dueDate = if (obj.has("dueDate") && !obj.isNull("dueDate")) obj.optLong("dueDate") else null,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            completedAt = if (obj.has("completedAt") && !obj.isNull("completedAt")) obj.optLong("completedAt") else null
                        )

                        parsedTasks.add(task)
                    }
                }
            }

            // 2. Parse Notes
            val parsedNotes = mutableListOf<NoteEntity>()
            if (root.has("notes")) {
                val notesArray = root.optJSONArray("notes")
                if (notesArray != null) {
                    for (i in 0 until notesArray.length()) {
                        val obj = notesArray.optJSONObject(i) ?: continue
                        val title = obj.optString("title", "").trim()
                        val content = obj.optString("content", "")
                        if (title.isEmpty() && content.isEmpty() && !obj.has("checklistItems")) continue

                        val checklistList = mutableListOf<NoteChecklistItem>()
                        if (obj.has("checklistItems")) {
                            val checkArr = obj.optJSONArray("checklistItems")
                            if (checkArr != null) {
                                for (j in 0 until checkArr.length()) {
                                    val itemObj = checkArr.optJSONObject(j) ?: continue
                                    val itemText = itemObj.optString("text", "").trim()
                                    if (itemText.isNotEmpty()) {
                                        checklistList.add(
                                            NoteChecklistItem(
                                                id = itemObj.optString("id", UUID.randomUUID().toString()),
                                                text = itemText,
                                                isChecked = itemObj.optBoolean("isChecked", false)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        val checklistRaw = if (checklistList.isNotEmpty()) {
                            NoteEntity.encodeChecklist(checklistList)
                        } else {
                            obj.optString("checklistRaw", "")
                        }

                        val note = NoteEntity(
                            id = obj.optLong("id", 0),
                            title = title.ifEmpty { "Not" },
                            content = content,
                            category = obj.optString("category", "Fikirler"),
                            colorHex = obj.optLong("colorHex", 0xFF0D9488),
                            isPinned = obj.optBoolean("isPinned", false),
                            isChecklist = obj.optBoolean("isChecklist", checklistList.isNotEmpty()),
                            checklistRaw = checklistRaw,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )

                        parsedNotes.add(note)
                    }
                }
            }

            // 3. Parse Expenses & Budget
            val parsedExpenses = mutableListOf<ExpenseEntity>()
            if (root.has("expenses")) {
                val expenseArray = root.optJSONArray("expenses")
                if (expenseArray != null) {
                    for (i in 0 until expenseArray.length()) {
                        val obj = expenseArray.optJSONObject(i) ?: continue
                        val title = obj.optString("title", "").trim()
                        val amount = obj.optDouble("amount", 0.0)
                        if (title.isEmpty() && amount <= 0.0) continue

                        val type = try {
                            TransactionType.valueOf(obj.optString("type", TransactionType.EXPENSE.name))
                        } catch (e: Exception) {
                            TransactionType.EXPENSE
                        }

                        val expense = ExpenseEntity(
                            id = obj.optLong("id", 0),
                            title = title,
                            amount = amount,
                            type = type,
                            category = obj.optString("category", "Genel"),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            note = obj.optString("note", ""),
                            isImpulseWishlist = obj.optBoolean("isImpulseWishlist", false),
                            coolingHours = obj.optInt("coolingHours", 48),
                            impulseAddedAt = obj.optLong("impulseAddedAt", System.currentTimeMillis()),
                            isImpulsePurchased = obj.optBoolean("isImpulsePurchased", false),
                            isImpulseCancelled = obj.optBoolean("isImpulseCancelled", false)
                        )

                        parsedExpenses.add(expense)
                    }
                }
            }

            // 4. Parse Calendar Events
            val parsedEvents = mutableListOf<CalendarEventEntity>()
            if (root.has("calendarEvents") || root.has("events")) {
                val eventArray = root.optJSONArray("calendarEvents") ?: root.optJSONArray("events")
                if (eventArray != null) {
                    for (i in 0 until eventArray.length()) {
                        val obj = eventArray.optJSONObject(i) ?: continue
                        val title = obj.optString("title", "").trim()
                        if (title.isEmpty()) continue

                        val event = CalendarEventEntity(
                            id = obj.optLong("id", 0),
                            title = title,
                            description = obj.optString("description", ""),
                            startTimestamp = obj.optLong("startTimestamp", System.currentTimeMillis()),
                            endTimestamp = obj.optLong("endTimestamp", System.currentTimeMillis() + 3600000),
                            colorHex = obj.optLong("colorHex", 0xFF0D9488),
                            category = obj.optString("category", "Genel"),
                            isAllDay = obj.optBoolean("isAllDay", false),
                            location = obj.optString("location", "")
                        )

                        parsedEvents.add(event)
                    }
                }
            }

            val result = ParsedBackupData(
                appName = appName,
                version = version,
                exportDate = exportDate,
                tasks = parsedTasks,
                notes = parsedNotes,
                expenses = parsedExpenses,
                calendarEvents = parsedEvents
            )

            if (result.totalCount == 0) {
                return Result.failure(IllegalArgumentException("Yedek dosyasında içe aktarılacak geçerli görev, not, harcama veya takvim verisi bulunamadı."))
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(Exception("JSON çözümlenirken hata oluştu: ${e.localizedMessage ?: e.message}"))
        }
    }
}
