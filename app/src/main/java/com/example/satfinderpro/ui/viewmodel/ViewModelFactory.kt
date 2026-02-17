package com.example.satfinderpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.satfinderpro.data.repository.AlignmentRepository
import com.example.satfinderpro.data.repository.UserRepository

class ViewModelFactory(
    private val userRepository: UserRepository,
    private val alignmentRepository: AlignmentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AuthViewModel::class.java -> AuthViewModel(userRepository) as T
            SatFinderViewModel::class.java -> SatFinderViewModel(alignmentRepository) as T
            HistoryViewModel::class.java -> HistoryViewModel(alignmentRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
