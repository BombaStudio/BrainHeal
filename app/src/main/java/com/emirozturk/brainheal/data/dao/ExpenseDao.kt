package com.emirozturk.brainheal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.emirozturk.brainheal.data.model.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE isImpulseWishlist = 0 ORDER BY date DESC")
    fun getActualExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE isImpulseWishlist = 1 AND isImpulsePurchased = 0 AND isImpulseCancelled = 0 ORDER BY impulseAddedAt DESC")
    fun getActiveImpulseWishlist(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}
