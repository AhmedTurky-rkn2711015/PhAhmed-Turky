package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unit_progress")
data class UnitProgressEntity(
    @PrimaryKey val unitId: Int,
    val isCompleted: Boolean = false,
    val quizScore: Int = 0,
    val bestScore: Int = 0,
    val completedAt: Long = 0L
)

@Entity(tableName = "abcde_tasks")
data class AbcdeTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "A", "B", "C", "D", "E"
    val priorityRank: Int = 1, // e.g. A1, A2
    val note: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskName: String,
    val targetMinutes: Int,
    val actualMinutes: Int,
    val completed: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "salami_projects")
data class SalamiProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val totalSlices: Int = 0,
    val completedSlices: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "salami_slices")
data class SalamiSliceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val isDone: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "dead_time_logs")
data class DeadTimeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "مواصلات", "استراحة قهوة", "انتظار", "غداء"
    val minutes: Int,
    val activity: String, // "كتاب صوتي", "بودكاست", "قراءة مقال"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int = 0,
    val currentStreak: Int = 1,
    val lastActiveDate: Long = System.currentTimeMillis(),
    val totalFocusMinutes: Int = 0,
    val tasksCompletedCount: Int = 0,
    val isCertificateUnlocked: Boolean = false
)
