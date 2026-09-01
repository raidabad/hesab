package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AccountingRepository
import com.example.ui.components.Formatters
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppSection(val arabicTitle: String) {
    DASHBOARD("الرئيسية"),
    GENERAL_LEDGER("الأستاذ العام"),
    SALES("المبيعات"),
    PURCHASES("المشتريات"),
    INVENTORY("المخازن"),
    REPORTS("التقارير المالية")
}

data class DashboardStats(
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val netProfit: Double = 0.0,
    val inventoryCostValue: Double = 0.0,
    val cashAndBankBalance: Double = 0.0,
    val totalReceivables: Double = 0.0,
    val totalPayables: Double = 0.0,
    val lowStockCount: Int = 0,
    val totalAccountsCount: Int = 0,
    val totalJournalEntriesCount: Int = 0
)

data class TrialBalanceRow(
    val accountCode: String,
    val accountName: String,
    val accountType: AccountType,
    val debitBalance: Double,
    val creditBalance: Double
)

data class AccountStatementRow(
    val date: Long,
    val entryNumber: String,
    val description: String,
    val debit: Double,
    val credit: Double,
    val runningBalance: Double
)

class AccountingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AccountingRepository(
        database.accountDao(),
        database.journalDao(),
        database.productDao(),
        database.partnerDao(),
        database.invoiceDao()
    )

    // System Settings (SharedPreferences)
    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _currencySymbol = MutableStateFlow(prefs.getString("currency_symbol", "ر.س") ?: "ر.س")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _storeName = MutableStateFlow(prefs.getString("store_name", "الحساب المتكامل") ?: "الحساب المتكامل")
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _storePhone = MutableStateFlow(prefs.getString("store_phone", "") ?: "")
    val storePhone: StateFlow<String> = _storePhone.asStateFlow()

    // Current navigation section
    private val _currentSection = MutableStateFlow(AppSection.DASHBOARD)
    val currentSection: StateFlow<AppSection> = _currentSection.asStateFlow()

    // Snackbar / User message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Search queries
    val searchQuery = MutableStateFlow("")

    // Reactive Data Flows from Room
    val accounts = repository.allAccounts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val postingAccounts = repository.postingAccounts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val journalEntries = repository.allJournalEntries.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val products = repository.allProducts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val lowStockProducts = repository.lowStockProducts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val stockMovements = repository.allStockMovements.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val customers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val suppliers = repository.allSuppliers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val salesInvoices = repository.allSalesInvoices.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val purchaseInvoices = repository.allPurchaseInvoices.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val journalLines = repository.allJournalLines.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dashboard dynamic statistics
    val dashboardStats: StateFlow<DashboardStats> = combine(
        combine(accounts, products, customers) { accs, prods, custs ->
            Triple(accs, prods, custs)
        },
        combine(suppliers, salesInvoices, purchaseInvoices) { supps, sales, purchases ->
            Triple(supps, sales, purchases)
        },
        journalEntries
    ) { (accs, prods, custs), (supps, sales, purchases), entries ->
        val totalSales = sales.filter { !it.isCancelled }.sumOf { it.totalAmount }
        val totalPurchases = purchases.filter { !it.isCancelled }.sumOf { it.totalAmount }
        
        // Revenue minus Expenses from Chart of Accounts
        val totalRevenues = accs.filter { it.type == AccountType.REVENUE && !it.isGroup }.sumOf { it.currentBalance }
        val totalExpenses = accs.filter { it.type == AccountType.EXPENSE && !it.isGroup }.sumOf { it.currentBalance }
        val netProfit = totalRevenues - totalExpenses

        val inventoryCost = prods.sumOf { it.totalCostValue }
        
        // Cash & Bank accounts (codes starting with 111 or 112)
        val cashBank = accs.filter { (it.code.startsWith("111") || it.code.startsWith("112")) && !it.isGroup }
            .sumOf { it.currentBalance }

        val receivables = custs.sumOf { it.currentBalance }
        val payables = supps.sumOf { it.currentBalance }
        val lowStock = prods.count { it.isLowStock }

        DashboardStats(
            totalSales = totalSales,
            totalPurchases = totalPurchases,
            netProfit = netProfit,
            inventoryCostValue = inventoryCost,
            cashAndBankBalance = cashBank,
            totalReceivables = receivables,
            totalPayables = payables,
            lowStockCount = lowStock,
            totalAccountsCount = accs.count { !it.isGroup },
            totalJournalEntriesCount = entries.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    init {
        Formatters.currencySymbol = _currencySymbol.value
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            // Clean up any previous demo/sample transactions to initialize the system cleanly
            val isCleaned = prefs.getBoolean("system_data_cleaned_v2", false)
            if (!isCleaned) {
                repository.clearInvoicesAndTransactionsOnly()
                prefs.edit().putBoolean("system_data_cleaned_v2", true).apply()
            }
        }
    }

    fun clearInvoicesAndTransactions(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearInvoicesAndTransactionsOnly()
            showMessage("تم حذف جميع فواتير البيع والشراء والقيود وتصفير الأرصدة بنجاح")
            onSuccess()
        }
    }

    fun resetSystemCompletely(keepChartOfAccounts: Boolean = true, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resetSystemCompletely(keepChartOfAccounts)
            showMessage("تمت تهيئة وتصفير النظام بنجاح")
            onSuccess()
        }
    }

    fun updateCurrency(newSymbol: String) {
        val symbol = newSymbol.trim().ifEmpty { "ر.س" }
        _currencySymbol.value = symbol
        Formatters.currencySymbol = symbol
        prefs.edit().putString("currency_symbol", symbol).apply()
        showMessage("تم تغيير العملة بنجاح إلى: $symbol")
    }

    fun updateStoreSettings(name: String, phone: String, currency: String) {
        val validName = name.trim().ifEmpty { "الحساب المتكامل" }
        val validPhone = phone.trim()
        val validCurrency = currency.trim().ifEmpty { "ر.س" }

        _storeName.value = validName
        _storePhone.value = validPhone
        _currencySymbol.value = validCurrency
        Formatters.currencySymbol = validCurrency

        prefs.edit()
            .putString("store_name", validName)
            .putString("store_phone", validPhone)
            .putString("currency_symbol", validCurrency)
            .apply()

        showMessage("تم حفظ إعدادات النظام بنجاح")
    }

    fun setSection(section: AppSection) {
        _currentSection.value = section
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // -------------------------------------------------------------
    // General Ledger Actions & Calculations
    // -------------------------------------------------------------
    fun addAccount(account: Account, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addAccount(account)
            showMessage("تمت إضافة الحساب بنجاح: ${account.nameAr}")
            onSuccess()
        }
    }

    fun updateAccount(account: Account, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateAccount(account)
            showMessage("تم تعديل بيانات الحساب")
            onSuccess()
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            showMessage("تم حذف الحساب: ${account.nameAr}")
        }
    }

    fun createJournalEntry(
        entryNumber: String,
        date: Long,
        description: String,
        referenceNumber: String,
        lines: List<JournalEntryLine>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createJournalEntry(
                entryNumber, date, description, referenceNumber, lines, source = "MANUAL"
            )
            result.onSuccess {
                showMessage("تم ترحيل القيد المحاسبي بنجاح!")
                onSuccess()
            }.onFailure { ex ->
                val err = ex.message ?: "خطأ أثناء حفظ القيد"
                showMessage(err)
                onError(err)
            }
        }
    }

    suspend fun getJournalLinesForEntry(entryId: Long): List<JournalEntryLine> {
        return repository.getJournalLines(entryId)
    }

    // Trial Balance Calculation (ميزان المراجعة)
    fun calculateTrialBalance(): List<TrialBalanceRow> {
        val nonGroupAccounts = accounts.value.filter { !it.isGroup }
        return nonGroupAccounts.map { acc ->
            if (acc.type.isDebitDefault) {
                if (acc.currentBalance >= 0) {
                    TrialBalanceRow(acc.code, acc.nameAr, acc.type, debitBalance = acc.currentBalance, creditBalance = 0.0)
                } else {
                    TrialBalanceRow(acc.code, acc.nameAr, acc.type, debitBalance = 0.0, creditBalance = -acc.currentBalance)
                }
            } else {
                if (acc.currentBalance >= 0) {
                    TrialBalanceRow(acc.code, acc.nameAr, acc.type, debitBalance = 0.0, creditBalance = acc.currentBalance)
                } else {
                    TrialBalanceRow(acc.code, acc.nameAr, acc.type, debitBalance = -acc.currentBalance, creditBalance = 0.0)
                }
            }
        }
    }

    // Account Statement / Ledger (كشف حساب)
    fun getAccountStatement(accountId: Long): List<AccountStatementRow> {
        val acc = accounts.value.find { it.id == accountId } ?: return emptyList()
        val lines = journalLines.value.filter { it.accountId == accountId }
        val entries = journalEntries.value.associateBy { it.id }

        var running = acc.initialBalance
        val rows = mutableListOf<AccountStatementRow>()

        // Initial balance row if non zero
        if (acc.initialBalance != 0.0) {
            rows.add(
                AccountStatementRow(
                    date = 0L,
                    entryNumber = "افتتاحي",
                    description = "رصيد أول المدة",
                    debit = if (acc.type.isDebitDefault && acc.initialBalance > 0) acc.initialBalance else 0.0,
                    credit = if (!acc.type.isDebitDefault && acc.initialBalance > 0) acc.initialBalance else 0.0,
                    runningBalance = running
                )
            )
        }

        for (line in lines) {
            val entry = entries[line.entryId]
            if (acc.type.isDebitDefault) {
                running += (line.debit - line.credit)
            } else {
                running += (line.credit - line.debit)
            }
            rows.add(
                AccountStatementRow(
                    date = entry?.date ?: System.currentTimeMillis(),
                    entryNumber = entry?.entryNumber ?: "JV-${line.entryId}",
                    description = line.description.ifBlank { entry?.description ?: "" },
                    debit = line.debit,
                    credit = line.credit,
                    runningBalance = running
                )
            )
        }
        return rows
    }

    // -------------------------------------------------------------
    // Sales Actions
    // -------------------------------------------------------------
    fun createSalesInvoice(
        invoiceNumber: String,
        date: Long,
        customerId: Long?,
        customerName: String,
        items: List<SalesInvoiceItem>,
        discount: Double,
        taxRate: Double,
        paymentType: PaymentType,
        notes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createSalesInvoice(
                invoiceNumber, date, customerId, customerName, items, discount, taxRate, paymentType, notes
            )
            result.onSuccess {
                showMessage("تم إصدار فاتورة المبيعات وتحديث المخزون والقيود بنجاح!")
                onSuccess()
            }.onFailure { ex ->
                val err = ex.message ?: "خطأ في إنشاء الفاتورة"
                showMessage(err)
                onError(err)
            }
        }
    }

    suspend fun getSalesInvoiceItems(invoiceId: Long): List<SalesInvoiceItem> {
        return repository.getSalesInvoiceItems(invoiceId)
    }

    fun addCustomer(customer: Customer, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addCustomer(customer)
            showMessage("تمت إضافة العميل: ${customer.name}")
            onSuccess()
        }
    }

    fun updateCustomer(customer: Customer, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            showMessage("تم تحديث بيانات العميل")
            onSuccess()
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            showMessage("تم حذف العميل: ${customer.name}")
        }
    }

    // -------------------------------------------------------------
    // Purchases Actions
    // -------------------------------------------------------------
    fun createPurchaseInvoice(
        billNumber: String,
        supplierInvoiceRef: String,
        date: Long,
        supplierId: Long?,
        supplierName: String,
        items: List<PurchaseInvoiceItem>,
        discount: Double,
        taxRate: Double,
        paymentType: PaymentType,
        notes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createPurchaseInvoice(
                billNumber, supplierInvoiceRef, date, supplierId, supplierName, items, discount, taxRate, paymentType, notes
            )
            result.onSuccess {
                showMessage("تم حفظ فاتورة المشتريات وإضافة الكميات للمخزن بنجاح!")
                onSuccess()
            }.onFailure { ex ->
                val err = ex.message ?: "خطأ في حفظ فاتورة المشتريات"
                showMessage(err)
                onError(err)
            }
        }
    }

    suspend fun getPurchaseInvoiceItems(billId: Long): List<PurchaseInvoiceItem> {
        return repository.getPurchaseInvoiceItems(billId)
    }

    fun addSupplier(supplier: Supplier, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addSupplier(supplier)
            showMessage("تمت إضافة المورد: ${supplier.name}")
            onSuccess()
        }
    }

    fun updateSupplier(supplier: Supplier, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateSupplier(supplier)
            showMessage("تم تحديث بيانات المورد")
            onSuccess()
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
            showMessage("تم حذف المورد: ${supplier.name}")
        }
    }

    // -------------------------------------------------------------
    // Inventory Actions
    // -------------------------------------------------------------
    fun addProduct(product: Product, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addProduct(product)
            showMessage("تمت إضافة الصنف إلى المخزن: ${product.nameAr}")
            onSuccess()
        }
    }

    fun updateProduct(product: Product, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateProduct(product)
            showMessage("تم تعديل الصنف")
            onSuccess()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showMessage("تم حذف الصنف: ${product.nameAr}")
        }
    }

    fun adjustStock(
        productId: Long,
        quantity: Double,
        isAddition: Boolean,
        reason: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.adjustStock(productId, quantity, isAddition, reason)
            res.onSuccess {
                showMessage("تمت التسوية المخزنية بنجاح")
                onSuccess()
            }.onFailure { ex ->
                val err = ex.message ?: "خطأ في التسوية"
                showMessage(err)
                onError(err)
            }
        }
    }
}
