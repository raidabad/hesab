package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val taxNumber: String = "",
    val address: String = "",
    val currentBalance: Double = 0.0, // Amount owed by customer (مدين)
    val notes: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val taxNumber: String = "",
    val address: String = "",
    val currentBalance: Double = 0.0, // Amount owed to supplier (دائن)
    val notes: String = "",
    val isActive: Boolean = true
)
