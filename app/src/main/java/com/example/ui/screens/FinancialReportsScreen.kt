package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

@Composable
fun FinancialReportsScreen(
    viewModel: AccountingViewModel
) {
    var selectedReportTab by remember { mutableStateOf(0) }
    var selectedStatementAccountId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val accounts by viewModel.accounts.collectAsState()
    val journalEntries by viewModel.journalEntries.collectAsState()
    val journalLines by viewModel.journalLines.collectAsState()
    val salesInvoices by viewModel.salesInvoices.collectAsState()
    val salesReturns by viewModel.salesReturns.collectAsState()
    val purchaseInvoices by viewModel.purchaseInvoices.collectAsState()
    val purchaseReturns by viewModel.purchaseReturns.collectAsState()
    val stockMovements by viewModel.stockMovements.collectAsState()
    val products by viewModel.products.collectAsState()

    val trialBalanceRows = remember(accounts, journalLines) { viewModel.calculateTrialBalance() }
    val summaryRows = remember(accounts, journalLines) { viewModel.calculateAccountSummaryReport() }
    val profitabilityRows = remember(products, stockMovements) { viewModel.calculateProductProfitabilityReport() }
    val incomeStatement = remember(salesInvoices, salesReturns, purchaseInvoices, purchaseReturns, products, stockMovements, accounts) {
        viewModel.calculateIncomeStatement()
    }

    val totalAssets = remember(accounts) {
        accounts.filter { it.type == AccountType.ASSET && !it.isGroup }.sumOf { it.currentBalance }
    }
    val totalLiabilities = remember(accounts) {
        accounts.filter { it.type == AccountType.LIABILITY && !it.isGroup }.sumOf { it.currentBalance }
    }
    val totalEquity = remember(accounts) {
        accounts.filter { it.type == AccountType.EQUITY && !it.isGroup }.sumOf { it.currentBalance }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedReportTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedReportTab == 0,
                onClick = { selectedReportTab = 0 },
                text = { Text("ميزان المراجعة") }
            )
            Tab(
                selected = selectedReportTab == 1,
                onClick = { selectedReportTab = 1 },
                text = { Text("إجمالي الحسابات") }
            )
            Tab(
                selected = selectedReportTab == 2,
                onClick = { selectedReportTab = 2 },
                text = { Text("كشف حساب تفصيلي") }
            )
            Tab(
                selected = selectedReportTab == 3,
                onClick = { selectedReportTab = 3 },
                text = { Text("قائمة الدخل وتكلفة المبيعات") }
            )
            Tab(
                selected = selectedReportTab == 4,
                onClick = { selectedReportTab = 4 },
                text = { Text("ربحية وتكلفة الأصناف") }
            )
            Tab(
                selected = selectedReportTab == 5,
                onClick = { selectedReportTab = 5 },
                text = { Text("الميزانية العمومية") }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedReportTab) {
            0 -> {
                // =========================================================
                // 0. Trial Balance (ميزان المراجعة)
                // =========================================================
                val filteredTB = remember(trialBalanceRows, searchQuery) {
                    if (searchQuery.isBlank()) trialBalanceRows
                    else trialBalanceRows.filter { it.accountCode.contains(searchQuery) || it.accountName.contains(searchQuery, ignoreCase = true) }
                }
                val sumDebit = trialBalanceRows.sumOf { it.debitBalance }
                val sumCredit = trialBalanceRows.sumOf { it.creditBalance }
                val isBalanced = kotlin.math.abs(sumDebit - sumCredit) < 0.01

                Column(modifier = Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (isBalanced) EmeraldContainer else Color(0xFFFEE2E2))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBalanced) "حالة الميزان: متزن تماماً ✓" else "حالة الميزان: غير متزن (يوجد فارق) ⚠",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBalanced) EmeraldPrimary else ErrorRed
                                )
                                Text(
                                    text = "الفرق: ${Formatters.currency(kotlin.math.abs(sumDebit - sumCredit))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("إجمالي الأرصدة المدينة:", style = MaterialTheme.typography.bodySmall)
                                    Text(Formatters.currency(sumDebit), fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("إجمالي الأرصدة الدائنة:", style = MaterialTheme.typography.bodySmall)
                                    Text(Formatters.currency(sumCredit), fontWeight = FontWeight.Bold, color = ErrorRed)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("بحث في ميزان المراجعة...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredTB, key = { it.accountCode }) { row ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${row.accountCode} - ${row.accountName}", fontWeight = FontWeight.Bold)
                                        Text(row.accountType.arabicName, style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                        Text(
                                            "إجمالي الحركات: مدين ${Formatters.currency(row.totalDebit)} | دائن ${Formatters.currency(row.totalCredit)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (row.debitBalance > 0) {
                                            Text("رصيد مدين", style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                                            Text(Formatters.currency(row.debitBalance), color = SuccessGreen, fontWeight = FontWeight.Bold)
                                        }
                                        if (row.creditBalance > 0) {
                                            Text("رصيد دائن", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
                                            Text(Formatters.currency(row.creditBalance), color = ErrorRed, fontWeight = FontWeight.Bold)
                                        }
                                        if (row.debitBalance == 0.0 && row.creditBalance == 0.0) {
                                            Text("رصيد صفري", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                            Text(Formatters.currency(0.0), color = GrayMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // =========================================================
                // 1. Account Summary Report (تقرير إجمالي الحسابات)
                // =========================================================
                val filteredSummary = remember(summaryRows, searchQuery) {
                    if (searchQuery.isBlank()) summaryRows
                    else summaryRows.filter { it.accountCode.contains(searchQuery) || it.accountName.contains(searchQuery, ignoreCase = true) }
                }

                val totalDebitsSum = summaryRows.sumOf { it.totalDebit }
                val totalCreditsSum = summaryRows.sumOf { it.totalCredit }

                Column(modifier = Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("إجمالي الحركات المدينة:", style = MaterialTheme.typography.bodySmall)
                                Text(Formatters.currency(totalDebitsSum), fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Column {
                                Text("إجمالي الحركات الدائنة:", style = MaterialTheme.typography.bodySmall)
                                Text(Formatters.currency(totalCreditsSum), fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("عدد الحسابات:", style = MaterialTheme.typography.bodySmall)
                                Text("${summaryRows.size}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("بحث في ملخص الحسابات...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSummary, key = { it.accountId }) { row ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${row.accountCode} - ${row.accountName}", fontWeight = FontWeight.Bold)
                                            Text(row.accountType.arabicName, style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                        }
                                        Button(
                                            onClick = {
                                                selectedStatementAccountId = row.accountId
                                                selectedReportTab = 2 // Switch to statement tab
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("كشف تفصيلي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("مجموع المدين", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                            Text(Formatters.currency(row.totalDebit), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                                        }
                                        Column {
                                            Text("مجموع الدائن", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                            Text(Formatters.currency(row.totalCredit), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = ErrorRed)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("الرصيد النهائي", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                            val balanceColor = if (row.currentBalance >= 0) EmeraldPrimary else ErrorRed
                                            Text(Formatters.currency(row.currentBalance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = balanceColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // =========================================================
                // 2. Detailed Account Statement (كشف حساب تفصيلي)
                // =========================================================
                val nonGroupAccounts = remember(accounts) { accounts.filter { !it.isGroup } }
                val selectedAccount = accounts.find { it.id == selectedStatementAccountId } ?: nonGroupAccounts.firstOrNull()

                val statementRows = remember(selectedAccount, journalLines, journalEntries) {
                    if (selectedAccount != null) viewModel.getAccountStatement(selectedAccount.id)
                    else emptyList()
                }

                var accountDropdownExpanded by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Account Selector
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("اختر الحساب لعرض كشف الحساب التفصيلي:", style = MaterialTheme.typography.labelMedium, color = GrayMedium)
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { accountDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (selectedAccount != null) "${selectedAccount.code} - ${selectedAccount.nameAr} (${selectedAccount.type.arabicName})" else "اختر حساب...",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }

                                DropdownMenu(
                                    expanded = accountDropdownExpanded,
                                    onDismissRequest = { accountDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    nonGroupAccounts.forEach { acc ->
                                        DropdownMenuItem(
                                            text = { Text("${acc.code} - ${acc.nameAr} (${Formatters.currency(acc.currentBalance)})") },
                                            onClick = {
                                                selectedStatementAccountId = acc.id
                                                accountDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (selectedAccount != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الرصيد الافتتاحي: ${Formatters.currency(selectedAccount.initialBalance)}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "الرصيد الحالي: ${Formatters.currency(selectedAccount.currentBalance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedAccount.currentBalance >= 0) EmeraldPrimary else ErrorRed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (statementRows.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد حركات مسجلة لهذا الحساب",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(statementRows) { row ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = row.entryNumber,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = if (row.date > 0) Formatters.date(row.date) else "رصيد بداية",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GrayMedium
                                            )
                                        }
                                        if (row.description.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = row.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                if (row.debit > 0) {
                                                    Text("مدين: ${Formatters.currency(row.debit)}", color = SuccessGreen, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                                }
                                                if (row.credit > 0) {
                                                    Text("دائن: ${Formatters.currency(row.credit)}", color = ErrorRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                            Text(
                                                text = "الرصيد: ${Formatters.currency(row.runningBalance)}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (row.runningBalance >= 0) EmeraldPrimary else ErrorRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // =========================================================
                // 3. Income Statement & COGS (قائمة الدخل وحساب تكلفة المبيعات)
                // =========================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Summary Banner: Net Profit
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (incomeStatement.netProfit >= 0) EmeraldContainer else Color(0xFFFEE2E2)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("صافي الأرباح / الخسائر للفترة", style = MaterialTheme.typography.titleSmall)
                                Surface(
                                    color = (if (incomeStatement.netProfit >= 0) EmeraldPrimary else ErrorRed).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "هامش صافي الربح: ${Formatters.number(incomeStatement.netMarginPercent)}%",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (incomeStatement.netProfit >= 0) EmeraldPrimary else ErrorRed
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Formatters.currency(incomeStatement.netProfit),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (incomeStatement.netProfit >= 0) EmeraldPrimary else ErrorRed
                            )
                        }
                    }

                    // 1. Trading Account / Sales & COGS Section (حساب المتاجرة وتكلفة البضاعة المباعة)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("حساب المتاجرة وتكلفة البضاعة المباعة (COGS)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Surface(
                                    color = EmeraldPrimary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "هامش مجمل الربح: ${Formatters.number(incomeStatement.grossMarginPercent)}%",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي إيراد المبيعات:")
                                Text(Formatters.currency(incomeStatement.grossSales), fontWeight = FontWeight.Bold)
                            }
                            if (incomeStatement.salesReturns > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("يخصم: مردود المبيعات:")
                                    Text("(${Formatters.currency(incomeStatement.salesReturns)})", color = ErrorRed)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("صافي المبيعات (Net Sales):", fontWeight = FontWeight.SemiBold)
                                Text(Formatters.currency(incomeStatement.netSales), fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("تكلفة البضاعة المباعة (COGS):", fontWeight = FontWeight.Bold, color = AmberWarning)
                                    Text("محسوبة من حركات صرف المخزن وتكلفة الشراء", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                }
                                Text("(${Formatters.currency(incomeStatement.cogs)})", fontWeight = FontWeight.Bold, color = AmberWarning, style = MaterialTheme.typography.titleMedium)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("مجمل الربح (Gross Profit):", fontWeight = FontWeight.Bold)
                                Text(
                                    Formatters.currency(incomeStatement.grossProfit),
                                    fontWeight = FontWeight.Bold,
                                    color = if (incomeStatement.grossProfit >= 0) SuccessGreen else ErrorRed,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // 2. Operating & General Expenses (المصروفات التشغيلية والعمومية)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("المصروفات التشغيلية والعمومية", fontWeight = FontWeight.Bold, color = ErrorRed)
                                Text(Formatters.currency(incomeStatement.totalOperatingExpenses), fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            if (incomeStatement.detailedExpenses.isEmpty()) {
                                Text("لا توجد مصروفات إضافية مسجلة", style = MaterialTheme.typography.bodySmall, color = GrayMedium)
                            } else {
                                incomeStatement.detailedExpenses.forEach { (name, balance) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(name, style = MaterialTheme.typography.bodyMedium)
                                        Text(Formatters.currency(balance), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Final Net Profit Calculation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("مجمل الربح من المبيعات:")
                                Text(Formatters.currency(incomeStatement.grossProfit), fontWeight = FontWeight.Bold)
                            }
                            if (incomeStatement.otherRevenues > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("يضاف: إيرادات أخرى:")
                                    Text("+ ${Formatters.currency(incomeStatement.otherRevenues)}", color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("يخصم: المصروفات الإدارية والتشغيلية:")
                                Text("- ${Formatters.currency(incomeStatement.totalOperatingExpenses)}", color = ErrorRed, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("صافي أرباح النشاط (Net Profit):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    Formatters.currency(incomeStatement.netProfit),
                                    fontWeight = FontWeight.Bold,
                                    color = if (incomeStatement.netProfit >= 0) EmeraldPrimary else ErrorRed,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }

                    // 4. Inventory & Purchases Supplementary Info (معلومات إضافية للمخزون والمشتريات)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ملخص المشتريات وقيمة بضاعة المخزن", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي المشتريات للفترة:")
                                Text(Formatters.currency(incomeStatement.totalPurchases), fontWeight = FontWeight.SemiBold)
                            }
                            if (incomeStatement.totalPurchaseReturns > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("يخصم: مردود المشتريات:")
                                    Text("(${Formatters.currency(incomeStatement.totalPurchaseReturns)})", color = ErrorRed)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("صافي المشتريات:", fontWeight = FontWeight.SemiBold)
                                Text(Formatters.currency(incomeStatement.netPurchases), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("قيمة بضاعة آخر المدة المتاحة:", fontWeight = FontWeight.SemiBold)
                                Text(Formatters.currency(incomeStatement.endingInventoryValue), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                    }
                }
            }

            4 -> {
                // =========================================================
                // 4. Product Profitability Report (ربحية وتكلفة الأصناف)
                // =========================================================
                val filteredProfitability = remember(profitabilityRows, searchQuery) {
                    if (searchQuery.isBlank()) profitabilityRows
                    else profitabilityRows.filter { it.productCode.contains(searchQuery) || it.productName.contains(searchQuery, ignoreCase = true) }
                }

                val totalRevenue = profitabilityRows.sumOf { it.salesRevenue }
                val totalCost = profitabilityRows.sumOf { it.costOfSales }
                val totalGrossProfit = profitabilityRows.sumOf { it.grossProfit }

                Column(modifier = Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("إجمالي إيراد الأصناف:", style = MaterialTheme.typography.bodySmall)
                                Text(Formatters.currency(totalRevenue), fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("إجمالي تكلفة المبيعات:", style = MaterialTheme.typography.bodySmall)
                                Text(Formatters.currency(totalCost), fontWeight = FontWeight.Bold, color = AmberWarning)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("إجمالي ربح الأصناف:", style = MaterialTheme.typography.bodySmall)
                                Text(Formatters.currency(totalGrossProfit), fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("بحث في ربحية وتكلفة الأصناف...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredProfitability.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد مبيعات مسجلة للأصناف حتى الآن", style = MaterialTheme.typography.bodyMedium, color = GrayMedium)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredProfitability, key = { it.productId }) { row ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${row.productCode} - ${row.productName}", fontWeight = FontWeight.Bold)
                                            Surface(
                                                color = if (row.grossProfit >= 0) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "هامش: ${Formatters.number(row.marginPercentage)}%",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (row.grossProfit >= 0) SuccessGreen else ErrorRed
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "الكمية المباعة: ${Formatters.number(row.quantitySold)} | المردود: ${Formatters.number(row.quantityReturned)} | الصافي: ${Formatters.number(row.netQuantitySold)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GrayMedium
                                        )

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("إيراد المبيعات", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                                Text(Formatters.currency(row.salesRevenue), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            }
                                            Column {
                                                Text("تكلفة البضاعة المباعة", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                                Text(Formatters.currency(row.costOfSales), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = AmberWarning)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("مجمل الربح", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                                Text(
                                                    Formatters.currency(row.grossProfit),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (row.grossProfit >= 0) SuccessGreen else ErrorRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // =========================================================
                // 5. Balance Sheet (قائمة المركز المالي / الميزانية العمومية)
                // =========================================================
                val assetAccounts = accounts.filter { it.type == AccountType.ASSET && !it.isGroup }
                val liabilityAccounts = accounts.filter { it.type == AccountType.LIABILITY && !it.isGroup }
                val equityAccounts = accounts.filter { it.type == AccountType.EQUITY && !it.isGroup }
                val currentNetProfit = incomeStatement.netProfit

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Assets
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الأصول (Assets)", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                Text(Formatters.currency(totalAssets), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            assetAccounts.forEach { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${acc.code} - ${acc.nameAr}", style = MaterialTheme.typography.bodyMedium)
                                    Text(Formatters.currency(acc.currentBalance), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Liabilities
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الالتزامات (Liabilities)", fontWeight = FontWeight.Bold, color = ErrorRed)
                                Text(Formatters.currency(totalLiabilities), fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            liabilityAccounts.forEach { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${acc.code} - ${acc.nameAr}", style = MaterialTheme.typography.bodyMedium)
                                    Text(Formatters.currency(acc.currentBalance), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Equity + Retained Earnings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("حقوق الملكية والأرباح", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                Text(Formatters.currency(totalEquity + currentNetProfit), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            equityAccounts.forEach { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${acc.code} - ${acc.nameAr}", style = MaterialTheme.typography.bodyMedium)
                                    Text(Formatters.currency(acc.currentBalance), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("أرباح الفترة الحالية المنقولة", style = MaterialTheme.typography.bodyMedium, color = SuccessGreen)
                                Text(Formatters.currency(currentNetProfit), fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
