package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoucherDialog(
    initialType: VoucherType,
    initialNumber: String,
    customers: List<Customer>,
    suppliers: List<Supplier>,
    accounts: List<Account>,
    existingVoucher: Voucher? = null,
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    val isEditing = existingVoucher != null
    var voucherType by remember { mutableStateOf(existingVoucher?.type ?: initialType) }
    var voucherNumber by remember { mutableStateOf(existingVoucher?.voucherNumber ?: initialNumber) }
    var operationDate by remember { mutableStateOf(existingVoucher?.date ?: System.currentTimeMillis()) }
    var amountText by remember { mutableStateOf(existingVoucher?.amount?.toString() ?: "") }
    var paymentType by remember { mutableStateOf(existingVoucher?.paymentType ?: PaymentType.CASH) }
    var partnerType by remember {
        mutableStateOf(
            existingVoucher?.partnerType ?: (if (initialType == VoucherType.RECEIPT) VoucherPartnerType.CUSTOMER else VoucherPartnerType.SUPPLIER)
        )
    }

    var selectedCustomer by remember {
        mutableStateOf<Customer?>(
            if (existingVoucher?.partnerType == VoucherPartnerType.CUSTOMER && existingVoucher.partnerId != null) {
                customers.find { it.id == existingVoucher.partnerId } ?: customers.firstOrNull()
            } else customers.firstOrNull()
        )
    }
    var selectedSupplier by remember {
        mutableStateOf<Supplier?>(
            if (existingVoucher?.partnerType == VoucherPartnerType.SUPPLIER && existingVoucher.partnerId != null) {
                suppliers.find { it.id == existingVoucher.partnerId } ?: suppliers.firstOrNull()
            } else suppliers.firstOrNull()
        )
    }
    var selectedAccount by remember {
        mutableStateOf<Account?>(
            if (existingVoucher?.accountId != null) {
                accounts.find { it.id == existingVoucher.accountId } ?: accounts.filter { !it.isGroup }.firstOrNull()
            } else accounts.filter { !it.isGroup }.firstOrNull()
        )
    }

    var customPartnerName by remember { mutableStateOf(existingVoucher?.partnerName ?: "") }
    var notes by remember { mutableStateOf(existingVoucher?.notes ?: "") }

    val isReceipt = voucherType == VoucherType.RECEIPT
    val primaryColor = if (isReceipt) IncomeGreen else AmberWarning

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isReceipt) Icons.Default.CallReceived else Icons.Default.CallMade,
                            contentDescription = null,
                            tint = primaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) "تعديل ${voucherType.arabicName} (${existingVoucher!!.voucherNumber})" else "إصدار ${voucherType.arabicName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Type Selector Tabs
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    voucherType = VoucherType.RECEIPT
                                    partnerType = VoucherPartnerType.CUSTOMER
                                },
                            color = if (isReceipt) IncomeGreen else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "سند قبض (استلام نقدية)",
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isReceipt) FontWeight.Bold else FontWeight.Normal,
                                color = if (isReceipt) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    voucherType = VoucherType.PAYMENT
                                    partnerType = VoucherPartnerType.SUPPLIER
                                },
                            color = if (!isReceipt) AmberWarning else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "سند صرف (دفع نقدية)",
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (!isReceipt) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isReceipt) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Voucher Number
                    OutlinedTextField(
                        value = voucherNumber,
                        onValueChange = { voucherNumber = it },
                        label = { Text("رقم السند (تسلسلي)") },
                        leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Date Picker Field
                    OperationDatePickerField(
                        selectedDate = operationDate,
                        onDateSelected = { operationDate = it },
                        label = "تاريخ السند"
                    )

                    // Amount
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("المبلغ") },
                        placeholder = { Text("0.00") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = primaryColor) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voucher_amount_input"),
                        singleLine = true
                    )

                    // Payment Method (الصندوق أو البنك)
                    Text("طريقة السداد / الخزينة:", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(PaymentType.CASH, PaymentType.BANK).forEach { pt ->
                            val selected = paymentType == pt
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { paymentType = pt },
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (pt == PaymentType.CASH) "الصندوق / الخزينة" else "البنك / تحويل",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Partner Type (عميل / مورد / حساب عام)
                    Text(if (isReceipt) "المستلم منه (الطرف الدائن):" else "المصروف له (الطرف المدين):", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        VoucherPartnerType.values().forEach { vpt ->
                            val selected = partnerType == vpt
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { partnerType = vpt },
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = vpt.arabicName,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Partner Selection based on Type
                    when (partnerType) {
                        VoucherPartnerType.CUSTOMER -> {
                            if (customers.isNotEmpty()) {
                                ScrollableTabRow(
                                    selectedTabIndex = customers.indexOf(selectedCustomer).coerceAtLeast(0),
                                    edgePadding = 0.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    customers.forEach { c ->
                                        Tab(
                                            selected = selectedCustomer?.id == c.id,
                                            onClick = { selectedCustomer = c },
                                            text = { Text("${c.name} (رصيد: ${Formatters.currency(c.currentBalance)})") }
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = customPartnerName.ifEmpty { selectedCustomer?.name ?: "" },
                                onValueChange = { customPartnerName = it },
                                label = { Text("اسم العميل") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        VoucherPartnerType.SUPPLIER -> {
                            if (suppliers.isNotEmpty()) {
                                ScrollableTabRow(
                                    selectedTabIndex = suppliers.indexOf(selectedSupplier).coerceAtLeast(0),
                                    edgePadding = 0.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    suppliers.forEach { s ->
                                        Tab(
                                            selected = selectedSupplier?.id == s.id,
                                            onClick = { selectedSupplier = s },
                                            text = { Text("${s.name} (مستحق: ${Formatters.currency(s.currentBalance)})") }
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = customPartnerName.ifEmpty { selectedSupplier?.name ?: "" },
                                onValueChange = { customPartnerName = it },
                                label = { Text("اسم المورد") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        VoucherPartnerType.GENERAL_ACCOUNT -> {
                            val postingAccounts = accounts.filter { !it.isGroup }
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
                            OutlinedTextField(
                                value = customPartnerName.ifEmpty { selectedAccount?.nameAr ?: "" },
                                onValueChange = { customPartnerName = it },
                                label = { Text("اسم الحساب / البيان") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("بيان السند / ملاحظات إضافية") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && voucherNumber.isNotBlank()) {
                                val pName = when (partnerType) {
                                    VoucherPartnerType.CUSTOMER -> customPartnerName.ifBlank { selectedCustomer?.name ?: "عميل" }
                                    VoucherPartnerType.SUPPLIER -> customPartnerName.ifBlank { selectedSupplier?.name ?: "مورد" }
                                    VoucherPartnerType.GENERAL_ACCOUNT -> customPartnerName.ifBlank { selectedAccount?.nameAr ?: "حساب عام" }
                                }
                                val pId = when (partnerType) {
                                    VoucherPartnerType.CUSTOMER -> selectedCustomer?.id
                                    VoucherPartnerType.SUPPLIER -> selectedSupplier?.id
                                    VoucherPartnerType.GENERAL_ACCOUNT -> null
                                }
                                val aId = when (partnerType) {
                                    VoucherPartnerType.GENERAL_ACCOUNT -> selectedAccount?.id
                                    else -> null
                                }
                                val aName = selectedAccount?.nameAr ?: ""

                                onConfirm(
                                    voucherNumber.trim(),
                                    voucherType,
                                    operationDate,
                                    amt,
                                    paymentType,
                                    partnerType,
                                    pId,
                                    pName,
                                    aId,
                                    aName,
                                    notes
                                )
                            }
                        },
                        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && voucherNumber.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEditing) "حفظ تعديل ${voucherType.arabicName}" else "حفظ وترحيل ${voucherType.arabicName}")
                    }
                }
            }
        }
    }
}
