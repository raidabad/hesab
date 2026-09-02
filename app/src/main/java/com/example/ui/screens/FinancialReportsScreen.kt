package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountType
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun FinancialReportsScreen(
    viewModel: AccountingViewModel
) {
    var selectedReportTab by remember { mutableStateOf(0) }

    val accounts by viewModel.accounts.collectAsState()
    val trialBalanceRows = remember(accounts) { viewModel.calculateTrialBalance() }

    // Revenue calculations
    val salesRevenueAccount = accounts.firstOrNull { it.code == "4101" }
    val salesReturnsAccount = accounts.firstOrNull { it.code == "4102" }
    val otherRevenueAccounts = accounts.filter { it.type == AccountType.REVENUE && !it.isGroup && it.code != "4101" && it.code != "4102" }

    val grossSales = salesRevenueAccount?.currentBalance ?: 0.0
    val salesReturnsAmount = salesReturnsAccount?.currentBalance ?: 0.0
    val netSales = (grossSales - salesReturnsAmount).coerceAtLeast(0.0)

    // COGS calculation (تكلفة البضاعة المباعة)
    val cogsAccount = accounts.firstOrNull { it.code == "5101" }
    val cogsAmount = cogsAccount?.currentBalance ?: 0.0

    // Gross Profit (مجمل الربح = صافي المبيعات - تكلفة البضاعة)
    val grossProfit = netSales - cogsAmount

    // Operating expenses (المصروفات التشغيلية والعمومية الأخرى عدا تكلفة البضاعة)
    val otherExpenseAccounts = accounts.filter { it.type == AccountType.EXPENSE && !it.isGroup && it.code != "5101" }
    val totalOperatingExpenses = otherExpenseAccounts.sumOf { it.currentBalance }

    // Net Profit (صافي الربح)
    val otherRevenues = otherRevenueAccounts.sumOf { it.currentBalance }
    val netProfit = grossProfit + otherRevenues - totalOperatingExpenses

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
                text = { Text("قائمة الدخل وتكلفة البضاعة") }
            )
            Tab(
                selected = selectedReportTab == 2,
                onClick = { selectedReportTab = 2 },
                text = { Text("المركز المالي (الميزانية)") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedReportTab) {
            0 -> {
                // Trial Balance (ميزان المراجعة)
                val sumDebit = trialBalanceRows.sumOf { it.debitBalance }
                val sumCredit = trialBalanceRows.sumOf { it.creditBalance }

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
                                Text("إجمالي الأرصدة المدينة:", style = MaterialTheme.typography.bodySmall)
                                Text(Formatters.currency(sumDebit), fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("إجمالي الأرصدة الدائنة:", style = MaterialTheme.typography.bodySmall)
                                Text(Formatters.currency(sumCredit), fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(trialBalanceRows) { row ->
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
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (row.debitBalance > 0) {
                                            Text("مدين: ${Formatters.currency(row.debitBalance)}", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                        }
                                        if (row.creditBalance > 0) {
                                            Text("دائن: ${Formatters.currency(row.creditBalance)}", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Income Statement (قائمة الدخل مع تكلفة البضاعة المباعة ومجمل وصافي الربح)
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
                            containerColor = if (netProfit >= 0) EmeraldContainer else Color(0xFFFEE2E2)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("صافي الأرباح / الخسائر للفترة", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Formatters.currency(netProfit),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) EmeraldPrimary else ErrorRed
                            )
                        }
                    }

                    // 1. Trading Account / Sales & COGS Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("حساب المتاجرة وتكلفة البضاعة المباعة (COGS)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي إيراد المبيعات:")
                                Text(Formatters.currency(grossSales), fontWeight = FontWeight.Bold)
                            }
                            if (salesReturnsAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("يخصم: مردود المبيعات:")
                                    Text("(${Formatters.currency(salesReturnsAmount)})", color = ErrorRed)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("صافي المبيعات:", fontWeight = FontWeight.SemiBold)
                                Text(Formatters.currency(netSales), fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("تكلفة البضاعة المباعة (COGS):", fontWeight = FontWeight.Bold, color = AmberWarning)
                                Text("(${Formatters.currency(cogsAmount)})", fontWeight = FontWeight.Bold, color = AmberWarning)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("مجمل الربح (Gross Profit):", fontWeight = FontWeight.Bold)
                                Text(
                                    Formatters.currency(grossProfit),
                                    fontWeight = FontWeight.Bold,
                                    color = if (grossProfit >= 0) SuccessGreen else ErrorRed,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // 2. Operating & General Expenses
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("المصروفات التشغيلية والعمومية", fontWeight = FontWeight.Bold, color = ErrorRed)
                                Text(Formatters.currency(totalOperatingExpenses), fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            if (otherExpenseAccounts.isEmpty()) {
                                Text("لا توجد مصروفات إضافية مسجلة", style = MaterialTheme.typography.bodySmall, color = GrayMedium)
                            } else {
                                otherExpenseAccounts.forEach { acc ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${acc.code} - ${acc.nameAr}", style = MaterialTheme.typography.bodyMedium)
                                        Text(Formatters.currency(acc.currentBalance), fontWeight = FontWeight.SemiBold)
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
                                Text(Formatters.currency(grossProfit), fontWeight = FontWeight.Bold)
                            }
                            if (otherRevenues > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("يضاف: إيرادات أخرى:")
                                    Text("+ ${Formatters.currency(otherRevenues)}", color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("يخصم: المصروفات الإدارية والتشغيلية:")
                                Text("- ${Formatters.currency(totalOperatingExpenses)}", color = ErrorRed, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("صافي أرباح النشاط (Net Profit):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    Formatters.currency(netProfit),
                                    fontWeight = FontWeight.Bold,
                                    color = if (netProfit >= 0) EmeraldPrimary else ErrorRed,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Balance Sheet (قائمة المركز المالي)
                val assetAccounts = accounts.filter { it.type == AccountType.ASSET && !it.isGroup }
                val liabilityAccounts = accounts.filter { it.type == AccountType.LIABILITY && !it.isGroup }
                val equityAccounts = accounts.filter { it.type == AccountType.EQUITY && !it.isGroup }

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
                                Text(Formatters.currency(totalEquity + netProfit), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
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
                                Text(Formatters.currency(netProfit), fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
