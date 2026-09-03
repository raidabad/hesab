package com.example.data.local

import androidx.room.*
import com.example.data.model.PurchaseReturn
import com.example.data.model.PurchaseReturnItem
import com.example.data.model.SalesReturn
import com.example.data.model.SalesReturnItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnDao {
    // Sales Returns
    @Query("SELECT * FROM sales_returns ORDER BY date DESC, id DESC")
    fun getAllSalesReturns(): Flow<List<SalesReturn>>

    @Query("SELECT * FROM sales_returns WHERE id = :id")
    suspend fun getSalesReturnById(id: Long): SalesReturn?

    @Query("SELECT * FROM sales_return_items WHERE returnId = :returnId")
    suspend fun getSalesReturnItems(returnId: Long): List<SalesReturnItem>

    @Query("SELECT COUNT(*) FROM sales_returns")
    suspend fun getSalesReturnCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesReturn(returnRecord: SalesReturn): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesReturns(returns: List<SalesReturn>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesReturnItems(items: List<SalesReturnItem>)

    @Query("SELECT * FROM sales_returns")
    suspend fun getAllSalesReturnsList(): List<SalesReturn>

    @Query("SELECT * FROM sales_return_items")
    suspend fun getAllSalesReturnItemsList(): List<SalesReturnItem>

    @Query("UPDATE sales_return_items SET unitCost = :unitCost WHERE id = :itemId")
    suspend fun updateSalesReturnItemCost(itemId: Long, unitCost: Double)

    @Update
    suspend fun updateSalesReturn(returnRecord: SalesReturn)

    @Delete
    suspend fun deleteSalesReturn(returnRecord: SalesReturn)

    @Query("DELETE FROM sales_return_items WHERE returnId = :returnId")
    suspend fun deleteSalesReturnItems(returnId: Long)

    // Purchase Returns
    @Query("SELECT * FROM purchase_returns ORDER BY date DESC, id DESC")
    fun getAllPurchaseReturns(): Flow<List<PurchaseReturn>>

    @Query("SELECT * FROM purchase_returns WHERE id = :id")
    suspend fun getPurchaseReturnById(id: Long): PurchaseReturn?

    @Query("SELECT * FROM purchase_return_items WHERE returnId = :returnId")
    suspend fun getPurchaseReturnItems(returnId: Long): List<PurchaseReturnItem>

    @Query("SELECT COUNT(*) FROM purchase_returns")
    suspend fun getPurchaseReturnCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseReturn(returnRecord: PurchaseReturn): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseReturns(returns: List<PurchaseReturn>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseReturnItems(items: List<PurchaseReturnItem>)

    @Query("SELECT * FROM purchase_returns")
    suspend fun getAllPurchaseReturnsList(): List<PurchaseReturn>

    @Query("SELECT * FROM purchase_return_items")
    suspend fun getAllPurchaseReturnItemsList(): List<PurchaseReturnItem>

    @Update
    suspend fun updatePurchaseReturn(returnRecord: PurchaseReturn)

    @Delete
    suspend fun deletePurchaseReturn(returnRecord: PurchaseReturn)

    @Query("DELETE FROM purchase_return_items WHERE returnId = :returnId")
    suspend fun deletePurchaseReturnItems(returnId: Long)

    // Clear all
    @Query("DELETE FROM sales_returns")
    suspend fun deleteAllSalesReturns()

    @Query("DELETE FROM sales_return_items")
    suspend fun deleteAllSalesReturnItems()

    @Query("DELETE FROM purchase_returns")
    suspend fun deleteAllPurchaseReturns()

    @Query("DELETE FROM purchase_return_items")
    suspend fun deleteAllPurchaseReturnItems()
}
