package com.example.satfinderpro.data.repository

import com.example.satfinderpro.data.dao.AlignmentDao
import com.example.satfinderpro.data.model.Alignment
import kotlinx.coroutines.flow.Flow

class AlignmentRepository(private val alignmentDao: AlignmentDao) {
    suspend fun saveAlignment(alignment: Alignment) {
        alignmentDao.insertAlignment(alignment)
    }

    fun getAllAlignments(): Flow<List<Alignment>> = alignmentDao.getAllAlignments()

    suspend fun deleteAlignment(alignment: Alignment) {
        alignmentDao.deleteAlignment(alignment)
    }

    suspend fun deleteAlignmentById(id: Int) {
        alignmentDao.deleteAlignmentById(id)
    }
}
