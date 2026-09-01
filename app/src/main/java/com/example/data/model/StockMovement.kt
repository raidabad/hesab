package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MovementType(val arabicName: String) {
    PURCHASE("شراء / توريد"),
    SALE("بيع / صرف"),
    ADJUSTMENT_ADD("تسوية إضافة"),
    ADJUSTMENT_SUB("تسوية عجز / إنقاص"),
    RETURN_IN("مرتجع مبيعات"),
    RETURN_OUT("مرتجع مشتريات")
}

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val date: Long,
    val movementType: MovementType,
    val quantity: Double,
    val unitPrice: Double,
    val totalCost: Double = quantity * unitPrice,
    val referenceType: String = "", // SALE_INVOICE, PURCHASE_INVOICE, MANUAL_ADJUSTMENT
    val referenceId: Long? = null,
    val referenceNumber: String = "",
    val notes: String = ""
)
