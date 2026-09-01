package com.example.data.local

import androidx.room.*
import com.example.data.model.JournalEntry
import com.example.data.model.JournalEntryLine
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): JournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<JournalEntryLine>)

    @Query("SELECT * FROM journal_entry_lines WHERE entryId = :entryId")
    suspend fun getLinesForEntry(entryId: Long): List<JournalEntryLine>

    @Query("SELECT * FROM journal_entry_lines")
    fun getAllLines(): Flow<List<JournalEntryLine>>

    @Query("SELECT * FROM journal_entry_lines WHERE accountId = :accountId ORDER BY id ASC")
    suspend fun getLinesForAccount(accountId: Long): List<JournalEntryLine>
}
