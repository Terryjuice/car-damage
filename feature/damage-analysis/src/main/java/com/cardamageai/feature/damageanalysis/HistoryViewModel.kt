package com.cardamageai.feature.damageanalysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardamageai.core.database.dao.DamageAnalysisDao
import com.cardamageai.core.database.entities.DamageAnalysisEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val damageAnalysisDao: DamageAnalysisDao
) : ViewModel() {

    val analyses: StateFlow<List<DamageAnalysisEntity>> =
        damageAnalysisDao.getAllAnalyses()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

