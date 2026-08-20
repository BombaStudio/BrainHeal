package com.emirozturk.brainheal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppLanguage(val code: String, val labelTr: String, val labelEn: String) {
    SYSTEM("", "Sistem Varsayılanı", "System Default"),
    TURKISH("tr", "Türkçe", "Turkish"),
    ENGLISH("en", "English", "English");

    fun getLabel(): String {
        return if (java.util.Locale.getDefault().language == "tr") labelTr else labelEn
    }
}

enum class ThemeMode(val labelTr: String, val labelEn: String) {
    SYSTEM("Sistem Varsayılanı", "System Default"),
    LIGHT("Açık Tema", "Light Mode"),
    DARK("Koyu Tema", "Dark Mode"),
    AMOLED("AMOLED Saf Siyah (Düşük Pil)", "AMOLED Pitch Black (Battery Saver)");

    fun getLabel(): String {
        return if (java.util.Locale.getDefault().language == "tr") labelTr else labelEn
    }
}

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isMinimalistMode: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.AMOLED,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val dailyBudgetLimit: Double = 500.0,
    val monthlyBudgetLimit: Double = 15000.0,
    val defaultFocusDurationMinutes: Int = 25,
    val soundEffectsEnabled: Boolean = true,
    val completedTasksCountTotal: Int = 0
)
