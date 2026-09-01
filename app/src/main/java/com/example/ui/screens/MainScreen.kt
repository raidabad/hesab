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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dialog States
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNewSalesInvoiceDialog by remember { mutableStateOf(false) }
    var showNewPurchaseInvoiceDialog by remember { mutableStateOf(false) }
    var showNewJournalDialog by remember { mutableStateOf(false) }
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

    var selectedPurchaseInvoiceForDetail by remember { mutableStateOf<PurchaseInvoice?>(null) }
    var purchaseInvoiceDetailItems by remember { mutableStateOf<List<PurchaseInvoiceItem>>(emptyList()) }

    var selectedJournalForDetail by remember { mutableStateOf<JournalEntry?>(null) }
    var journalDetailLines by remember { mutableStateOf<List<JournalEntryLine>>(emptyList()) }

    // Data lists for dialog pickers
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()
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
                                contentDescription = "الإعدادات والعملة",
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
                                onNewSaleClick = { showNewSalesInvoiceDialog = true },
                                onNewPurchaseClick = { showNewPurchaseInvoiceDialog = true },
                                onNewJournalClick = { showNewJournalDialog = true },
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
                                onNewJournalClick = { showNewJournalDialog = true },
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
                        AppSection.SALES -> {
                            SalesScreen(
                                viewModel = viewModel,
                                onNewInvoiceClick = { showNewSalesInvoiceDialog = true },
                                onNewCustomerClick = { showNewCustomerDialog = true },
                                onEditCustomerClick = { editingCustomer = it },
                                onViewInvoiceDetail = { sale ->
                                    scope.launch {
                                        salesInvoiceDetailItems = viewModel.getSalesInvoiceItems(sale.id)
                                        selectedSalesInvoiceForDetail = sale
                                    }
                                }
                            )
                        }
                        AppSection.PURCHASES -> {
                            PurchasesScreen(
                                viewModel = viewModel,
                                onNewPurchaseClick = { showNewPurchaseInvoiceDialog = true },
                                onNewSupplierClick = { showNewSupplierDialog = true },
                                onEditSupplierClick = { editingSupplier = it },
                                onViewPurchaseDetail = { purchase ->
                                    scope.launch {
                                        purchaseInvoiceDetailItems = viewModel.getPurchaseInvoiceItems(purchase.id)
                                        selectedPurchaseInvoiceForDetail = purchase
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
                customers = customers,
                availableProducts = products,
                onDismiss = { showNewSalesInvoiceDialog = false },
                onConfirm = { cust, custName, items, discount, taxRate, paymentType, notes ->
                    viewModel.createSalesInvoice(
                        invoiceNumber = "INV-${System.currentTimeMillis() % 100000}",
                        date = System.currentTimeMillis(),
                        customerId = cust?.id,
                        customerName = custName,
                        items = items,
                        discount = discount,
                        taxRate = taxRate,
                        paymentType = paymentType,
                        notes = notes,
                        onSuccess = { showNewSalesInvoiceDialog = false }
                    )
                }
            )
        }

        // 2. Create Purchase Invoice Dialog
        if (showNewPurchaseInvoiceDialog) {
            CreatePurchaseInvoiceDialog(
                suppliers = suppliers,
                availableProducts = products,
                onDismiss = { showNewPurchaseInvoiceDialog = false },
                onConfirm = { supp, suppName, ref, items, discount, taxRate, paymentType, notes ->
                    viewModel.createPurchaseInvoice(
                        billNumber = "BILL-${System.currentTimeMillis() % 100000}",
                        supplierInvoiceRef = ref,
                        date = System.currentTimeMillis(),
                        supplierId = supp?.id,
                        supplierName = suppName,
                        items = items,
                        discount = discount,
                        taxRate = taxRate,
                        paymentType = paymentType,
                        notes = notes,
                        onSuccess = { showNewPurchaseInvoiceDialog = false }
                    )
                }
            )
        }

        // 3. Create Journal Entry Dialog
        if (showNewJournalDialog) {
            CreateJournalEntryDialog(
                postingAccounts = postingAccounts,
                onDismiss = { showNewJournalDialog = false },
                onConfirm = { entryNo, date, desc, ref, lines ->
                    viewModel.createJournalEntry(
                        entryNumber = entryNo.ifBlank { "JV-${System.currentTimeMillis() % 100000}" },
                        date = date,
                        description = desc,
                        referenceNumber = ref,
                        lines = lines,
                        onSuccess = { showNewJournalDialog = false }
                    )
                }
            )
        }

        // 4. Add/Edit Account Dialog
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

        // 5. Add/Edit Product Dialog
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

        // 6. Add/Edit Customer Dialog
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

        // 7. Add/Edit Supplier Dialog
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

        // 8. Stock Adjustment Dialog
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

        // 9. Account Statement Dialog
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

        // 10. Sales Invoice Detail Dialog
        if (selectedSalesInvoiceForDetail != null) {
            SalesInvoiceDetailDialog(
                invoice = selectedSalesInvoiceForDetail!!,
                items = salesInvoiceDetailItems,
                onDismiss = { selectedSalesInvoiceForDetail = null }
            )
        }

        // 11. Purchase Invoice Detail Dialog
        if (selectedPurchaseInvoiceForDetail != null) {
            PurchaseInvoiceDetailDialog(
                invoice = selectedPurchaseInvoiceForDetail!!,
                items = purchaseInvoiceDetailItems,
                onDismiss = { selectedPurchaseInvoiceForDetail = null }
            )
        }

        // 12. Journal Entry Detail Dialog
        if (selectedJournalForDetail != null) {
            JournalEntryDetailDialog(
                entry = selectedJournalForDetail!!,
                lines = journalDetailLines,
                onDismiss = { selectedJournalForDetail = null }
            )
        }

        // 13. System Settings and Currency Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                currentStoreName = storeName,
                currentStorePhone = storePhone,
                currentCurrencySymbol = currencySymbol,
                onDismiss = { showSettingsDialog = false },
                onSave = { name, phone, currency ->
                    viewModel.updateStoreSettings(name, phone, currency)
                    showSettingsDialog = false
                },
                onClearTransactions = {
                    viewModel.clearInvoicesAndTransactions()
                },
                onResetAllData = {
                    viewModel.resetSystemCompletely()
                }
            )
        }
    }
}
