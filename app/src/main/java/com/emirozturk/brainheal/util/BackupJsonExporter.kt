package com.emirozturk.brainheal.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.emirozturk.brainheal.data.model.NoteEntity
import com.emirozturk.brainheal.data.model.TaskEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupJsonExporter {

    fun exportToJsonString(
        tasks: List<TaskEntity>,
        notes: List<NoteEntity>,
        expenses: List<com.emirozturk.brainheal.data.model.ExpenseEntity> = emptyList(),
        calendarEvents: List<com.emirozturk.brainheal.data.model.CalendarEventEntity> = emptyList()
    ): String {
        val root = JSONObject()
        val prettyDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        root.put("appName", "Brain Heal")
        root.put("version", 2)
        root.put("exportDate", prettyDate)
        root.put("exportTimestamp", System.currentTimeMillis())

        val tasksArray = JSONArray()
        for (task in tasks) {
            val taskObj = JSONObject()
            taskObj.put("id", task.id)
            taskObj.put("title", task.title)
            taskObj.put("description", task.description)
            taskObj.put("isCompleted", task.isCompleted)
            taskObj.put("priority", task.priority.name)
            taskObj.put("energyLevel", task.energyLevel.name)
            taskObj.put("estimatedMinutes", task.estimatedMinutes)
            taskObj.put("category", task.category)
            taskObj.put("dueDate", task.dueDate ?: JSONObject.NULL)
            taskObj.put("createdAt", task.createdAt)
            taskObj.put("completedAt", task.completedAt ?: JSONObject.NULL)

            val subtasksArray = JSONArray()
            for (sub in task.getSubtasks()) {
                val subObj = JSONObject()
                subObj.put("id", sub.id)
                subObj.put("title", sub.title)
                subObj.put("isCompleted", sub.isCompleted)
                subtasksArray.put(subObj)
            }
            taskObj.put("subtasks", subtasksArray)
            tasksArray.put(taskObj)
        }
        root.put("tasks", tasksArray)

        val notesArray = JSONArray()
        for (note in notes) {
            val noteObj = JSONObject()
            noteObj.put("id", note.id)
            noteObj.put("title", note.title)
            noteObj.put("content", note.content)
            noteObj.put("category", note.category)
            noteObj.put("colorHex", note.colorHex)
            noteObj.put("isPinned", note.isPinned)
            noteObj.put("isChecklist", note.isChecklist)
            noteObj.put("createdAt", note.createdAt)
            noteObj.put("updatedAt", note.updatedAt)

            val checklistArray = JSONArray()
            for (item in note.getChecklistItems()) {
                val itemObj = JSONObject()
                itemObj.put("id", item.id)
                itemObj.put("text", item.text)
                itemObj.put("isChecked", item.isChecked)
                checklistArray.put(itemObj)
            }
            noteObj.put("checklistItems", checklistArray)
            notesArray.put(noteObj)
        }
        root.put("notes", notesArray)

        val expensesArray = JSONArray()
        for (expense in expenses) {
            val expenseObj = JSONObject()
            expenseObj.put("id", expense.id)
            expenseObj.put("title", expense.title)
            expenseObj.put("amount", expense.amount)
            expenseObj.put("type", expense.type.name)
            expenseObj.put("category", expense.category)
            expenseObj.put("date", expense.date)
            expenseObj.put("note", expense.note)
            expenseObj.put("isImpulseWishlist", expense.isImpulseWishlist)
            expenseObj.put("coolingHours", expense.coolingHours)
            expenseObj.put("impulseAddedAt", expense.impulseAddedAt)
            expenseObj.put("isImpulsePurchased", expense.isImpulsePurchased)
            expenseObj.put("isImpulseCancelled", expense.isImpulseCancelled)
            expensesArray.put(expenseObj)
        }
        root.put("expenses", expensesArray)

        val calendarArray = JSONArray()
        for (event in calendarEvents) {
            val eventObj = JSONObject()
            eventObj.put("id", event.id)
            eventObj.put("title", event.title)
            eventObj.put("description", event.description)
            eventObj.put("startTimestamp", event.startTimestamp)
            eventObj.put("endTimestamp", event.endTimestamp)
            eventObj.put("colorHex", event.colorHex)
            eventObj.put("category", event.category)
            eventObj.put("isAllDay", event.isAllDay)
            eventObj.put("location", event.location)
            calendarArray.put(eventObj)
        }
        root.put("calendarEvents", calendarArray)

        return root.toString(2)
    }

    fun copyToClipboard(context: Context, jsonText: String, successMessage: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Brain Heal JSON Backup", jsonText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
    }

    fun shareJsonBackup(context: Context, jsonText: String, chooserTitle: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, jsonText)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "brain_heal_backup_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.json")
        }
        val shareIntent = Intent.createChooser(sendIntent, chooserTitle)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
