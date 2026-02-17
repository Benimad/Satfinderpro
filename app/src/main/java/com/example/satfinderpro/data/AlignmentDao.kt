package com.example.satfinderpro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.satfinderpro.data.model.Alignment
import kotlinx.coroutines.flow.Flow

@Dao
interface AlignmentDao {
    @Insert
    suspend fun insertAlignment(alignment: Alignment)

    @Query("SELECT * FROM alignments ORDER BY timestamp DESC")
    fun getAllAlignments(): Flow<List<Alignment>>

    @Query("SELECT * FROM alignments WHERE id = :id")
    suspend fun getAlignmentById(id: Int): Alignment?

    @Delete
    suspend fun deleteAlignment(alignment: Alignment)

    @Query("DELETE FROM alignments WHERE id = :id")
    suspend fun deleteAlignmentById(id: Int)
}
