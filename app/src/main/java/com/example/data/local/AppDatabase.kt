package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.*

@Database(
    entities = [
        Account::class,
        JournalEntry::class,
        JournalEntryLine::class,
        Product::class,
        StockMovement::class,
        Customer::class,
        Supplier::class,
        SalesInvoice::class,
        SalesInvoiceItem::class,
        PurchaseInvoice::class,
        PurchaseInvoiceItem::class,
        Voucher::class,
        SalesReturn::class,
        SalesReturnItem::class,
        PurchaseReturn::class,
        PurchaseReturnItem::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun journalDao(): JournalDao
    abstract fun productDao(): ProductDao
    abstract fun partnerDao(): PartnerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun voucherDao(): VoucherDao
    abstract fun returnDao(): ReturnDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "accounting_app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

