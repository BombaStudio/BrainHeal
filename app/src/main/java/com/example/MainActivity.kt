package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CelebrationBanner
import com.example.ui.components.ConfettiCelebrationOverlay
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.minimalist.MinimalistModeScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.tasks.FocusSingleTaskScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.OdakFlowTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.appSettings.collectAsStateWithLifecycle()

            val currentLocale = remember(settings.language) {
                if (settings.language.code.isNotEmpty()) {
                    Locale(settings.language.code)
                } else {
                    Locale.getDefault()
                }
            }

            LaunchedEffect(currentLocale) {
                Locale.setDefault(currentLocale)
                val config = android.content.res.Configuration(resources.configuration)
                config.setLocale(currentLocale)
                config.setLayoutDirection(currentLocale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)
            }

            val context = LocalContext.current
            val localizedContext = remember(context, currentLocale) {
                val config = android.content.res.Configuration(context.resources.configuration)
                config.setLocale(currentLocale)
                context.createConfigurationContext(config)
            }
            val localizedConfiguration = remember(currentLocale) {
                val config = android.content.res.Configuration(context.resources.configuration)
                config.setLocale(currentLocale)
                config
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration,
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                OdakFlowTheme(themeMode = settings.themeMode) {
                    OdakFlowApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun OdakFlowApp(viewModel: MainViewModel) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val celebrationText by viewModel.celebrationText.collectAsStateWithLifecycle()
    val confettiTrigger by viewModel.confettiTrigger.collectAsStateWithLifecycle()

    val currentFocusTask by viewModel.currentFocusTask.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

    val currentFocusTaskLive = remember(currentFocusTask, allTasks) {
        if (currentFocusTask == null) null
        else allTasks.find { it.id == currentFocusTask?.id } ?: currentFocusTask
    }

    // 1. Single Task Focus Mode takes priority
    if (currentFocusTaskLive != null) {
        val task = currentFocusTaskLive
        val focusRemainingSeconds by viewModel.focusRemainingSeconds.collectAsStateWithLifecycle()
        val focusInitialSeconds by viewModel.focusInitialSeconds.collectAsStateWithLifecycle()
        val isFocusTimerRunning by viewModel.isFocusTimerRunning.collectAsStateWithLifecycle()

        Box(modifier = Modifier.fillMaxSize()) {
            FocusSingleTaskScreen(
                task = task,
                remainingSeconds = focusRemainingSeconds,
                initialSeconds = focusInitialSeconds,
                isRunning = isFocusTimerRunning,
                onStartTimer = viewModel::startFocusTimer,
                onPauseTimer = viewModel::pauseFocusTimer,
                onResetTimer = viewModel::resetFocusTimer,
                onAddExtraMinutes = { mins ->
                    viewModel.setFocusDurationMinutes((focusInitialSeconds / 60) + mins)
                },
                onToggleSubtask = { subId -> viewModel.toggleSubtask(task, subId) },
                onCompleteTask = { viewModel.toggleTaskComplete(task) },
                onClose = viewModel::closeFocusTask
            )

            CelebrationBanner(
                text = celebrationText,
                onDismiss = viewModel::dismissCelebration,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 28.dp)
            )

            ConfettiCelebrationOverlay(
                triggerTimestamp = confettiTrigger,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    // 2. Zen Minimalist Mode (if enabled)
    if (settings.isMinimalistMode) {
        val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
        val pomodoroMode by viewModel.pomodoroMode.collectAsStateWithLifecycle()
        val pomodoroRound by viewModel.pomodoroRound.collectAsStateWithLifecycle()
        val focusRemainingSeconds by viewModel.focusRemainingSeconds.collectAsStateWithLifecycle()
        val focusInitialSeconds by viewModel.focusInitialSeconds.collectAsStateWithLifecycle()
        val isFocusTimerRunning by viewModel.isFocusTimerRunning.collectAsStateWithLifecycle()
        val customFocusMinutes by viewModel.customFocusMinutes.collectAsStateWithLifecycle()
        val customShortBreakMinutes by viewModel.customShortBreakMinutes.collectAsStateWithLifecycle()
        val customLongBreakMinutes by viewModel.customLongBreakMinutes.collectAsStateWithLifecycle()

        Box(modifier = Modifier.fillMaxSize()) {
            MinimalistModeScreen(
                activeTasks = activeTasks,
                pomodoroMode = pomodoroMode,
                pomodoroRound = pomodoroRound,
                remainingSeconds = focusRemainingSeconds,
                initialSeconds = focusInitialSeconds,
                isTimerRunning = isFocusTimerRunning,
                customFocusMinutes = customFocusMinutes,
                customShortBreakMinutes = customShortBreakMinutes,
                customLongBreakMinutes = customLongBreakMinutes,
                onSelectMode = viewModel::setPomodoroMode,
                onSetCustomDuration = viewModel::setCustomPomodoroDuration,
                onAdjustMinutes = viewModel::adjustFocusMinutes,
                onStartTimer = viewModel::startFocusTimer,
                onPauseTimer = viewModel::pauseFocusTimer,
                onResetTimer = viewModel::resetFocusTimer,
                onToggleTaskComplete = viewModel::toggleTaskComplete,
                onToggleSubtask = viewModel::toggleSubtask,
                onQuickBrainDump = viewModel::quickBrainDump,
                onExitMinimalist = viewModel::toggleMinimalistMode
            )

            CelebrationBanner(
                text = celebrationText,
                onDismiss = viewModel::dismissCelebration,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 28.dp)
            )

            ConfettiCelebrationOverlay(
                triggerTimestamp = confettiTrigger,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    // 3. Standard Navigation Mode
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.TASKS,
                    onClick = { viewModel.setTab(AppTab.TASKS) },
                    icon = { Icon(Icons.Default.TaskAlt, contentDescription = stringResource(R.string.nav_tasks)) },
                    label = { Text(stringResource(R.string.nav_tasks), fontWeight = if (currentTab == AppTab.TASKS) FontWeight.Bold else FontWeight.Normal) },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_tasks")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.EXPENSES,
                    onClick = { viewModel.setTab(AppTab.EXPENSES) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = stringResource(R.string.nav_expenses)) },
                    label = { Text(stringResource(R.string.nav_expenses), fontWeight = if (currentTab == AppTab.EXPENSES) FontWeight.Bold else FontWeight.Normal) },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_expenses")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.CALENDAR,
                    onClick = { viewModel.setTab(AppTab.CALENDAR) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.nav_calendar)) },
                    label = { Text(stringResource(R.string.nav_calendar), fontWeight = if (currentTab == AppTab.CALENDAR) FontWeight.Bold else FontWeight.Normal) },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_calendar")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.NOTES,
                    onClick = { viewModel.setTab(AppTab.NOTES) },
                    icon = { Icon(Icons.Default.Description, contentDescription = stringResource(R.string.nav_notes)) },
                    label = { Text(stringResource(R.string.nav_notes), fontWeight = if (currentTab == AppTab.NOTES) FontWeight.Bold else FontWeight.Normal) },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_notes")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { viewModel.setTab(AppTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                    label = { Text(stringResource(R.string.nav_settings), fontWeight = if (currentTab == AppTab.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_animation"
                ) { tab ->
                    when (tab) {
                        AppTab.TASKS -> {
                            val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
                            val filteredActiveTasks by viewModel.filteredActiveTasks.collectAsStateWithLifecycle()
                            val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()
                            val selectedEnergy by viewModel.filterEnergy.collectAsStateWithLifecycle()
                            val selectedPriority by viewModel.filterPriority.collectAsStateWithLifecycle()
                            val selectedDay by viewModel.selectedDayCalendar.collectAsStateWithLifecycle()

                            TasksScreen(
                                activeTasksCount = activeTasks.size,
                                filteredActiveTasks = filteredActiveTasks,
                                completedTasks = completedTasks,
                                selectedEnergy = selectedEnergy,
                                selectedPriority = selectedPriority,
                                selectedDay = selectedDay,
                                onSelectEnergy = viewModel::setEnergyFilter,
                                onSelectPriority = viewModel::setPriorityFilter,
                                onSelectDay = viewModel::setSelectedDay,
                                onToggleComplete = viewModel::toggleTaskComplete,
                                onToggleSubtask = viewModel::toggleSubtask,
                                onStartFocus = viewModel::startFocusOnTask,
                                onSaveTask = viewModel::saveTask,
                                onDeleteTask = viewModel::deleteTask
                            )
                        }

                        AppTab.EXPENSES -> {
                            val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
                            val actualExpenses by viewModel.actualExpenses.collectAsStateWithLifecycle()
                            val impulseWishlist by viewModel.impulseWishlist.collectAsStateWithLifecycle()

                            ExpensesScreen(
                                allTransactions = allTransactions,
                                actualExpenses = actualExpenses,
                                impulseWishlist = impulseWishlist,
                                settings = settings,
                                onSaveExpense = viewModel::saveExpense,
                                onDeleteExpense = viewModel::deleteExpense,
                                onDecideImpulse = viewModel::decideImpulsePurchase
                            )
                        }

                        AppTab.CALENDAR -> {
                            val allEvents by viewModel.allEvents.collectAsStateWithLifecycle()
                            val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
                            val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()

                            CalendarScreen(
                                allEvents = allEvents,
                                allTasks = allTasks,
                                selectedDate = selectedCalendarDate,
                                onSelectDate = viewModel::selectCalendarDate,
                                onSaveEvent = viewModel::saveEvent,
                                onDeleteEvent = viewModel::deleteEvent,
                                onToggleCompleteTask = viewModel::toggleTaskComplete
                            )
                        }

                        AppTab.NOTES -> {
                            val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
                            val noteCategories by viewModel.noteCategories.collectAsStateWithLifecycle()
                            val selectedNoteCategory by viewModel.selectedNoteCategory.collectAsStateWithLifecycle()

                            NotesScreen(
                                allNotes = allNotes,
                                categories = noteCategories,
                                selectedCategory = selectedNoteCategory,
                                onSelectCategory = viewModel::selectNoteCategory,
                                onQuickBrainDump = viewModel::quickBrainDump,
                                onSaveNote = viewModel::saveNote,
                                onDeleteNote = viewModel::deleteNote,
                                onTogglePinNote = viewModel::togglePinNote,
                                onToggleChecklistItem = viewModel::toggleNoteChecklistItem
                            )
                        }

                        AppTab.SETTINGS -> {
                            val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
                            val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
                            val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
                            val allEvents by viewModel.allEvents.collectAsStateWithLifecycle()

                            SettingsScreen(
                                settings = settings,
                                totalCompletedTasks = settings.completedTasksCountTotal,
                                cancelledImpulseCount = allTransactions.count { it.isImpulseCancelled },
                                allTasks = allTasks,
                                allNotes = allNotes,
                                allExpenses = allTransactions,
                                allEvents = allEvents,
                                onSetThemeMode = viewModel::setThemeMode,
                                onSetLanguage = viewModel::setLanguage,
                                onToggleMinimalistMode = viewModel::toggleMinimalistMode,
                                onUpdateBudgets = viewModel::updateBudgets,
                                onImportBackup = viewModel::importBackupData
                            )
                        }
                    }
                }

                CelebrationBanner(
                    text = celebrationText,
                    onDismiss = viewModel::dismissCelebration,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                )

                ConfettiCelebrationOverlay(
                    triggerTimestamp = confettiTrigger,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
