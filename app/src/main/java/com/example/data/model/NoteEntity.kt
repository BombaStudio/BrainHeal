package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class NoteChecklistItem(
    val id: String,
    val text: String,
    val isChecked: Boolean = false
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String = "",
    val category: String = "Genel", // Kişisel, İş, Fikirler, Hatırlatıcı, vb.
    val colorHex: Long = 0xFF1E293B, // Slate/dark surface
    val isPinned: Boolean = false,
    val isChecklist: Boolean = false,
    val checklistRaw: String = "", // id||text||isChecked###...
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getChecklistItems(): List<NoteChecklistItem> {
        if (checklistRaw.isBlank()) return emptyList()
        return checklistRaw.split("###").mapNotNull { item ->
            val parts = item.split("||")
            if (parts.size >= 3) {
                NoteChecklistItem(id = parts[0], text = parts[1], isChecked = parts[2].toBoolean())
            } else null
        }
    }

    companion object {
        fun encodeChecklist(items: List<NoteChecklistItem>): String {
            return items.joinToString("###") { "${it.id}||${it.text}||${it.isChecked}" }
        }
    }
}
