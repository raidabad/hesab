package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryNumber: String,
    val date: Long,
    val description: String,
    val referenceNumber: String = "",
    val source: String = "MANUAL", // MANUAL, SALES, PURCHASES, INVENTORY
    val totalDebit: Double,
    val totalCredit: Double,
    val isPosted: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "journal_entry_lines",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["entryId"]), Index(value = ["accountId"])]
)
data class JournalEntryLine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: Long = 0,
    val accountId: Long,
    val accountCode: String,
    val accountName: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val description: String = ""
)
