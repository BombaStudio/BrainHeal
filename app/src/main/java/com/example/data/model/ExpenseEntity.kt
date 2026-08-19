package com.example.data.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R

enum class TransactionType(
    @StringRes val stringRes: Int,
    val labelTr: String,
    val labelEn: String
) {
    EXPENSE(R.string.expense_type_expense, "Gider", "Expense"),
    INCOME(R.string.expense_type_income, "Gelir", "Income");

    fun getLabel(): String {
        return if (java.util.Locale.getDefault().language == "tr") labelTr else labelEn
    }
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "Genel", // Yemek, Ulaşım, Alışveriş, Faturalar, Eğlence, Sağlık, vb.
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    // ADHD Impulse Spending Protection (Dürtüsel Harcama Soğuma Süresi)
    val isImpulseWishlist: Boolean = false,
    val coolingHours: Int = 48, // default 48h cooling off
    val impulseAddedAt: Long = System.currentTimeMillis(),
    val isImpulsePurchased: Boolean = false,
    val isImpulseCancelled: Boolean = false
) {
    fun isCoolingActive(): Boolean {
        if (!isImpulseWishlist || isImpulsePurchased || isImpulseCancelled) return false
        val passedHours = (System.currentTimeMillis() - impulseAddedAt) / (1000 * 60 * 60)
        return passedHours < coolingHours
    }

    fun remainingCoolingHours(): Int {
        val passedHours = (System.currentTimeMillis() - impulseAddedAt) / (1000 * 60 * 60)
        val remaining = coolingHours - passedHours.toInt()
        return if (remaining > 0) remaining else 0
    }
}
