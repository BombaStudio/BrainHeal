package com.emirozturk.brainheal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTimestamp: Long,
    val endTimestamp: Long,
    val colorHex: Long = 0xFF0D9488,
    val category: String = "Genel",
    val isAllDay: Boolean = false,
    val location: String = ""
)
