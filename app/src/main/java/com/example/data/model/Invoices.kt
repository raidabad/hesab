package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PaymentType(val arabicName: String) {
    CASH("نقداً"),
    BANK("تحويل بنكي / شبكة"),
    CREDIT("آجل / على الحساب"),
    PARTIAL("دفع جزئي")
}

@Entity(tableName = "sales_invoices")
data class SalesInvoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val date: Long,
    val customerId: Long? = null,
    val customerName: String,
    val subtotal: Double,
    val discount: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentType: PaymentType = PaymentType.CASH,
    val journalEntryId: Long? = null,
    val notes: String = "",
    val isCancelled: Boolean = false
)

@Entity(
    tableName = "sales_invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = SalesInvoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["invoiceId"]), Index(value = ["productId"])]
)
data class SalesInvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val unitCost: Double = 0.0,
    val discount: Double = 0.0,
    val lineTotal: Double = (quantity * unitPrice) - discount
)

@Entity(tableName = "purchase_invoices")
data class PurchaseInvoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billNumber: String,
    val supplierInvoiceRef: String = "",
    val date: Long,
    val supplierId: Long? = null,
    val supplierName: String,
    val subtotal: Double,
    val discount: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentType: PaymentType = PaymentType.CASH,
    val journalEntryId: Long? = null,
    val notes: String = "",
    val isCancelled: Boolean = false
)

@Entity(
    tableName = "purchase_invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseInvoice::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["billId"]), Index(value = ["productId"])]
)
data class PurchaseInvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billId: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val lineTotal: Double = (quantity * unitPrice) - discount
)
