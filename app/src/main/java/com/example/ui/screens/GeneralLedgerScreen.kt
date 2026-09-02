package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.JournalEntry
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GeneralLedgerScreen(
    viewModel: AccountingViewModel,
    onNewJournalClick: () -> Unit,
    onNewAccountClick: () -> Unit,
    onEditAccountClick: (Account) -> Unit,
    onViewAccountStatement: (Account) -> Unit,
    onViewJournalDetail: (JournalEntry) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }
    var journalToDelete by remember { mutableStateOf<JournalEntry?>(null) }

    val accounts by viewModel.accounts.collectAsState()
    val journalEntries by viewModel.journalEntries.collectAsState()

    val filteredAccounts = remember(accounts, searchQuery) {
        if (searchQuery.isBlank()) accounts
        else accounts.filter { it.nameAr.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery) }
    }

    val filteredEntries = remember(journalEntries, searchQuery) {
        if (searchQuery.isBlank()) journalEntries
        else journalEntries.filter { it.entryNumber.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Action Bar & Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.weight(1f)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("دليل الحسابات (${accounts.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("قيود اليومية (${journalEntries.size})") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Add Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (selectedTab == 0) "بحث بالاسم أو الكود..." else "بحث برقم القيد أو البيان...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (selectedTab == 0) {
                Button(
                    onClick = onNewAccountClick,
                    modifier = Modifier.testTag("new_account_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حساب جديد")
                }
            } else {
                Button(
                    onClick = onNewJournalClick,
                    modifier = Modifier.testTag("new_journal_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("قيد جديد")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        if (selectedTab == 0) {
            // Chart of Accounts List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredAccounts, key = { it.id }) { acc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!acc.isGroup) onViewAccountStatement(acc)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (acc.isGroup) GrayLight else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (acc.isGroup) GrayMedium else EmeraldPrimary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = acc.code,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = acc.nameAr,
                                        fontWeight = if (acc.isGroup) FontWeight.Bold else FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${acc.type.arabicName} • ${if (acc.isGroup) "حساب رئيسي" else "حساب فرعي قابل للحركة"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayMedium
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                if (!acc.isGroup) {
                                    Text(
                                        text = Formatters.currency(acc.currentBalance),
                                        fontWeight = FontWeight.Bold,
                                        color = if (acc.currentBalance >= 0) EmeraldPrimary else ErrorRed
                                    )
                                }
                                Row {
                                    IconButton(onClick = { onEditAccountClick(acc) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(18.dp))
                                    }
                                    if (!acc.isGroup) {
                                        IconButton(onClick = { onViewAccountStatement(acc) }) {
                                            Icon(Icons.Default.ReceiptLong, contentDescription = "كشف حساب", modifier = Modifier.size(18.dp), tint = EmeraldPrimary)
                                        }
                                    }
                                    IconButton(onClick = { accountToDelete = acc }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف الحساب", modifier = Modifier.size(18.dp), tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Journal Entries List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewJournalDetail(entry) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = EmeraldPrimary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = entry.entryNumber,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = EmeraldPrimary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Formatters.date(entry.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (entry.referenceNumber.isNotBlank()) {
                                    Text(
                                        text = "المرجع: ${entry.referenceNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GrayMedium
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.currency(entry.totalDebit),
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                    Text(
                                        text = "قيد متزن ✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { journalToDelete = entry }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف القيد", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Account Delete Confirmation
    accountToDelete?.let { acc ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف الحساب",
            message = "هل أنت متأكد من رغبتك في حذف الحساب ${acc.code} - ${acc.nameAr}؟ تنبيه: لا يمكن حذف الحسابات التي عليها حركات أو قيود مسجلة.",
            onDismiss = { accountToDelete = null },
            onConfirm = {
                viewModel.deleteAccountSafe(acc)
                accountToDelete = null
            }
        )
    }

    // Journal Entry Delete Confirmation
    journalToDelete?.let { entry ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف القيد المحاسبي",
            message = "هل أنت متأكد من حذف القيد ${entry.entryNumber}؟ تنبيه: القيود التلقائية الناتجة عن فواتير أو سندات لا تحذف إلا بحذف أصل الفاتورة أو السند.",
            onDismiss = { journalToDelete = null },
            onConfirm = {
                viewModel.deleteJournalEntrySafe(entry)
                journalToDelete = null
            }
        )
    }
}
