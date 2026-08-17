package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FlexyTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FlexyDao {
    @Query("SELECT * FROM flexy_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FlexyTransaction>>

    @Query("SELECT * FROM flexy_transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<FlexyTransaction>>

    @Query("SELECT * FROM flexy_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): FlexyTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FlexyTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: FlexyTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: FlexyTransaction)

    @Query("DELETE FROM flexy_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM flexy_transactions")
    suspend fun clearAll()
}
