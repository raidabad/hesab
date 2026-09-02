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
import java.text.SimpleDateFormat
import java.util.*

enum class AppSection(val arabicTitle: String) {
    DASHBOARD("الرئيسية"),
    SALES("المبيعات والمردودات"),
    PURCHASES("المشتريات والمردودات"),
    VOUCHERS("سندات القبض والصرف"),
    INVENTORY("المخازن والأصناف"),
    GENERAL_LEDGER("الأستاذ والقيود"),
    REPORTS("التقارير المالية والأرباح")
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
    val totalJournalEntriesCount: Int = 0,
    val totalVouchersCount: Int = 0
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

data class IncomeStatementData(
    val grossSales: Double = 0.0,
    val salesReturns: Double = 0.0,
    val netSales: Double = 0.0,
    val cogs: Double = 0.0,
    val grossProfit: Double = 0.0,
    val otherRevenues: Double = 0.0,
    val totalOperatingExpenses: Double = 0.0,
    val detailedExpenses: List<Pair<String, Double>> = emptyList(),
    val detailedRevenues: List<Pair<String, Double>> = emptyList(),
    val netProfit: Double = 0.0
)

class AccountingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AccountingRepository(
        database.accountDao(),
        database.journalDao(),
        database.productDao(),
        database.partnerDao(),
        database.invoiceDao(),
        database.voucherDao(),
        database.returnDao()
    )

    // System Settings (SharedPreferences)
    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _currencySymbol = MutableStateFlow(prefs.getString("currency_symbol", "ر.س") ?: "ر.س")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _storeName = MutableStateFlow(prefs.getString("store_name", "الحساب المتكامل") ?: "الحساب المتكامل")
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _storePhone = MutableStateFlow(prefs.getString("store_phone", "") ?: "")
    val storePhone: StateFlow<String> = _storePhone.asStateFlow()

    // Tax Settings (Default: Tax disabled / 0%)
    private val _isTaxEnabled = MutableStateFlow(prefs.getBoolean("is_tax_enabled", false))
    val isTaxEnabled: StateFlow<Boolean> = _isTaxEnabled.asStateFlow()

    private val _defaultTaxRate = MutableStateFlow(prefs.getFloat("default_tax_rate", 0.0f).toDouble())
    val defaultTaxRate: StateFlow<Double> = _defaultTaxRate.asStateFlow()

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
    val allVouchers = repository.allVouchers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val receiptVouchers = repository.receiptVouchers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val paymentVouchers = repository.paymentVouchers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val salesReturns = repository.allSalesReturns.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val purchaseReturns = repository.allPurchaseReturns.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dashboard dynamic statistics
    val dashboardStats: StateFlow<DashboardStats> = combine(
        combine(accounts, products, customers) { accs, prods, custs ->
            Triple(accs, prods, custs)
        },
        combine(suppliers, salesInvoices, purchaseInvoices) { supps, sales, purchases ->
            Triple(supps, sales, purchases)
        },
        combine(journalEntries, allVouchers, salesReturns) { entries, vouchers, sReturns ->
            Triple(entries, vouchers, sReturns)
        }
    ) { (accs, prods, custs), (supps, sales, purchases), (entries, vouchers, sReturns) ->
        val totalSales = sales.filter { !it.isCancelled }.sumOf { it.totalAmount }
        val totalPurchases = purchases.filter { !it.isCancelled }.sumOf { it.totalAmount }
        val totalSalesReturns = sReturns.sumOf { it.totalAmount }

        // Revenue minus Expenses from Chart of Accounts
        val totalRevenues = accs.filter { it.type == AccountType.REVENUE && !it.isGroup }.sumOf { it.currentBalance }
        val totalExpenses = accs.filter { it.type == AccountType.EXPENSE && !it.isGroup }.sumOf { it.currentBalance }
        val netProfit = (totalRevenues - totalSalesReturns) - totalExpenses

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
            totalJournalEntriesCount = entries.size,
            totalVouchersCount = vouchers.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    init {
        Formatters.currencySymbol = _currencySymbol.value
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            val isCleaned = prefs.getBoolean("system_data_cleaned_v3", false)
            if (!isCleaned) {
                repository.clearInvoicesAndTransactionsOnly()
                prefs.edit().putBoolean("system_data_cleaned_v3", true).apply()
            }
        }
    }

    // -------------------------------------------------------------
    // Sequential Number Generators (1, 2, 3...)
    // -------------------------------------------------------------
    suspend fun getNextSalesInvoiceNumber(): String = repository.getNextSalesInvoiceNumber()
    suspend fun getNextPurchaseInvoiceNumber(): String = repository.getNextPurchaseInvoiceNumber()
    suspend fun getNextPurchaseBillNumber(): String = repository.getNextPurchaseInvoiceNumber()
    suspend fun getNextSalesReturnNumber(): String = repository.getNextSalesReturnNumber()
    suspend fun getNextPurchaseReturnNumber(): String = repository.getNextPurchaseReturnNumber()
    suspend fun getNextReceiptVoucherNumber(): String = repository.getNextReceiptVoucherNumber()
    suspend fun getNextPaymentVoucherNumber(): String = repository.getNextPaymentVoucherNumber()
    suspend fun getNextVoucherNumber(type: VoucherType): String = when (type) {
        VoucherType.RECEIPT -> repository.getNextReceiptVoucherNumber()
        VoucherType.PAYMENT -> repository.getNextPaymentVoucherNumber()
    }
    suspend fun getNextJournalEntryNumber(): String = repository.getNextJournalEntryNumber()

    // -------------------------------------------------------------
    // System Reset & Settings Actions
    // -------------------------------------------------------------
    fun clearInvoicesAndTransactions(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearInvoicesAndTransactionsOnly()
            showMessage("تم حذف جميع الفواتير والسندات والقيود وتصفير الأرصدة بنجاح")
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

    fun updateStoreSettings(
        name: String,
        phone: String,
        currency: String,
        isTaxActive: Boolean,
        defaultTax: Double
    ) {
        val validName = name.trim().ifEmpty { "الحساب المتكامل" }
        val validPhone = phone.trim()
        val validCurrency = currency.trim().ifEmpty { "ر.س" }

        _storeName.value = validName
        _storePhone.value = validPhone
        _currencySymbol.value = validCurrency
        _isTaxEnabled.value = isTaxActive
        _defaultTaxRate.value = defaultTax
        Formatters.currencySymbol = validCurrency

        prefs.edit()
            .putString("store_name", validName)
            .putString("store_phone", validPhone)
            .putString("currency_symbol", validCurrency)
            .putBoolean("is_tax_enabled", isTaxActive)
            .putFloat("default_tax_rate", defaultTax.toFloat())
            .apply()

        showMessage("تم حفظ إعدادات النظام والضريبة بنجاح")
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
    // General Ledger & Chart of Accounts
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

    fun deleteAccountSafe(account: Account, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteAccountSafe(account)
            result.onSuccess {
                showMessage("تم حذف الحساب: ${account.nameAr}")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "لا يمكن حذف الحساب لوجود ارتباطات")
            }
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
                showMessage("تم ترحيل القيد المحاسبي رقم $entryNumber بنجاح!")
                onSuccess()
            }.onFailure { ex ->
                val err = ex.message ?: "خطأ أثناء حفظ القيد"
                showMessage(err)
                onError(err)
            }
        }
    }

    fun deleteJournalEntrySafe(entry: JournalEntry, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteJournalEntrySafe(entry)
            result.onSuccess {
                showMessage("تم حذف القيد المحاسبي ${entry.entryNumber} بنجاح")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "لا يمكن حذف هذا القيد")
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

    // Account Statement / Ledger (كشف حساب تفصيلي)
    fun getAccountStatement(accountId: Long): List<AccountStatementRow> {
        val acc = accounts.value.find { it.id == accountId } ?: return emptyList()
        val lines = journalLines.value.filter { it.accountId == accountId }
        val entries = journalEntries.value.associateBy { it.id }

        var running = acc.initialBalance
        val rows = mutableListOf<AccountStatementRow>()

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

    // Income Statement with Cost of Goods Sold (COGS) Calculation
    fun calculateIncomeStatement(): IncomeStatementData {
        val allSales = salesInvoices.value.filter { !it.isCancelled }
        val allReturns = salesReturns.value

        val grossSales = allSales.sumOf { it.subtotal - it.discount }
        val salesReturnsTotal = allReturns.sumOf { it.subtotal }
        val netSales = (grossSales - salesReturnsTotal).coerceAtLeast(0.0)

        // COGS from stock movements or products sold
        val movements = stockMovements.value
        val salesMovementCost = movements.filter { it.movementType == MovementType.SALE }
            .sumOf { it.quantity * it.unitPrice }
        val returnsMovementCost = movements.filter { it.movementType == MovementType.RETURN_IN }
            .sumOf { it.quantity * it.unitPrice }
        val cogs = (salesMovementCost - returnsMovementCost).coerceAtLeast(0.0)

        val grossProfit = netSales - cogs

        val expenseAccounts = accounts.value.filter { it.type == AccountType.EXPENSE && !it.isGroup }
        val detailedExpenses = expenseAccounts.map { it.nameAr to it.currentBalance }
        val totalExpenses = expenseAccounts.sumOf { it.currentBalance }

        val otherRevenueAccounts = accounts.value.filter { it.type == AccountType.REVENUE && !it.isGroup && it.code != "41" && it.code != "412" }
        val detailedRevenues = otherRevenueAccounts.map { it.nameAr to it.currentBalance }
        val otherRevenues = otherRevenueAccounts.sumOf { it.currentBalance }

        val netProfit = (grossProfit + otherRevenues) - totalExpenses

        return IncomeStatementData(
            grossSales = grossSales,
            salesReturns = salesReturnsTotal,
            netSales = netSales,
            cogs = cogs,
            grossProfit = grossProfit,
            otherRevenues = otherRevenues,
            totalOperatingExpenses = totalExpenses,
            detailedExpenses = detailedExpenses,
            detailedRevenues = detailedRevenues,
            netProfit = netProfit
        )
    }

    // -------------------------------------------------------------
    // Sales Invoices Actions
    // -------------------------------------------------------------
    fun createSalesInvoice(
        invoiceNumber: String,
        date: Long,
        customerId: Long?,
        customerName: String,
        items: List<SalesInvoiceItem>,
        discount: Double,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createSalesInvoice(
                invoiceNumber, date, customerId, customerName, items, discount, taxRate, taxAmountOverride, paymentType, notes
            )
            result.onSuccess {
                showMessage("تم إصدار فاتورة المبيعات رقم $invoiceNumber بنجاح!")
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

    fun deleteSalesInvoice(invoice: SalesInvoice, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteSalesInvoice(invoice)
            result.onSuccess {
                showMessage("تم حذف فاتورة المبيعات رقم ${invoice.invoiceNumber} وإلغاء أثرها المحاسبي والمخزني")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "خطأ في حذف الفاتورة")
            }
        }
    }

    // -------------------------------------------------------------
    // Sales Returns Actions
    // -------------------------------------------------------------
    fun createSalesReturn(
        returnNumber: String,
        originalInvoiceNumber: String,
        date: Long,
        customerId: Long?,
        customerName: String,
        items: List<SalesReturnItem>,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createSalesReturn(
                returnNumber, originalInvoiceNumber, date, customerId, customerName, items, taxRate, taxAmountOverride, paymentType, notes
            )
            result.onSuccess {
                showMessage("تم إصدار مردود المبيعات رقم $returnNumber وإعادة الأصناف للمخزن بنجاح!")
                onSuccess()
            }.onFailure { ex ->
                val err = ex.message ?: "خطأ في تسجيل مردود المبيعات"
                showMessage(err)
                onError(err)
            }
        }
    }

    suspend fun getSalesReturnItems(returnId: Long): List<SalesReturnItem> {
        return repository.getSalesReturnItems(returnId)
    }

    fun deleteSalesReturn(sReturn: SalesReturn, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteSalesReturn(sReturn)
            result.onSuccess {
                showMessage("تم حذف مردود المبيعات رقم ${sReturn.returnNumber} بنجاح")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "خطأ في حذف المردود")
            }
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
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createPurchaseInvoice(
                billNumber, supplierInvoiceRef, date, supplierId, supplierName, items, discount, taxRate, taxAmountOverride, paymentType, notes
            )
            result.onSuccess {
                showMessage("تم حفظ فاتورة المشتريات رقم $billNumber وإضافة الكميات للمخزن بنجاح!")
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

    fun deletePurchaseInvoice(bill: PurchaseInvoice, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deletePurchaseInvoice(bill)
            result.onSuccess {
                showMessage("تم حذف فاتورة المشتريات رقم ${bill.billNumber} وإلغاء أثرها المحاسبي والمخزني")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "خطأ في حذف فاتورة المشتريات")
            }
        }
    }

    // -------------------------------------------------------------
    // Purchase Returns Actions
    // -------------------------------------------------------------
    fun createPurchaseReturn(
        returnNumber: String,
        originalBillNumber: String,
        date: Long,
        supplierId: Long?,
        supplierName: String,
        items: List<PurchaseReturnItem>,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createPurchaseReturn(
                returnNumber, originalBillNumber, date, supplierId, supplierName, items, taxRate, taxAmountOverride, paymentType, notes
            )
            result.onSuccess {
                showMessage("تم إصدار مردود المشتريات رقم $returnNumber وخصم الكميات من المخزن بنجاح!")
                onSuccess()
            }.onFailure { ex ->
                val err = ex.message ?: "خطأ في تسجيل مردود المشتريات"
                showMessage(err)
                onError(err)
            }
        }
    }

    suspend fun getPurchaseReturnItems(returnId: Long): List<PurchaseReturnItem> {
        return repository.getPurchaseReturnItems(returnId)
    }

    fun deletePurchaseReturn(pReturn: PurchaseReturn, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deletePurchaseReturn(pReturn)
            result.onSuccess {
                showMessage("تم حذف مردود المشتريات رقم ${pReturn.returnNumber} بنجاح")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "خطأ في حذف مردود المشتريات")
            }
        }
    }

    // -------------------------------------------------------------
    // Vouchers Actions (سندات القبض والصرف)
    // -------------------------------------------------------------
    fun createVoucher(
        voucherNumber: String,
        type: VoucherType,
        date: Long,
        amount: Double,
        paymentType: PaymentType,
        partnerType: VoucherPartnerType,
        partnerId: Long?,
        partnerName: String,
        accountId: Long?,
        accountName: String,
        notes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createVoucher(
                voucherNumber, type, date, amount, paymentType, partnerType, partnerId, partnerName, accountId, accountName, notes
            )
            result.onSuccess {
                showMessage("تم إصدار ${type.arabicName} رقم $voucherNumber بنجاح!")
                onSuccess()
            }.onFailure { ex ->
                val err = ex.message ?: "خطأ في إصدار السند"
                showMessage(err)
                onError(err)
            }
        }
    }

    fun deleteVoucher(voucher: Voucher, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteVoucher(voucher)
            result.onSuccess {
                showMessage("تم حذف ${voucher.type.arabicName} رقم ${voucher.voucherNumber} بنجاح")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "خطأ في حذف السند")
            }
        }
    }

    // -------------------------------------------------------------
    // Customers & Suppliers Safe Actions
    // -------------------------------------------------------------
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

    fun deleteCustomerSafe(customer: Customer, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteCustomerSafe(customer)
            result.onSuccess {
                showMessage("تم حذف العميل: ${customer.name}")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "لا يمكن حذف هذا العميل لوجود عمليات مرتبطة به")
            }
        }
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

    fun deleteSupplierSafe(supplier: Supplier, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteSupplierSafe(supplier)
            result.onSuccess {
                showMessage("تم حذف المورد: ${supplier.name}")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "لا يمكن حذف هذا المورد لوجود عمليات مرتبطة به")
            }
        }
    }

    // -------------------------------------------------------------
    // Products & Inventory Safe Actions
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
            showMessage("تم تعديل بيانات الصنف: ${product.nameAr}")
            onSuccess()
        }
    }

    fun deleteProductSafe(product: Product, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteProductSafe(product)
            result.onSuccess {
                showMessage("تم حذف الصنف: ${product.nameAr}")
                onSuccess()
            }.onFailure { ex ->
                showMessage(ex.message ?: "لا يمكن حذف هذا الصنف لوجود حركات مرتبطة به")
            }
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
