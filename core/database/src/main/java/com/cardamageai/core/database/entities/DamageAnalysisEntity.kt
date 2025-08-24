package com.cardamageai.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "damage_analysis")
data class DamageAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUri: String,
    val damageType: String,
    val severityLevel: Int, // 1-5 scale
    val confidence: Float,
    val estimatedCost: Double,
    val timestamp: Long,
    val description: String?
)