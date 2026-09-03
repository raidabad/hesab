package com.example.data.local

import androidx.room.*
import com.example.data.model.Customer
import com.example.data.model.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface PartnerDao {
    // Customers
    @Query("SELECT * FROM customers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersList(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET currentBalance = currentBalance + :delta WHERE id = :customerId")
    suspend fun updateCustomerBalance(customerId: Long, delta: Double)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int

    // Suppliers
    @Query("SELECT * FROM suppliers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers")
    suspend fun getAllSuppliersList(): List<Supplier>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<Supplier>)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("UPDATE suppliers SET currentBalance = currentBalance + :delta WHERE id = :supplierId")
    suspend fun updateSupplierBalance(supplierId: Long, delta: Double)

    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun getSupplierCount(): Int

    // Reset & Clear methods for system initialization
    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()

    @Query("DELETE FROM suppliers")
    suspend fun deleteAllSuppliers()

    @Query("UPDATE customers SET currentBalance = 0.0")
    suspend fun resetAllCustomerBalances()

    @Query("UPDATE suppliers SET currentBalance = 0.0")
    suspend fun resetAllSupplierBalances()
}
