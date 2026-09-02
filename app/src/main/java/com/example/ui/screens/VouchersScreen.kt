package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun VouchersScreen(
    viewModel: AccountingViewModel,
    onNewVoucherClick: (VoucherType) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var voucherToDelete by remember { mutableStateOf<Voucher?>(null) }
    var voucherToView by remember { mutableStateOf<Voucher?>(null) }

    val allVouchers by viewModel.allVouchers.collectAsState()
    val receiptVouchers by viewModel.receiptVouchers.collectAsState()
    val paymentVouchers by viewModel.paymentVouchers.collectAsState()

    val currentList = when (selectedTab) {
        0 -> receiptVouchers
        1 -> paymentVouchers
        else -> allVouchers
    }

    val filteredVouchers = remember(currentList, searchQuery) {
        if (searchQuery.isBlank()) currentList
        else currentList.filter {
            it.voucherNumber.contains(searchQuery, ignoreCase = true) ||
                    it.partnerName.contains(searchQuery, ignoreCase = true) ||
                    it.notes.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("سندات القبض (${receiptVouchers.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("سندات الصرف (${paymentVouchers.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("كافة السندات (${allVouchers.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search and Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("بحث برقم السند أو اسم الطرف...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Button(
                onClick = {
                    val defaultType = if (selectedTab == 1) VoucherType.PAYMENT else VoucherType.RECEIPT
                    onNewVoucherClick(defaultType)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) AmberWarning else IncomeGreen
                ),
                modifier = Modifier.testTag("new_voucher_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (selectedTab == 1) "سند صرف جديد" else "سند قبض جديد")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        if (filteredVouchers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (searchQuery.isBlank()) "لا توجد سندات مسجلة" else "لم يتم العثور على نتائج للبحث",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "انقر على زر إصدار سند جديد لإنشاء سند قبض أو صرف وترحيله تلقائياً للأستاذ العام.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredVouchers, key = { it.id }) { voucher ->
                    val isReceipt = voucher.type == VoucherType.RECEIPT
                    val badgeColor = if (isReceipt) IncomeGreen else AmberWarning

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { voucherToView = voucher },
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
                                        color = badgeColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = voucher.type.arabicName,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "سند رقم: ${voucher.voucherNumber}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${if (isReceipt) "مستلم من:" else "مصروف إلى:"} ${voucher.partnerName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (voucher.notes.isNotBlank()) {
                                    Text(
                                        text = voucher.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "التاريخ: ${Formatters.date(voucher.date)} | ${voucher.paymentType.arabicName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.currency(voucher.amount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { voucherToDelete = voucher }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف السند", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Voucher Delete Confirmation
    voucherToDelete?.let { voucher ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف ${voucher.type.arabicName}",
            message = "هل أنت متأكد من حذف ${voucher.type.arabicName} رقم ${voucher.voucherNumber} بمبلغ ${Formatters.currency(voucher.amount)}؟ سيتم إلغاء أثره المالي والقيد المرتبط به.",
            onDismiss = { voucherToDelete = null },
            onConfirm = {
                viewModel.deleteVoucher(voucher)
                voucherToDelete = null
            }
        )
    }

    // Voucher Detail Dialog
    voucherToView?.let { voucher ->
        val isReceipt = voucher.type == VoucherType.RECEIPT
        val color = if (isReceipt) IncomeGreen else AmberWarning

        AlertDialog(
            onDismissRequest = { voucherToView = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isReceipt) Icons.Default.CallReceived else Icons.Default.CallMade,
                        contentDescription = null,
                        tint = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${voucher.type.arabicName}: ${voucher.voucherNumber}", fontWeight = FontWeight.Bold, color = color)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الطرف: ${voucher.partnerName} (${voucher.partnerType.arabicName})", fontWeight = FontWeight.SemiBold)
                    Text("المبلغ: ${Formatters.currency(voucher.amount)}", fontWeight = FontWeight.Bold, color = color)
                    Text("طريقة الدفع: ${voucher.paymentType.arabicName}")
                    Text("التاريخ: ${Formatters.date(voucher.date)}")
                    if (voucher.notes.isNotBlank()) {
                        Text("البيان: ${voucher.notes}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { voucherToView = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
