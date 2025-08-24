package com.cardamageai.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cardamageai.core.database.entities.DamageAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DamageAnalysisDao {
    
    @Query("SELECT * FROM damage_analysis ORDER BY timestamp DESC")
    fun getAllAnalyses(): Flow<List<DamageAnalysisEntity>>
    
    @Query("SELECT * FROM damage_analysis WHERE id = :id")
    suspend fun getAnalysisById(id: Long): DamageAnalysisEntity?
    
    @Insert
    suspend fun insertAnalysis(analysis: DamageAnalysisEntity): Long
    
    @Query("DELETE FROM damage_analysis WHERE id = :id")
    suspend fun deleteAnalysis(id: Long)
}