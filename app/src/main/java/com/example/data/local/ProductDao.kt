package com.example.data.local

import androidx.room.*
import com.example.data.model.Product
import com.example.data.model.StockMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY nameAr ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND currentStock <= minStockLevel ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET currentStock = currentStock + :delta WHERE id = :productId")
    suspend fun updateStockQuantity(productId: Long, delta: Double)

    @Query("UPDATE products SET purchasePrice = :newPrice WHERE id = :productId")
    suspend fun updatePurchasePrice(productId: Long, newPrice: Double)

    @Query("SELECT * FROM products")
    suspend fun getAllProductsList(): List<Product>

    @Query("SELECT * FROM stock_movements")
    suspend fun getAllMovementsList(): List<StockMovement>

    @Query("SELECT unitPrice FROM stock_movements WHERE productId = :productId AND movementType = 'PURCHASE' AND unitPrice > 0 ORDER BY date DESC, id DESC LIMIT 1")
    suspend fun getLastPurchasePrice(productId: Long): Double?

    @Query("UPDATE stock_movements SET unitPrice = :unitPrice WHERE id = :movementId")
    suspend fun updateMovementUnitPrice(movementId: Long, unitPrice: Double)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int

    // Stock movements
    @Query("SELECT * FROM stock_movements ORDER BY date DESC, id DESC")
    fun getAllMovements(): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY date DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovement>)

    @Query("SELECT COUNT(*) FROM stock_movements WHERE productId = :productId")
    suspend fun getMovementCountForProduct(productId: Long): Int

    @Query("DELETE FROM stock_movements WHERE referenceType = :refType AND referenceId = :refId")
    suspend fun deleteMovementsForReference(refType: String, refId: Long)

    // Reset & Clear methods for system initialization
    @Query("DELETE FROM stock_movements")
    suspend fun deleteAllStockMovements()

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("UPDATE products SET currentStock = 0.0")
    suspend fun resetAllStockQuantities()

    @Query("UPDATE products SET minStockLevel = :minStock")
    suspend fun updateAllMinStock(minStock: Double)
}
