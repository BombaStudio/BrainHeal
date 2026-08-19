package com.example.data.repository

import com.example.data.dao.AppSettingsDao
import com.example.data.dao.CalendarEventDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.NoteDao
import com.example.data.dao.TaskDao
import com.example.data.model.AppSettingsEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CalendarEventEntity
import com.example.data.model.EnergyLevel
import com.example.data.model.ExpenseEntity
import com.example.data.model.NoteChecklistItem
import com.example.data.model.NoteEntity
import com.example.data.model.SubTask
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.ThemeMode
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID

class OdakRepository(
    private val taskDao: TaskDao,
    private val expenseDao: ExpenseDao,
    private val calendarEventDao: CalendarEventDao,
    private val noteDao: NoteDao,
    private val appSettingsDao: AppSettingsDao
) {
    // Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val activeTasks: Flow<List<TaskEntity>> = taskDao.getActiveTasks()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun toggleTaskComplete(task: TaskEntity): Boolean {
        val newState = !task.isCompleted
        val completedTimestamp = if (newState) System.currentTimeMillis() else null
        val updated = task.copy(isCompleted = newState, completedAt = completedTimestamp)
        taskDao.updateTask(updated)
        if (newState) {
            val settings = appSettingsDao.getSettingsDirect() ?: AppSettingsEntity()
            appSettingsDao.insertOrUpdate(settings.copy(completedTasksCountTotal = settings.completedTasksCountTotal + 1))
        }
        return newState
    }

    // Expenses
    val allTransactions: Flow<List<ExpenseEntity>> = expenseDao.getAllTransactions()
    val actualExpenses: Flow<List<ExpenseEntity>> = expenseDao.getActualExpenses()
    val activeImpulseWishlist: Flow<List<ExpenseEntity>> = expenseDao.getActiveImpulseWishlist()

    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)

    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)

    // Calendar
    val allEvents: Flow<List<CalendarEventEntity>> = calendarEventDao.getAllEvents()

    fun getEventsInRange(start: Long, end: Long): Flow<List<CalendarEventEntity>> =
        calendarEventDao.getEventsInRange(start, end)

    suspend fun insertEvent(event: CalendarEventEntity): Long = calendarEventDao.insertEvent(event)

    suspend fun updateEvent(event: CalendarEventEntity) = calendarEventDao.updateEvent(event)

    suspend fun deleteEvent(event: CalendarEventEntity) = calendarEventDao.deleteEvent(event)

    suspend fun deleteEventById(id: Long) = calendarEventDao.deleteEventById(id)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allCategories: Flow<List<String>> = noteDao.getAllCategories()

    fun getNotesByCategory(category: String): Flow<List<NoteEntity>> =
        noteDao.getNotesByCategory(category)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    suspend fun togglePinNote(note: NoteEntity) {
        noteDao.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    // Settings
    val appSettings: Flow<AppSettingsEntity?> = appSettingsDao.getSettingsFlow()

    suspend fun getSettingsDirect(): AppSettingsEntity {
        return appSettingsDao.getSettingsDirect() ?: AppSettingsEntity().also {
            appSettingsDao.insertOrUpdate(it)
        }
    }

    suspend fun updateSettings(settings: AppSettingsEntity) = appSettingsDao.insertOrUpdate(settings)

    suspend fun toggleMinimalistMode() {
        val current = getSettingsDirect()
        appSettingsDao.insertOrUpdate(current.copy(isMinimalistMode = !current.isMinimalistMode))
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        val current = getSettingsDirect()
        appSettingsDao.insertOrUpdate(current.copy(themeMode = mode))
    }

    suspend fun setLanguage(language: AppLanguage) {
        val current = getSettingsDirect()
        appSettingsDao.insertOrUpdate(current.copy(language = language))
    }

    suspend fun updateBudgets(daily: Double, monthly: Double) {
        val current = getSettingsDirect()
        appSettingsDao.insertOrUpdate(current.copy(dailyBudgetLimit = daily, monthlyBudgetLimit = monthly))
    }

    // Import / Restore Backup Data
    suspend fun importBackupData(data: com.example.util.ParsedBackupData, replaceExisting: Boolean) {
        if (replaceExisting) {
            taskDao.deleteAllTasks()
            noteDao.deleteAllNotes()
            expenseDao.deleteAllExpenses()
            calendarEventDao.deleteAllEvents()

            if (data.tasks.isNotEmpty()) taskDao.insertTasks(data.tasks)
            if (data.notes.isNotEmpty()) noteDao.insertNotes(data.notes)
            if (data.expenses.isNotEmpty()) expenseDao.insertExpenses(data.expenses)
            if (data.calendarEvents.isNotEmpty()) calendarEventDao.insertEvents(data.calendarEvents)
        } else {
            // Merge / Append: reassign id=0 so Room auto-generates fresh unique IDs
            if (data.tasks.isNotEmpty()) {
                val freshTasks = data.tasks.map { it.copy(id = 0) }
                taskDao.insertTasks(freshTasks)
            }
            if (data.notes.isNotEmpty()) {
                val freshNotes = data.notes.map { it.copy(id = 0) }
                noteDao.insertNotes(freshNotes)
            }
            if (data.expenses.isNotEmpty()) {
                val freshExpenses = data.expenses.map { it.copy(id = 0) }
                expenseDao.insertExpenses(freshExpenses)
            }
            if (data.calendarEvents.isNotEmpty()) {
                val freshEvents = data.calendarEvents.map { it.copy(id = 0) }
                calendarEventDao.insertEvents(freshEvents)
            }
        }
    }

    // Initialize settings if fresh install (no mock/dummy data)
    suspend fun seedInitialDataIfNeeded() {
        val settings = appSettingsDao.getSettingsDirect()
        if (settings == null) {
            appSettingsDao.insertOrUpdate(AppSettingsEntity())
        }
    }
}
