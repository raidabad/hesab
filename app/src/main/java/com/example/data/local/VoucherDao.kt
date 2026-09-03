package com.example.data.local

import androidx.room.*
import com.example.data.model.Voucher
import com.example.data.model.VoucherType
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    @Query("SELECT * FROM vouchers ORDER BY date DESC, id DESC")
    fun getAllVouchers(): Flow<List<Voucher>>

    @Query("SELECT * FROM vouchers WHERE type = :type ORDER BY date DESC, id DESC")
    fun getVouchersByType(type: VoucherType): Flow<List<Voucher>>

    @Query("SELECT * FROM vouchers WHERE id = :id")
    suspend fun getVoucherById(id: Long): Voucher?

    @Query("SELECT COUNT(*) FROM vouchers WHERE type = :type")
    suspend fun getCountByType(type: VoucherType): Int

    @Query("SELECT COUNT(*) FROM vouchers WHERE partnerId = :partnerId")
    suspend fun getCountByPartnerId(partnerId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: Voucher): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVouchers(vouchers: List<Voucher>)

    @Query("SELECT * FROM vouchers")
    suspend fun getAllVouchersList(): List<Voucher>

    @Update
    suspend fun updateVoucher(voucher: Voucher)

    @Delete
    suspend fun deleteVoucher(voucher: Voucher)

    @Query("DELETE FROM vouchers")
    suspend fun deleteAllVouchers()
}
