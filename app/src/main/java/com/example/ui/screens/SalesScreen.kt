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
import com.example.data.model.Customer
import com.example.data.model.SalesInvoice
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun SalesScreen(
    viewModel: AccountingViewModel,
    onNewInvoiceClick: () -> Unit,
    onNewCustomerClick: () -> Unit,
    onEditCustomerClick: (Customer) -> Unit,
    onViewInvoiceDetail: (SalesInvoice) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val salesInvoices by viewModel.salesInvoices.collectAsState()
    val customers by viewModel.customers.collectAsState()

    val filteredInvoices = remember(salesInvoices, searchQuery) {
        if (searchQuery.isBlank()) salesInvoices
        else salesInvoices.filter { it.invoiceNumber.contains(searchQuery, ignoreCase = true) || it.customerName.contains(searchQuery, ignoreCase = true) }
    }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter { it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery) }
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
                text = { Text("فواتير المبيعات (${salesInvoices.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("سجل العملاء (${customers.size})") }
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
                placeholder = { Text(if (selectedTab == 0) "بحث برقم الفاتورة أو العميل..." else "بحث باسم العميل أو الهاتف...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (selectedTab == 0) {
                Button(
                    onClick = onNewInvoiceClick,
                    modifier = Modifier.testTag("new_sales_invoice_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فاتورة جديدة")
                }
            } else {
                Button(
                    onClick = onNewCustomerClick,
                    modifier = Modifier.testTag("new_customer_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عميل جديد")
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
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "لا توجد فواتير مبيعات سابقة" else "لم يتم العثور على نتائج للبحث",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "النظام جاهز ونظيف. انقر على \"فاتورة جديدة\" للبدء." else "تأكد من كتابة رقم الفاتورة أو اسم العميل بشكل صحيح",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                // Sales Invoices List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredInvoices, key = { it.id }) { inv ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewInvoiceDetail(inv) },
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
                                                text = inv.invoiceNumber,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color = EmeraldPrimary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = Formatters.date(inv.date),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GrayMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = inv.customerName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "طريقة السداد: ${inv.paymentType.arabicName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GrayMedium
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.currency(inv.totalAmount),
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (inv.taxAmount > 0) {
                                        Text(
                                            text = "الضريبة: ${Formatters.currency(inv.taxAmount)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GrayMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Customers List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCustomers, key = { it.id }) { cust ->
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
                                Text(cust.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                if (cust.phone.isNotBlank()) {
                                    Text("الجوال: ${cust.phone}", style = MaterialTheme.typography.bodySmall, color = GrayMedium)
                                }
                                if (cust.taxNumber.isNotBlank()) {
                                    Text("الرقم الضريبي: ${cust.taxNumber}", style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "الرصيد: ${Formatters.currency(cust.currentBalance)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (cust.currentBalance > 0) ErrorRed else SuccessGreen
                                )
                                IconButton(onClick = { onEditCustomerClick(cust) }) {
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
