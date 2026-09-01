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
    private val invoiceDao: InvoiceDao
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

    // -------------------------------------------------------------
    // Initial Seed Data (Standard Arab Accounting Chart of Accounts)
    // -------------------------------------------------------------
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val count = accountDao.getCount()
        if (count == 0) {
            val defaultAccounts = listOf(
                // 1. الأصول (Assets)
                Account(code = "1", nameAr = "الأصول", type = AccountType.ASSET, isGroup = true),
                Account(code = "11", nameAr = "الأصول المتداولة", type = AccountType.ASSET, isGroup = true),
                Account(code = "111", nameAr = "الصندوق / الخزينة الرئيسية", type = AccountType.ASSET, isGroup = false, currentBalance = 50000.0),
                Account(code = "112", nameAr = "البنك / الحساب الجاري", type = AccountType.ASSET, isGroup = false, currentBalance = 120000.0),
                Account(code = "113", nameAr = "العملاء والمدينون", type = AccountType.ASSET, isGroup = false, currentBalance = 15000.0),
                Account(code = "114", nameAr = "مخزون البضائع", type = AccountType.ASSET, isGroup = false, currentBalance = 35000.0),
                Account(code = "12", nameAr = "الأصول الثابتة", type = AccountType.ASSET, isGroup = true),
                Account(code = "121", nameAr = "الأجهزة والمعدات", type = AccountType.ASSET, isGroup = false, currentBalance = 25000.0),

                // 2. الالتزامات (Liabilities)
                Account(code = "2", nameAr = "الالتزامات", type = AccountType.LIABILITY, isGroup = true),
                Account(code = "21", nameAr = "الالتزامات المتداولة", type = AccountType.LIABILITY, isGroup = true),
                Account(code = "211", nameAr = "الموردون والدائنون", type = AccountType.LIABILITY, isGroup = false, currentBalance = 18000.0),
                Account(code = "212", nameAr = "أمانات ضريبة القيمة المضافة", type = AccountType.LIABILITY, isGroup = false, currentBalance = 2000.0),

                // 3. حقوق الملكية (Equity)
                Account(code = "3", nameAr = "حقوق الملكية", type = AccountType.EQUITY, isGroup = true),
                Account(code = "31", nameAr = "رأس المال", type = AccountType.EQUITY, isGroup = false, currentBalance = 225000.0),
                Account(code = "32", nameAr = "الأرباح المبقاة / المحتجزة", type = AccountType.EQUITY, isGroup = false, currentBalance = 0.0),

                // 4. الإيرادات (Revenues)
                Account(code = "4", nameAr = "الإيرادات", type = AccountType.REVENUE, isGroup = true),
                Account(code = "41", nameAr = "إيرادات المبيعات", type = AccountType.REVENUE, isGroup = false, currentBalance = 45000.0),
                Account(code = "42", nameAr = "إيرادات خدمات وأخرى", type = AccountType.REVENUE, isGroup = false, currentBalance = 5000.0),

                // 5. المصروفات (Expenses)
                Account(code = "5", nameAr = "المصروفات", type = AccountType.EXPENSE, isGroup = true),
                Account(code = "51", nameAr = "تكلفة البضاعة المباعة", type = AccountType.EXPENSE, isGroup = false, currentBalance = 22000.0),
                Account(code = "52", nameAr = "مصروفات إدارية وعمومية", type = AccountType.EXPENSE, isGroup = true),
                Account(code = "521", nameAr = "مصروف الإيجار", type = AccountType.EXPENSE, isGroup = false, currentBalance = 6000.0),
                Account(code = "522", nameAr = "الرواتب والأجور", type = AccountType.EXPENSE, isGroup = false, currentBalance = 12000.0),
                Account(code = "523", nameAr = "مصاريف الكهرباء والمياه والاتصالات", type = AccountType.EXPENSE, isGroup = false, currentBalance = 1500.0)
            )
            accountDao.insertAccounts(defaultAccounts)

            // Initial Products
            val defaultProducts = listOf(
                Product(code = "PRD-001", barcode = "6281001", nameAr = "كمبيوتر محمول Core i7", category = "إلكترونيات", unit = "جهاز", purchasePrice = 3200.0, sellingPrice = 3800.0, currentStock = 12.0, minStockLevel = 3.0),
                Product(code = "PRD-002", barcode = "6281002", nameAr = "شاشة عرض 27 بوصة 4K", category = "إلكترونيات", unit = "شاشة", purchasePrice = 950.0, sellingPrice = 1250.0, currentStock = 15.0, minStockLevel = 4.0),
                Product(code = "PRD-003", barcode = "6281003", nameAr = "طابعة ليزر متعددة المهام", category = "أجهزة مكتبية", unit = "طابعة", purchasePrice = 1100.0, sellingPrice = 1450.0, currentStock = 6.0, minStockLevel = 2.0),
                Product(code = "PRD-004", barcode = "6281004", nameAr = "لوحة مفاتيح وماوس لاسلكي", category = "إكسسوارات", unit = "طقم", purchasePrice = 120.0, sellingPrice = 180.0, currentStock = 30.0, minStockLevel = 8.0),
                Product(code = "PRD-005", barcode = "6281005", nameAr = "ورق طباعة A4 80g (كرتون)", category = "قرطاسية", unit = "كرتون", purchasePrice = 90.0, sellingPrice = 115.0, currentStock = 2.0, minStockLevel = 5.0) // Low stock
            )
            productDao.insertProducts(defaultProducts)

            // Initial Customers
            val defaultCustomers = listOf(
                Customer(name = "مؤسسة الأفق للتجارة", phone = "0501234567", email = "info@alofooq.sa", taxNumber = "300123456700003", address = "الرياض - الملز", currentBalance = 8500.0),
                Customer(name = "شركة النور للحلول التقنية", phone = "0559876543", email = "sales@alnoor.com", taxNumber = "310987654300003", address = "جدة - الروضة", currentBalance = 6500.0),
                Customer(name = "عميل نقدي عام", phone = "-", email = "", address = "الفرع الرئيسي", currentBalance = 0.0)
            )
            partnerDao.insertCustomers(defaultCustomers)

            // Initial Suppliers
            val defaultSuppliers = listOf(
                Supplier(name = "شركة التقنية العالمية للتوزيع", phone = "0112345678", email = "orders@globaltech.sa", taxNumber = "300999888100003", address = "الرياض - السلي", currentBalance = 12000.0),
                Supplier(name = "مؤسسة التوريدات المكتبية الحديثة", phone = "0123456789", email = "supply@modernoffice.sa", taxNumber = "300777666200003", address = "الدمام - الميناء", currentBalance = 6000.0)
            )
            partnerDao.insertSuppliers(defaultSuppliers)

            // Seed an opening journal entry
            val openingLines = listOf(
                JournalEntryLine(accountId = 3, accountCode = "111", accountName = "الصندوق / الخزينة الرئيسية", debit = 50000.0, credit = 0.0, description = "رصيد افتتاحي الصندوق"),
                JournalEntryLine(accountId = 4, accountCode = "112", accountName = "البنك / الحساب الجاري", debit = 120000.0, credit = 0.0, description = "رصيد افتتاحي البنك"),
                JournalEntryLine(accountId = 6, accountCode = "114", accountName = "مخزون البضائع", debit = 35000.0, credit = 0.0, description = "رصيد افتتاحي المخزون"),
                JournalEntryLine(accountId = 8, accountCode = "121", accountName = "الأجهزة والمعدات", debit = 25000.0, credit = 0.0, description = "أصول ثابتة افتتاحية"),
                JournalEntryLine(accountId = 12, accountCode = "31", accountName = "رأس المال", debit = 0.0, credit = 230000.0, description = "إثبات رأس المال الافتتاحي")
            )
            val jEntry = JournalEntry(
                entryNumber = "JV-OPENING-01",
                date = System.currentTimeMillis() - (86400000L * 30),
                description = "القيد الافتتاحي وبدء الدورة المحاسبية",
                referenceNumber = "REF-2025-001",
                source = "MANUAL",
                totalDebit = 230000.0,
                totalCredit = 230000.0
            )
            val entryId = journalDao.insertEntry(jEntry)
            val linkedLines = openingLines.map { it.copy(entryId = entryId) }
            journalDao.insertLines(linkedLines)
        }
    }

    // -------------------------------------------------------------
    // Accounts
    // -------------------------------------------------------------
    suspend fun addAccount(account: Account): Long = withContext(Dispatchers.IO) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: Account) = withContext(Dispatchers.IO) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: Account) = withContext(Dispatchers.IO) {
        accountDao.deleteAccount(account)
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

        // Check balance (allowing tiny rounding error)
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
        paymentType: PaymentType,
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("يجب إضافة أصناف للفاتورة"))
        }

        val subtotal = items.sumOf { it.lineTotal }
        val afterDiscount = (subtotal - discount).coerceAtLeast(0.0)
        val taxAmount = afterDiscount * (taxRate / 100.0)
        val totalAmount = afterDiscount + taxAmount

        val paidAmount = if (paymentType == PaymentType.CREDIT) 0.0 else totalAmount
        val remainingAmount = totalAmount - paidAmount

        // 1. Create Automated Accounting Journal Entry for the Sale
        val cashAccount = accountDao.getAccountByCode("111") ?: accountDao.getAccountByCode("112")
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

    suspend fun addCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        partnerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        partnerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        partnerDao.deleteCustomer(customer)
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
        paymentType: PaymentType,
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("يجب إضافة أصناف لفاتورة الشراء"))
        }

        val subtotal = items.sumOf { it.lineTotal }
        val afterDiscount = (subtotal - discount).coerceAtLeast(0.0)
        val taxAmount = afterDiscount * (taxRate / 100.0)
        val totalAmount = afterDiscount + taxAmount

        val paidAmount = if (paymentType == PaymentType.CREDIT) 0.0 else totalAmount
        val remainingAmount = totalAmount - paidAmount

        // 1. Create Automated Accounting Journal Entry for Purchase
        val cashAccount = accountDao.getAccountByCode("111") ?: accountDao.getAccountByCode("112")
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

    suspend fun addSupplier(supplier: Supplier): Long = withContext(Dispatchers.IO) {
        partnerDao.insertSupplier(supplier)
    }

    suspend fun updateSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        partnerDao.updateSupplier(supplier)
    }

    suspend fun deleteSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        partnerDao.deleteSupplier(supplier)
    }

    // -------------------------------------------------------------
    // Products & Inventory
    // -------------------------------------------------------------
    suspend fun addProduct(product: Product): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
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
