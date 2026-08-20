package com.emirozturk.brainheal.ui.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emirozturk.brainheal.R
import com.emirozturk.brainheal.data.AppDatabase
import com.emirozturk.brainheal.data.model.AppSettingsEntity
import com.emirozturk.brainheal.data.model.AppLanguage
import com.emirozturk.brainheal.data.model.CalendarEventEntity
import com.emirozturk.brainheal.data.model.EnergyLevel
import com.emirozturk.brainheal.data.model.ExpenseEntity
import com.emirozturk.brainheal.data.model.NoteEntity
import com.emirozturk.brainheal.data.model.PomodoroMode
import com.emirozturk.brainheal.data.model.SubTask
import com.emirozturk.brainheal.data.model.TaskEntity
import com.emirozturk.brainheal.data.model.TaskPriority
import com.emirozturk.brainheal.data.model.ThemeMode
import com.emirozturk.brainheal.data.model.TransactionType
import com.emirozturk.brainheal.data.repository.OdakRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppTab(val titleTr: String) {
    TASKS("Görevler"),
    EXPENSES("Harcamalar"),
    CALENDAR("Takvim"),
    NOTES("Notlar"),
    SETTINGS("Ayarlar")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OdakRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = OdakRepository(
            taskDao = db.taskDao(),
            expenseDao = db.expenseDao(),
            calendarEventDao = db.calendarEventDao(),
            noteDao = db.noteDao(),
            appSettingsDao = db.appSettingsDao()
        )
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.TASKS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // App Settings
    val appSettings: StateFlow<AppSettingsEntity> = repository.appSettings
        .combine(MutableStateFlow(AppSettingsEntity())) { settings, default ->
            settings ?: default
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    fun toggleMinimalistMode() {
        viewModelScope.launch { repository.toggleMinimalistMode() }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun updateBudgets(daily: Double, monthly: Double) {
        viewModelScope.launch { repository.updateBudgets(daily, monthly) }
    }

    // Tasks State
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<TaskEntity>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<TaskEntity>> = repository.completedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Energy & Priority Filters for ADHD low-friction selection
    private val _filterEnergy = MutableStateFlow<EnergyLevel?>(null)
    val filterEnergy: StateFlow<EnergyLevel?> = _filterEnergy.asStateFlow()

    private val _filterPriority = MutableStateFlow<TaskPriority?>(null)
    val filterPriority: StateFlow<TaskPriority?> = _filterPriority.asStateFlow()

    private val _selectedDayCalendar = MutableStateFlow<Calendar?>(Calendar.getInstance())
    val selectedDayCalendar: StateFlow<Calendar?> = _selectedDayCalendar.asStateFlow()

    fun setEnergyFilter(level: EnergyLevel?) {
        _filterEnergy.value = if (_filterEnergy.value == level) null else level
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _filterPriority.value = if (_filterPriority.value == priority) null else priority
    }

    fun setSelectedDay(cal: Calendar?) {
        _selectedDayCalendar.value = cal
    }

    val filteredActiveTasks: StateFlow<List<TaskEntity>> = combine(
        activeTasks,
        _filterEnergy,
        _filterPriority,
        _selectedDayCalendar
    ) { tasks, energy, priority, day ->
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        tasks.filter { task ->
            val energyMatches = energy == null || task.energyLevel == energy
            val priorityMatches = priority == null || task.priority == priority
            val dayMatches = if (day == null) {
                true
            } else {
                val selectedDayStr = dateFormat.format(day.time)
                if (task.dueDate != null) {
                    dateFormat.format(Date(task.dueDate)) == selectedDayStr
                } else {
                    val createdDayStr = dateFormat.format(Date(task.createdAt))
                    val isTodaySelected = isSameDay(day, Calendar.getInstance())
                    createdDayStr == selectedDayStr || (isTodaySelected && task.createdAt <= day.timeInMillis)
                }
            }
            energyMatches && priorityMatches && dayMatches
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    // Task Actions
    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            val isNowCompleted = repository.toggleTaskComplete(task)
            if (isNowCompleted) {
                triggerCelebration(
                    msg = getApplication<Application>().getString(R.string.celebration_task_completed),
                    withConfetti = true
                )
            }
        }
    }

    fun toggleSubtask(task: TaskEntity, subtaskId: String) {
        val updatedSubtasks = task.getSubtasks().map {
            if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        val updatedTask = task.copy(subtasksRaw = TaskEntity.encodeSubtasks(updatedSubtasks))
        if (_currentFocusTask.value?.id == task.id) {
            _currentFocusTask.value = updatedTask
        }
        val completedCount = updatedSubtasks.count { it.isCompleted }
        if (completedCount == updatedSubtasks.size && updatedSubtasks.isNotEmpty()) {
            triggerCelebration(
                msg = getApplication<Application>().getString(R.string.celebration_all_subtasks_done),
                withConfetti = true
            )
        }
        saveTask(updatedTask)
    }

    // Single Task Focus & Pomodoro Timer (ADHD Un-overwhelm tool)
    private val _currentFocusTask = MutableStateFlow<TaskEntity?>(null)
    val currentFocusTask: StateFlow<TaskEntity?> = _currentFocusTask.asStateFlow()

    private val _pomodoroMode = MutableStateFlow(PomodoroMode.FOCUS)
    val pomodoroMode: StateFlow<PomodoroMode> = _pomodoroMode.asStateFlow()

    private val _pomodoroRound = MutableStateFlow(1)
    val pomodoroRound: StateFlow<Int> = _pomodoroRound.asStateFlow()

    private val _customFocusMinutes = MutableStateFlow(25)
    val customFocusMinutes: StateFlow<Int> = _customFocusMinutes.asStateFlow()

    private val _customShortBreakMinutes = MutableStateFlow(5)
    val customShortBreakMinutes: StateFlow<Int> = _customShortBreakMinutes.asStateFlow()

    private val _customLongBreakMinutes = MutableStateFlow(15)
    val customLongBreakMinutes: StateFlow<Int> = _customLongBreakMinutes.asStateFlow()

    private val _focusRemainingSeconds = MutableStateFlow(25 * 60)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private val _focusInitialSeconds = MutableStateFlow(25 * 60)
    val focusInitialSeconds: StateFlow<Int> = _focusInitialSeconds.asStateFlow()

    private val _isFocusTimerRunning = MutableStateFlow(false)
    val isFocusTimerRunning: StateFlow<Boolean> = _isFocusTimerRunning.asStateFlow()

    private var focusCountDownTimer: CountDownTimer? = null

    fun startFocusOnTask(task: TaskEntity, minutes: Int = task.estimatedMinutes.coerceAtLeast(1)) {
        _currentFocusTask.value = task
        _pomodoroMode.value = PomodoroMode.FOCUS
        _customFocusMinutes.value = minutes
        _focusInitialSeconds.value = minutes * 60
        _focusRemainingSeconds.value = minutes * 60
        startFocusTimer()
    }

    fun setPomodoroMode(mode: PomodoroMode) {
        pauseFocusTimer()
        _pomodoroMode.value = mode
        val durationMins = when (mode) {
            PomodoroMode.FOCUS -> _customFocusMinutes.value
            PomodoroMode.SHORT_BREAK -> _customShortBreakMinutes.value
            PomodoroMode.LONG_BREAK -> _customLongBreakMinutes.value
        }
        _focusInitialSeconds.value = durationMins * 60
        _focusRemainingSeconds.value = durationMins * 60
    }

    fun setCustomPomodoroDuration(mode: PomodoroMode, minutes: Int) {
        val clampedMins = minutes.coerceIn(1, 180)
        when (mode) {
            PomodoroMode.FOCUS -> _customFocusMinutes.value = clampedMins
            PomodoroMode.SHORT_BREAK -> _customShortBreakMinutes.value = clampedMins
            PomodoroMode.LONG_BREAK -> _customLongBreakMinutes.value = clampedMins
        }
        if (_pomodoroMode.value == mode && !_isFocusTimerRunning.value) {
            _focusInitialSeconds.value = clampedMins * 60
            _focusRemainingSeconds.value = clampedMins * 60
        }
    }

    fun adjustFocusMinutes(deltaMinutes: Int) {
        val currentMins = _focusRemainingSeconds.value / 60
        val newMins = (currentMins + deltaMinutes).coerceIn(1, 180)
        val newRemainingSecs = (_focusRemainingSeconds.value + deltaMinutes * 60).coerceIn(60, 180 * 60)
        _focusRemainingSeconds.value = newRemainingSecs
        _focusInitialSeconds.value = maxOf(_focusInitialSeconds.value, newRemainingSecs)
        
        // Also update custom duration if we were paused
        if (!_isFocusTimerRunning.value) {
            when (_pomodoroMode.value) {
                PomodoroMode.FOCUS -> _customFocusMinutes.value = newMins
                PomodoroMode.SHORT_BREAK -> _customShortBreakMinutes.value = newMins
                PomodoroMode.LONG_BREAK -> _customLongBreakMinutes.value = newMins
            }
        }
    }

    fun setFocusDurationMinutes(minutes: Int) {
        if (!_isFocusTimerRunning.value) {
            val clamped = minutes.coerceIn(1, 180)
            _focusInitialSeconds.value = clamped * 60
            _focusRemainingSeconds.value = clamped * 60
            when (_pomodoroMode.value) {
                PomodoroMode.FOCUS -> _customFocusMinutes.value = clamped
                PomodoroMode.SHORT_BREAK -> _customShortBreakMinutes.value = clamped
                PomodoroMode.LONG_BREAK -> _customLongBreakMinutes.value = clamped
            }
        }
    }

    fun startFocusTimer() {
        focusCountDownTimer?.cancel()
        _isFocusTimerRunning.value = true
        focusCountDownTimer = object : CountDownTimer((_focusRemainingSeconds.value * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _focusRemainingSeconds.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _isFocusTimerRunning.value = false
                _focusRemainingSeconds.value = 0
                
                val wasFocusMode = _pomodoroMode.value == PomodoroMode.FOCUS
                if (wasFocusMode) {
                    val currentR = _pomodoroRound.value
                    triggerCelebration(
                        msg = getApplication<Application>().getString(R.string.celebration_timer_finished),
                        withConfetti = true
                    )
                    // Auto-prep next break mode
                    val nextMode = if (currentR % 4 == 0) PomodoroMode.LONG_BREAK else PomodoroMode.SHORT_BREAK
                    _pomodoroRound.value = currentR + 1
                    _pomodoroMode.value = nextMode
                    val breakMins = if (nextMode == PomodoroMode.LONG_BREAK) _customLongBreakMinutes.value else _customShortBreakMinutes.value
                    _focusInitialSeconds.value = breakMins * 60
                    _focusRemainingSeconds.value = breakMins * 60
                } else {
                    triggerCelebration(
                        msg = getApplication<Application>().getString(R.string.pomodoro_break_finished),
                        withConfetti = false
                    )
                    // Auto-prep focus mode
                    _pomodoroMode.value = PomodoroMode.FOCUS
                    val focusMins = _customFocusMinutes.value
                    _focusInitialSeconds.value = focusMins * 60
                    _focusRemainingSeconds.value = focusMins * 60
                }
            }
        }.start()
    }

    fun pauseFocusTimer() {
        focusCountDownTimer?.cancel()
        _isFocusTimerRunning.value = false
    }

    fun resetFocusTimer() {
        focusCountDownTimer?.cancel()
        _isFocusTimerRunning.value = false
        val durationMins = when (_pomodoroMode.value) {
            PomodoroMode.FOCUS -> _customFocusMinutes.value
            PomodoroMode.SHORT_BREAK -> _customShortBreakMinutes.value
            PomodoroMode.LONG_BREAK -> _customLongBreakMinutes.value
        }
        _focusInitialSeconds.value = durationMins * 60
        _focusRemainingSeconds.value = durationMins * 60
    }

    fun closeFocusTask() {
        pauseFocusTimer()
        _currentFocusTask.value = null
    }

    // Celebration & Dopamine Banner & Confetti
    private val _celebrationText = MutableStateFlow<String?>(null)
    val celebrationText: StateFlow<String?> = _celebrationText.asStateFlow()

    private val _confettiTrigger = MutableStateFlow(0L)
    val confettiTrigger: StateFlow<Long> = _confettiTrigger.asStateFlow()

    fun triggerCelebration(msg: String, withConfetti: Boolean = false) {
        _celebrationText.value = msg
        if (withConfetti) {
            _confettiTrigger.value = System.currentTimeMillis()
        }
    }

    fun triggerConfetti() {
        _confettiTrigger.value = System.currentTimeMillis()
    }

    fun dismissCelebration() {
        _celebrationText.value = null
    }

    // Expenses State
    val allTransactions: StateFlow<List<ExpenseEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val actualExpenses: StateFlow<List<ExpenseEntity>> = repository.actualExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val impulseWishlist: StateFlow<List<ExpenseEntity>> = repository.activeImpulseWishlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            if (expense.id == 0L) {
                repository.insertExpense(expense)
                if (expense.isImpulseWishlist) {
                    triggerCelebration(getApplication<Application>().getString(R.string.celebration_impulse_shield))
                }
            } else {
                repository.updateExpense(expense)
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun decideImpulsePurchase(expense: ExpenseEntity, buy: Boolean) {
        viewModelScope.launch {
            val updated = if (buy) {
                expense.copy(
                    isImpulsePurchased = true,
                    isImpulseWishlist = false,
                    date = System.currentTimeMillis()
                )
            } else {
                expense.copy(
                    isImpulseCancelled = true
                )
            }
            repository.updateExpense(updated)
            if (!buy) {
                triggerCelebration(getApplication<Application>().getString(R.string.celebration_impulse_cancelled))
            }
        }
    }

    // Calendar Events
    val allEvents: StateFlow<List<CalendarEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCalendarDate = MutableStateFlow(Calendar.getInstance())
    val selectedCalendarDate: StateFlow<Calendar> = _selectedCalendarDate.asStateFlow()

    fun selectCalendarDate(cal: Calendar) {
        _selectedCalendarDate.value = cal
    }

    fun saveEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            if (event.id == 0L) {
                repository.insertEvent(event)
            } else {
                repository.updateEvent(event)
            }
        }
    }

    fun deleteEvent(event: CalendarEventEntity) {
        viewModelScope.launch { repository.deleteEvent(event) }
    }

    // Notes & Brain Dump
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteCategories: StateFlow<List<String>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedNoteCategory = MutableStateFlow<String?>(null)
    val selectedNoteCategory: StateFlow<String?> = _selectedNoteCategory.asStateFlow()

    fun selectNoteCategory(cat: String?) {
        _selectedNoteCategory.value = if (_selectedNoteCategory.value == cat) null else cat
    }

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun quickBrainDump(thought: String, category: String = "Fikirler") {
        if (thought.isBlank()) return
        viewModelScope.launch {
            val note = NoteEntity(
                title = thought.take(30) + if (thought.length > 30) "..." else "",
                content = thought,
                category = category,
                colorHex = 0xFF0D9488
            )
            repository.insertNote(note)
            triggerCelebration(getApplication<Application>().getString(R.string.celebration_brain_dump))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun togglePinNote(note: NoteEntity) {
        viewModelScope.launch { repository.togglePinNote(note) }
    }

    fun toggleNoteChecklistItem(note: NoteEntity, itemId: String) {
        val updated = note.getChecklistItems().map {
            if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
        }
        saveNote(note.copy(checklistRaw = NoteEntity.encodeChecklist(updated)))
    }

    // JSON Data Backup & Restore
    fun importBackupData(
        jsonText: String,
        replaceExisting: Boolean,
        onResult: (success: Boolean, message: String, count: Int) -> Unit
    ) {
        viewModelScope.launch {
            val parseResult = com.emirozturk.brainheal.util.BackupJsonImporter.parseBackupJson(jsonText)
            if (parseResult.isSuccess) {
                val data = parseResult.getOrThrow()
                try {
                    repository.importBackupData(data, replaceExisting)
                    val celebrationMsg = if (appSettings.value.language == com.emirozturk.brainheal.data.model.AppLanguage.TURKISH) {
                        "Veriler başarıyla içe aktarıldı! (${data.totalCount} öge) 🎉"
                    } else {
                        "Data successfully imported! (${data.totalCount} items) 🎉"
                    }
                    triggerCelebration(celebrationMsg)
                    onResult(true, "Success", data.totalCount)
                } catch (e: Exception) {
                    onResult(false, e.localizedMessage ?: "Import error", 0)
                }
            } else {
                val errorMsg = parseResult.exceptionOrNull()?.localizedMessage ?: "Invalid JSON format"
                onResult(false, errorMsg, 0)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        focusCountDownTimer?.cancel()
    }
}
