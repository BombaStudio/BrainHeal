package com.example.data.model

import androidx.annotation.StringRes
import com.example.R

enum class PomodoroMode(
    val defaultMinutes: Int,
    @StringRes val labelRes: Int
) {
    FOCUS(25, R.string.pomodoro_mode_focus),
    SHORT_BREAK(5, R.string.pomodoro_mode_short_break),
    LONG_BREAK(15, R.string.pomodoro_mode_long_break)
}
