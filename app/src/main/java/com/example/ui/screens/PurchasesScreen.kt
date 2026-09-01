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
import com.example.data.model.Supplier
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun PurchasesScreen(
    viewModel: AccountingViewModel,
    onNewPurchaseClick: () -> Unit,
    onNewSupplierClick: () -> Unit,
    onEditSupplierClick: (Supplier) -> Unit,
    onViewPurchaseDetail: (PurchaseInvoice) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val purchaseInvoices by viewModel.purchaseInvoices.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    val filteredInvoices = remember(purchaseInvoices, searchQuery) {
        if (searchQuery.isBlank()) purchaseInvoices
        else purchaseInvoices.filter { it.billNumber.contains(searchQuery, ignoreCase = true) || it.supplierName.contains(searchQuery, ignoreCase = true) }
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
                placeholder = { Text(if (selectedTab == 0) "بحث برقم الفاتورة أو المورد..." else "بحث باسم المورد...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (selectedTab == 0) {
                Button(
                    onClick = onNewPurchaseClick,
                    modifier = Modifier.testTag("new_purchase_invoice_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فاتورة شراء")
                }
            } else {
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

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            if (filteredInvoices.isEmpty()) {
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
                        Text(
                            text = if (searchQuery.isBlank()) "النظام جاهز ونظيف. انقر على \"فاتورة جديدة\" لتسجيل مشترياتك." else "تأكد من كتابة رقم الفاتورة أو اسم المورد بشكل صحيح",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                // Purchase Invoices List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredInvoices, key = { it.id }) { bill ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewPurchaseDetail(bill) },
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
                                                text = bill.billNumber,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color = EmeraldPrimary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = Formatters.date(bill.date),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GrayMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = bill.supplierName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (bill.supplierInvoiceRef.isNotBlank()) {
                                        Text(
                                            text = "مرجع المورد: ${bill.supplierInvoiceRef}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GrayMedium
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.currency(bill.totalAmount),
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "طريقة السداد: ${bill.paymentType.arabicName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GrayMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Suppliers List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSuppliers, key = { it.id }) { sup ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Text(sup.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                if (sup.phone.isNotBlank()) {
                                    Text("الهاتف: ${sup.phone}", style = MaterialTheme.typography.bodySmall, color = GrayMedium)
                                }
                                if (sup.taxNumber.isNotBlank()) {
                                    Text("الرقم الضريبي: ${sup.taxNumber}", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "المستحق: ${Formatters.currency(sup.currentBalance)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (sup.currentBalance > 0) ErrorRed else EmeraldPrimary
                                )
                                IconButton(onClick = { onEditSupplierClick(sup) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
