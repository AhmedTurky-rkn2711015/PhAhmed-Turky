package com.example.data.repository

import com.example.data.database.dao.TimeMasteryDao
import com.example.data.database.entities.*
import com.example.data.model.LearningCurriculum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class TimeMasteryRepository(private val dao: TimeMasteryDao) {

    val allProgress: Flow<List<UnitProgressEntity>> = dao.getAllProgress()
    val allTasks: Flow<List<AbcdeTaskEntity>> = dao.getAllAbcdeTasks()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()
    val totalFocusMinutes: Flow<Int?> = dao.getTotalFocusMinutes()
    val allSalamiProjects: Flow<List<SalamiProjectEntity>> = dao.getAllSalamiProjects()
    val allDeadTimeLogs: Flow<List<DeadTimeLogEntity>> = dao.getAllDeadTimeLogs()
    val totalDeadTimeMinutes: Flow<Int?> = dao.getTotalDeadTimeSavedMinutes()
    val userStats: Flow<UserStatsEntity?> = dao.getUserStats()

    fun getSlicesForProject(projectId: Long): Flow<List<SalamiSliceEntity>> {
        return dao.getSlicesForProject(projectId)
    }

    suspend fun completeUnitQuiz(unitId: Int, score: Int): Boolean {
        val existing = dao.getProgressForUnit(unitId)
        val best = if (existing != null) maxOf(existing.bestScore, score) else score
        val progress = UnitProgressEntity(
            unitId = unitId,
            isCompleted = score >= 60, // passing threshold
            quizScore = score,
            bestScore = best,
            completedAt = System.currentTimeMillis()
        )
        dao.saveUnitProgress(progress)

        // Award XP
        val isFirstTime = existing == null || !existing.isCompleted
        if (progress.isCompleted && isFirstTime) {
            val unit = LearningCurriculum.units.find { it.id == unitId }
            val xpBonus = unit?.rewardXp ?: 150
            addXp(xpBonus)
        }
        return progress.isCompleted
    }

    suspend fun addXp(amount: Int) {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val updated = currentStats.copy(
            totalXp = currentStats.totalXp + amount
        )
        dao.saveUserStats(updated)
    }

    suspend fun incrementFocusStats(minutes: Int) {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val updated = currentStats.copy(
            totalFocusMinutes = currentStats.totalFocusMinutes + minutes,
            totalXp = currentStats.totalXp + (minutes / 2) // e.g. 60 min focus = 30 XP
        )
        dao.saveUserStats(updated)
    }

    suspend fun insertTask(title: String, category: String, rank: Int, note: String): Long {
        val entity = AbcdeTaskEntity(
            title = title,
            category = category,
            priorityRank = rank,
            note = note,
            isCompleted = false
        )
        val id = dao.insertAbcdeTask(entity)
        addXp(10) // reward for organizing
        return id
    }

    suspend fun toggleTaskComplete(task: AbcdeTaskEntity) {
        val newStatus = !task.isCompleted
        dao.setTaskCompleted(task.id, newStatus)
        if (newStatus) {
            addXp(25) // reward for completing task
            val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
            dao.saveUserStats(currentStats.copy(tasksCompletedCount = currentStats.tasksCompletedCount + 1))
        }
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteAbcdeTask(taskId)
    }

    suspend fun recordFocusSession(taskName: String, targetMins: Int, actualMins: Int, completed: Boolean): Long {
        val session = FocusSessionEntity(
            taskName = taskName,
            targetMinutes = targetMins,
            actualMinutes = actualMins,
            completed = completed,
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertFocusSession(session)
        if (completed) {
            incrementFocusStats(actualMins)
        }
        return id
    }

    suspend fun createSalamiProject(title: String, description: String, slices: List<String>): Long {
        val project = SalamiProjectEntity(
            title = title,
            description = description,
            totalSlices = slices.size,
            completedSlices = 0
        )
        val projectId = dao.insertSalamiProject(project)
        slices.forEachIndexed { index, sliceTitle ->
            if (sliceTitle.isNotBlank()) {
                dao.insertSalamiSlice(
                    SalamiSliceEntity(
                        projectId = projectId,
                        title = sliceTitle,
                        isDone = false,
                        orderIndex = index
                    )
                )
            }
        }
        addXp(30)
        return projectId
    }

    suspend fun toggleSalamiSlice(slice: SalamiSliceEntity, project: SalamiProjectEntity) {
        val updatedSlice = slice.copy(isDone = !slice.isDone)
        dao.updateSalamiSlice(updatedSlice)
        val delta = if (updatedSlice.isDone) 1 else -1
        val newCount = (project.completedSlices + delta).coerceIn(0, project.totalSlices)
        dao.insertSalamiProject(project.copy(completedSlices = newCount))
        if (updatedSlice.isDone) {
            addXp(15)
        }
    }

    suspend fun deleteSalamiProject(projectId: Long) {
        dao.deleteSlicesForProject(projectId)
        dao.deleteSalamiProject(projectId)
    }

    suspend fun logDeadTime(category: String, minutes: Int, activity: String): Long {
        val log = DeadTimeLogEntity(
            category = category,
            minutes = minutes,
            activity = activity,
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertDeadTimeLog(log)
        addXp(minutes) // 1 XP per minute saved
        return id
    }

    suspend fun initDefaultDataIfNeeded() {
        val stats = dao.getUserStats().firstOrNull()
        if (stats == null) {
            dao.saveUserStats(
                UserStatsEntity(
                    id = 1,
                    totalXp = 100,
                    currentStreak = 3,
                    lastActiveDate = System.currentTimeMillis(),
                    totalFocusMinutes = 90,
                    tasksCompletedCount = 5,
                    isCertificateUnlocked = false
                )
            )
            // Seed a starter sample task in ABCDE to guide user
            dao.insertAbcdeTask(
                AbcdeTaskEntity(
                    title = "إعداد التقرير الاستراتيجي السنوي للإدارة",
                    category = "A",
                    priorityRank = 1,
                    note = "مهمة ذات عواقب جسيمة - تمثل الـ 20% الحيوية",
                    isCompleted = false
                )
            )
            dao.insertAbcdeTask(
                AbcdeTaskEntity(
                    title = "مراجعة إيميلات غير عاجلة مع الزملاء",
                    category = "B",
                    priorityRank = 1,
                    note = "قاعدة ذهبية: لا تلمس هذه المهمة قبل إنهاء مهمة A1",
                    isCompleted = false
                )
            )
            dao.insertAbcdeTask(
                AbcdeTaskEntity(
                    title = "إعادة ترتيب أدراج المكتب وملفات الكمبيوتر",
                    category = "C",
                    priorityRank = 1,
                    note = "مهام هروب لطيفة لكن بدون عواقب إذا تركتها",
                    isCompleted = false
                )
            )
            // Seed a starter Dead Time log to showcase the power of University on Wheels
            dao.insertDeadTimeLog(
                DeadTimeLogEntity(
                    category = "مواصلات",
                    minutes = 45,
                    activity = "استماع لكتاب صوتي: فلسفة برايان تريسي لإدارة الوقت"
                )
            )
            // Seed sample Salami Project
            val projId = dao.insertSalamiProject(
                SalamiProjectEntity(
                    title = "إنهاء خطة التسويق للمشروع الجديد",
                    description = "تقنية السلامي: تفكيك الجبل إلى شرائح تافهة تكسر التسويف",
                    totalSlices = 3,
                    completedSlices = 1
                )
            )
            dao.insertSalamiSlice(
                SalamiSliceEntity(
                    projectId = projId,
                    title = "الشريحة 1: فتح ملف العرض وكتابة العنوان فقط (دقيقتين)",
                    isDone = true,
                    orderIndex = 0
                )
            )
            dao.insertSalamiSlice(
                SalamiSliceEntity(
                    projectId = projId,
                    title = "الشريحة 2: كتابة النقاط الثلاث الرئيسية بدون تفاصيل",
                    isDone = false,
                    orderIndex = 1
                )
            )
            dao.insertSalamiSlice(
                SalamiSliceEntity(
                    projectId = projId,
                    title = "الشريحة 3: إضافة شريحة الأرقام والجدول الزمني",
                    isDone = false,
                    orderIndex = 2
                )
            )
        }
    }
}
