package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType(val arabicName: String, val isDebitDefault: Boolean) {
    ASSET("أصول", true),
    LIABILITY("التزامات / خصوم", false),
    EQUITY("حقوق الملكية", false),
    REVENUE("إيرادات", false),
    EXPENSE("مصروفات", true)
}

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val nameAr: String,
    val nameEn: String = "",
    val type: AccountType,
    val parentId: Long? = null,
    val isGroup: Boolean = false,
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val notes: String = "",
    val isActive: Boolean = true
)
