package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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

// -------------------------------------------------------------
// 1. Create Sales Invoice Dialog
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSalesInvoiceDialog(
    initialInvoiceNumber: String,
    isTaxActive: Boolean,
    defaultTaxRate: Double,
    customers: List<Customer>,
    availableProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (
        invoiceNumber: String,
        date: Long,
        customer: Customer?,
        customerName: String,
        items: List<SalesInvoiceItem>,
        discount: Double,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String
    ) -> Unit
) {
    var invoiceNumber by remember { mutableStateOf(initialInvoiceNumber) }
    var operationDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var customerNameInput by remember { mutableStateOf(customers.firstOrNull()?.name ?: "عميل نقدي عام") }
    var paymentType by remember { mutableStateOf(PaymentType.CASH) }
    var discountText by remember { mutableStateOf("0.0") }

    var enableTaxInThisInvoice by remember { mutableStateOf(isTaxActive) }
    var taxRateText by remember { mutableStateOf(if (isTaxActive) defaultTaxRate.toString() else "0.0") }
    var overrideTaxAmountText by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }

    // Invoice items state
    val invoiceItems = remember { mutableStateListOf<SalesInvoiceItem>() }

    // Item adding state
    var selectedProductForAdd by remember { mutableStateOf<Product?>(availableProducts.firstOrNull()) }
    var addQtyText by remember { mutableStateOf("1.0") }
    var addPriceText by remember { mutableStateOf(availableProducts.firstOrNull()?.sellingPrice?.toString() ?: "0.0") }

    val subtotal = invoiceItems.sumOf { it.lineTotal }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val afterDiscount = (subtotal - discount).coerceAtLeast(0.0)

    val customTaxAmount = overrideTaxAmountText.toDoubleOrNull()
    val taxRate = if (enableTaxInThisInvoice) (taxRateText.toDoubleOrNull() ?: 0.0) else 0.0
    val calculatedTax = if (enableTaxInThisInvoice) {
        customTaxAmount ?: (afterDiscount * (taxRate / 100.0))
    } else 0.0
    val totalAmount = afterDiscount + calculatedTax

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إصدار فاتورة مبيعات جديدة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Number & Date row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it },
                                label = { Text("رقم الفاتورة (تسلسلي)") },
                                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        OperationDatePickerField(
                            selectedDate = operationDate,
                            onDateSelected = { operationDate = it },
                            label = "تاريخ إصدار الفاتورة"
                        )
                    }

                    // Customer & Payment Type
                    item {
                        Column {
                            Text("بيانات العميل وطريقة الدفع:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customerNameInput,
                                onValueChange = { customerNameInput = it },
                                label = { Text("اسم العميل") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PaymentType.values().forEach { pt ->
                                    val selected = paymentType == pt
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { paymentType = pt },
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = pt.arabicName,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add items section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("إضافة أصناف للفاتورة:", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (availableProducts.isNotEmpty()) {
                                    ScrollableTabRow(
                                        selectedTabIndex = availableProducts.indexOf(selectedProductForAdd).coerceAtLeast(0),
                                        edgePadding = 0.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        availableProducts.forEach { p ->
                                            Tab(
                                                selected = selectedProductForAdd?.id == p.id,
                                                onClick = {
                                                    selectedProductForAdd = p
                                                    addPriceText = p.sellingPrice.toString()
                                                },
                                                text = { Text("${p.nameAr} (متاح: ${Formatters.number(p.currentStock)})") }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = addQtyText,
                                        onValueChange = { addQtyText = it },
                                        label = { Text("الكمية") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = addPriceText,
                                        onValueChange = { addPriceText = it },
                                        label = { Text("سعر الوحدة") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            val prod = selectedProductForAdd
                                            val qty = addQtyText.toDoubleOrNull() ?: 0.0
                                            val price = addPriceText.toDoubleOrNull() ?: 0.0
                                            if (prod != null && qty > 0 && price >= 0) {
                                                invoiceItems.add(
                                                    SalesInvoiceItem(
                                                        invoiceId = 0,
                                                        productId = prod.id,
                                                        productName = prod.nameAr,
                                                        quantity = qty,
                                                        unitPrice = price,
                                                        lineTotal = qty * price
                                                    )
                                                )
                                                addQtyText = "1.0"
                                            }
                                        },
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Text("إضافة")
                                    }
                                }
                            }
                        }
                    }

                    // Added items list
                    if (invoiceItems.isNotEmpty()) {
                        item {
                            Text("الأصناف المضافة للفاتورة (${invoiceItems.size}):", fontWeight = FontWeight.Bold)
                        }
                        itemsIndexed(invoiceItems) { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)} = ${Formatters.currency(item.lineTotal)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { invoiceItems.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }

                    // Tax, Discount & Totals
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("المجموع قبل الخصم والضريبة:")
                                    Text(Formatters.currency(subtotal), fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = discountText,
                                        onValueChange = { discountText = it },
                                        label = { Text("الخصم") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = enableTaxInThisInvoice,
                                                onCheckedChange = { enableTaxInThisInvoice = it }
                                            )
                                            Text("تطبيق الضريبة", style = MaterialTheme.typography.labelSmall)
                                        }
                                        if (enableTaxInThisInvoice) {
                                            OutlinedTextField(
                                                value = taxRateText,
                                                onValueChange = { taxRateText = it },
                                                label = { Text("نسبة الضريبة %") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }

                                if (enableTaxInThisInvoice) {
                                    OutlinedTextField(
                                        value = overrideTaxAmountText,
                                        onValueChange = { overrideTaxAmountText = it },
                                        label = { Text("تعديل يدوي لقيمة الضريبة (اختياري)") },
                                        placeholder = { Text("اتركه فارغاً للحساب التلقائي (${Formatters.currency(afterDiscount * (taxRate / 100.0))})") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("قيمة الضريبة:")
                                        Text(Formatters.currency(calculatedTax), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الإجمالي النهائي للفاتورة:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        Formatters.currency(totalAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات إضافية على الفاتورة") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
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
                            if (invoiceItems.isNotEmpty()) {
                                onConfirm(
                                    invoiceNumber.trim(),
                                    operationDate,
                                    selectedCustomer,
                                    customerNameInput.trim().ifEmpty { "عميل نقدي" },
                                    invoiceItems.toList(),
                                    discount,
                                    taxRate,
                                    if (overrideTaxAmountText.isNotBlank()) customTaxAmount else null,
                                    paymentType,
                                    notes
                                )
                            }
                        },
                        enabled = invoiceItems.isNotEmpty() && invoiceNumber.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إصدار الفاتورة (${Formatters.currency(totalAmount)})")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Create Purchase Invoice Dialog
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePurchaseInvoiceDialog(
    initialBillNumber: String,
    isTaxActive: Boolean,
    defaultTaxRate: Double,
    suppliers: List<Supplier>,
    availableProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (
        billNumber: String,
        supplier: Supplier?,
        supplierName: String,
        supplierInvoiceRef: String,
        date: Long,
        items: List<PurchaseInvoiceItem>,
        discount: Double,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String
    ) -> Unit
) {
    var billNumber by remember { mutableStateOf(initialBillNumber) }
    var operationDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var supplierInvoiceRef by remember { mutableStateOf("") }
    var selectedSupplier by remember { mutableStateOf<Supplier?>(suppliers.firstOrNull()) }
    var supplierNameInput by remember { mutableStateOf(suppliers.firstOrNull()?.name ?: "مورد عام") }
    var paymentType by remember { mutableStateOf(PaymentType.CASH) }
    var discountText by remember { mutableStateOf("0.0") }

    var enableTaxInThisBill by remember { mutableStateOf(isTaxActive) }
    var taxRateText by remember { mutableStateOf(if (isTaxActive) defaultTaxRate.toString() else "0.0") }
    var overrideTaxAmountText by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }

    val billItems = remember { mutableStateListOf<PurchaseInvoiceItem>() }

    var selectedProductForAdd by remember { mutableStateOf<Product?>(availableProducts.firstOrNull()) }
    var addQtyText by remember { mutableStateOf("1.0") }
    var addCostText by remember { mutableStateOf(availableProducts.firstOrNull()?.purchasePrice?.toString() ?: "0.0") }

    val subtotal = billItems.sumOf { it.lineTotal }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val afterDiscount = (subtotal - discount).coerceAtLeast(0.0)

    val customTaxAmount = overrideTaxAmountText.toDoubleOrNull()
    val taxRate = if (enableTaxInThisBill) (taxRateText.toDoubleOrNull() ?: 0.0) else 0.0
    val calculatedTax = if (enableTaxInThisBill) {
        customTaxAmount ?: (afterDiscount * (taxRate / 100.0))
    } else 0.0
    val totalAmount = afterDiscount + calculatedTax

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تسجيل فاتورة مشتريات وتوريد مخزن",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Number and Ref
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = billNumber,
                                onValueChange = { billNumber = it },
                                label = { Text("رقم الفاتورة (تسلسلي)") },
                                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = supplierInvoiceRef,
                                onValueChange = { supplierInvoiceRef = it },
                                label = { Text("رقم فاتورة المورد (اختياري)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        OperationDatePickerField(
                            selectedDate = operationDate,
                            onDateSelected = { operationDate = it },
                            label = "تاريخ فاتورة الشراء والتوريد"
                        )
                    }

                    // Supplier & Payment Type
                    item {
                        Column {
                            Text("بيانات المورد وطريقة السداد:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = supplierNameInput,
                                onValueChange = { supplierNameInput = it },
                                label = { Text("اسم المورد / الشركة") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PaymentType.values().forEach { pt ->
                                    val selected = paymentType == pt
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { paymentType = pt },
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = pt.arabicName,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add items section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("إضافة أصناف مشتراة للمخزن:", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (availableProducts.isNotEmpty()) {
                                    ScrollableTabRow(
                                        selectedTabIndex = availableProducts.indexOf(selectedProductForAdd).coerceAtLeast(0),
                                        edgePadding = 0.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        availableProducts.forEach { p ->
                                            Tab(
                                                selected = selectedProductForAdd?.id == p.id,
                                                onClick = {
                                                    selectedProductForAdd = p
                                                    addCostText = p.purchasePrice.toString()
                                                },
                                                text = { Text(p.nameAr) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = addQtyText,
                                        onValueChange = { addQtyText = it },
                                        label = { Text("الكمية") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = addCostText,
                                        onValueChange = { addCostText = it },
                                        label = { Text("سعر التكلفة") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            val prod = selectedProductForAdd
                                            val qty = addQtyText.toDoubleOrNull() ?: 0.0
                                            val cost = addCostText.toDoubleOrNull() ?: 0.0
                                            if (prod != null && qty > 0 && cost >= 0) {
                                                billItems.add(
                                                    PurchaseInvoiceItem(
                                                        billId = 0,
                                                        productId = prod.id,
                                                        productName = prod.nameAr,
                                                        quantity = qty,
                                                        unitPrice = cost,
                                                        lineTotal = qty * cost
                                                    )
                                                )
                                                addQtyText = "1.0"
                                            }
                                        },
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Text("إضافة")
                                    }
                                }
                            }
                        }
                    }

                    // Added items list
                    if (billItems.isNotEmpty()) {
                        item {
                            Text("الأصناف المضافة (${billItems.size}):", fontWeight = FontWeight.Bold)
                        }
                        itemsIndexed(billItems) { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)} = ${Formatters.currency(item.lineTotal)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { billItems.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }

                    // Tax, Discount & Totals
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("المجموع قبل الخصم والضريبة:")
                                    Text(Formatters.currency(subtotal), fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = discountText,
                                        onValueChange = { discountText = it },
                                        label = { Text("الخصم") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = enableTaxInThisBill,
                                                onCheckedChange = { enableTaxInThisBill = it }
                                            )
                                            Text("تطبيق الضريبة", style = MaterialTheme.typography.labelSmall)
                                        }
                                        if (enableTaxInThisBill) {
                                            OutlinedTextField(
                                                value = taxRateText,
                                                onValueChange = { taxRateText = it },
                                                label = { Text("نسبة الضريبة %") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }

                                if (enableTaxInThisBill) {
                                    OutlinedTextField(
                                        value = overrideTaxAmountText,
                                        onValueChange = { overrideTaxAmountText = it },
                                        label = { Text("تعديل يدوي لقيمة الضريبة (اختياري)") },
                                        placeholder = { Text("اتركه فارغاً للحساب التلقائي (${Formatters.currency(afterDiscount * (taxRate / 100.0))})") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("قيمة الضريبة المدفوعة:")
                                        Text(Formatters.currency(calculatedTax), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الإجمالي النهائي لفاتورة الشراء:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        Formatters.currency(totalAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات إضافية") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
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
                            if (billItems.isNotEmpty()) {
                                onConfirm(
                                    billNumber.trim(),
                                    selectedSupplier,
                                    supplierNameInput.trim().ifEmpty { "مورد عام" },
                                    supplierInvoiceRef.trim(),
                                    operationDate,
                                    billItems.toList(),
                                    discount,
                                    taxRate,
                                    if (overrideTaxAmountText.isNotBlank()) customTaxAmount else null,
                                    paymentType,
                                    notes
                                )
                            }
                        },
                        enabled = billItems.isNotEmpty() && billNumber.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ وتوريد المخزن (${Formatters.currency(totalAmount)})")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Create Sales Return Dialog (مردود مبيعات)
// -------------------------------------------------------------
@Composable
fun CreateSalesReturnDialog(
    initialReturnNumber: String,
    isTaxActive: Boolean,
    defaultTaxRate: Double,
    customers: List<Customer>,
    availableProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (
        returnNumber: String,
        originalInvoiceNumber: String,
        date: Long,
        customer: Customer?,
        customerName: String,
        items: List<SalesReturnItem>,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String
    ) -> Unit
) {
    var returnNumber by remember { mutableStateOf(initialReturnNumber) }
    var originalInvoiceNumber by remember { mutableStateOf("") }
    var operationDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var customerNameInput by remember { mutableStateOf(customers.firstOrNull()?.name ?: "عميل نقدي عام") }
    var paymentType by remember { mutableStateOf(PaymentType.CASH) }

    var enableTax by remember { mutableStateOf(isTaxActive) }
    var taxRateText by remember { mutableStateOf(if (isTaxActive) defaultTaxRate.toString() else "0.0") }
    var overrideTaxAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val returnItems = remember { mutableStateListOf<SalesReturnItem>() }

    var selectedProductForAdd by remember { mutableStateOf<Product?>(availableProducts.firstOrNull()) }
    var addQtyText by remember { mutableStateOf("1.0") }
    var addPriceText by remember { mutableStateOf(availableProducts.firstOrNull()?.sellingPrice?.toString() ?: "0.0") }

    val subtotal = returnItems.sumOf { it.lineTotal }
    val customTaxAmount = overrideTaxAmountText.toDoubleOrNull()
    val taxRate = if (enableTax) (taxRateText.toDoubleOrNull() ?: 0.0) else 0.0
    val calculatedTax = if (enableTax) {
        customTaxAmount ?: (subtotal * (taxRate / 100.0))
    } else 0.0
    val totalAmount = subtotal + calculatedTax

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = ErrorRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إصدار فاتورة مردود مبيعات (مرتجع)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = returnNumber,
                                onValueChange = { returnNumber = it },
                                label = { Text("رقم المردود (تسلسلي)") },
                                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = originalInvoiceNumber,
                                onValueChange = { originalInvoiceNumber = it },
                                label = { Text("رقم الفاتورة الأصلية (اختياري)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        OperationDatePickerField(
                            selectedDate = operationDate,
                            onDateSelected = { operationDate = it },
                            label = "تاريخ المردود"
                        )
                    }

                    item {
                        Column {
                            Text("العميل وطريقة استرجاع المبلغ:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customerNameInput,
                                onValueChange = { customerNameInput = it },
                                label = { Text("اسم العميل") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(PaymentType.CASH, PaymentType.BANK, PaymentType.CREDIT).forEach { pt ->
                                    val selected = paymentType == pt
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { paymentType = pt },
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = if (pt == PaymentType.CREDIT) "تخفيض حساب العميل (آجل)" else pt.arabicName,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add items
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("الأصناف المسترجعة إلى المخزن:", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (availableProducts.isNotEmpty()) {
                                    ScrollableTabRow(
                                        selectedTabIndex = availableProducts.indexOf(selectedProductForAdd).coerceAtLeast(0),
                                        edgePadding = 0.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        availableProducts.forEach { p ->
                                            Tab(
                                                selected = selectedProductForAdd?.id == p.id,
                                                onClick = {
                                                    selectedProductForAdd = p
                                                    addPriceText = p.sellingPrice.toString()
                                                },
                                                text = { Text(p.nameAr) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = addQtyText,
                                        onValueChange = { addQtyText = it },
                                        label = { Text("الكمية") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = addPriceText,
                                        onValueChange = { addPriceText = it },
                                        label = { Text("سعر البيع المرتجع") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            val prod = selectedProductForAdd
                                            val qty = addQtyText.toDoubleOrNull() ?: 0.0
                                            val price = addPriceText.toDoubleOrNull() ?: 0.0
                                            if (prod != null && qty > 0 && price >= 0) {
                                                returnItems.add(
                                                    SalesReturnItem(
                                                        returnId = 0,
                                                        productId = prod.id,
                                                        productName = prod.nameAr,
                                                        quantity = qty,
                                                        unitPrice = price,
                                                        lineTotal = qty * price
                                                    )
                                                )
                                                addQtyText = "1.0"
                                            }
                                        },
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Text("إضافة")
                                    }
                                }
                            }
                        }
                    }

                    if (returnItems.isNotEmpty()) {
                        item {
                            Text("الأصناف المسترجعة (${returnItems.size}):", fontWeight = FontWeight.Bold)
                        }
                        itemsIndexed(returnItems) { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)} = ${Formatters.currency(item.lineTotal)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { returnItems.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("المجموع قبل الضريبة:")
                                    Text(Formatters.currency(subtotal), fontWeight = FontWeight.Bold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = enableTax, onCheckedChange = { enableTax = it })
                                    Text("احتساب ضريبة مستردة", style = MaterialTheme.typography.labelSmall)
                                }

                                if (enableTax) {
                                    OutlinedTextField(
                                        value = taxRateText,
                                        onValueChange = { taxRateText = it },
                                        label = { Text("نسبة الضريبة %") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("قيمة الضريبة:")
                                        Text(Formatters.currency(calculatedTax), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("إجمالي قيمة المردود المسترجع:", fontWeight = FontWeight.Bold)
                                    Text(Formatters.currency(totalAmount), fontWeight = FontWeight.Bold, color = ErrorRed)
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("سبب المرتجع / ملاحظات") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (returnItems.isNotEmpty()) {
                                onConfirm(
                                    returnNumber.trim(),
                                    originalInvoiceNumber.trim(),
                                    operationDate,
                                    selectedCustomer,
                                    customerNameInput.trim().ifEmpty { "عميل عام" },
                                    returnItems.toList(),
                                    taxRate,
                                    if (overrideTaxAmountText.isNotBlank()) customTaxAmount else null,
                                    paymentType,
                                    notes
                                )
                            }
                        },
                        enabled = returnItems.isNotEmpty() && returnNumber.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إصدار المردود (${Formatters.currency(totalAmount)})")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Create Purchase Return Dialog (مردود مشتريات)
// -------------------------------------------------------------
@Composable
fun CreatePurchaseReturnDialog(
    initialReturnNumber: String,
    isTaxActive: Boolean,
    defaultTaxRate: Double,
    suppliers: List<Supplier>,
    availableProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (
        returnNumber: String,
        originalBillNumber: String,
        date: Long,
        supplier: Supplier?,
        supplierName: String,
        items: List<PurchaseReturnItem>,
        taxRate: Double,
        taxAmountOverride: Double?,
        paymentType: PaymentType,
        notes: String
    ) -> Unit
) {
    var returnNumber by remember { mutableStateOf(initialReturnNumber) }
    var originalBillNumber by remember { mutableStateOf("") }
    var operationDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedSupplier by remember { mutableStateOf<Supplier?>(suppliers.firstOrNull()) }
    var supplierNameInput by remember { mutableStateOf(suppliers.firstOrNull()?.name ?: "مورد عام") }
    var paymentType by remember { mutableStateOf(PaymentType.CASH) }

    var enableTax by remember { mutableStateOf(isTaxActive) }
    var taxRateText by remember { mutableStateOf(if (isTaxActive) defaultTaxRate.toString() else "0.0") }
    var overrideTaxAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val returnItems = remember { mutableStateListOf<PurchaseReturnItem>() }

    var selectedProductForAdd by remember { mutableStateOf<Product?>(availableProducts.firstOrNull()) }
    var addQtyText by remember { mutableStateOf("1.0") }
    var addCostText by remember { mutableStateOf(availableProducts.firstOrNull()?.purchasePrice?.toString() ?: "0.0") }

    val subtotal = returnItems.sumOf { it.lineTotal }
    val customTaxAmount = overrideTaxAmountText.toDoubleOrNull()
    val taxRate = if (enableTax) (taxRateText.toDoubleOrNull() ?: 0.0) else 0.0
    val calculatedTax = if (enableTax) {
        customTaxAmount ?: (subtotal * (taxRate / 100.0))
    } else 0.0
    val totalAmount = subtotal + calculatedTax

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = ErrorRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إصدار فاتورة مردود مشتريات (إرجاع للمورد)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = returnNumber,
                                onValueChange = { returnNumber = it },
                                label = { Text("رقم المردود (تسلسلي)") },
                                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = originalBillNumber,
                                onValueChange = { originalBillNumber = it },
                                label = { Text("رقم فاتورة الشراء الأصلية") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        OperationDatePickerField(
                            selectedDate = operationDate,
                            onDateSelected = { operationDate = it },
                            label = "تاريخ مردود المشتريات"
                        )
                    }

                    item {
                        Column {
                            Text("المورد وطريقة استرداد القيمة:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = supplierNameInput,
                                onValueChange = { supplierNameInput = it },
                                label = { Text("اسم المورد") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(PaymentType.CASH, PaymentType.BANK, PaymentType.CREDIT).forEach { pt ->
                                    val selected = paymentType == pt
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { paymentType = pt },
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = if (pt == PaymentType.CREDIT) "تخفيض حساب المورد (آجل)" else pt.arabicName,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add items
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("الأصناف المراد إرجاعها من المخزن:", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (availableProducts.isNotEmpty()) {
                                    ScrollableTabRow(
                                        selectedTabIndex = availableProducts.indexOf(selectedProductForAdd).coerceAtLeast(0),
                                        edgePadding = 0.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        availableProducts.forEach { p ->
                                            Tab(
                                                selected = selectedProductForAdd?.id == p.id,
                                                onClick = {
                                                    selectedProductForAdd = p
                                                    addCostText = p.purchasePrice.toString()
                                                },
                                                text = { Text("${p.nameAr} (مخزون: ${Formatters.number(p.currentStock)})") }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = addQtyText,
                                        onValueChange = { addQtyText = it },
                                        label = { Text("الكمية") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = addCostText,
                                        onValueChange = { addCostText = it },
                                        label = { Text("سعر التكلفة المرتجع") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            val prod = selectedProductForAdd
                                            val qty = addQtyText.toDoubleOrNull() ?: 0.0
                                            val cost = addCostText.toDoubleOrNull() ?: 0.0
                                            if (prod != null && qty > 0 && cost >= 0) {
                                                returnItems.add(
                                                    PurchaseReturnItem(
                                                        returnId = 0,
                                                        productId = prod.id,
                                                        productName = prod.nameAr,
                                                        quantity = qty,
                                                        unitPrice = cost,
                                                        lineTotal = qty * cost
                                                    )
                                                )
                                                addQtyText = "1.0"
                                            }
                                        },
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Text("إضافة")
                                    }
                                }
                            }
                        }
                    }

                    if (returnItems.isNotEmpty()) {
                        item {
                            Text("الأصناف المراد إرجاعها (${returnItems.size}):", fontWeight = FontWeight.Bold)
                        }
                        itemsIndexed(returnItems) { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)} = ${Formatters.currency(item.lineTotal)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { returnItems.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("المجموع قبل الضريبة:")
                                    Text(Formatters.currency(subtotal), fontWeight = FontWeight.Bold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = enableTax, onCheckedChange = { enableTax = it })
                                    Text("احتساب ضريبة مسترجعة", style = MaterialTheme.typography.labelSmall)
                                }

                                if (enableTax) {
                                    OutlinedTextField(
                                        value = taxRateText,
                                        onValueChange = { taxRateText = it },
                                        label = { Text("نسبة الضريبة %") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("قيمة الضريبة:")
                                        Text(Formatters.currency(calculatedTax), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("إجمالي قيمة المردود المسترد:", fontWeight = FontWeight.Bold)
                                    Text(Formatters.currency(totalAmount), fontWeight = FontWeight.Bold, color = ErrorRed)
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (returnItems.isNotEmpty()) {
                                onConfirm(
                                    returnNumber.trim(),
                                    originalBillNumber.trim(),
                                    operationDate,
                                    selectedSupplier,
                                    supplierNameInput.trim().ifEmpty { "مورد عام" },
                                    returnItems.toList(),
                                    taxRate,
                                    if (overrideTaxAmountText.isNotBlank()) customTaxAmount else null,
                                    paymentType,
                                    notes
                                )
                            }
                        },
                        enabled = returnItems.isNotEmpty() && returnNumber.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إصدار المردود (${Formatters.currency(totalAmount)})")
                    }
                }
            }
        }
    }
}
