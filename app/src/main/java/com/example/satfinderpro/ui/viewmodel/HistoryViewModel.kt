package com.example.satfinderpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.satfinderpro.data.model.Alignment
import com.example.satfinderpro.data.repository.AlignmentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val alignmentRepository: AlignmentRepository) : ViewModel() {
    val alignments: StateFlow<List<Alignment>> = alignmentRepository.getAllAlignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteAlignment(alignment: Alignment) {
        viewModelScope.launch {
            alignmentRepository.deleteAlignment(alignment)
        }
    }

    fun deleteAlignmentById(id: Int) {
        viewModelScope.launch {
            alignmentRepository.deleteAlignmentById(id)
        }
    }
}
