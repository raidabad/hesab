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
import com.example.data.model.PurchaseInvoice
import com.example.data.model.PurchaseReturn
import com.example.data.model.Supplier
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun PurchasesScreen(
    viewModel: AccountingViewModel,
    onNewBillClick: () -> Unit,
    onNewReturnClick: () -> Unit,
    onNewSupplierClick: () -> Unit,
    onEditSupplierClick: (Supplier) -> Unit,
    onViewBillDetail: (PurchaseInvoice) -> Unit,
    onViewReturnDetail: (PurchaseReturn) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var supplierToDelete by remember { mutableStateOf<Supplier?>(null) }
    var billToDelete by remember { mutableStateOf<PurchaseInvoice?>(null) }
    var returnToDelete by remember { mutableStateOf<PurchaseReturn?>(null) }

    val purchaseInvoices by viewModel.purchaseInvoices.collectAsState()
    val purchaseReturns by viewModel.purchaseReturns.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    val filteredBills = remember(purchaseInvoices, searchQuery) {
        if (searchQuery.isBlank()) purchaseInvoices
        else purchaseInvoices.filter { it.billNumber.contains(searchQuery, ignoreCase = true) || it.supplierName.contains(searchQuery, ignoreCase = true) }
    }

    val filteredReturns = remember(purchaseReturns, searchQuery) {
        if (searchQuery.isBlank()) purchaseReturns
        else purchaseReturns.filter { it.returnNumber.contains(searchQuery, ignoreCase = true) || it.supplierName.contains(searchQuery, ignoreCase = true) }
    }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        if (searchQuery.isBlank()) suppliers
        else suppliers.filter { it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("فواتير المشتريات (${purchaseInvoices.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("مردود المشتريات (${purchaseReturns.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("سجل الموردين (${suppliers.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        when (selectedTab) {
                            0 -> "بحث برقم الفاتورة أو المورد..."
                            1 -> "بحث برقم المردود أو المورد..."
                            else -> "بحث باسم المورد أو الهاتف..."
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            when (selectedTab) {
                0 -> {
                    Button(
                        onClick = onNewBillClick,
                        modifier = Modifier.testTag("new_purchase_invoice_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فاتورة شراء")
                    }
                }
                1 -> {
                    Button(
                        onClick = onNewReturnClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        modifier = Modifier.testTag("new_purchase_return_btn")
                    ) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مردود مشتريات")
                    }
                }
                else -> {
                    Button(
                        onClick = onNewSupplierClick,
                        modifier = Modifier.testTag("new_supplier_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مورد جديد")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB 0: Purchase Invoices
        if (selectedTab == 0) {
            if (filteredBills.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "لا توجد فواتير مشتريات سابقة" else "لم يتم العثور على نتائج للبحث",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBills, key = { it.id }) { bill ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewBillDetail(bill) },
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
                                        Text(
                                            text = "فاتورة: ${bill.billNumber}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (bill.supplierInvoiceRef.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(مرجع: ${bill.supplierInvoiceRef})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "المورد: ${bill.supplierName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "التاريخ: ${Formatters.date(bill.date)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Formatters.currency(bill.totalAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { billToDelete = bill }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف الفاتورة", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // TAB 1: Purchase Returns
            if (filteredReturns.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentReturn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "لا توجد فواتير مردود مشتريات" else "لا توجد نتائج للبحث",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredReturns, key = { it.id }) { ret ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewReturnDetail(ret) },
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
                                        Text(
                                            text = "مردود: ${ret.returnNumber}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ErrorRed
                                        )
                                        if (ret.originalBillNumber.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(أصل: ${ret.originalBillNumber})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "المورد: ${ret.supplierName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "التاريخ: ${Formatters.date(ret.date)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Formatters.currency(ret.totalAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorRed
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { returnToDelete = ret }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف المردود", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 2: Suppliers
            if (filteredSuppliers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "لا يوجد موردون مسجلون حالياً" else "لم يتم العثور على موردين",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSuppliers, key = { it.id }) { supplier ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditSupplierClick(supplier) },
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
                                    Text(
                                        text = supplier.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (supplier.phone.isNotBlank()) {
                                        Text(
                                            text = "الهاتف: ${supplier.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "مستحق للمورد:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = Formatters.currency(supplier.currentBalance),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (supplier.currentBalance > 0) ErrorRed else IncomeGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { supplierToDelete = supplier }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف المورد", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Bill Delete Confirmation
    billToDelete?.let { bill ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف فاتورة المشتريات",
            message = "هل أنت متأكد من رغبتك في حذف فاتورة المشتريات رقم ${bill.billNumber}؟ سيتم خصم الكميات من المخزن وإلغاء القيد المحاسبي وتعديل رصيد المورد.",
            onDismiss = { billToDelete = null },
            onConfirm = {
                viewModel.deletePurchaseInvoice(bill)
                billToDelete = null
            }
        )
    }

    // Return Delete Confirmation
    returnToDelete?.let { ret ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف مردود المشتريات",
            message = "هل أنت متأكد من حذف مردود المشتريات ${ret.returnNumber}؟",
            onDismiss = { returnToDelete = null },
            onConfirm = {
                viewModel.deletePurchaseReturn(ret)
                returnToDelete = null
            }
        )
    }

    // Supplier Delete Confirmation
    supplierToDelete?.let { supp ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف المورد",
            message = "هل أنت متأكد من حذف المورد \"${supp.name}\"؟ تنبيه: لن يتم الحذف إذا كانت هناك فواتير أو سندات أو أرصدة مرتبطة به.",
            onDismiss = { supplierToDelete = null },
            onConfirm = {
                viewModel.deleteSupplierSafe(supp)
                supplierToDelete = null
            }
        )
    }
}
