package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeMasteryDao {

    // --- Unit Progress ---
    @Query("SELECT * FROM unit_progress")
    fun getAllProgress(): Flow<List<UnitProgressEntity>>

    @Query("SELECT * FROM unit_progress WHERE unitId = :unitId LIMIT 1")
    suspend fun getProgressForUnit(unitId: Int): UnitProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUnitProgress(progress: UnitProgressEntity)

    // --- ABCDE Tasks ---
    @Query("SELECT * FROM abcde_tasks ORDER BY isCompleted ASC, category ASC, priorityRank ASC, createdAt DESC")
    fun getAllAbcdeTasks(): Flow<List<AbcdeTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbcdeTask(task: AbcdeTaskEntity): Long

    @Update
    suspend fun updateAbcdeTask(task: AbcdeTaskEntity)

    @Query("DELETE FROM abcde_tasks WHERE id = :taskId")
    suspend fun deleteAbcdeTask(taskId: Long)

    @Query("UPDATE abcde_tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean)

    // --- Focus Sessions ---
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Query("SELECT SUM(actualMinutes) FROM focus_sessions WHERE completed = 1")
    fun getTotalFocusMinutes(): Flow<Int?>

    // --- Salami Projects & Slices ---
    @Query("SELECT * FROM salami_projects ORDER BY createdAt DESC")
    fun getAllSalamiProjects(): Flow<List<SalamiProjectEntity>>

    @Query("SELECT * FROM salami_slices WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun getSlicesForProject(projectId: Long): Flow<List<SalamiSliceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalamiProject(project: SalamiProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalamiSlice(slice: SalamiSliceEntity): Long

    @Update
    suspend fun updateSalamiSlice(slice: SalamiSliceEntity)

    @Query("DELETE FROM salami_projects WHERE id = :projectId")
    suspend fun deleteSalamiProject(projectId: Long)

    @Query("DELETE FROM salami_slices WHERE projectId = :projectId")
    suspend fun deleteSlicesForProject(projectId: Long)

    // --- Dead Time Logs ---
    @Query("SELECT * FROM dead_time_logs ORDER BY timestamp DESC")
    fun getAllDeadTimeLogs(): Flow<List<DeadTimeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeadTimeLog(log: DeadTimeLogEntity): Long

    @Query("SELECT SUM(minutes) FROM dead_time_logs")
    fun getTotalDeadTimeSavedMinutes(): Flow<Int?>

    // --- User Stats ---
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserStats(stats: UserStatsEntity)
}
