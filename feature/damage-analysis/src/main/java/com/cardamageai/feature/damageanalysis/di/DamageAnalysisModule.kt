package com.cardamageai.feature.damageanalysis.di

import com.cardamageai.feature.damageanalysis.DamageAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DamageAnalysisModule {
    
    @Provides
    @Singleton
    fun provideDamageAnalyzer(
        damageAnalyzer: DamageAnalyzer
    ): DamageAnalyzer = damageAnalyzer
}