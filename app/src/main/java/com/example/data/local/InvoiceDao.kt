package com.example.data.local

import androidx.room.*
import com.example.data.model.PurchaseInvoice
import com.example.data.model.PurchaseInvoiceItem
import com.example.data.model.SalesInvoice
import com.example.data.model.SalesInvoiceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    // Sales
    @Query("SELECT * FROM sales_invoices ORDER BY date DESC, id DESC")
    fun getAllSalesInvoices(): Flow<List<SalesInvoice>>

    @Query("SELECT * FROM sales_invoices WHERE id = :id")
    suspend fun getSalesInvoiceById(id: Long): SalesInvoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesInvoice(invoice: SalesInvoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesInvoiceItems(items: List<SalesInvoiceItem>)

    @Query("SELECT * FROM sales_invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getSalesInvoiceItems(invoiceId: Long): List<SalesInvoiceItem>

    // Purchases
    @Query("SELECT * FROM purchase_invoices ORDER BY date DESC, id DESC")
    fun getAllPurchaseInvoices(): Flow<List<PurchaseInvoice>>

    @Query("SELECT * FROM purchase_invoices WHERE id = :id")
    suspend fun getPurchaseInvoiceById(id: Long): PurchaseInvoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseInvoice(invoice: PurchaseInvoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseInvoiceItems(items: List<PurchaseInvoiceItem>)

    @Query("SELECT * FROM purchase_invoice_items WHERE billId = :billId")
    suspend fun getPurchaseInvoiceItems(billId: Long): List<PurchaseInvoiceItem>
}
