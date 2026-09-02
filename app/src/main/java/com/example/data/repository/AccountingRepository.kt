package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.abs

class AccountingRepository(
    private val accountDao: AccountDao,
    private val journalDao: JournalDao,
    private val productDao: ProductDao,
    private val partnerDao: PartnerDao,
    private val invoiceDao: InvoiceDao,
    private val voucherDao: VoucherDao,
    private val returnDao: ReturnDao
) {
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val postingAccounts: Flow<List<Account>> = accountDao.getPostingAccounts()
    val allJournalEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()
    val allJournalLines: Flow<List<JournalEntryLine>> = journalDao.getAllLines()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val allStockMovements: Flow<List<StockMovement>> = productDao.getAllMovements()
    val allCustomers: Flow<List<Customer>> = partnerDao.getAllCustomers()
    val allSuppliers: Flow<List<Supplier>> = partnerDao.getAllSuppliers()
    val allSalesInvoices: Flow<List<SalesInvoice>> = invoiceDao.getAllSalesInvoices()
    val allPurchaseInvoices: Flow<List<PurchaseInvoice>> = invoiceDao.getAllPurchaseInvoices()
    val allVouchers: Flow<List<Voucher>> = voucherDao.getAllVouchers()
    val receiptVouchers: Flow<List<Voucher>> = voucherDao.getVouchersByType(VoucherType.RECEIPT)
    val paymentVouchers: Flow<List<Voucher>> = voucherDao.getVouchersByType(VoucherType.PAYMENT)
    val allSalesReturns: Flow<List<SalesReturn>> = returnDao.getAllSalesReturns()
    val allPurchaseReturns: Flow<List<PurchaseReturn>> = returnDao.getAllPurchaseReturns()

    // -------------------------------------------------------------
    // Sequential Numbering Generation (1, 2, 3...)
    // -------------------------------------------------------------
    suspend fun getNextSalesInvoiceNumber(): String = withContext(Dispatchers.IO) {
        val count = invoiceDao.getSalesInvoiceCount()
        (count + 1).toString()
    }

    suspend fun getNextPurchaseInvoiceNumber(): String = withContext(Dispatchers.IO) {
        val count = invoiceDao.getPurchaseInvoiceCount()
        (count + 1).toString()
    }

    suspend fun getNextSalesReturnNumber(): String = withContext(Dispatchers.IO) {
        val count = returnDao.getSalesReturnCount()
        (count + 1).toString()
    }

    suspend fun getNextPurchaseReturnNumber(): String = withContext(Dispatchers.IO) {
        val count = returnDao.getPurchaseReturnCount()
        (count + 1).toString()
    }

    suspend fun getNextReceiptVoucherNumber(): String = withContext(Dispatchers.IO) {
        val count = voucherDao.getCountByType(VoucherType.RECEIPT)
        (count + 1).toString()
    }

    suspend fun getNextPaymentVoucherNumber(): String = withContext(Dispatchers.IO) {
        val count = voucherDao.getCountByType(VoucherType.PAYMENT)
        (count + 1).toString()
    }

    suspend fun getNextJournalEntryNumber(): String = withContext(Dispatchers.IO) {
        // approximate from accounts or count
        val count = journalDao.getLinesCountForAccount(0)
        (count + 1).toString()
    }

    // -------------------------------------------------------------
    // Initial Seed Data (Standard Arab Accounting Chart of Accounts)
    // -------------------------------------------------------------
    fun getDefaultCleanAccounts(): List<Account> {
        return listOf(
            // 1. الأصول (Assets)
            Account(code = "1", nameAr = "الأصول", type = AccountType.ASSET, isGroup = true),
            Account(code = "11", nameAr = "الأصول المتداولة", type = AccountType.ASSET, isGroup = true),
            Account(code = "111", nameAr = "الصندوق / الخزينة الرئيسية", type = AccountType.ASSET, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "112", nameAr = "البنك / الحساب الجاري", type = AccountType.ASSET, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "113", nameAr = "العملاء والمدينون", type = AccountType.ASSET, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "114", nameAr = "مخزون البضائع", type = AccountType.ASSET, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "12", nameAr = "الأصول الثابتة", type = AccountType.ASSET, isGroup = true),
            Account(code = "121", nameAr = "الأجهزة والمعدات والأثاث", type = AccountType.ASSET, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),

            // 2. الالتزامات (Liabilities)
            Account(code = "2", nameAr = "الالتزامات", type = AccountType.LIABILITY, isGroup = true),
            Account(code = "21", nameAr = "الالتزامات المتداولة", type = AccountType.LIABILITY, isGroup = true),
            Account(code = "211", nameAr = "الموردون والدائنون", type = AccountType.LIABILITY, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "212", nameAr = "أمانات ضريبة القيمة المضافة", type = AccountType.LIABILITY, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),

            // 3. حقوق الملكية (Equity)
            Account(code = "3", nameAr = "حقوق الملكية", type = AccountType.EQUITY, isGroup = true),
            Account(code = "31", nameAr = "رأس المال", type = AccountType.EQUITY, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "32", nameAr = "الأرباح المبقاة / المحتجزة", type = AccountType.EQUITY, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),

            // 4. الإيرادات (Revenues)
            Account(code = "4", nameAr = "الإيرادات", type = AccountType.REVENUE, isGroup = true),
            Account(code = "41", nameAr = "إيرادات المبيعات", type = AccountType.REVENUE, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "412", nameAr = "مردودات ومسموحات المبيعات", type = AccountType.REVENUE, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "42", nameAr = "إيرادات خدمات وأخرى", type = AccountType.REVENUE, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),

            // 5. المصروفات (Expenses)
            Account(code = "5", nameAr = "المصروفات", type = AccountType.EXPENSE, isGroup = true),
            Account(code = "51", nameAr = "تكلفة البضاعة المباعة", type = AccountType.EXPENSE, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "52", nameAr = "مصروفات إدارية وعمومية", type = AccountType.EXPENSE, isGroup = true),
            Account(code = "521", nameAr = "مصروف الإيجار", type = AccountType.EXPENSE, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "522", nameAr = "الرواتب والأجور", type = AccountType.EXPENSE, isGroup = false, currentBalance = 0.0, initialBalance = 0.0),
            Account(code = "523", nameAr = "مصاريف الكهرباء والمياه والاتصالات", type = AccountType.EXPENSE, isGroup = false, currentBalance = 0.0, initialBalance = 0.0)
        )
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val count = accountDao.getCount()
        if (count == 0) {
            accountDao.insertAccounts(getDefaultCleanAccounts())
            // General default cash customer
            partnerDao.insertCustomer(
                Customer(name = "عميل نقدي عام", phone = "-", email = "", address = "الفرع الرئيسي", currentBalance = 0.0)
            )
        }
    }

    // -------------------------------------------------------------
    // System Reset & Data Clearing (تهيئة وتصفير النظام)
    // -------------------------------------------------------------
    suspend fun clearInvoicesAndTransactionsOnly(): Unit = withContext(Dispatchers.IO) {
        // 1. Delete all invoices, returns, and vouchers
        invoiceDao.deleteAllSalesInvoiceItems()
        invoiceDao.deleteAllSalesInvoices()
        invoiceDao.deleteAllPurchaseInvoiceItems()
        invoiceDao.deleteAllPurchaseInvoices()
        returnDao.deleteAllSalesReturnItems()
        returnDao.deleteAllSalesReturns()
        returnDao.deleteAllPurchaseReturnItems()
        returnDao.deleteAllPurchaseReturns()
        voucherDao.deleteAllVouchers()

        // 2. Delete all journal entries and lines
        journalDao.deleteAllLines()
        journalDao.deleteAllEntries()

        // 3. Delete all stock movements and reset product stock quantities to 0
        productDao.deleteAllStockMovements()
        productDao.resetAllStockQuantities()

        // 4. Reset balances of customers and suppliers to 0
        partnerDao.resetAllCustomerBalances()
        partnerDao.resetAllSupplierBalances()

        // 5. Reset all account balances to 0.0
        accountDao.resetAllAccountBalances()
    }

    suspend fun resetSystemCompletely(keepChartOfAccounts: Boolean = true): Unit = withContext(Dispatchers.IO) {
        clearInvoicesAndTransactionsOnly()
        productDao.deleteAllProducts()
        partnerDao.deleteAllSuppliers()
        partnerDao.deleteAllCustomers()
        partnerDao.insertCustomer(
            Customer(name = "عميل نقدي عام", phone = "-", email = "", address = "الفرع الرئيسي", currentBalance = 0.0)
        )

        if (!keepChartOfAccounts) {
            accountDao.deleteAllAccounts()
            accountDao.insertAccounts(getDefaultCleanAccounts())
        } else {
            accountDao.resetAllAccountBalances()
        }
    }

    // -------------------------------------------------------------
    // Accounts CRUD with Protected Deletion
    // -------------------------------------------------------------
    suspend fun addAccount(account: Account): Long = withContext(Dispatchers.IO) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: Account) = withContext(Dispatchers.IO) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccountSafe(account: Account): Result<Unit> = withContext(Dispatchers.IO) {
        val linesCount = journalDao.getLinesCountForAccount(account.id)
        if (linesCount > 0) {
            return@withContext Result.failure(
                IllegalStateException("لا يمكن حذف الحساب \"${account.nameAr}\" لوجود قيود يومية مسجلة عليه ($linesCount حركة). يرجى حذف القيود المرتبطة أولاً.")
            )
        }
        accountDao.deleteAccount(account)
        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Journal Entries (Double-Entry Bookkeeping Engine)
    // -------------------------------------------------------------
    suspend fun createJournalEntry(
        entryNumber: String,
        date: Long,
        description: String,
        referenceNumber: String,
        lines: List<JournalEntryLine>,
        source: String = "MANUAL"
    ): Result<Long> = withContext(Dispatchers.IO) {
        val totalDebit = lines.sumOf { it.debit }
        val totalCredit = lines.sumOf { it.credit }

        if (lines.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("يجب أن يحتوي القيد على سطرين محاسبيين على الأقل"))
        }

        if (abs(totalDebit - totalCredit) > 0.01) {
            return@withContext Result.failure(IllegalArgumentException("القيد غير متزن! المدين: $totalDebit / الدائن: $totalCredit"))
        }

        val entry = JournalEntry(
            entryNumber = entryNumber,
            date = date,
            description = description,
            referenceNumber = referenceNumber,
            source = source,
            totalDebit = totalDebit,
            totalCredit = totalCredit
        )

        val entryId = journalDao.insertEntry(entry)
        val linkedLines = lines.map { it.copy(entryId = entryId) }
        journalDao.insertLines(linkedLines)

        // Update account balances automatically
        for (line in lines) {
            val acc = accountDao.getAccountById(line.accountId)
            if (acc != null) {
                val newBalance = if (acc.type.isDebitDefault) {
                    acc.currentBalance + (line.debit - line.credit)
                } else {
                    acc.currentBalance + (line.credit - line.debit)
                }
                accountDao.updateBalance(acc.id, newBalance)
            }
        }

        Result.success(entryId)
    }

    suspend fun getJournalLines(entryId: Long): List<JournalEntryLine> = withContext(Dispatchers.IO) {
        journalDao.getLinesForEntry(entryId)
    }

    suspend fun deleteJournalEntrySafe(entry: JournalEntry): Result<Unit> = withContext(Dispatchers.IO) {
        if (entry.source != "MANUAL") {
            return@withContext Result.failure(
                IllegalStateException("لا يمكن حذف هذا القيد مباشرة لأنه قيد آلي تم إنشاؤه من ${entry.source} برقم مرجعي (${entry.referenceNumber}). يرجى حذف العملية الأصلية.")
            )
        }

        // Reverse balances
        val lines = journalDao.getLinesForEntry(entry.id)
        for (line in lines) {
            val acc = accountDao.getAccountById(line.accountId)
            if (acc != null) {
                val newBalance = if (acc.type.isDebitDefault) {
                    acc.currentBalance - (line.debit - line.credit)
                } else {
                    acc.currentBalance - (line.credit - line.debit)
                }
                accountDao.updateBalance(acc.id, newBalance)
            }
        }

        journalDao.deleteLinesForEntry(entry.id)
        journalDao.deleteEntry(entry)
        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Sales Invoices & Automated Journaling
    // -------------------------------------------------------------
    suspend fun createSalesInvoice(
        invoiceNumber: String,
        date: Long,
        customerId: Long?,
        customerName: String,
        items: List<SalesInvoiceItem>,
        discount: Double,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("يجب إضافة أصناف للفاتورة"))
        }

        val subtotal = items.sumOf { it.lineTotal }
        val afterDiscount = (subtotal - discount).coerceAtLeast(0.0)
        val taxAmount = taxAmountOverride ?: (afterDiscount * (taxRate / 100.0))
        val totalAmount = afterDiscount + taxAmount

        val paidAmount = if (paymentType == PaymentType.CREDIT) 0.0 else totalAmount
        val remainingAmount = totalAmount - paidAmount

        // 1. Create Automated Accounting Journal Entry for the Sale
        val cashAccount = if (paymentType == PaymentType.BANK) {
            accountDao.getAccountByCode("112") ?: accountDao.getAccountByCode("111")
        } else {
            accountDao.getAccountByCode("111") ?: accountDao.getAccountByCode("112")
        }
        val receivablesAccount = accountDao.getAccountByCode("113")
        val salesRevenueAccount = accountDao.getAccountByCode("41")
        val taxLiabilityAccount = accountDao.getAccountByCode("212")

        val journalLines = mutableListOf<JournalEntryLine>()

        // Debit side (Cash/Bank or Receivables)
        if (paymentType == PaymentType.CASH || paymentType == PaymentType.BANK) {
            cashAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = totalAmount,
                        credit = 0.0,
                        description = "تحصيل فاتورة مبيعات $invoiceNumber"
                    )
                )
            }
        } else {
            receivablesAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = totalAmount,
                        credit = 0.0,
                        description = "مبيعات آجلة فاتورة $invoiceNumber للعميل $customerName"
                    )
                )
            }
        }

        // Credit side (Sales Revenue & Tax)
        salesRevenueAccount?.let {
            journalLines.add(
                JournalEntryLine(
                    accountId = it.id,
                    accountCode = it.code,
                    accountName = it.nameAr,
                    debit = 0.0,
                    credit = afterDiscount,
                    description = "إيراد مبيعات فاتورة $invoiceNumber"
                )
            )
        }

        if (taxAmount > 0) {
            taxLiabilityAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = 0.0,
                        credit = taxAmount,
                        description = "ضريبة القيمة المضافة فاتورة $invoiceNumber"
                    )
                )
            }
        }

        var journalId: Long? = null
        if (journalLines.size >= 2) {
            val jResult = createJournalEntry(
                entryNumber = "JV-SALE-$invoiceNumber",
                date = date,
                description = "قيد مبيعات الفاتورة $invoiceNumber - $customerName",
                referenceNumber = invoiceNumber,
                lines = journalLines,
                source = "SALES"
            )
            journalId = jResult.getOrNull()
        }

        // 2. Insert Sales Invoice
        val invoice = SalesInvoice(
            invoiceNumber = invoiceNumber,
            date = date,
            customerId = customerId,
            customerName = customerName,
            subtotal = subtotal,
            discount = discount,
            taxRate = taxRate,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            paymentType = paymentType,
            journalEntryId = journalId,
            notes = notes
        )
        val invoiceId = invoiceDao.insertSalesInvoice(invoice)
        val linkedItems = items.map { it.copy(invoiceId = invoiceId) }
        invoiceDao.insertSalesInvoiceItems(linkedItems)

        // 3. Update Product Stocks and record stock movements
        for (item in items) {
            productDao.updateStockQuantity(item.productId, -item.quantity)
            productDao.insertMovement(
                StockMovement(
                    productId = item.productId,
                    productName = item.productName,
                    date = date,
                    movementType = MovementType.SALE,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    referenceType = "SALES_INVOICE",
                    referenceId = invoiceId,
                    referenceNumber = invoiceNumber,
                    notes = "صرف مبيعات فاتورة $invoiceNumber"
                )
            )
        }

        // 4. Update Customer balance if credit
        if (paymentType == PaymentType.CREDIT && customerId != null) {
            partnerDao.updateCustomerBalance(customerId, totalAmount)
        }

        Result.success(invoiceId)
    }

    suspend fun getSalesInvoiceItems(invoiceId: Long): List<SalesInvoiceItem> = withContext(Dispatchers.IO) {
        invoiceDao.getSalesInvoiceItems(invoiceId)
    }

    suspend fun deleteSalesInvoice(invoice: SalesInvoice): Result<Unit> = withContext(Dispatchers.IO) {
        // 1. Rollback customer balance
        if (invoice.paymentType == PaymentType.CREDIT && invoice.customerId != null) {
            partnerDao.updateCustomerBalance(invoice.customerId, -invoice.totalAmount)
        }

        // 2. Rollback product stocks
        val items = invoiceDao.getSalesInvoiceItems(invoice.id)
        for (item in items) {
            productDao.updateStockQuantity(item.productId, item.quantity)
        }
        productDao.deleteMovementsForReference("SALES_INVOICE", invoice.id)

        // 3. Delete journal entry if present
        if (invoice.journalEntryId != null) {
            val jEntry = journalDao.getEntryById(invoice.journalEntryId)
            if (jEntry != null) {
                val lines = journalDao.getLinesForEntry(jEntry.id)
                for (line in lines) {
                    val acc = accountDao.getAccountById(line.accountId)
                    if (acc != null) {
                        val newBalance = if (acc.type.isDebitDefault) {
                            acc.currentBalance - (line.debit - line.credit)
                        } else {
                            acc.currentBalance - (line.credit - line.debit)
                        }
                        accountDao.updateBalance(acc.id, newBalance)
                    }
                }
                journalDao.deleteLinesForEntry(jEntry.id)
                journalDao.deleteEntry(jEntry)
            }
        }

        // 4. Delete invoice & items
        invoiceDao.deleteSalesInvoiceItems(invoice.id)
        invoiceDao.deleteSalesInvoice(invoice)
        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Purchases Invoices & Automated Journaling
    // -------------------------------------------------------------
    suspend fun createPurchaseInvoice(
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
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("يجب إضافة أصناف لفاتورة الشراء"))
        }

        val subtotal = items.sumOf { it.lineTotal }
        val afterDiscount = (subtotal - discount).coerceAtLeast(0.0)
        val taxAmount = taxAmountOverride ?: (afterDiscount * (taxRate / 100.0))
        val totalAmount = afterDiscount + taxAmount

        val paidAmount = if (paymentType == PaymentType.CREDIT) 0.0 else totalAmount
        val remainingAmount = totalAmount - paidAmount

        // 1. Create Automated Accounting Journal Entry for Purchase
        val cashAccount = if (paymentType == PaymentType.BANK) {
            accountDao.getAccountByCode("112") ?: accountDao.getAccountByCode("111")
        } else {
            accountDao.getAccountByCode("111") ?: accountDao.getAccountByCode("112")
        }
        val inventoryAccount = accountDao.getAccountByCode("114")
        val payablesAccount = accountDao.getAccountByCode("211")
        val taxAccount = accountDao.getAccountByCode("212")

        val journalLines = mutableListOf<JournalEntryLine>()

        // Debit side (Inventory & Tax)
        inventoryAccount?.let {
            journalLines.add(
                JournalEntryLine(
                    accountId = it.id,
                    accountCode = it.code,
                    accountName = it.nameAr,
                    debit = afterDiscount,
                    credit = 0.0,
                    description = "توريد مخزون فاتورة شراء $billNumber"
                )
            )
        }

        if (taxAmount > 0) {
            taxAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = taxAmount,
                        credit = 0.0,
                        description = "ضريبة مشتريات مدفوعة $billNumber"
                    )
                )
            }
        }

        // Credit side (Cash or Payables)
        if (paymentType == PaymentType.CASH || paymentType == PaymentType.BANK) {
            cashAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = 0.0,
                        credit = totalAmount,
                        description = "سداد فاتورة مشتريات $billNumber للمورد $supplierName"
                    )
                )
            }
        } else {
            payablesAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = 0.0,
                        credit = totalAmount,
                        description = "استحقاق مورد فاتورة مشتريات آجلة $billNumber - $supplierName"
                    )
                )
            }
        }

        var journalId: Long? = null
        if (journalLines.size >= 2) {
            val jResult = createJournalEntry(
                entryNumber = "JV-PURCHASE-$billNumber",
                date = date,
                description = "قيد مشتريات الفاتورة $billNumber - $supplierName",
                referenceNumber = billNumber,
                lines = journalLines,
                source = "PURCHASES"
            )
            journalId = jResult.getOrNull()
        }

        // 2. Insert Purchase Invoice
        val bill = PurchaseInvoice(
            billNumber = billNumber,
            supplierInvoiceRef = supplierInvoiceRef,
            date = date,
            supplierId = supplierId,
            supplierName = supplierName,
            subtotal = subtotal,
            discount = discount,
            taxRate = taxRate,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            paymentType = paymentType,
            journalEntryId = journalId,
            notes = notes
        )
        val billId = invoiceDao.insertPurchaseInvoice(bill)
        val linkedItems = items.map { it.copy(billId = billId) }
        invoiceDao.insertPurchaseInvoiceItems(linkedItems)

        // 3. Update Product Stocks and purchase prices
        for (item in items) {
            productDao.updateStockQuantity(item.productId, item.quantity)
            productDao.insertMovement(
                StockMovement(
                    productId = item.productId,
                    productName = item.productName,
                    date = date,
                    movementType = MovementType.PURCHASE,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    referenceType = "PURCHASE_INVOICE",
                    referenceId = billId,
                    referenceNumber = billNumber,
                    notes = "إضافة مشتريات فاتورة $billNumber"
                )
            )
        }

        // 4. Update Supplier balance if credit
        if (paymentType == PaymentType.CREDIT && supplierId != null) {
            partnerDao.updateSupplierBalance(supplierId, totalAmount)
        }

        Result.success(billId)
    }

    suspend fun getPurchaseInvoiceItems(billId: Long): List<PurchaseInvoiceItem> = withContext(Dispatchers.IO) {
        invoiceDao.getPurchaseInvoiceItems(billId)
    }

    suspend fun deletePurchaseInvoice(bill: PurchaseInvoice): Result<Unit> = withContext(Dispatchers.IO) {
        // 1. Rollback supplier balance
        if (bill.paymentType == PaymentType.CREDIT && bill.supplierId != null) {
            partnerDao.updateSupplierBalance(bill.supplierId, -bill.totalAmount)
        }

        // 2. Rollback product stocks
        val items = invoiceDao.getPurchaseInvoiceItems(bill.id)
        for (item in items) {
            productDao.updateStockQuantity(item.productId, -item.quantity)
        }
        productDao.deleteMovementsForReference("PURCHASE_INVOICE", bill.id)

        // 3. Delete journal entry
        if (bill.journalEntryId != null) {
            val jEntry = journalDao.getEntryById(bill.journalEntryId)
            if (jEntry != null) {
                val lines = journalDao.getLinesForEntry(jEntry.id)
                for (line in lines) {
                    val acc = accountDao.getAccountById(line.accountId)
                    if (acc != null) {
                        val newBalance = if (acc.type.isDebitDefault) {
                            acc.currentBalance - (line.debit - line.credit)
                        } else {
                            acc.currentBalance - (line.credit - line.debit)
                        }
                        accountDao.updateBalance(acc.id, newBalance)
                    }
                }
                journalDao.deleteLinesForEntry(jEntry.id)
                journalDao.deleteEntry(jEntry)
            }
        }

        // 4. Delete bill & items
        invoiceDao.deletePurchaseInvoiceItems(bill.id)
        invoiceDao.deletePurchaseInvoice(bill)
        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Sales Returns (مردود مبيعات)
    // -------------------------------------------------------------
    suspend fun createSalesReturn(
        returnNumber: String,
        originalInvoiceNumber: String,
        date: Long,
        customerId: Long?,
        customerName: String,
        items: List<SalesReturnItem>,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("يجب إضافة أصناف لمردود المبيعات"))
        }

        val subtotal = items.sumOf { it.lineTotal }
        val taxAmount = taxAmountOverride ?: (subtotal * (taxRate / 100.0))
        val totalAmount = subtotal + taxAmount

        // 1. Accounting Entry: Debit Sales Returns (412 or 41) & Tax (212), Credit Cash or Receivables
        val cashAccount = if (paymentType == PaymentType.BANK) {
            accountDao.getAccountByCode("112") ?: accountDao.getAccountByCode("111")
        } else {
            accountDao.getAccountByCode("111") ?: accountDao.getAccountByCode("112")
        }
        val receivablesAccount = accountDao.getAccountByCode("113")
        val salesReturnAccount = accountDao.getAccountByCode("412") ?: accountDao.getAccountByCode("41")
        val taxLiabilityAccount = accountDao.getAccountByCode("212")

        val journalLines = mutableListOf<JournalEntryLine>()

        // Debit side (Sales Return Revenue & Tax)
        salesReturnAccount?.let {
            journalLines.add(
                JournalEntryLine(
                    accountId = it.id,
                    accountCode = it.code,
                    accountName = it.nameAr,
                    debit = subtotal,
                    credit = 0.0,
                    description = "مردود مبيعات رقم $returnNumber"
                )
            )
        }

        if (taxAmount > 0) {
            taxLiabilityAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = taxAmount,
                        credit = 0.0,
                        description = "ضريبة مردود مبيعات $returnNumber"
                    )
                )
            }
        }

        // Credit side (Refund Cash or Reduce Receivables)
        if (paymentType == PaymentType.CASH || paymentType == PaymentType.BANK) {
            cashAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = 0.0,
                        credit = totalAmount,
                        description = "صرف قيمة مردود مبيعات $returnNumber"
                    )
                )
            }
        } else {
            receivablesAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = 0.0,
                        credit = totalAmount,
                        description = "تخفيض حساب العميل مردود مبيعات $returnNumber - $customerName"
                    )
                )
            }
        }

        var journalId: Long? = null
        if (journalLines.size >= 2) {
            val jResult = createJournalEntry(
                entryNumber = "JV-SALES-RET-$returnNumber",
                date = date,
                description = "قيد مردود مبيعات $returnNumber - $customerName",
                referenceNumber = returnNumber,
                lines = journalLines,
                source = "SALES_RETURN"
            )
            journalId = jResult.getOrNull()
        }

        // 2. Insert Sales Return Record
        val sReturn = SalesReturn(
            returnNumber = returnNumber,
            originalInvoiceNumber = originalInvoiceNumber,
            date = date,
            customerId = customerId,
            customerName = customerName,
            subtotal = subtotal,
            taxRate = taxRate,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paymentType = paymentType,
            journalEntryId = journalId,
            notes = notes
        )
        val returnId = returnDao.insertSalesReturn(sReturn)
        val linkedItems = items.map { it.copy(returnId = returnId) }
        returnDao.insertSalesReturnItems(linkedItems)

        // 3. Return items back to Stock
        for (item in items) {
            productDao.updateStockQuantity(item.productId, item.quantity)
            productDao.insertMovement(
                StockMovement(
                    productId = item.productId,
                    productName = item.productName,
                    date = date,
                    movementType = MovementType.RETURN_IN,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    referenceType = "SALES_RETURN",
                    referenceId = returnId,
                    referenceNumber = returnNumber,
                    notes = "مرتجع مبيعات إشعار $returnNumber"
                )
            )
        }

        // 4. Reduce customer balance if credit
        if (paymentType == PaymentType.CREDIT && customerId != null) {
            partnerDao.updateCustomerBalance(customerId, -totalAmount)
        }

        Result.success(returnId)
    }

    suspend fun getSalesReturnItems(returnId: Long): List<SalesReturnItem> = withContext(Dispatchers.IO) {
        returnDao.getSalesReturnItems(returnId)
    }

    suspend fun deleteSalesReturn(sReturn: SalesReturn): Result<Unit> = withContext(Dispatchers.IO) {
        if (sReturn.paymentType == PaymentType.CREDIT && sReturn.customerId != null) {
            partnerDao.updateCustomerBalance(sReturn.customerId, sReturn.totalAmount)
        }

        val items = returnDao.getSalesReturnItems(sReturn.id)
        for (item in items) {
            productDao.updateStockQuantity(item.productId, -item.quantity)
        }
        productDao.deleteMovementsForReference("SALES_RETURN", sReturn.id)

        if (sReturn.journalEntryId != null) {
            val jEntry = journalDao.getEntryById(sReturn.journalEntryId)
            if (jEntry != null) {
                val lines = journalDao.getLinesForEntry(jEntry.id)
                for (line in lines) {
                    val acc = accountDao.getAccountById(line.accountId)
                    if (acc != null) {
                        val newBalance = if (acc.type.isDebitDefault) {
                            acc.currentBalance - (line.debit - line.credit)
                        } else {
                            acc.currentBalance - (line.credit - line.debit)
                        }
                        accountDao.updateBalance(acc.id, newBalance)
                    }
                }
                journalDao.deleteLinesForEntry(jEntry.id)
                journalDao.deleteEntry(jEntry)
            }
        }

        returnDao.deleteSalesReturnItems(sReturn.id)
        returnDao.deleteSalesReturn(sReturn)
        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Purchase Returns (مردود مشتريات)
    // -------------------------------------------------------------
    suspend fun createPurchaseReturn(
        returnNumber: String,
        originalBillNumber: String,
        date: Long,
        supplierId: Long?,
        supplierName: String,
        items: List<PurchaseReturnItem>,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("يجب إضافة أصناف لمردود المشتريات"))
        }

        val subtotal = items.sumOf { it.lineTotal }
        val taxAmount = taxAmountOverride ?: (subtotal * (taxRate / 100.0))
        val totalAmount = subtotal + taxAmount

        // 1. Accounting: Debit Cash or Payables, Credit Inventory (114) & Tax (212)
        val cashAccount = if (paymentType == PaymentType.BANK) {
            accountDao.getAccountByCode("112") ?: accountDao.getAccountByCode("111")
        } else {
            accountDao.getAccountByCode("111") ?: accountDao.getAccountByCode("112")
        }
        val inventoryAccount = accountDao.getAccountByCode("114")
        val payablesAccount = accountDao.getAccountByCode("211")
        val taxAccount = accountDao.getAccountByCode("212")

        val journalLines = mutableListOf<JournalEntryLine>()

        // Debit side (Receive Cash or Reduce Payables)
        if (paymentType == PaymentType.CASH || paymentType == PaymentType.BANK) {
            cashAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = totalAmount,
                        credit = 0.0,
                        description = "استلام نقدية مردود مشتريات $returnNumber"
                    )
                )
            }
        } else {
            payablesAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = totalAmount,
                        credit = 0.0,
                        description = "تخفيض حساب المورد مردود مشتريات $returnNumber - $supplierName"
                    )
                )
            }
        }

        // Credit side (Reduce Inventory & Tax)
        inventoryAccount?.let {
            journalLines.add(
                JournalEntryLine(
                    accountId = it.id,
                    accountCode = it.code,
                    accountName = it.nameAr,
                    debit = 0.0,
                    credit = subtotal,
                    description = "صرف مخزون مردود مشتريات $returnNumber"
                )
            )
        }

        if (taxAmount > 0) {
            taxAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = 0.0,
                        credit = taxAmount,
                        description = "تخفيض ضريبة مشتريات مستردة $returnNumber"
                    )
                )
            }
        }

        var journalId: Long? = null
        if (journalLines.size >= 2) {
            val jResult = createJournalEntry(
                entryNumber = "JV-PURCHASE-RET-$returnNumber",
                date = date,
                description = "قيد مردود مشتريات $returnNumber - $supplierName",
                referenceNumber = returnNumber,
                lines = journalLines,
                source = "PURCHASE_RETURN"
            )
            journalId = jResult.getOrNull()
        }

        // 2. Insert Purchase Return
        val pReturn = PurchaseReturn(
            returnNumber = returnNumber,
            originalBillNumber = originalBillNumber,
            date = date,
            supplierId = supplierId,
            supplierName = supplierName,
            subtotal = subtotal,
            taxRate = taxRate,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paymentType = paymentType,
            journalEntryId = journalId,
            notes = notes
        )
        val returnId = returnDao.insertPurchaseReturn(pReturn)
        val linkedItems = items.map { it.copy(returnId = returnId) }
        returnDao.insertPurchaseReturnItems(linkedItems)

        // 3. Deduct stock quantity
        for (item in items) {
            productDao.updateStockQuantity(item.productId, -item.quantity)
            productDao.insertMovement(
                StockMovement(
                    productId = item.productId,
                    productName = item.productName,
                    date = date,
                    movementType = MovementType.RETURN_OUT,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    referenceType = "PURCHASE_RETURN",
                    referenceId = returnId,
                    referenceNumber = returnNumber,
                    notes = "مرتجع مشتريات للمورد $returnNumber"
                )
            )
        }

        // 4. Reduce supplier balance if credit
        if (paymentType == PaymentType.CREDIT && supplierId != null) {
            partnerDao.updateSupplierBalance(supplierId, -totalAmount)
        }

        Result.success(returnId)
    }

    suspend fun getPurchaseReturnItems(returnId: Long): List<PurchaseReturnItem> = withContext(Dispatchers.IO) {
        returnDao.getPurchaseReturnItems(returnId)
    }

    suspend fun deletePurchaseReturn(pReturn: PurchaseReturn): Result<Unit> = withContext(Dispatchers.IO) {
        if (pReturn.paymentType == PaymentType.CREDIT && pReturn.supplierId != null) {
            partnerDao.updateSupplierBalance(pReturn.supplierId, pReturn.totalAmount)
        }

        val items = returnDao.getPurchaseReturnItems(pReturn.id)
        for (item in items) {
            productDao.updateStockQuantity(item.productId, item.quantity)
        }
        productDao.deleteMovementsForReference("PURCHASE_RETURN", pReturn.id)

        if (pReturn.journalEntryId != null) {
            val jEntry = journalDao.getEntryById(pReturn.journalEntryId)
            if (jEntry != null) {
                val lines = journalDao.getLinesForEntry(jEntry.id)
                for (line in lines) {
                    val acc = accountDao.getAccountById(line.accountId)
                    if (acc != null) {
                        val newBalance = if (acc.type.isDebitDefault) {
                            acc.currentBalance - (line.debit - line.credit)
                        } else {
                            acc.currentBalance - (line.credit - line.debit)
                        }
                        accountDao.updateBalance(acc.id, newBalance)
                    }
                }
                journalDao.deleteLinesForEntry(jEntry.id)
                journalDao.deleteEntry(jEntry)
            }
        }

        returnDao.deletePurchaseReturnItems(pReturn.id)
        returnDao.deletePurchaseReturn(pReturn)
        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Vouchers (سندات القبض والصرف)
    // -------------------------------------------------------------
    suspend fun createVoucher(
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
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (amount <= 0) {
            return@withContext Result.failure(IllegalArgumentException("يجب أن يكون مبلغ السند أكبر من صفر"))
        }

        val cashAccount = if (paymentType == PaymentType.BANK) {
            accountDao.getAccountByCode("112") ?: accountDao.getAccountByCode("111")
        } else {
            accountDao.getAccountByCode("111") ?: accountDao.getAccountByCode("112")
        }
        val receivablesAccount = accountDao.getAccountByCode("113")
        val payablesAccount = accountDao.getAccountByCode("211")

        val journalLines = mutableListOf<JournalEntryLine>()

        if (type == VoucherType.RECEIPT) {
            // Receipt Voucher: Debit Cash/Bank, Credit Partner/Account
            cashAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = amount,
                        credit = 0.0,
                        description = "قبض مبلغ بسند رقم $voucherNumber من $partnerName"
                    )
                )
            }

            when (partnerType) {
                VoucherPartnerType.CUSTOMER -> {
                    receivablesAccount?.let {
                        journalLines.add(
                            JournalEntryLine(
                                accountId = it.id,
                                accountCode = it.code,
                                accountName = it.nameAr,
                                debit = 0.0,
                                credit = amount,
                                description = "سداد عميل سند قبض $voucherNumber - $partnerName"
                            )
                        )
                    }
                    if (partnerId != null) {
                        partnerDao.updateCustomerBalance(partnerId, -amount)
                    }
                }
                VoucherPartnerType.SUPPLIER -> {
                    payablesAccount?.let {
                        journalLines.add(
                            JournalEntryLine(
                                accountId = it.id,
                                accountCode = it.code,
                                accountName = it.nameAr,
                                debit = 0.0,
                                credit = amount,
                                description = "تحصيل مسترد من مورد سند قبض $voucherNumber - $partnerName"
                            )
                        )
                    }
                    if (partnerId != null) {
                        partnerDao.updateSupplierBalance(partnerId, amount)
                    }
                }
                VoucherPartnerType.GENERAL_ACCOUNT -> {
                    val targetAcc = if (accountId != null) accountDao.getAccountById(accountId) else null
                    targetAcc?.let {
                        journalLines.add(
                            JournalEntryLine(
                                accountId = it.id,
                                accountCode = it.code,
                                accountName = it.nameAr,
                                debit = 0.0,
                                credit = amount,
                                description = "إيراد / قبض لحساب ${it.nameAr} سند $voucherNumber"
                            )
                        )
                    }
                }
            }
        } else {
            // Payment Voucher: Credit Cash/Bank, Debit Partner/Account
            when (partnerType) {
                VoucherPartnerType.SUPPLIER -> {
                    payablesAccount?.let {
                        journalLines.add(
                            JournalEntryLine(
                                accountId = it.id,
                                accountCode = it.code,
                                accountName = it.nameAr,
                                debit = amount,
                                credit = 0.0,
                                description = "سداد مورد سند صرف $voucherNumber - $partnerName"
                            )
                        )
                    }
                    if (partnerId != null) {
                        partnerDao.updateSupplierBalance(partnerId, -amount)
                    }
                }
                VoucherPartnerType.CUSTOMER -> {
                    receivablesAccount?.let {
                        journalLines.add(
                            JournalEntryLine(
                                accountId = it.id,
                                accountCode = it.code,
                                accountName = it.nameAr,
                                debit = amount,
                                credit = 0.0,
                                description = "صرف مسترد لعميل سند صرف $voucherNumber - $partnerName"
                            )
                        )
                    }
                    if (partnerId != null) {
                        partnerDao.updateCustomerBalance(partnerId, amount)
                    }
                }
                VoucherPartnerType.GENERAL_ACCOUNT -> {
                    val targetAcc = if (accountId != null) accountDao.getAccountById(accountId) else null
                    targetAcc?.let {
                        journalLines.add(
                            JournalEntryLine(
                                accountId = it.id,
                                accountCode = it.code,
                                accountName = it.nameAr,
                                debit = amount,
                                credit = 0.0,
                                description = "صرف مصروف / لحساب ${it.nameAr} سند $voucherNumber"
                            )
                        )
                    }
                }
            }

            cashAccount?.let {
                journalLines.add(
                    JournalEntryLine(
                        accountId = it.id,
                        accountCode = it.code,
                        accountName = it.nameAr,
                        debit = 0.0,
                        credit = amount,
                        description = "صرف من الصندوق/البنك سند رقم $voucherNumber إلى $partnerName"
                    )
                )
            }
        }

        var journalId: Long? = null
        if (journalLines.size >= 2) {
            val jResult = createJournalEntry(
                entryNumber = "JV-VOUCHER-$voucherNumber",
                date = date,
                description = "${type.arabicName} رقم $voucherNumber - $partnerName",
                referenceNumber = voucherNumber,
                lines = journalLines,
                source = if (type == VoucherType.RECEIPT) "RECEIPT_VOUCHER" else "PAYMENT_VOUCHER"
            )
            journalId = jResult.getOrNull()
        }

        val voucher = Voucher(
            voucherNumber = voucherNumber,
            type = type,
            date = date,
            amount = amount,
            paymentType = paymentType,
            partnerType = partnerType,
            partnerId = partnerId,
            partnerName = partnerName,
            accountId = accountId,
            accountName = accountName,
            notes = notes,
            journalEntryId = journalId
        )

        val vId = voucherDao.insertVoucher(voucher)
        Result.success(vId)
    }

    suspend fun deleteVoucher(voucher: Voucher): Result<Unit> = withContext(Dispatchers.IO) {
        // Rollback partner balance
        if (voucher.type == VoucherType.RECEIPT) {
            if (voucher.partnerType == VoucherPartnerType.CUSTOMER && voucher.partnerId != null) {
                partnerDao.updateCustomerBalance(voucher.partnerId, voucher.amount)
            } else if (voucher.partnerType == VoucherPartnerType.SUPPLIER && voucher.partnerId != null) {
                partnerDao.updateSupplierBalance(voucher.partnerId, -voucher.amount)
            }
        } else {
            if (voucher.partnerType == VoucherPartnerType.SUPPLIER && voucher.partnerId != null) {
                partnerDao.updateSupplierBalance(voucher.partnerId, voucher.amount)
            } else if (voucher.partnerType == VoucherPartnerType.CUSTOMER && voucher.partnerId != null) {
                partnerDao.updateCustomerBalance(voucher.partnerId, -voucher.amount)
            }
        }

        // Delete journal entry
        if (voucher.journalEntryId != null) {
            val jEntry = journalDao.getEntryById(voucher.journalEntryId)
            if (jEntry != null) {
                val lines = journalDao.getLinesForEntry(jEntry.id)
                for (line in lines) {
                    val acc = accountDao.getAccountById(line.accountId)
                    if (acc != null) {
                        val newBalance = if (acc.type.isDebitDefault) {
                            acc.currentBalance - (line.debit - line.credit)
                        } else {
                            acc.currentBalance - (line.credit - line.debit)
                        }
                        accountDao.updateBalance(acc.id, newBalance)
                    }
                }
                journalDao.deleteLinesForEntry(jEntry.id)
                journalDao.deleteEntry(jEntry)
            }
        }

        voucherDao.deleteVoucher(voucher)
        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Customers & Suppliers CRUD with Safe Deletion
    // -------------------------------------------------------------
    suspend fun addCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        partnerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        partnerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomerSafe(customer: Customer): Result<Unit> = withContext(Dispatchers.IO) {
        val invoiceCount = invoiceDao.getSalesInvoiceCountForCustomer(customer.id)
        val voucherCount = voucherDao.getCountByPartnerId(customer.id)
        if (invoiceCount > 0 || voucherCount > 0 || abs(customer.currentBalance) > 0.01) {
            return@withContext Result.failure(
                IllegalStateException("لا يمكن حذف العميل \"${customer.name}\" لوجود فواتير ($invoiceCount) أو سندات ($voucherCount) أو رصيد (${customer.currentBalance}) مرتبط به. يرجى تصفية أو حذف العمليات المرتبطة أولاً.")
            )
        }
        partnerDao.deleteCustomer(customer)
        Result.success(Unit)
    }

    suspend fun addSupplier(supplier: Supplier): Long = withContext(Dispatchers.IO) {
        partnerDao.insertSupplier(supplier)
    }

    suspend fun updateSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        partnerDao.updateSupplier(supplier)
    }

    suspend fun deleteSupplierSafe(supplier: Supplier): Result<Unit> = withContext(Dispatchers.IO) {
        val billCount = invoiceDao.getPurchaseInvoiceCountForSupplier(supplier.id)
        val voucherCount = voucherDao.getCountByPartnerId(supplier.id)
        if (billCount > 0 || voucherCount > 0 || abs(supplier.currentBalance) > 0.01) {
            return@withContext Result.failure(
                IllegalStateException("لا يمكن حذف المورد \"${supplier.name}\" لوجود فواتير شراء ($billCount) أو سندات ($voucherCount) أو رصيد (${supplier.currentBalance}) مرتبط به. يرجى تصفية أو حذف العمليات المرتبطة أولاً.")
            )
        }
        partnerDao.deleteSupplier(supplier)
        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Products & Inventory with Safe Deletion
    // -------------------------------------------------------------
    suspend fun addProduct(product: Product): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProductSafe(product: Product): Result<Unit> = withContext(Dispatchers.IO) {
        val salesCount = invoiceDao.getSalesItemCountForProduct(product.id)
        val purchaseCount = invoiceDao.getPurchaseItemCountForProduct(product.id)
        val movCount = productDao.getMovementCountForProduct(product.id)

        if (salesCount > 0 || purchaseCount > 0 || movCount > 0) {
            return@withContext Result.failure(
                IllegalStateException("لا يمكن حذف الصنف \"${product.nameAr}\" لوجود فواتير مبيعات/مشتريات أو حركات مخزون سابقة مسجلة عليه. يرجى حذف الفواتير والحركات المرتبطة أولاً.")
            )
        }
        productDao.deleteProduct(product)
        Result.success(Unit)
    }

    suspend fun adjustStock(
        productId: Long,
        quantity: Double,
        isAddition: Boolean,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val prod = productDao.getProductById(productId)
            ?: return@withContext Result.failure(IllegalArgumentException("الصنف غير موجود"))

        val delta = if (isAddition) quantity else -quantity
        if (!isAddition && prod.currentStock < quantity) {
            return@withContext Result.failure(IllegalArgumentException("الكمية المطلوبة للتسوية أكبر من الرصيد المتاح بالمخزن"))
        }

        productDao.updateStockQuantity(productId, delta)
        productDao.insertMovement(
            StockMovement(
                productId = productId,
                productName = prod.nameAr,
                date = System.currentTimeMillis(),
                movementType = if (isAddition) MovementType.ADJUSTMENT_ADD else MovementType.ADJUSTMENT_SUB,
                quantity = quantity,
                unitPrice = prod.purchasePrice,
                referenceType = "MANUAL_ADJUSTMENT",
                notes = reason.ifBlank { "تسوية جردية يدوية" }
            )
        )
        Result.success(Unit)
    }
}
