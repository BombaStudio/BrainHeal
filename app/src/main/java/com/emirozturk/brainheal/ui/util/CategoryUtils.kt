package com.emirozturk.brainheal.ui.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.emirozturk.brainheal.R
import java.util.Locale

object CategoryUtils {
    @StringRes
    fun getCategoryStringRes(category: String, fallback: Int = R.string.category_general): Int {
        return when (category.lowercase(Locale.ROOT).trim()) {
            "food", "yemek" -> R.string.category_food
            "transport", "ulaşım", "ulasim" -> R.string.category_transport
            "shopping", "alışveriş", "alisveris" -> R.string.category_shopping
            "bills", "faturalar", "fatura" -> R.string.category_bills
            "entertainment", "eğlence", "eglence" -> R.string.category_entertainment
            "health", "sağlık", "saglik" -> R.string.category_health
            "tech", "teknoloji" -> R.string.category_tech
            "work", "iş", "is" -> R.string.category_work
            "personal", "kişisel", "kisisel" -> R.string.category_personal
            "study", "ders", "çalışma", "calisma" -> R.string.category_study
            "focus block", "odak bloğu", "odak blogu", "focus_block" -> R.string.category_focus_block
            "appointment", "randevu" -> R.string.category_appointment
            "ideas", "fikirler", "fikir" -> R.string.category_ideas
            "reminder", "hatırlatıcı", "hatirlatici" -> R.string.category_reminder
            "projects", "projeler", "proje" -> R.string.category_projects
            "other", "diğer", "diger" -> R.string.category_other
            "general", "genel" -> R.string.category_general
            else -> fallback
        }
    }

    @Composable
    fun getLocalizedCategoryName(category: String): String {
        return stringResource(getCategoryStringRes(category))
    }
}
