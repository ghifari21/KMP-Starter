package com.project.starter.core.data.source.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.starter.core.model.entity.ExampleEntity
import kotlinx.coroutines.flow.Flow

// @Dao
interface ExampleDao {
    // @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExample(example: ExampleEntity)

    // @Query("SELECT * FROM example_table")
    fun getExamples(): Flow<List<ExampleEntity>>
}
