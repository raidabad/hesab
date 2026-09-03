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
import com.example.data.model.SalesReturn
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun SalesScreen(
    viewModel: AccountingViewModel,
    onNewInvoiceClick: () -> Unit,
    onNewReturnClick: () -> Unit,
    onNewCustomerClick: () -> Unit,
    onEditCustomerClick: (Customer) -> Unit,
    onViewInvoiceDetail: (SalesInvoice) -> Unit,
    onViewReturnDetail: (SalesReturn) -> Unit,
    onEditInvoiceClick: ((SalesInvoice) -> Unit)? = null,
    onEditReturnClick: ((SalesReturn) -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var invoiceToDelete by remember { mutableStateOf<SalesInvoice?>(null) }
    var returnToDelete by remember { mutableStateOf<SalesReturn?>(null) }

    val salesInvoices by viewModel.salesInvoices.collectAsState()
    val salesReturns by viewModel.salesReturns.collectAsState()
    val customers by viewModel.customers.collectAsState()

    val filteredInvoices = remember(salesInvoices, searchQuery) {
        if (searchQuery.isBlank()) salesInvoices
        else salesInvoices.filter { it.invoiceNumber.contains(searchQuery, ignoreCase = true) || it.customerName.contains(searchQuery, ignoreCase = true) }
    }

    val filteredReturns = remember(salesReturns, searchQuery) {
        if (searchQuery.isBlank()) salesReturns
        else salesReturns.filter { it.returnNumber.contains(searchQuery, ignoreCase = true) || it.customerName.contains(searchQuery, ignoreCase = true) }
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
                text = { Text("مردود المبيعات (${salesReturns.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
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
                placeholder = {
                    Text(
                        when (selectedTab) {
                            0 -> "بحث برقم الفاتورة أو العميل..."
                            1 -> "بحث برقم المردود أو العميل..."
                            else -> "بحث باسم العميل أو الهاتف..."
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            when (selectedTab) {
                0 -> {
                    Button(
                        onClick = onNewInvoiceClick,
                        modifier = Modifier.testTag("new_sales_invoice_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فاتورة جديدة")
                    }
                }
                1 -> {
                    Button(
                        onClick = onNewReturnClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        modifier = Modifier.testTag("new_sales_return_btn")
                    ) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مردود جديد")
                    }
                }
                else -> {
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB 0: Sales Invoices
        if (selectedTab == 0) {
            if (filteredInvoices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
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
                    }
                }
            } else {
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
                                        Text(
                                            text = "فاتورة: ${inv.invoiceNumber}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = EmeraldPrimary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = inv.paymentType.arabicName,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "العميل: ${inv.customerName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "التاريخ: ${Formatters.date(inv.date)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Formatters.currency(inv.totalAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    if (onEditInvoiceClick != null) {
                                        IconButton(onClick = { onEditInvoiceClick(inv) }) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل الفاتورة", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    IconButton(onClick = { invoiceToDelete = inv }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف الفاتورة", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // TAB 1: Sales Returns (مردود مبيعات)
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
                            text = if (searchQuery.isBlank()) "لا توجد فواتير مردود مبيعات" else "لا توجد نتائج للبحث",
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
                                        if (ret.originalInvoiceNumber.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(أصل: ${ret.originalInvoiceNumber})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "العميل: ${ret.customerName}",
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
                                    if (onEditReturnClick != null) {
                                        IconButton(onClick = { onEditReturnClick(ret) }) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل المردود", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
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
            // TAB 2: Customers
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "لا يوجد عملاء مسجلون حالياً" else "لم يتم العثور على عملاء",
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
                    items(filteredCustomers, key = { it.id }) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditCustomerClick(customer) },
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
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (customer.phone.isNotBlank()) {
                                        Text(
                                            text = "الهاتف: ${customer.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "الرصيد المدين:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = Formatters.currency(customer.currentBalance),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (customer.currentBalance > 0) AmberWarning else IncomeGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { customerToDelete = customer }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف العميل", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Invoice Delete Confirmation
    invoiceToDelete?.let { inv ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف فاتورة المبيعات",
            message = "هل أنت متأكد من رغبتك في حذف فاتورة المبيعات رقم ${inv.invoiceNumber}؟ سيتم إلغاء أثرها المالي وإعادة الأصناف للمخزن وتحديث رصيد العميل.",
            onDismiss = { invoiceToDelete = null },
            onConfirm = {
                viewModel.deleteSalesInvoice(inv)
                invoiceToDelete = null
            }
        )
    }

    // Return Delete Confirmation
    returnToDelete?.let { ret ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف مردود المبيعات",
            message = "هل أنت متأكد من حذف مردود المبيعات ${ret.returnNumber}؟",
            onDismiss = { returnToDelete = null },
            onConfirm = {
                viewModel.deleteSalesReturn(ret)
                returnToDelete = null
            }
        )
    }

    // Customer Delete Confirmation with referential check
    customerToDelete?.let { cust ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف العميل",
            message = "هل أنت متأكد من حذف العميل \"${cust.name}\"؟ تنبيه: لن يتم الحذف إذا كانت هناك فواتير أو سندات أو أرصدة مرتبطة به.",
            onDismiss = { customerToDelete = null },
            onConfirm = {
                viewModel.deleteCustomerSafe(cust)
                customerToDelete = null
            }
        )
    }
}
