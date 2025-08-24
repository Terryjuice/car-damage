package com.cardamageai.core.database.di

import android.content.Context
import androidx.room.Room
import com.cardamageai.core.database.CarDamageDatabase
import com.cardamageai.core.database.dao.DamageAnalysisDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideCarDamageDatabase(
        @ApplicationContext context: Context
    ): CarDamageDatabase {
        return Room.databaseBuilder(
            context,
            CarDamageDatabase::class.java,
            CarDamageDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    fun provideDamageAnalysisDao(database: CarDamageDatabase): DamageAnalysisDao {
        return database.damageAnalysisDao()
    }
}