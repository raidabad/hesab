package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val barcode: String = "",
    val nameAr: String,
    val category: String = "عام",
    val unit: String = "قطعة",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Double = 0.0,
    val minStockLevel: Double = 5.0,
    val notes: String = "",
    val isActive: Boolean = true
) {
    val totalCostValue: Double
        get() = currentStock * purchasePrice

    val isLowStock: Boolean
        get() = currentStock <= minStockLevel
}
