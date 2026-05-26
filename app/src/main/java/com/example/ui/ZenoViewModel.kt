package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ZenoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ZenoDatabase.getDatabase(application)
    private val repository = ZenoRepository(application, db.taskDao(), db.habitDao())

    // --- Date State ---
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate = _selectedDate.asStateFlow()

    // --- Reactive Task Stream ---
    val tasksForSelectedDate: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { date -> repository.getTasksForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Lock Screen Top 3 Tasks Stream ---
    val top3TasksForToday: StateFlow<List<Task>> = repository.getTop3TasksForDate(getTodayDateString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Habits Stream ---
    val habits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Weather Stream ---
    val weatherState: StateFlow<WeatherState> = repository.weatherState

    // --- Focus Mode States ---
    private val _isFocusActive = MutableStateFlow(false)
    val isFocusActive = _isFocusActive.asStateFlow()

    private val _isZenMode = MutableStateFlow(false)
    val isZenMode = _isZenMode.asStateFlow()

    private val _focusTask = MutableStateFlow<Task?>(null)
    val focusTask = _focusTask.asStateFlow()

    private val _focusTimeRemaining = MutableStateFlow(1500) // Default 25 minutes = 1500 seconds
    val focusTimeRemaining = _focusTimeRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    private val _selectedFocusSound = MutableStateFlow("Rain") // Rain, White Noise, Forest, Off
    val selectedFocusSound = _selectedFocusSound.asStateFlow()

    // Simulated Incoming Call State
    private val _isIncomingCallActive = MutableStateFlow(false)
    val isIncomingCallActive = _isIncomingCallActive.asStateFlow()

    private val _callerName = MutableStateFlow("Emergency Contact")
    val callerName = _callerName.asStateFlow()

    // Customizable Widgets Systems
    private val _widgetsOrder = MutableStateFlow<List<String>>(
        listOf("weather", "tasks", "calendar", "habits", "stocks", "battery", "quote")
    )
    val widgetsOrder = _widgetsOrder.asStateFlow()

    private val _widgetsEnabled = MutableStateFlow<Map<String, Boolean>>(
        mapOf(
            "weather" to true,
            "tasks" to true,
            "calendar" to true,
            "habits" to true,
            "stocks" to true,
            "battery" to true,
            "quote" to true
        )
    )
    val widgetsEnabled = _widgetsEnabled.asStateFlow()

    // Sky Simulation Overrides
    private val _skyTimeOverride = MutableStateFlow(-1) // -1 means use system time
    val skyTimeOverride = _skyTimeOverride.asStateFlow()

    private val _skyWeatherOverride = MutableStateFlow("") // empty means use real weather
    val skyWeatherOverride = _skyWeatherOverride.asStateFlow()

    // Breathing guidance loop for mindfulness
    private val _breatheState = MutableStateFlow("Inhale") // Inhale, Hold, Exhale, Pause
    val breatheState = _breatheState.asStateFlow()

    private val _breatheProgress = MutableStateFlow(0f) // 0f to 1.0f for smooth breathing pulse circle
    val breatheProgress = _breatheProgress.asStateFlow()

    private var timerJob: Job? = null
    private var breatheJob: Job? = null

    // Ambient Quotes List (Zero power cache)
    val zenQuotes = listOf(
        "“Nature does not hurry, yet everything is accomplished.” — Lao Tzu",
        "“Adopt the pace of nature: her secret is patience.” — Ralph Waldo Emerson",
        "“Simplify your life, amplify your mind.” — Zen Proverb",
        "“What is in front of you is your entire universe right now.”",
        "“Quiet Minds hear the whispers of the universe.”",
        "“An empty calendar is a canvas of immense space.”",
        "“With less, there is room for everything.”"
    )

    private val _currentQuote = MutableStateFlow(zenQuotes.first())
    val currentQuote = _currentQuote.asStateFlow()

    init {
        // Load widget configs from SharedPreferences
        val prefs = application.getSharedPreferences("zeno_prefs", Context.MODE_PRIVATE)
        val orderStr = prefs.getString("widgets_order", "weather,tasks,calendar,habits,stocks,battery,quote") ?: "weather,tasks,calendar,habits,stocks,battery,quote"
        _widgetsOrder.value = orderStr.split(",")

        val enabledStr = prefs.getString("widgets_enabled", "weather,tasks,calendar,habits,stocks,battery,quote") ?: "weather,tasks,calendar,habits,stocks,battery,quote"
        val enabledList = enabledStr.split(",")
        _widgetsEnabled.value = mapOf(
            "weather" to enabledList.contains("weather"),
            "tasks" to enabledList.contains("tasks"),
            "calendar" to enabledList.contains("calendar"),
            "habits" to enabledList.contains("habits"),
            "stocks" to enabledList.contains("stocks"),
            "battery" to enabledList.contains("battery"),
            "quote" to enabledList.contains("quote")
        )

        // Fetch weather immediately on low frequency, caching limits automatic network calls
        viewModelScope.launch {
            repository.refreshWeather()
            cycleDailyQuote()
        }
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun setDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun cycleDailyQuote() {
        val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % zenQuotes.size
        _currentQuote.value = zenQuotes[todayIndex]
    }

    // --- Task CRUD Actions ---
    fun addTask(title: String, timeOfDay: String, priority: Int) = viewModelScope.launch {
        val task = Task(
            title = title,
            timeOfDay = timeOfDay,
            priority = priority,
            date = _selectedDate.value
        )
        repository.insertTask(task)
    }

    fun toggleTaskCompletion(task: Task) = viewModelScope.launch {
        val updated = task.copy(completed = !task.completed)
        repository.updateTask(updated)
    }

    fun deleteTask(taskId: Int) = viewModelScope.launch {
        repository.deleteTaskById(taskId)
    }

    // --- Habit Actions ---
    fun addHabit(name: String) = viewModelScope.launch {
        val habit = Habit(name = name)
        repository.insertHabit(habit)
    }

    fun deleteHabit(habitId: Int) = viewModelScope.launch {
        repository.deleteHabitById(habitId)
    }

    fun toggleHabitForToday(habit: Habit) = viewModelScope.launch {
        val today = getTodayDateString()
        val completedList = if (habit.completedDates.isEmpty()) {
            emptyList()
        } else {
            habit.completedDates.split(",").toMutableList()
        }.toMutableList()

        val isCompletedToday = completedList.contains(today)
        val newCompletedDates: String
        val newStreak: Int

        if (isCompletedToday) {
            completedList.remove(today)
            newCompletedDates = completedList.joinToString(",")
            // Recalculate streak
            newStreak = (habit.streak - 1).coerceAtLeast(0)
        } else {
            completedList.add(today)
            newCompletedDates = completedList.joinToString(",")
            newStreak = habit.streak + 1
        }

        val updated = habit.copy(
            completedDates = newCompletedDates,
            streak = newStreak,
            lastCompletedDate = if (!isCompletedToday) today else habit.lastCompletedDate
        )
        repository.updateHabit(updated)
    }

    // --- Weather manual refresh ---
    fun refreshWeatherForce() = viewModelScope.launch {
        repository.refreshWeather(force = true)
    }

    // --- Focus Sound controller ---
    fun setFocusSound(soundType: String) {
        _selectedFocusSound.value = soundType
    }

    // --- Widgets Configuration Saver & Loader ---
    fun updateWidgetConfig(order: List<String>, enabled: Map<String, Boolean>) {
        _widgetsOrder.value = order
        _widgetsEnabled.value = enabled

        val prefs = getApplication<Application>().getSharedPreferences("zeno_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("widgets_order", order.joinToString(","))
            putString("widgets_enabled", enabled.filter { it.value }.keys.joinToString(","))
            apply()
        }
    }

    // --- Simulated Calling Exception Control ---
    fun simulateIncomingCall(caller: String = "Emergency Contact") {
        _callerName.value = caller
        _isIncomingCallActive.value = true
    }

    fun answerIncomingCall() {
        // Keeps user in the call overlay, simulated answering
        _callerName.value = "Active: " + _callerName.value
    }

    fun declineIncomingCall() {
        _isIncomingCallActive.value = false
    }

    // --- Sky Simulation Overrides for Testing ---
    fun setSkyTimeOverride(hour: Int) {
        _skyTimeOverride.value = hour
    }

    fun setSkyWeatherOverride(weatherStr: String) {
        _skyWeatherOverride.value = weatherStr
    }

    // --- Focus Mode State Machine ---
    fun enterFocusMode(task: Task? = null, durationMinutes: Int = 25, isZen: Boolean = false) {
        _focusTask.value = task
        _focusTimeRemaining.value = durationMinutes * 60
        _isZenMode.value = isZen
        _isFocusActive.value = true
        _isIncomingCallActive.value = false
        startFocusTimer()
        if (!isZen) {
            startBreathingCycle()
        }
    }

    fun exitFocusMode() {
        stopFocusTimer()
        stopBreathingCycle()
        _isFocusActive.value = false
        _isZenMode.value = false
        _isTimerRunning.value = false
        _isIncomingCallActive.value = false
    }

    fun toggleFocusTimer() {
        if (_isTimerRunning.value) {
            stopFocusTimer()
        } else {
            startFocusTimer()
        }
    }

    private fun startFocusTimer() {
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _focusTimeRemaining.value > 0) {
                delay(1000)
                _focusTimeRemaining.value -= 1
            }
            if (_focusTimeRemaining.value <= 0) {
                // Focus block complete!
                exitFocusMode()
            }
        }
    }

    private fun stopFocusTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    private fun startBreathingCycle() {
        breatheJob?.cancel()
        breatheJob = viewModelScope.launch {
            while (true) {
                // Harmonic 4-7-8 breathing or custom minimal 4-4 inhale-exhale rhythm
                // Inhale 4s
                _breatheState.value = "Inhale Safely"
                for (i in 1..40) {
                    _breatheProgress.value = i / 40f
                    delay(100)
                }
                
                // Hold 4s
                _breatheState.value = "Hold"
                delay(4000)

                // Exhale 4s
                _breatheState.value = "Exhale Purely"
                for (i in 40 downTo 1) {
                    _breatheProgress.value = i / 40f
                    delay(100)
                }

                // Pause 2s
                _breatheState.value = "Pause"
                delay(2000)
            }
        }
    }

    private fun stopBreathingCycle() {
        breatheJob?.cancel()
        _breatheProgress.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        breatheJob?.cancel()
    }
}
