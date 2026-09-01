package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    customers: List<Customer>,
    availableProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (
        customer: Customer?,
        customerName: String,
        items: List<SalesInvoiceItem>,
        discount: Double,
        taxRate: Double,
        paymentType: PaymentType,
        notes: String
    ) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var customerNameInput by remember { mutableStateOf(customers.firstOrNull()?.name ?: "عميل نقدي") }
    var paymentType by remember { mutableStateOf(PaymentType.CASH) }
    var discountText by remember { mutableStateOf("0.0") }
    var taxRateText by remember { mutableStateOf("15.0") }
    var notes by remember { mutableStateOf("") }

    // Invoice items state
    val invoiceItems = remember { mutableStateListOf<SalesInvoiceItem>() }

    // Item adding state
    var selectedProductForAdd by remember { mutableStateOf<Product?>(availableProducts.firstOrNull()) }
    var addQtyText by remember { mutableStateOf("1.0") }
    var addPriceText by remember { mutableStateOf(availableProducts.firstOrNull()?.sellingPrice?.toString() ?: "0.0") }

    val subtotal = invoiceItems.sumOf { it.lineTotal }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val taxRate = taxRateText.toDoubleOrNull() ?: 0.0
    val afterDiscount = (subtotal - discount).coerceAtLeast(0.0)
    val taxAmount = afterDiscount * (taxRate / 100.0)
    val totalAmount = afterDiscount + taxAmount

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
                        text = "إصدار فاتورة مبيعات جديدة",
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
                            colors = CardDefaults.cardColors(containerColor = GrayLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("إضافة أصناف للفاتورة:", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Product quick selector
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
                                                text = { Text("${p.nameAr} (${Formatters.number(p.currentStock)})") }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = addQtyText,
                                        onValueChange = { addQtyText = it },
                                        label = { Text("الكمية") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = addPriceText,
                                        onValueChange = { addPriceText = it },
                                        label = { Text("السعر") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val p = selectedProductForAdd
                                        val q = addQtyText.toDoubleOrNull() ?: 0.0
                                        val pr = addPriceText.toDoubleOrNull() ?: 0.0
                                        if (p != null && q > 0 && pr >= 0) {
                                            invoiceItems.add(
                                                SalesInvoiceItem(
                                                    productId = p.id,
                                                    productName = p.nameAr,
                                                    quantity = q,
                                                    unitPrice = pr,
                                                    unitCost = p.purchasePrice
                                                )
                                            )
                                            addQtyText = "1.0"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("add_item_to_invoice_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة الصنف إلى الفاتورة")
                                }
                            }
                        }
                    }

                    // Invoice items list
                    itemsIndexed(invoiceItems) { index, item ->
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
                                    Text(item.productName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)} = ${Formatters.currency(item.lineTotal)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { invoiceItems.removeAt(index) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                }
                            }
                        }
                    }

                    // Summary & Totals
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = discountText,
                                        onValueChange = { discountText = it },
                                        label = { Text("الخصم") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = taxRateText,
                                        onValueChange = { taxRateText = it },
                                        label = { Text("الضريبة %") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("المجموع قبل الضريبة:")
                                    Text(Formatters.currency(subtotal), fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الضريبة ($taxRate%):")
                                    Text(Formatters.currency(taxAmount), fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الإجمالي النهائي:", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                    Text(Formatters.currency(totalAmount), fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.titleMedium)
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
                            if (invoiceItems.isNotEmpty() && customerNameInput.isNotBlank()) {
                                onConfirm(
                                    selectedCustomer,
                                    customerNameInput.trim(),
                                    invoiceItems.toList(),
                                    discount,
                                    taxRate,
                                    paymentType,
                                    notes
                                )
                            }
                        },
                        modifier = Modifier.testTag("confirm_sales_invoice_btn")
                    ) {
                        Text("إصدار وترحيل الفاتورة")
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
    suppliers: List<Supplier>,
    availableProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (
        supplier: Supplier?,
        supplierName: String,
        supplierRef: String,
        items: List<PurchaseInvoiceItem>,
        discount: Double,
        taxRate: Double,
        paymentType: PaymentType,
        notes: String
    ) -> Unit
) {
    var selectedSupplier by remember { mutableStateOf<Supplier?>(suppliers.firstOrNull()) }
    var supplierNameInput by remember { mutableStateOf(suppliers.firstOrNull()?.name ?: "مورد عام") }
    var supplierRef by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf(PaymentType.CASH) }
    var discountText by remember { mutableStateOf("0.0") }
    var taxRateText by remember { mutableStateOf("15.0") }
    var notes by remember { mutableStateOf("") }

    val invoiceItems = remember { mutableStateListOf<PurchaseInvoiceItem>() }

    var selectedProductForAdd by remember { mutableStateOf<Product?>(availableProducts.firstOrNull()) }
    var addQtyText by remember { mutableStateOf("1.0") }
    var addPriceText by remember { mutableStateOf(availableProducts.firstOrNull()?.purchasePrice?.toString() ?: "0.0") }

    val subtotal = invoiceItems.sumOf { it.lineTotal }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val taxRate = taxRateText.toDoubleOrNull() ?: 0.0
    val afterDiscount = (subtotal - discount).coerceAtLeast(0.0)
    val taxAmount = afterDiscount * (taxRate / 100.0)
    val totalAmount = afterDiscount + taxAmount

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تسجيل فاتورة مشتريات جديدة",
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
                            Text("بيانات المورد والفاتورة:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = supplierNameInput,
                                    onValueChange = { supplierNameInput = it },
                                    label = { Text("اسم المورد") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = supplierRef,
                                    onValueChange = { supplierRef = it },
                                    label = { Text("رقم فاتورة المورد") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
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

                    // Add Items
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GrayLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("إضافة بضائع ومشتريات للمخزن:", fontWeight = FontWeight.SemiBold)
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
                                                    addPriceText = p.purchasePrice.toString()
                                                },
                                                text = { Text(p.nameAr) }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = addQtyText,
                                        onValueChange = { addQtyText = it },
                                        label = { Text("الكمية المشتراة") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = addPriceText,
                                        onValueChange = { addPriceText = it },
                                        label = { Text("سعر الشراء") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val p = selectedProductForAdd
                                        val q = addQtyText.toDoubleOrNull() ?: 0.0
                                        val pr = addPriceText.toDoubleOrNull() ?: 0.0
                                        if (p != null && q > 0 && pr >= 0) {
                                            invoiceItems.add(
                                                PurchaseInvoiceItem(
                                                    productId = p.id,
                                                    productName = p.nameAr,
                                                    quantity = q,
                                                    unitPrice = pr
                                                )
                                            )
                                            addQtyText = "1.0"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("add_item_to_purchase_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة الصنف إلى فاتورة الشراء")
                                }
                            }
                        }
                    }

                    // List items
                    itemsIndexed(invoiceItems) { index, item ->
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
                                    Text(item.productName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)} = ${Formatters.currency(item.lineTotal)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { invoiceItems.removeAt(index) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                }
                            }
                        }
                    }

                    // Summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = discountText,
                                        onValueChange = { discountText = it },
                                        label = { Text("خصم المورد") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = taxRateText,
                                        onValueChange = { taxRateText = it },
                                        label = { Text("ضريبة المشتريات %") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الإجمالي النهائي للمشتريات:", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                    Text(Formatters.currency(totalAmount), fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.titleMedium)
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
                            if (invoiceItems.isNotEmpty() && supplierNameInput.isNotBlank()) {
                                onConfirm(
                                    selectedSupplier,
                                    supplierNameInput.trim(),
                                    supplierRef.trim(),
                                    invoiceItems.toList(),
                                    discount,
                                    taxRate,
                                    paymentType,
                                    notes
                                )
                            }
                        },
                        modifier = Modifier.testTag("confirm_purchase_invoice_btn")
                    ) {
                        Text("حفظ وترحيل المشتريات")
                    }
                }
            }
        }
    }
}
