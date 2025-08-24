package com.cardamageai.core.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.cardamageai.core.database.dao.DamageAnalysisDao
import com.cardamageai.core.database.entities.DamageAnalysisEntity

@Database(
    entities = [DamageAnalysisEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CarDamageDatabase : RoomDatabase() {
    
    abstract fun damageAnalysisDao(): DamageAnalysisDao
    
    companion object {
        const val DATABASE_NAME = "car_damage_database"
    }
}