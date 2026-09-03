package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel
import com.example.ui.viewmodel.AppSection
import kotlinx.coroutines.launch

data class NavigationTabItem(
    val section: AppSection,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AccountingViewModel = viewModel()) {
    val currentSection by viewModel.currentSection.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val storeName by viewModel.storeName.collectAsState()
    val storePhone by viewModel.storePhone.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val isTaxEnabled by viewModel.isTaxEnabled.collectAsState()
    val defaultTaxRate by viewModel.defaultTaxRate.collectAsState()
    val showDecimals by viewModel.showDecimals.collectAsState()
    val defaultMinStock by viewModel.defaultMinStock.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dialog States
    var showSettingsDialog by remember { mutableStateOf(false) }

    var showNewSalesInvoiceDialog by remember { mutableStateOf(false) }
    var nextSalesInvoiceNo by remember { mutableStateOf("1") }

    var showNewSalesReturnDialog by remember { mutableStateOf(false) }
    var nextSalesReturnNo by remember { mutableStateOf("1") }

    var showNewPurchaseInvoiceDialog by remember { mutableStateOf(false) }
    var nextPurchaseBillNo by remember { mutableStateOf("1") }

    var showNewPurchaseReturnDialog by remember { mutableStateOf(false) }
    var nextPurchaseReturnNo by remember { mutableStateOf("1") }

    var showNewVoucherDialog by remember { mutableStateOf(false) }
    var newVoucherInitialType by remember { mutableStateOf(VoucherType.RECEIPT) }
    var nextVoucherNo by remember { mutableStateOf("1") }

    var showNewJournalDialog by remember { mutableStateOf(false) }
    var nextJournalEntryNo by remember { mutableStateOf("1") }

    var showNewAccountDialog by remember { mutableStateOf(false) }
    var showNewProductDialog by remember { mutableStateOf(false) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var showNewSupplierDialog by remember { mutableStateOf(false) }

    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var editingSupplier by remember { mutableStateOf<Supplier?>(null) }
    var adjustingProduct by remember { mutableStateOf<Product?>(null) }
    var statementAccount by remember { mutableStateOf<Account?>(null) }

    var selectedSalesInvoiceForDetail by remember { mutableStateOf<SalesInvoice?>(null) }
    var salesInvoiceDetailItems by remember { mutableStateOf<List<SalesInvoiceItem>>(emptyList()) }

    var selectedSalesReturnForDetail by remember { mutableStateOf<SalesReturn?>(null) }
    var salesReturnDetailItems by remember { mutableStateOf<List<SalesReturnItem>>(emptyList()) }

    var selectedPurchaseInvoiceForDetail by remember { mutableStateOf<PurchaseInvoice?>(null) }
    var purchaseInvoiceDetailItems by remember { mutableStateOf<List<PurchaseInvoiceItem>>(emptyList()) }

    var selectedPurchaseReturnForDetail by remember { mutableStateOf<PurchaseReturn?>(null) }
    var purchaseReturnDetailItems by remember { mutableStateOf<List<PurchaseReturnItem>>(emptyList()) }

    var selectedJournalForDetail by remember { mutableStateOf<JournalEntry?>(null) }
    var journalDetailLines by remember { mutableStateOf<List<JournalEntryLine>>(emptyList()) }

    // Data lists for dialog pickers
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val postingAccounts by viewModel.postingAccounts.collectAsState()

    // Show snackbars when userMessage changes
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val navItems = listOf(
        NavigationTabItem(AppSection.DASHBOARD, "الرئيسية", Icons.Default.Dashboard),
        NavigationTabItem(AppSection.GENERAL_LEDGER, "الأستاذ العام", Icons.Default.AccountBalance),
        NavigationTabItem(AppSection.VOUCHERS, "السندات", Icons.Default.Receipt),
        NavigationTabItem(AppSection.SALES, "المبيعات", Icons.Default.PointOfSale),
        NavigationTabItem(AppSection.PURCHASES, "المشتريات", Icons.Default.ShoppingBag),
        NavigationTabItem(AppSection.INVENTORY, "المخازن", Icons.Default.Inventory2),
        NavigationTabItem(AppSection.REPORTS, "التقارير", Icons.Default.Assessment)
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(EmeraldPrimary, GoldAccent)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = storeName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentSection.arabicTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Currency Badge
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showSettingsDialog = true }
                                .testTag("currency_quick_badge"),
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currencySymbol,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                        }
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "الإعدادات والعملة والضريبة",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentSection == item.section
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setSection(item.section) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Crossfade(targetState = currentSection, label = "section_fade") { section ->
                    when (section) {
                        AppSection.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateSection = { viewModel.setSection(it) },
                                onNewSaleClick = {
                                    scope.launch {
                                        nextSalesInvoiceNo = viewModel.getNextSalesInvoiceNumber()
                                        showNewSalesInvoiceDialog = true
                                    }
                                },
                                onNewPurchaseClick = {
                                    scope.launch {
                                        nextPurchaseBillNo = viewModel.getNextPurchaseBillNumber()
                                        showNewPurchaseInvoiceDialog = true
                                    }
                                },
                                onNewJournalClick = {
                                    scope.launch {
                                        nextJournalEntryNo = viewModel.getNextJournalEntryNumber()
                                        showNewJournalDialog = true
                                    }
                                },
                                onNewProductClick = { showNewProductDialog = true },
                                onOpenSettingsClick = { showSettingsDialog = true },
                                onViewSaleDetail = { sale ->
                                    scope.launch {
                                        salesInvoiceDetailItems = viewModel.getSalesInvoiceItems(sale.id)
                                        selectedSalesInvoiceForDetail = sale
                                    }
                                },
                                onViewPurchaseDetail = { purchase ->
                                    scope.launch {
                                        purchaseInvoiceDetailItems = viewModel.getPurchaseInvoiceItems(purchase.id)
                                        selectedPurchaseInvoiceForDetail = purchase
                                    }
                                },
                                onViewJournalDetail = { jv ->
                                    scope.launch {
                                        journalDetailLines = viewModel.getJournalLinesForEntry(jv.id)
                                        selectedJournalForDetail = jv
                                    }
                                }
                            )
                        }
                        AppSection.GENERAL_LEDGER -> {
                            GeneralLedgerScreen(
                                viewModel = viewModel,
                                onNewJournalClick = {
                                    scope.launch {
                                        nextJournalEntryNo = viewModel.getNextJournalEntryNumber()
                                        showNewJournalDialog = true
                                    }
                                },
                                onNewAccountClick = { showNewAccountDialog = true },
                                onEditAccountClick = { editingAccount = it },
                                onViewAccountStatement = { statementAccount = it },
                                onViewJournalDetail = { jv ->
                                    scope.launch {
                                        journalDetailLines = viewModel.getJournalLinesForEntry(jv.id)
                                        selectedJournalForDetail = jv
                                    }
                                }
                            )
                        }
                        AppSection.VOUCHERS -> {
                            VouchersScreen(
                                viewModel = viewModel,
                                onNewVoucherClick = { type ->
                                    scope.launch {
                                        newVoucherInitialType = type
                                        nextVoucherNo = viewModel.getNextVoucherNumber(type)
                                        showNewVoucherDialog = true
                                    }
                                }
                            )
                        }
                        AppSection.SALES -> {
                            SalesScreen(
                                viewModel = viewModel,
                                onNewInvoiceClick = {
                                    scope.launch {
                                        nextSalesInvoiceNo = viewModel.getNextSalesInvoiceNumber()
                                        showNewSalesInvoiceDialog = true
                                    }
                                },
                                onNewReturnClick = {
                                    scope.launch {
                                        nextSalesReturnNo = viewModel.getNextSalesReturnNumber()
                                        showNewSalesReturnDialog = true
                                    }
                                },
                                onNewCustomerClick = { showNewCustomerDialog = true },
                                onEditCustomerClick = { editingCustomer = it },
                                onViewInvoiceDetail = { sale ->
                                    scope.launch {
                                        salesInvoiceDetailItems = viewModel.getSalesInvoiceItems(sale.id)
                                        selectedSalesInvoiceForDetail = sale
                                    }
                                },
                                onViewReturnDetail = { sRet ->
                                    scope.launch {
                                        salesReturnDetailItems = viewModel.getSalesReturnItems(sRet.id)
                                        selectedSalesReturnForDetail = sRet
                                    }
                                }
                            )
                        }
                        AppSection.PURCHASES -> {
                            PurchasesScreen(
                                viewModel = viewModel,
                                onNewBillClick = {
                                    scope.launch {
                                        nextPurchaseBillNo = viewModel.getNextPurchaseBillNumber()
                                        showNewPurchaseInvoiceDialog = true
                                    }
                                },
                                onNewReturnClick = {
                                    scope.launch {
                                        nextPurchaseReturnNo = viewModel.getNextPurchaseReturnNumber()
                                        showNewPurchaseReturnDialog = true
                                    }
                                },
                                onNewSupplierClick = { showNewSupplierDialog = true },
                                onEditSupplierClick = { editingSupplier = it },
                                onViewBillDetail = { purchase ->
                                    scope.launch {
                                        purchaseInvoiceDetailItems = viewModel.getPurchaseInvoiceItems(purchase.id)
                                        selectedPurchaseInvoiceForDetail = purchase
                                    }
                                },
                                onViewReturnDetail = { pRet ->
                                    scope.launch {
                                        purchaseReturnDetailItems = viewModel.getPurchaseReturnItems(pRet.id)
                                        selectedPurchaseReturnForDetail = pRet
                                    }
                                }
                            )
                        }
                        AppSection.INVENTORY -> {
                            InventoryScreen(
                                viewModel = viewModel,
                                onNewProductClick = { showNewProductDialog = true },
                                onEditProductClick = { editingProduct = it },
                                onStockAdjustmentClick = { adjustingProduct = it }
                            )
                        }
                        AppSection.REPORTS -> {
                            FinancialReportsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // Dialog Overlays
        // -------------------------------------------------------------

        // 1. Create Sales Invoice Dialog
        if (showNewSalesInvoiceDialog) {
            CreateSalesInvoiceDialog(
                initialInvoiceNumber = nextSalesInvoiceNo,
                isTaxActive = isTaxEnabled,
                defaultTaxRate = defaultTaxRate,
                customers = customers,
                availableProducts = products,
                onDismiss = { showNewSalesInvoiceDialog = false },
                onConfirm = { invoiceNo, date, cust, custName, items, discount, taxRate, taxOverride, paymentType, notes ->
                    viewModel.createSalesInvoice(
                        invoiceNumber = invoiceNo,
                        date = date,
                        customerId = cust?.id,
                        customerName = custName,
                        items = items,
                        discount = discount,
                        taxRate = taxRate,
                        taxAmountOverride = taxOverride,
                        paymentType = paymentType,
                        notes = notes,
                        onSuccess = { showNewSalesInvoiceDialog = false }
                    )
                }
            )
        }

        // 2. Create Sales Return Dialog
        if (showNewSalesReturnDialog) {
            CreateSalesReturnDialog(
                initialReturnNumber = nextSalesReturnNo,
                isTaxActive = isTaxEnabled,
                defaultTaxRate = defaultTaxRate,
                customers = customers,
                availableProducts = products,
                onDismiss = { showNewSalesReturnDialog = false },
                onConfirm = { returnNo, origInv, date, cust, custName, items, taxRate, taxOverride, paymentType, notes ->
                    viewModel.createSalesReturn(
                        returnNumber = returnNo,
                        originalInvoiceNumber = origInv,
                        date = date,
                        customerId = cust?.id,
                        customerName = custName,
                        items = items,
                        taxRate = taxRate,
                        taxAmountOverride = taxOverride,
                        paymentType = paymentType,
                        notes = notes,
                        onSuccess = { showNewSalesReturnDialog = false }
                    )
                }
            )
        }

        // 3. Create Purchase Invoice Dialog
        if (showNewPurchaseInvoiceDialog) {
            CreatePurchaseInvoiceDialog(
                initialBillNumber = nextPurchaseBillNo,
                isTaxActive = isTaxEnabled,
                defaultTaxRate = defaultTaxRate,
                suppliers = suppliers,
                availableProducts = products,
                onDismiss = { showNewPurchaseInvoiceDialog = false },
                onConfirm = { billNo, supp, suppName, ref, date, items, discount, taxRate, taxOverride, paymentType, notes ->
                    viewModel.createPurchaseInvoice(
                        billNumber = billNo,
                        supplierInvoiceRef = ref,
                        date = date,
                        supplierId = supp?.id,
                        supplierName = suppName,
                        items = items,
                        discount = discount,
                        taxRate = taxRate,
                        taxAmountOverride = taxOverride,
                        paymentType = paymentType,
                        notes = notes,
                        onSuccess = { showNewPurchaseInvoiceDialog = false }
                    )
                }
            )
        }

        // 4. Create Purchase Return Dialog
        if (showNewPurchaseReturnDialog) {
            CreatePurchaseReturnDialog(
                initialReturnNumber = nextPurchaseReturnNo,
                isTaxActive = isTaxEnabled,
                defaultTaxRate = defaultTaxRate,
                suppliers = suppliers,
                availableProducts = products,
                onDismiss = { showNewPurchaseReturnDialog = false },
                onConfirm = { returnNo, origBill, date, supp, suppName, items, taxRate, taxOverride, paymentType, notes ->
                    viewModel.createPurchaseReturn(
                        returnNumber = returnNo,
                        originalBillNumber = origBill,
                        date = date,
                        supplierId = supp?.id,
                        supplierName = suppName,
                        items = items,
                        taxRate = taxRate,
                        taxAmountOverride = taxOverride,
                        paymentType = paymentType,
                        notes = notes,
                        onSuccess = { showNewPurchaseReturnDialog = false }
                    )
                }
            )
        }

        // 5. Create Voucher Dialog (Receipt & Payment)
        if (showNewVoucherDialog) {
            CreateVoucherDialog(
                initialType = newVoucherInitialType,
                initialNumber = nextVoucherNo,
                customers = customers,
                suppliers = suppliers,
                accounts = accounts,
                onDismiss = { showNewVoucherDialog = false },
                onConfirm = { voucherNo, vType, date, amount, paymentType, partnerType, partnerId, partnerName, accountId, accountName, notes ->
                    viewModel.createVoucher(
                        voucherNumber = voucherNo,
                        type = vType,
                        date = date,
                        amount = amount,
                        paymentType = paymentType,
                        partnerType = partnerType,
                        partnerId = partnerId,
                        partnerName = partnerName,
                        accountId = accountId,
                        accountName = accountName,
                        notes = notes,
                        onSuccess = { showNewVoucherDialog = false }
                    )
                }
            )
        }

        // 6. Create Journal Entry Dialog
        if (showNewJournalDialog) {
            CreateJournalEntryDialog(
                initialEntryNumber = nextJournalEntryNo,
                postingAccounts = postingAccounts,
                onDismiss = { showNewJournalDialog = false },
                onConfirm = { entryNo, date, desc, ref, lines ->
                    viewModel.createJournalEntry(
                        entryNumber = entryNo,
                        date = date,
                        description = desc,
                        referenceNumber = ref,
                        lines = lines,
                        onSuccess = { showNewJournalDialog = false }
                    )
                }
            )
        }

        // 7. Add/Edit Account Dialog
        if (showNewAccountDialog) {
            AddEditAccountDialog(
                account = null,
                onDismiss = { showNewAccountDialog = false },
                onConfirm = { acc ->
                    viewModel.addAccount(acc) { showNewAccountDialog = false }
                }
            )
        }
        if (editingAccount != null) {
            AddEditAccountDialog(
                account = editingAccount,
                onDismiss = { editingAccount = null },
                onConfirm = { acc ->
                    viewModel.updateAccount(acc) { editingAccount = null }
                }
            )
        }

        // 8. Add/Edit Product Dialog
        if (showNewProductDialog) {
            AddEditProductDialog(
                product = null,
                onDismiss = { showNewProductDialog = false },
                onConfirm = { prod ->
                    viewModel.addProduct(prod) { showNewProductDialog = false }
                }
            )
        }
        if (editingProduct != null) {
            AddEditProductDialog(
                product = editingProduct,
                onDismiss = { editingProduct = null },
                onConfirm = { prod ->
                    viewModel.updateProduct(prod) { editingProduct = null }
                }
            )
        }

        // 9. Add/Edit Customer Dialog
        if (showNewCustomerDialog) {
            AddEditCustomerDialog(
                customer = null,
                onDismiss = { showNewCustomerDialog = false },
                onConfirm = { cust ->
                    viewModel.addCustomer(cust) { showNewCustomerDialog = false }
                }
            )
        }
        if (editingCustomer != null) {
            AddEditCustomerDialog(
                customer = editingCustomer,
                onDismiss = { editingCustomer = null },
                onConfirm = { cust ->
                    viewModel.updateCustomer(cust) { editingCustomer = null }
                }
            )
        }

        // 10. Add/Edit Supplier Dialog
        if (showNewSupplierDialog) {
            AddEditSupplierDialog(
                supplier = null,
                onDismiss = { showNewSupplierDialog = false },
                onConfirm = { supp ->
                    viewModel.addSupplier(supp) { showNewSupplierDialog = false }
                }
            )
        }
        if (editingSupplier != null) {
            AddEditSupplierDialog(
                supplier = editingSupplier,
                onDismiss = { editingSupplier = null },
                onConfirm = { supp ->
                    viewModel.updateSupplier(supp) { editingSupplier = null }
                }
            )
        }

        // 11. Stock Adjustment Dialog
        if (adjustingProduct != null) {
            StockAdjustmentDialog(
                product = adjustingProduct!!,
                onDismiss = { adjustingProduct = null },
                onConfirm = { qty, isAddition, reason ->
                    viewModel.adjustStock(
                        productId = adjustingProduct!!.id,
                        quantity = qty,
                        isAddition = isAddition,
                        reason = reason,
                        onSuccess = { adjustingProduct = null }
                    )
                }
            )
        }

        // 12. Account Statement Dialog
        if (statementAccount != null) {
            val statementRows = remember(statementAccount) {
                viewModel.getAccountStatement(statementAccount!!.id)
            }
            AccountStatementDialog(
                account = statementAccount!!,
                statementRows = statementRows,
                onDismiss = { statementAccount = null }
            )
        }

        // 13. Sales Invoice Detail Dialog (with Delete)
        if (selectedSalesInvoiceForDetail != null) {
            SalesInvoiceDetailDialog(
                invoice = selectedSalesInvoiceForDetail!!,
                items = salesInvoiceDetailItems,
                onDismiss = { selectedSalesInvoiceForDetail = null },
                onDelete = {
                    viewModel.deleteSalesInvoice(selectedSalesInvoiceForDetail!!)
                }
            )
        }

        // 14. Sales Return Detail Dialog (with Delete)
        if (selectedSalesReturnForDetail != null) {
            SalesReturnDetailDialog(
                sReturn = selectedSalesReturnForDetail!!,
                items = salesReturnDetailItems,
                onDismiss = { selectedSalesReturnForDetail = null },
                onDelete = {
                    viewModel.deleteSalesReturn(selectedSalesReturnForDetail!!)
                }
            )
        }

        // 15. Purchase Invoice Detail Dialog (with Delete)
        if (selectedPurchaseInvoiceForDetail != null) {
            PurchaseInvoiceDetailDialog(
                invoice = selectedPurchaseInvoiceForDetail!!,
                items = purchaseInvoiceDetailItems,
                onDismiss = { selectedPurchaseInvoiceForDetail = null },
                onDelete = {
                    viewModel.deletePurchaseInvoice(selectedPurchaseInvoiceForDetail!!)
                }
            )
        }

        // 16. Purchase Return Detail Dialog (with Delete)
        if (selectedPurchaseReturnForDetail != null) {
            PurchaseReturnDetailDialog(
                pReturn = selectedPurchaseReturnForDetail!!,
                items = purchaseReturnDetailItems,
                onDismiss = { selectedPurchaseReturnForDetail = null },
                onDelete = {
                    viewModel.deletePurchaseReturn(selectedPurchaseReturnForDetail!!)
                }
            )
        }

        // 17. Journal Entry Detail Dialog (with Safe Delete)
        if (selectedJournalForDetail != null) {
            JournalEntryDetailDialog(
                entry = selectedJournalForDetail!!,
                lines = journalDetailLines,
                onDismiss = { selectedJournalForDetail = null },
                onDelete = {
                    viewModel.deleteJournalEntrySafe(selectedJournalForDetail!!)
                }
            )
        }

        // 18. System Settings, Tax, Currency, Decimals, Backup, and Data Reset Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                currentStoreName = storeName,
                currentStorePhone = storePhone,
                currentCurrencySymbol = currencySymbol,
                isTaxActive = isTaxEnabled,
                currentTaxRate = defaultTaxRate,
                currentShowDecimals = showDecimals,
                currentMinStock = defaultMinStock,
                onDismiss = { showSettingsDialog = false },
                onSave = { name, phone, currency, taxActive, taxRate, showDec, minStock ->
                    viewModel.updateStoreSettings(name, phone, currency, taxActive, taxRate, showDec, minStock)
                    showSettingsDialog = false
                },
                onClearTransactions = {
                    viewModel.clearInvoicesAndTransactions()
                },
                onResetAllData = {
                    viewModel.resetSystemCompletely()
                },
                onExportBackup = {
                    viewModel.exportBackup()
                },
                onRestoreBackup = { json, onSuccess ->
                    viewModel.restoreBackup(json, onSuccess)
                },
                onRepairCOGS = {
                    viewModel.repairCOGS()
                },
                onApplyMinStockToAll = { newMin ->
                    viewModel.applyMinStockToAllProducts(newMin)
                }
            )
        }
    }
}
