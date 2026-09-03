package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(
    private val accountDao: AccountDao,
    private val journalDao: JournalDao,
    private val productDao: ProductDao,
    private val partnerDao: PartnerDao,
    private val invoiceDao: InvoiceDao,
    private val voucherDao: VoucherDao,
    private val returnDao: ReturnDao
) {
    suspend fun exportBackupJson(
        storeName: String,
        storePhone: String,
        currencySymbol: String,
        isTaxEnabled: Boolean,
        defaultTaxRate: Double,
        showDecimals: Boolean
    ): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "EasyAccounting")
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val settings = JSONObject()
        settings.put("storeName", storeName)
        settings.put("storePhone", storePhone)
        settings.put("currencySymbol", currencySymbol)
        settings.put("isTaxEnabled", isTaxEnabled)
        settings.put("defaultTaxRate", defaultTaxRate)
        settings.put("showDecimals", showDecimals)
        root.put("settings", settings)

        // Accounts
        val accounts = accountDao.getAllAccountsList()
        val accountsArr = JSONArray()
        for (a in accounts) {
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("code", a.code)
            obj.put("nameAr", a.nameAr)
            obj.put("nameEn", a.nameEn)
            obj.put("type", a.type.name)
            if (a.parentId != null) obj.put("parentId", a.parentId)
            obj.put("isGroup", a.isGroup)
            obj.put("initialBalance", a.initialBalance)
            obj.put("currentBalance", a.currentBalance)
            obj.put("notes", a.notes)
            obj.put("isActive", a.isActive)
            accountsArr.put(obj)
        }
        root.put("accounts", accountsArr)

        // Products
        val products = productDao.getAllProductsList()
        val productsArr = JSONArray()
        for (p in products) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("code", p.code)
            obj.put("barcode", p.barcode)
            obj.put("nameAr", p.nameAr)
            obj.put("category", p.category)
            obj.put("unit", p.unit)
            obj.put("purchasePrice", p.purchasePrice)
            obj.put("sellingPrice", p.sellingPrice)
            obj.put("currentStock", p.currentStock)
            obj.put("minStockLevel", p.minStockLevel)
            obj.put("notes", p.notes)
            obj.put("isActive", p.isActive)
            productsArr.put(obj)
        }
        root.put("products", productsArr)

        // Stock movements
        val movements = productDao.getAllMovementsList()
        val movementsArr = JSONArray()
        for (m in movements) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("productId", m.productId)
            obj.put("productName", m.productName)
            obj.put("date", m.date)
            obj.put("movementType", m.movementType.name)
            obj.put("quantity", m.quantity)
            obj.put("unitPrice", m.unitPrice)
            obj.put("totalCost", m.totalCost)
            obj.put("referenceType", m.referenceType)
            if (m.referenceId != null) obj.put("referenceId", m.referenceId)
            obj.put("referenceNumber", m.referenceNumber)
            obj.put("notes", m.notes)
            movementsArr.put(obj)
        }
        root.put("stockMovements", movementsArr)

        // Customers
        val customers = partnerDao.getAllCustomersList()
        val customersArr = JSONArray()
        for (c in customers) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("email", c.email)
            obj.put("taxNumber", c.taxNumber)
            obj.put("address", c.address)
            obj.put("currentBalance", c.currentBalance)
            obj.put("notes", c.notes)
            obj.put("isActive", c.isActive)
            customersArr.put(obj)
        }
        root.put("customers", customersArr)

        // Suppliers
        val suppliers = partnerDao.getAllSuppliersList()
        val suppliersArr = JSONArray()
        for (s in suppliers) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("phone", s.phone)
            obj.put("email", s.email)
            obj.put("taxNumber", s.taxNumber)
            obj.put("address", s.address)
            obj.put("currentBalance", s.currentBalance)
            obj.put("notes", s.notes)
            obj.put("isActive", s.isActive)
            suppliersArr.put(obj)
        }
        root.put("suppliers", suppliersArr)

        // Sales Invoices
        val salesInvoices = invoiceDao.getAllSalesInvoicesList()
        val salesInvArr = JSONArray()
        for (inv in salesInvoices) {
            val obj = JSONObject()
            obj.put("id", inv.id)
            obj.put("invoiceNumber", inv.invoiceNumber)
            obj.put("date", inv.date)
            if (inv.customerId != null) obj.put("customerId", inv.customerId)
            obj.put("customerName", inv.customerName)
            obj.put("subtotal", inv.subtotal)
            obj.put("discount", inv.discount)
            obj.put("taxRate", inv.taxRate)
            obj.put("taxAmount", inv.taxAmount)
            obj.put("totalAmount", inv.totalAmount)
            obj.put("paidAmount", inv.paidAmount)
            obj.put("remainingAmount", inv.remainingAmount)
            obj.put("paymentType", inv.paymentType.name)
            if (inv.journalEntryId != null) obj.put("journalEntryId", inv.journalEntryId)
            obj.put("notes", inv.notes)
            obj.put("isCancelled", inv.isCancelled)
            salesInvArr.put(obj)
        }
        root.put("salesInvoices", salesInvArr)

        // Sales Invoice Items
        val salesItems = invoiceDao.getAllSalesInvoiceItemsList()
        val salesItemsArr = JSONArray()
        for (it in salesItems) {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("invoiceId", it.invoiceId)
            obj.put("productId", it.productId)
            obj.put("productName", it.productName)
            obj.put("quantity", it.quantity)
            obj.put("unitPrice", it.unitPrice)
            obj.put("unitCost", it.unitCost)
            obj.put("discount", it.discount)
            obj.put("lineTotal", it.lineTotal)
            salesItemsArr.put(obj)
        }
        root.put("salesInvoiceItems", salesItemsArr)

        // Purchase Invoices
        val purchaseInvoices = invoiceDao.getAllPurchaseInvoicesList()
        val purchInvArr = JSONArray()
        for (b in purchaseInvoices) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("billNumber", b.billNumber)
            obj.put("supplierInvoiceRef", b.supplierInvoiceRef)
            obj.put("date", b.date)
            if (b.supplierId != null) obj.put("supplierId", b.supplierId)
            obj.put("supplierName", b.supplierName)
            obj.put("subtotal", b.subtotal)
            obj.put("discount", b.discount)
            obj.put("taxRate", b.taxRate)
            obj.put("taxAmount", b.taxAmount)
            obj.put("totalAmount", b.totalAmount)
            obj.put("paidAmount", b.paidAmount)
            obj.put("remainingAmount", b.remainingAmount)
            obj.put("paymentType", b.paymentType.name)
            if (b.journalEntryId != null) obj.put("journalEntryId", b.journalEntryId)
            obj.put("notes", b.notes)
            obj.put("isCancelled", b.isCancelled)
            purchInvArr.put(obj)
        }
        root.put("purchaseInvoices", purchInvArr)

        // Purchase Invoice Items
        val purchaseItems = invoiceDao.getAllPurchaseInvoiceItemsList()
        val purchItemsArr = JSONArray()
        for (it in purchaseItems) {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("billId", it.billId)
            obj.put("productId", it.productId)
            obj.put("productName", it.productName)
            obj.put("quantity", it.quantity)
            obj.put("unitPrice", it.unitPrice)
            obj.put("discount", it.discount)
            obj.put("lineTotal", it.lineTotal)
            purchItemsArr.put(obj)
        }
        root.put("purchaseInvoiceItems", purchItemsArr)

        // Sales Returns
        val salesReturns = returnDao.getAllSalesReturnsList()
        val sRetArr = JSONArray()
        for (r in salesReturns) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("returnNumber", r.returnNumber)
            obj.put("originalInvoiceNumber", r.originalInvoiceNumber)
            obj.put("date", r.date)
            if (r.customerId != null) obj.put("customerId", r.customerId)
            obj.put("customerName", r.customerName)
            obj.put("subtotal", r.subtotal)
            obj.put("taxRate", r.taxRate)
            obj.put("taxAmount", r.taxAmount)
            obj.put("totalAmount", r.totalAmount)
            obj.put("paymentType", r.paymentType.name)
            if (r.journalEntryId != null) obj.put("journalEntryId", r.journalEntryId)
            obj.put("notes", r.notes)
            sRetArr.put(obj)
        }
        root.put("salesReturns", sRetArr)

        // Sales Return Items
        val sRetItems = returnDao.getAllSalesReturnItemsList()
        val sRetItemsArr = JSONArray()
        for (it in sRetItems) {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("returnId", it.returnId)
            obj.put("productId", it.productId)
            obj.put("productName", it.productName)
            obj.put("quantity", it.quantity)
            obj.put("unitPrice", it.unitPrice)
            obj.put("unitCost", it.unitCost)
            obj.put("lineTotal", it.lineTotal)
            sRetItemsArr.put(obj)
        }
        root.put("salesReturnItems", sRetItemsArr)

        // Purchase Returns
        val purchReturns = returnDao.getAllPurchaseReturnsList()
        val pRetArr = JSONArray()
        for (r in purchReturns) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("returnNumber", r.returnNumber)
            obj.put("originalBillNumber", r.originalBillNumber)
            obj.put("date", r.date)
            if (r.supplierId != null) obj.put("supplierId", r.supplierId)
            obj.put("supplierName", r.supplierName)
            obj.put("subtotal", r.subtotal)
            obj.put("taxRate", r.taxRate)
            obj.put("taxAmount", r.taxAmount)
            obj.put("totalAmount", r.totalAmount)
            obj.put("paymentType", r.paymentType.name)
            if (r.journalEntryId != null) obj.put("journalEntryId", r.journalEntryId)
            obj.put("notes", r.notes)
            pRetArr.put(obj)
        }
        root.put("purchaseReturns", pRetArr)

        // Purchase Return Items
        val pRetItems = returnDao.getAllPurchaseReturnItemsList()
        val pRetItemsArr = JSONArray()
        for (it in pRetItems) {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("returnId", it.returnId)
            obj.put("productId", it.productId)
            obj.put("productName", it.productName)
            obj.put("quantity", it.quantity)
            obj.put("unitPrice", it.unitPrice)
            obj.put("lineTotal", it.lineTotal)
            pRetItemsArr.put(obj)
        }
        root.put("purchaseReturnItems", pRetItemsArr)

        // Vouchers
        val vouchers = voucherDao.getAllVouchersList()
        val vouchersArr = JSONArray()
        for (v in vouchers) {
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("voucherNumber", v.voucherNumber)
            obj.put("type", v.type.name)
            obj.put("date", v.date)
            obj.put("amount", v.amount)
            obj.put("paymentType", v.paymentType.name)
            obj.put("partnerType", v.partnerType.name)
            if (v.partnerId != null) obj.put("partnerId", v.partnerId)
            obj.put("partnerName", v.partnerName)
            if (v.accountId != null) obj.put("accountId", v.accountId)
            obj.put("accountName", v.accountName)
            obj.put("notes", v.notes)
            if (v.journalEntryId != null) obj.put("journalEntryId", v.journalEntryId)
            vouchersArr.put(obj)
        }
        root.put("vouchers", vouchersArr)

        // Journal Entries
        val journals = journalDao.getAllEntriesList()
        val jArr = JSONArray()
        for (j in journals) {
            val obj = JSONObject()
            obj.put("id", j.id)
            obj.put("entryNumber", j.entryNumber)
            obj.put("date", j.date)
            obj.put("description", j.description)
            obj.put("referenceNumber", j.referenceNumber)
            obj.put("source", j.source)
            obj.put("totalDebit", j.totalDebit)
            obj.put("totalCredit", j.totalCredit)
            obj.put("isPosted", j.isPosted)
            obj.put("createdAt", j.createdAt)
            jArr.put(obj)
        }
        root.put("journalEntries", jArr)

        // Journal Entry Lines
        val lines = journalDao.getAllLinesList()
        val linesArr = JSONArray()
        for (l in lines) {
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("entryId", l.entryId)
            obj.put("accountId", l.accountId)
            obj.put("accountCode", l.accountCode)
            obj.put("accountName", l.accountName)
            obj.put("debit", l.debit)
            obj.put("credit", l.credit)
            obj.put("description", l.description)
            linesArr.put(obj)
        }
        root.put("journalLines", linesArr)

        root.toString(2)
    }

    suspend fun restoreBackupJson(
        jsonString: String,
        onRestoreSettings: (storeName: String, storePhone: String, currencySymbol: String, isTaxEnabled: Boolean, defaultTaxRate: Double, showDecimals: Boolean) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("accounts") && !root.has("products") && !root.has("settings")) {
                return@withContext Result.failure(IllegalArgumentException("ملف النسخة الاحتياطية غير صالح أو تالف"))
            }

            // 1. Settings
            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                val storeName = s.optString("storeName", "مؤسسة تجارية")
                val storePhone = s.optString("storePhone", "")
                val currencySymbol = s.optString("currencySymbol", "ر.س")
                val isTaxEnabled = s.optBoolean("isTaxEnabled", false)
                val defaultTaxRate = s.optDouble("defaultTaxRate", 0.0)
                val showDecimals = s.optBoolean("showDecimals", false)
                onRestoreSettings(storeName, storePhone, currencySymbol, isTaxEnabled, defaultTaxRate, showDecimals)
            }

            // 2. Clear current database safely
            invoiceDao.deleteAllSalesInvoiceItems()
            invoiceDao.deleteAllSalesInvoices()
            invoiceDao.deleteAllPurchaseInvoiceItems()
            invoiceDao.deleteAllPurchaseInvoices()
            returnDao.deleteAllSalesReturnItems()
            returnDao.deleteAllSalesReturns()
            returnDao.deleteAllPurchaseReturnItems()
            returnDao.deleteAllPurchaseReturns()
            voucherDao.deleteAllVouchers()
            journalDao.deleteAllLines()
            journalDao.deleteAllEntries()
            productDao.deleteAllStockMovements()
            productDao.deleteAllProducts()
            partnerDao.deleteAllSuppliers()
            partnerDao.deleteAllCustomers()
            accountDao.deleteAllAccounts()

            // 3. Parse and Insert in foreign-key safe order

            // Accounts
            val accountsArr = root.optJSONArray("accounts") ?: JSONArray()
            val accountsList = mutableListOf<Account>()
            for (i in 0 until accountsArr.length()) {
                val o = accountsArr.getJSONObject(i)
                accountsList.add(
                    Account(
                        id = o.getLong("id"),
                        code = o.getString("code"),
                        nameAr = o.getString("nameAr"),
                        nameEn = o.optString("nameEn", ""),
                        type = try { AccountType.valueOf(o.getString("type")) } catch (e: Exception) { AccountType.ASSET },
                        parentId = if (o.has("parentId") && !o.isNull("parentId")) o.getLong("parentId") else null,
                        isGroup = o.optBoolean("isGroup", false),
                        initialBalance = o.optDouble("initialBalance", 0.0),
                        currentBalance = o.optDouble("currentBalance", 0.0),
                        notes = o.optString("notes", ""),
                        isActive = o.optBoolean("isActive", true)
                    )
                )
            }
            if (accountsList.isNotEmpty()) {
                accountDao.insertAccounts(accountsList)
            }

            // Products
            val productsArr = root.optJSONArray("products") ?: JSONArray()
            val productsList = mutableListOf<Product>()
            for (i in 0 until productsArr.length()) {
                val o = productsArr.getJSONObject(i)
                productsList.add(
                    Product(
                        id = o.getLong("id"),
                        code = o.getString("code"),
                        barcode = o.optString("barcode", ""),
                        nameAr = o.getString("nameAr"),
                        category = o.optString("category", "عام"),
                        unit = o.optString("unit", "قطعة"),
                        purchasePrice = o.optDouble("purchasePrice", 0.0),
                        sellingPrice = o.optDouble("sellingPrice", 0.0),
                        currentStock = o.optDouble("currentStock", 0.0),
                        minStockLevel = o.optDouble("minStockLevel", 5.0),
                        notes = o.optString("notes", ""),
                        isActive = o.optBoolean("isActive", true)
                    )
                )
            }
            if (productsList.isNotEmpty()) {
                productDao.insertProducts(productsList)
            }

            // Customers
            val customersArr = root.optJSONArray("customers") ?: JSONArray()
            val customersList = mutableListOf<Customer>()
            for (i in 0 until customersArr.length()) {
                val o = customersArr.getJSONObject(i)
                customersList.add(
                    Customer(
                        id = o.getLong("id"),
                        name = o.getString("name"),
                        phone = o.optString("phone", ""),
                        email = o.optString("email", ""),
                        taxNumber = o.optString("taxNumber", ""),
                        address = o.optString("address", ""),
                        currentBalance = o.optDouble("currentBalance", 0.0),
                        notes = o.optString("notes", ""),
                        isActive = o.optBoolean("isActive", true)
                    )
                )
            }
            if (customersList.isNotEmpty()) {
                partnerDao.insertCustomers(customersList)
            }

            // Suppliers
            val suppliersArr = root.optJSONArray("suppliers") ?: JSONArray()
            val suppliersList = mutableListOf<Supplier>()
            for (i in 0 until suppliersArr.length()) {
                val o = suppliersArr.getJSONObject(i)
                suppliersList.add(
                    Supplier(
                        id = o.getLong("id"),
                        name = o.getString("name"),
                        phone = o.optString("phone", ""),
                        email = o.optString("email", ""),
                        taxNumber = o.optString("taxNumber", ""),
                        address = o.optString("address", ""),
                        currentBalance = o.optDouble("currentBalance", 0.0),
                        notes = o.optString("notes", ""),
                        isActive = o.optBoolean("isActive", true)
                    )
                )
            }
            if (suppliersList.isNotEmpty()) {
                partnerDao.insertSuppliers(suppliersList)
            }

            // Journal Entries
            val jArr = root.optJSONArray("journalEntries") ?: JSONArray()
            val jList = mutableListOf<JournalEntry>()
            for (i in 0 until jArr.length()) {
                val o = jArr.getJSONObject(i)
                jList.add(
                    JournalEntry(
                        id = o.getLong("id"),
                        entryNumber = o.getString("entryNumber"),
                        date = o.getLong("date"),
                        description = o.getString("description"),
                        referenceNumber = o.optString("referenceNumber", ""),
                        source = o.optString("source", "MANUAL"),
                        totalDebit = o.optDouble("totalDebit", 0.0),
                        totalCredit = o.optDouble("totalCredit", 0.0),
                        isPosted = o.optBoolean("isPosted", true),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            if (jList.isNotEmpty()) {
                journalDao.insertEntries(jList)
            }

            // Journal Lines
            val linesArr = root.optJSONArray("journalLines") ?: JSONArray()
            val linesList = mutableListOf<JournalEntryLine>()
            for (i in 0 until linesArr.length()) {
                val o = linesArr.getJSONObject(i)
                linesList.add(
                    JournalEntryLine(
                        id = o.getLong("id"),
                        entryId = o.getLong("entryId"),
                        accountId = o.getLong("accountId"),
                        accountCode = o.getString("accountCode"),
                        accountName = o.getString("accountName"),
                        debit = o.optDouble("debit", 0.0),
                        credit = o.optDouble("credit", 0.0),
                        description = o.optString("description", "")
                    )
                )
            }
            if (linesList.isNotEmpty()) {
                journalDao.insertLines(linesList)
            }

            // Sales Invoices
            val sInvArr = root.optJSONArray("salesInvoices") ?: JSONArray()
            val sInvList = mutableListOf<SalesInvoice>()
            for (i in 0 until sInvArr.length()) {
                val o = sInvArr.getJSONObject(i)
                sInvList.add(
                    SalesInvoice(
                        id = o.getLong("id"),
                        invoiceNumber = o.getString("invoiceNumber"),
                        date = o.getLong("date"),
                        customerId = if (o.has("customerId") && !o.isNull("customerId")) o.getLong("customerId") else null,
                        customerName = o.getString("customerName"),
                        subtotal = o.optDouble("subtotal", 0.0),
                        discount = o.optDouble("discount", 0.0),
                        taxRate = o.optDouble("taxRate", 0.0),
                        taxAmount = o.optDouble("taxAmount", 0.0),
                        totalAmount = o.optDouble("totalAmount", 0.0),
                        paidAmount = o.optDouble("paidAmount", 0.0),
                        remainingAmount = o.optDouble("remainingAmount", 0.0),
                        paymentType = try { PaymentType.valueOf(o.getString("paymentType")) } catch (e: Exception) { PaymentType.CASH },
                        journalEntryId = if (o.has("journalEntryId") && !o.isNull("journalEntryId")) o.getLong("journalEntryId") else null,
                        notes = o.optString("notes", ""),
                        isCancelled = o.optBoolean("isCancelled", false)
                    )
                )
            }
            if (sInvList.isNotEmpty()) {
                invoiceDao.insertSalesInvoices(sInvList)
            }

            // Sales Items
            val sItemsArr = root.optJSONArray("salesInvoiceItems") ?: JSONArray()
            val sItemsList = mutableListOf<SalesInvoiceItem>()
            for (i in 0 until sItemsArr.length()) {
                val o = sItemsArr.getJSONObject(i)
                sItemsList.add(
                    SalesInvoiceItem(
                        id = o.getLong("id"),
                        invoiceId = o.getLong("invoiceId"),
                        productId = o.getLong("productId"),
                        productName = o.getString("productName"),
                        quantity = o.optDouble("quantity", 1.0),
                        unitPrice = o.optDouble("unitPrice", 0.0),
                        unitCost = o.optDouble("unitCost", 0.0),
                        discount = o.optDouble("discount", 0.0),
                        lineTotal = o.optDouble("lineTotal", 0.0)
                    )
                )
            }
            if (sItemsList.isNotEmpty()) {
                invoiceDao.insertSalesInvoiceItems(sItemsList)
            }

            // Purchase Invoices
            val pInvArr = root.optJSONArray("purchaseInvoices") ?: JSONArray()
            val pInvList = mutableListOf<PurchaseInvoice>()
            for (i in 0 until pInvArr.length()) {
                val o = pInvArr.getJSONObject(i)
                pInvList.add(
                    PurchaseInvoice(
                        id = o.getLong("id"),
                        billNumber = o.getString("billNumber"),
                        supplierInvoiceRef = o.optString("supplierInvoiceRef", ""),
                        date = o.getLong("date"),
                        supplierId = if (o.has("supplierId") && !o.isNull("supplierId")) o.getLong("supplierId") else null,
                        supplierName = o.getString("supplierName"),
                        subtotal = o.optDouble("subtotal", 0.0),
                        discount = o.optDouble("discount", 0.0),
                        taxRate = o.optDouble("taxRate", 0.0),
                        taxAmount = o.optDouble("taxAmount", 0.0),
                        totalAmount = o.optDouble("totalAmount", 0.0),
                        paidAmount = o.optDouble("paidAmount", 0.0),
                        remainingAmount = o.optDouble("remainingAmount", 0.0),
                        paymentType = try { PaymentType.valueOf(o.getString("paymentType")) } catch (e: Exception) { PaymentType.CASH },
                        journalEntryId = if (o.has("journalEntryId") && !o.isNull("journalEntryId")) o.getLong("journalEntryId") else null,
                        notes = o.optString("notes", ""),
                        isCancelled = o.optBoolean("isCancelled", false)
                    )
                )
            }
            if (pInvList.isNotEmpty()) {
                invoiceDao.insertPurchaseInvoices(pInvList)
            }

            // Purchase Items
            val pItemsArr = root.optJSONArray("purchaseInvoiceItems") ?: JSONArray()
            val pItemsList = mutableListOf<PurchaseInvoiceItem>()
            for (i in 0 until pItemsArr.length()) {
                val o = pItemsArr.getJSONObject(i)
                pItemsList.add(
                    PurchaseInvoiceItem(
                        id = o.getLong("id"),
                        billId = o.getLong("billId"),
                        productId = o.getLong("productId"),
                        productName = o.getString("productName"),
                        quantity = o.optDouble("quantity", 1.0),
                        unitPrice = o.optDouble("unitPrice", 0.0),
                        discount = o.optDouble("discount", 0.0),
                        lineTotal = o.optDouble("lineTotal", 0.0)
                    )
                )
            }
            if (pItemsList.isNotEmpty()) {
                invoiceDao.insertPurchaseInvoiceItems(pItemsList)
            }

            // Sales Returns
            val sRetArr = root.optJSONArray("salesReturns") ?: JSONArray()
            val sRetList = mutableListOf<SalesReturn>()
            for (i in 0 until sRetArr.length()) {
                val o = sRetArr.getJSONObject(i)
                sRetList.add(
                    SalesReturn(
                        id = o.getLong("id"),
                        returnNumber = o.getString("returnNumber"),
                        originalInvoiceNumber = o.optString("originalInvoiceNumber", ""),
                        date = o.getLong("date"),
                        customerId = if (o.has("customerId") && !o.isNull("customerId")) o.getLong("customerId") else null,
                        customerName = o.getString("customerName"),
                        subtotal = o.optDouble("subtotal", 0.0),
                        taxRate = o.optDouble("taxRate", 0.0),
                        taxAmount = o.optDouble("taxAmount", 0.0),
                        totalAmount = o.optDouble("totalAmount", 0.0),
                        paymentType = try { PaymentType.valueOf(o.getString("paymentType")) } catch (e: Exception) { PaymentType.CASH },
                        journalEntryId = if (o.has("journalEntryId") && !o.isNull("journalEntryId")) o.getLong("journalEntryId") else null,
                        notes = o.optString("notes", "")
                    )
                )
            }
            if (sRetList.isNotEmpty()) {
                returnDao.insertSalesReturns(sRetList)
            }

            // Sales Return Items
            val sRetItemsArr = root.optJSONArray("salesReturnItems") ?: JSONArray()
            val sRetItemsList = mutableListOf<SalesReturnItem>()
            for (i in 0 until sRetItemsArr.length()) {
                val o = sRetItemsArr.getJSONObject(i)
                sRetItemsList.add(
                    SalesReturnItem(
                        id = o.getLong("id"),
                        returnId = o.getLong("returnId"),
                        productId = o.getLong("productId"),
                        productName = o.getString("productName"),
                        quantity = o.optDouble("quantity", 1.0),
                        unitPrice = o.optDouble("unitPrice", 0.0),
                        unitCost = o.optDouble("unitCost", 0.0),
                        lineTotal = o.optDouble("lineTotal", 0.0)
                    )
                )
            }
            if (sRetItemsList.isNotEmpty()) {
                returnDao.insertSalesReturnItems(sRetItemsList)
            }

            // Purchase Returns
            val pRetArr = root.optJSONArray("purchaseReturns") ?: JSONArray()
            val pRetList = mutableListOf<PurchaseReturn>()
            for (i in 0 until pRetArr.length()) {
                val o = pRetArr.getJSONObject(i)
                pRetList.add(
                    PurchaseReturn(
                        id = o.getLong("id"),
                        returnNumber = o.getString("returnNumber"),
                        originalBillNumber = o.optString("originalBillNumber", ""),
                        date = o.getLong("date"),
                        supplierId = if (o.has("supplierId") && !o.isNull("supplierId")) o.getLong("supplierId") else null,
                        supplierName = o.getString("supplierName"),
                        subtotal = o.optDouble("subtotal", 0.0),
                        taxRate = o.optDouble("taxRate", 0.0),
                        taxAmount = o.optDouble("taxAmount", 0.0),
                        totalAmount = o.optDouble("totalAmount", 0.0),
                        paymentType = try { PaymentType.valueOf(o.getString("paymentType")) } catch (e: Exception) { PaymentType.CASH },
                        journalEntryId = if (o.has("journalEntryId") && !o.isNull("journalEntryId")) o.getLong("journalEntryId") else null,
                        notes = o.optString("notes", "")
                    )
                )
            }
            if (pRetList.isNotEmpty()) {
                returnDao.insertPurchaseReturns(pRetList)
            }

            // Purchase Return Items
            val pRetItemsArr = root.optJSONArray("purchaseReturnItems") ?: JSONArray()
            val pRetItemsList = mutableListOf<PurchaseReturnItem>()
            for (i in 0 until pRetItemsArr.length()) {
                val o = pRetItemsArr.getJSONObject(i)
                pRetItemsList.add(
                    PurchaseReturnItem(
                        id = o.getLong("id"),
                        returnId = o.getLong("returnId"),
                        productId = o.getLong("productId"),
                        productName = o.getString("productName"),
                        quantity = o.optDouble("quantity", 1.0),
                        unitPrice = o.optDouble("unitPrice", 0.0),
                        lineTotal = o.optDouble("lineTotal", 0.0)
                    )
                )
            }
            if (pRetItemsList.isNotEmpty()) {
                returnDao.insertPurchaseReturnItems(pRetItemsList)
            }

            // Stock movements
            val movementsArr = root.optJSONArray("stockMovements") ?: JSONArray()
            val movementsList = mutableListOf<StockMovement>()
            for (i in 0 until movementsArr.length()) {
                val o = movementsArr.getJSONObject(i)
                movementsList.add(
                    StockMovement(
                        id = o.getLong("id"),
                        productId = o.getLong("productId"),
                        productName = o.getString("productName"),
                        date = o.getLong("date"),
                        movementType = try { MovementType.valueOf(o.getString("movementType")) } catch (e: Exception) { MovementType.ADJUSTMENT_ADD },
                        quantity = o.optDouble("quantity", 0.0),
                        unitPrice = o.optDouble("unitPrice", 0.0),
                        totalCost = o.optDouble("totalCost", 0.0),
                        referenceType = o.optString("referenceType", ""),
                        referenceId = if (o.has("referenceId") && !o.isNull("referenceId")) o.getLong("referenceId") else null,
                        referenceNumber = o.optString("referenceNumber", ""),
                        notes = o.optString("notes", "")
                    )
                )
            }
            if (movementsList.isNotEmpty()) {
                productDao.insertMovements(movementsList)
            }

            // Vouchers
            val vouchersArr = root.optJSONArray("vouchers") ?: JSONArray()
            val vouchersList = mutableListOf<Voucher>()
            for (i in 0 until vouchersArr.length()) {
                val o = vouchersArr.getJSONObject(i)
                vouchersList.add(
                    Voucher(
                        id = o.getLong("id"),
                        voucherNumber = o.getString("voucherNumber"),
                        type = try { VoucherType.valueOf(o.getString("type")) } catch (e: Exception) { VoucherType.RECEIPT },
                        date = o.getLong("date"),
                        amount = o.optDouble("amount", 0.0),
                        paymentType = try { PaymentType.valueOf(o.getString("paymentType")) } catch (e: Exception) { PaymentType.CASH },
                        partnerType = try { VoucherPartnerType.valueOf(o.getString("partnerType")) } catch (e: Exception) { VoucherPartnerType.CUSTOMER },
                        partnerId = if (o.has("partnerId") && !o.isNull("partnerId")) o.getLong("partnerId") else null,
                        partnerName = o.optString("partnerName", ""),
                        accountId = if (o.has("accountId") && !o.isNull("accountId")) o.getLong("accountId") else null,
                        accountName = o.optString("accountName", ""),
                        notes = o.optString("notes", ""),
                        journalEntryId = if (o.has("journalEntryId") && !o.isNull("journalEntryId")) o.getLong("journalEntryId") else null
                    )
                )
            }
            if (vouchersList.isNotEmpty()) {
                voucherDao.insertVouchers(vouchersList)
            }

            val summaryMsg = "تمت استعادة البيانات بنجاح: ${accountsList.size} حساب، ${productsList.size} صنف، ${sInvList.size} فاتورة مبيعات، ${pInvList.size} فاتورة مشتريات، ${vouchersList.size} سند، ${jList.size} قيد يومي."
            Result.success(summaryMsg)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
