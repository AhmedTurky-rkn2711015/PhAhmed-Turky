package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.entities.*
import com.example.data.model.LearningCurriculum
import com.example.data.model.LearningUnit
import com.example.data.repository.TimeMasteryRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    CURRICULUM("الوحدات التعليمية"),
    TOOLS("الأدوات والحيل"),
    REPORTS("تقارير الأداء"),
    GAMIFICATION("الإنجازات والتلعيب")
}

data class QuizState(
    val unit: LearningUnit? = null,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isSubmitted: Boolean = false,
    val correctCount: Int = 0,
    val isFinished: Boolean = false,
    val earnedScore: Int = 0
)

data class TimerState(
    val isRunning: Boolean = false,
    val targetMinutes: Int = 25,
    val secondsRemaining: Int = 25 * 60,
    val taskName: String = "جلسة تركيز عميق (وضع الطيران)",
    val isSessionComplete: Boolean = false
)

class TimeMasteryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimeMasteryRepository

    val selectedTab = MutableStateFlow(AppTab.CURRICULUM)

    // Curriculum & Progress
    val unitProgressList: StateFlow<List<UnitProgressEntity>>
    val quizState = MutableStateFlow<QuizState?>(null)

    // ABCDE Tasks
    val tasks: StateFlow<List<AbcdeTaskEntity>>

    // Focus & Timer
    val focusSessions: StateFlow<List<FocusSessionEntity>>
    val totalFocusMinutes: StateFlow<Int>
    val timerState = MutableStateFlow(TimerState())
    private var timerJob: Job? = null

    // Salami Projects
    val salamiProjects: StateFlow<List<SalamiProjectEntity>>

    // Dead Time Logs
    val deadTimeLogs: StateFlow<List<DeadTimeLogEntity>>
    val totalDeadTimeMinutes: StateFlow<Int>

    // Gamification & User Stats
    val userStats: StateFlow<UserStatsEntity?>

    // Daily quote & toast messages
    val dailyQuote = MutableStateFlow(NotificationHelper.quotes.first())
    val snackbarMessage = MutableStateFlow<String?>(null)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TimeMasteryRepository(db.timeMasteryDao())

        unitProgressList = repository.allProgress
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        tasks = repository.allTasks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        focusSessions = repository.allFocusSessions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        totalFocusMinutes = repository.totalFocusMinutes
            .map { it ?: 0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        salamiProjects = repository.allSalamiProjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        deadTimeLogs = repository.allDeadTimeLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        totalDeadTimeMinutes = repository.totalDeadTimeMinutes
            .map { it ?: 0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        userStats = repository.userStats
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            refreshQuote()
        }
    }

    fun selectTab(tab: AppTab) {
        selectedTab.value = tab
    }

    fun refreshQuote() {
        dailyQuote.value = NotificationHelper.quotes.random()
    }

    fun triggerNotification() {
        val quote = dailyQuote.value
        NotificationHelper.sendMotivationalNotification(getApplication(), quote)
        snackbarMessage.value = "تم إرسال إشعار تحفيزي بنجاح!"
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }

    // --- Quiz Logic ---
    fun startQuiz(unit: LearningUnit) {
        quizState.value = QuizState(
            unit = unit,
            currentQuestionIndex = 0,
            selectedOptionIndex = null,
            isSubmitted = false,
            correctCount = 0,
            isFinished = false,
            earnedScore = 0
        )
    }

    fun selectQuizOption(index: Int) {
        val current = quizState.value ?: return
        if (!current.isSubmitted) {
            quizState.value = current.copy(selectedOptionIndex = index)
        }
    }

    fun submitAnswer() {
        val current = quizState.value ?: return
        val currentUnit = current.unit ?: return
        val question = currentUnit.questions.getOrNull(current.currentQuestionIndex) ?: return
        if (current.selectedOptionIndex == null) return

        val isCorrect = current.selectedOptionIndex == question.correctAnswerIndex
        val newCorrectCount = if (isCorrect) current.correctCount + 1 else current.correctCount

        quizState.value = current.copy(
            isSubmitted = true,
            correctCount = newCorrectCount
        )
    }

    fun nextQuestion() {
        val current = quizState.value ?: return
        val currentUnit = current.unit ?: return
        val nextIdx = current.currentQuestionIndex + 1

        if (nextIdx < currentUnit.questions.size) {
            quizState.value = current.copy(
                currentQuestionIndex = nextIdx,
                selectedOptionIndex = null,
                isSubmitted = false
            )
        } else {
            // Quiz completed!
            val totalQuestions = currentUnit.questions.size
            val finalScore = (current.correctCount.toFloat() / totalQuestions * 100).toInt()
            quizState.value = current.copy(
                isFinished = true,
                earnedScore = finalScore
            )

            viewModelScope.launch {
                val passed = repository.completeUnitQuiz(currentUnit.id, finalScore)
                if (passed) {
                    snackbarMessage.value = "تهانينا! أكملت ${currentUnit.title} وحصلت على ${currentUnit.rewardBadge} و +${currentUnit.rewardXp} نقطة XP!"
                    NotificationHelper.sendMotivationalNotification(
                        getApplication(),
                        "إنجاز رائع! أتممت ${currentUnit.title} بنجاح بنسبة $finalScore%!"
                    )
                }
            }
        }
    }

    fun closeQuiz() {
        quizState.value = null
    }

    // --- ABCDE Task Operations ---
    fun addTask(title: String, category: String, rank: Int, note: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(title.trim(), category, rank, note.trim())
            snackbarMessage.value = "تمت إضافة المهمة إلى مصفوفة $category$rank بنجاح"
        }
    }

    fun toggleTask(task: AbcdeTaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskComplete(task)
            if (!task.isCompleted) {
                snackbarMessage.value = "أحسنت! إنجاز مهمة يمنحك +25 XP"
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    // --- Focus Timer ---
    fun setTimerDuration(minutes: Int) {
        if (timerState.value.isRunning) return
        timerState.value = timerState.value.copy(
            targetMinutes = minutes,
            secondsRemaining = minutes * 60,
            isSessionComplete = false
        )
    }

    fun startTimer(customTaskName: String? = null) {
        val task = customTaskName ?: timerState.value.taskName
        timerState.value = timerState.value.copy(
            isRunning = true,
            taskName = task,
            isSessionComplete = false
        )
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timerState.value.secondsRemaining > 0 && timerState.value.isRunning) {
                delay(1000L)
                val remaining = timerState.value.secondsRemaining - 1
                timerState.value = timerState.value.copy(secondsRemaining = remaining)
            }
            if (timerState.value.secondsRemaining <= 0) {
                finishTimerSession(true)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerState.value = timerState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        val mins = timerState.value.targetMinutes
        timerState.value = TimerState(
            isRunning = false,
            targetMinutes = mins,
            secondsRemaining = mins * 60,
            taskName = timerState.value.taskName,
            isSessionComplete = false
        )
    }

    fun finishTimerSession(completedFull: Boolean) {
        timerJob?.cancel()
        val targetMins = timerState.value.targetMinutes
        val elapsedSeconds = (targetMins * 60) - timerState.value.secondsRemaining
        val actualMinutes = maxOf(1, elapsedSeconds / 60)

        viewModelScope.launch {
            repository.recordFocusSession(
                taskName = timerState.value.taskName,
                targetMins = targetMins,
                actualMins = actualMinutes,
                completed = completedFull
            )
            timerState.value = timerState.value.copy(
                isRunning = false,
                isSessionComplete = true
            )
            snackbarMessage.value = "جلسة تركيز رائعة! أضفت $actualMinutes دقيقة إلى رصيدك وحصلت على نقاط XP!"
            NotificationHelper.sendMotivationalNotification(
                getApplication(),
                "أتممت جلسة تركيز عميق لمدة $actualMinutes دقيقة! عقلك الآن في قمة التدفق والإنتاجية."
            )
        }
    }

    // --- Salami Projects ---
    fun addSalamiProject(title: String, description: String, slices: List<String>) {
        if (title.isBlank() || slices.isEmpty()) return
        viewModelScope.launch {
            repository.createSalamiProject(title.trim(), description.trim(), slices)
            snackbarMessage.value = "تم إنشاء مشروع السلامي وتفكيك المهمة بنجاح!"
        }
    }

    fun getSlicesForProject(projectId: Long): Flow<List<SalamiSliceEntity>> {
        return repository.getSlicesForProject(projectId)
    }

    fun toggleSalamiSlice(slice: SalamiSliceEntity, project: SalamiProjectEntity) {
        viewModelScope.launch {
            repository.toggleSalamiSlice(slice, project)
        }
    }

    fun deleteSalamiProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteSalamiProject(projectId)
        }
    }

    // --- Dead Time Logs ---
    fun logDeadTime(category: String, minutes: Int, activity: String) {
        if (minutes <= 0) return
        viewModelScope.launch {
            repository.logDeadTime(category, minutes, activity.trim())
            snackbarMessage.value = "تم استثمار $minutes دقيقة من الفتات وتحويلها إلى ثروة معرفية (+${minutes} XP)!"
        }
    }
}
