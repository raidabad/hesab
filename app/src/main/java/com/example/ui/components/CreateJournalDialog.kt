package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.JournalEntryLine
import com.example.ui.theme.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJournalEntryDialog(
    initialEntryNumber: String = "1",
    postingAccounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (
        entryNumber: String,
        date: Long,
        description: String,
        referenceNumber: String,
        lines: List<JournalEntryLine>
    ) -> Unit
) {
    var entryNumber by remember { mutableStateOf(initialEntryNumber) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var refNumber by remember { mutableStateOf("") }

    val lines = remember { mutableStateListOf<JournalEntryLine>() }

    // State for adding a line
    var selectedAccount by remember { mutableStateOf<Account?>(postingAccounts.firstOrNull()) }
    var debitText by remember { mutableStateOf("") }
    var creditText by remember { mutableStateOf("") }
    var lineDesc by remember { mutableStateOf("") }

    val totalDebit = lines.sumOf { it.debit }
    val totalCredit = lines.sumOf { it.credit }
    val isBalanced = lines.size >= 2 && abs(totalDebit - totalCredit) < 0.01

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إنشاء قيد يومية عام",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = entryNumber,
                                    onValueChange = { entryNumber = it },
                                    label = { Text("رقم القيد") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = refNumber,
                                    onValueChange = { refNumber = it },
                                    label = { Text("رقم المرجع / المستند") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Date selector
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = EmeraldPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تاريخ القيد:", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(
                                        text = Formatters.date(selectedDate),
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("شرح / بيان القيد العام") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    // Add Line Form
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GrayLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("إضافة طرف للقيد:", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Accounts Tab Row
                                if (postingAccounts.isNotEmpty()) {
                                    ScrollableTabRow(
                                        selectedTabIndex = postingAccounts.indexOf(selectedAccount).coerceAtLeast(0),
                                        edgePadding = 0.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        postingAccounts.forEach { acc ->
                                            Tab(
                                                selected = selectedAccount?.id == acc.id,
                                                onClick = { selectedAccount = acc },
                                                text = { Text("${acc.code} - ${acc.nameAr}") }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = debitText,
                                        onValueChange = {
                                            debitText = it
                                            if (it.isNotBlank()) creditText = ""
                                        },
                                        label = { Text("مدين (منه)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = creditText,
                                        onValueChange = {
                                            creditText = it
                                            if (it.isNotBlank()) debitText = ""
                                        },
                                        label = { Text("دائن (له)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = lineDesc,
                                    onValueChange = { lineDesc = it },
                                    label = { Text("البيان الخاص بالسطر (اختياري)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val acc = selectedAccount
                                        val d = debitText.toDoubleOrNull() ?: 0.0
                                        val c = creditText.toDoubleOrNull() ?: 0.0
                                        if (acc != null && (d > 0 || c > 0)) {
                                            lines.add(
                                                JournalEntryLine(
                                                    accountId = acc.id,
                                                    accountCode = acc.code,
                                                    accountName = acc.nameAr,
                                                    debit = d,
                                                    credit = c,
                                                    description = lineDesc
                                                )
                                            )
                                            debitText = ""
                                            creditText = ""
                                            lineDesc = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("add_journal_line_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة السطر للقيد")
                                }
                            }
                        }
                    }

                    // Added lines
                    itemsIndexed(lines) { index, line ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${line.accountCode} - ${line.accountName}", fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        if (line.debit > 0) Text("مدين: ${Formatters.currency(line.debit)}", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                        if (line.credit > 0) Text("دائن: ${Formatters.currency(line.credit)}", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                                    }
                                    if (line.description.isNotBlank()) {
                                        Text(line.description, style = MaterialTheme.typography.bodySmall, color = GrayMedium)
                                    }
                                }
                                IconButton(onClick = { lines.removeAt(index) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                }
                            }
                        }
                    }

                    // Balance check
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isBalanced) EmeraldContainer else Color(0xFFFEE2E2)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("إجمالي المدين:")
                                    Text(Formatters.currency(totalDebit), fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("إجمالي الدائن:")
                                    Text(Formatters.currency(totalCredit), fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("حالة الاتزان:", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (isBalanced) "القيد متزن ومكتمل ✓" else "القيد غير متزن (الفرق: ${Formatters.currency(abs(totalDebit - totalCredit))})",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBalanced) EmeraldPrimary else ErrorRed
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isBalanced) {
                                onConfirm(
                                    entryNumber.trim(),
                                    selectedDate,
                                    description.trim(),
                                    refNumber.trim(),
                                    lines.toList()
                                )
                            }
                        },
                        enabled = isBalanced,
                        modifier = Modifier.testTag("save_journal_entry_btn")
                    ) {
                        Text("ترحيل وحفظ القيد")
                    }
                }
            }
        }
    }
}
