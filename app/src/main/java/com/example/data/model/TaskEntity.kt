package com.example.data.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R

@Immutable
enum class TaskPriority(
    @StringRes val stringRes: Int,
    val colorHex: Long
) {
    URGENT(R.string.priority_urgent, 0xFFE11D48),
    IMPORTANT(R.string.priority_important, 0xFFF59E0B),
    CASUAL(R.string.priority_casual, 0xFF0D9488);

    fun getLabel(): String {
        return if (java.util.Locale.getDefault().language == "tr") {
            when (this) {
                URGENT -> "Acil & Önemli"
                IMPORTANT -> "Önemli"
                CASUAL -> "Rahat / Düşük Baskı"
            }
        } else {
            when (this) {
                URGENT -> "Urgent & Important"
                IMPORTANT -> "Important"
                CASUAL -> "Casual / Low Stakes"
            }
        }
    }
}

@Immutable
enum class EnergyLevel(
    @StringRes val stringRes: Int,
    @StringRes val shortStringRes: Int,
    val emoji: String
) {
    LOW(R.string.energy_low, R.string.energy_low_short, "🟢"),
    MEDIUM(R.string.energy_medium, R.string.energy_medium_short, "🟡"),
    HIGH(R.string.energy_high, R.string.energy_high_short, "🔴");

    fun getLabel(): String {
        return if (java.util.Locale.getDefault().language == "tr") {
            when (this) {
                LOW -> "Düşük Enerji (5-10 dk)"
                MEDIUM -> "Orta Enerji (15-30 dk)"
                HIGH -> "Yüksek Odak (45+ dk)"
            }
        } else {
            when (this) {
                LOW -> "Low Energy (5-10 min)"
                MEDIUM -> "Medium Energy (15-30 min)"
                HIGH -> "High Focus (45+ min)"
            }
        }
    }

    fun getShortLabel(): String {
        return if (java.util.Locale.getDefault().language == "tr") {
            when (this) {
                LOW -> "Düşük"
                MEDIUM -> "Orta"
                HIGH -> "Yüksek"
            }
        } else {
            when (this) {
                LOW -> "Low"
                MEDIUM -> "Medium"
                HIGH -> "High"
            }
        }
    }
}

@Immutable
data class SubTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

@Immutable
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.IMPORTANT,
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val estimatedMinutes: Int = 25,
    val dueDate: Long? = null, // timestamp in ms
    val category: String = "Genel",
    val subtasksRaw: String = "", // formatted: id||title||isCompleted###...
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    fun getSubtasks(): List<SubTask> {
        if (subtasksRaw.isBlank()) return emptyList()
        return subtasksRaw.split("###").mapNotNull { item ->
            val parts = item.split("||")
            if (parts.size >= 3) {
                SubTask(id = parts[0], title = parts[1], isCompleted = parts[2].toBoolean())
            } else null
        }
    }

    companion object {
        fun encodeSubtasks(subtasks: List<SubTask>): String {
            return subtasks.joinToString("###") { "${it.id}||${it.title}||${it.isCompleted}" }
        }
    }
}
