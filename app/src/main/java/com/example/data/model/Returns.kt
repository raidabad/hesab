package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sales_returns")
data class SalesReturn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val returnNumber: String, // 1, 2, 3...
    val originalInvoiceNumber: String = "",
    val date: Long,
    val customerId: Long? = null,
    val customerName: String,
    val subtotal: Double,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentType: PaymentType = PaymentType.CASH,
    val journalEntryId: Long? = null,
    val notes: String = ""
)

@Entity(
    tableName = "sales_return_items",
    foreignKeys = [
        ForeignKey(
            entity = SalesReturn::class,
            parentColumns = ["id"],
            childColumns = ["returnId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["returnId"]), Index(value = ["productId"])]
)
data class SalesReturnItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val returnId: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val unitCost: Double = 0.0,
    val lineTotal: Double = quantity * unitPrice
)

@Entity(tableName = "purchase_returns")
data class PurchaseReturn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val returnNumber: String, // 1, 2, 3...
    val originalBillNumber: String = "",
    val date: Long,
    val supplierId: Long? = null,
    val supplierName: String,
    val subtotal: Double,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentType: PaymentType = PaymentType.CASH,
    val journalEntryId: Long? = null,
    val notes: String = ""
)

@Entity(
    tableName = "purchase_return_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseReturn::class,
            parentColumns = ["id"],
            childColumns = ["returnId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["returnId"]), Index(value = ["productId"])]
)
data class PurchaseReturnItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val returnId: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val lineTotal: Double = quantity * unitPrice
)
