package com.timebasedfitness.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timebasedfitness.app.data.model.CompletionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionLogDao {

    @Query("SELECT * FROM completion_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<CompletionLog>>

    @Query("SELECT * FROM completion_logs ORDER BY date DESC")
    suspend fun getAllLogsSync(): List<CompletionLog>

    @Query("SELECT COUNT(*) FROM completion_logs WHERE category = :category AND date = :date")
    suspend fun getCountForDate(category: com.timebasedfitness.app.data.model.Category, date: java.time.LocalDate): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CompletionLog)
}
