package com.example.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*

// -------------------------------------------------------------
// 1. Sales Invoice Detail Dialog
// -------------------------------------------------------------
@Composable
fun SalesInvoiceDetailDialog(
    invoice: SalesInvoice,
    items: List<SalesInvoiceItem>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "فاتورة مبيعات: ${invoice.invoiceNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "العميل: ${invoice.customerName} | التاريخ: ${Formatters.date(invoice.date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onEdit != null) {
                            IconButton(onClick = {
                                onDismiss()
                                onEdit()
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل الفاتورة", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (onDelete != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الفاتورة", tint = ErrorRed)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GrayLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.productName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayMedium
                                    )
                                }
                                Text(
                                    Formatters.currency(item.lineTotal),
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المجموع قبل الضريبة:")
                            Text(Formatters.currency(invoice.subtotal), fontWeight = FontWeight.Bold)
                        }
                        if (invoice.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الخصم:")
                                Text(Formatters.currency(invoice.discount), color = ErrorRed)
                            }
                        }
                        if (invoice.taxAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الضريبة (${invoice.taxRate}%):")
                                Text(Formatters.currency(invoice.taxAmount), fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الإجمالي النهائي:", fontWeight = FontWeight.Bold)
                            Text(Formatters.currency(invoice.totalAmount), fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("طريقة السداد:")
                            Text(invoice.paymentType.arabicName, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        ConfirmDeleteDialog(
            title = "تأكيد حذف الفاتورة",
            message = "هل أنت متأكد من رغبتك في حذف فاتورة المبيعات رقم ${invoice.invoiceNumber}؟ سيتم إلغاء أثرها المالي وإعادة الأصناف للمخزن وتحديث رصيد العميل.",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
                onDismiss()
            }
        )
    }
}

// -------------------------------------------------------------
// 2. Purchase Invoice Detail Dialog
// -------------------------------------------------------------
@Composable
fun PurchaseInvoiceDetailDialog(
    invoice: PurchaseInvoice,
    items: List<PurchaseInvoiceItem>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "فاتورة مشتريات: ${invoice.billNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "المورد: ${invoice.supplierName} | التاريخ: ${Formatters.date(invoice.date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onEdit != null) {
                            IconButton(onClick = {
                                onDismiss()
                                onEdit()
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل الفاتورة", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (onDelete != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الفاتورة", tint = ErrorRed)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GrayLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.productName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayMedium
                                    )
                                }
                                Text(
                                    Formatters.currency(item.lineTotal),
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المجموع قبل الضريبة:")
                            Text(Formatters.currency(invoice.subtotal), fontWeight = FontWeight.Bold)
                        }
                        if (invoice.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الخصم:")
                                Text(Formatters.currency(invoice.discount), color = ErrorRed)
                            }
                        }
                        if (invoice.taxAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الضريبة (${invoice.taxRate}%):")
                                Text(Formatters.currency(invoice.taxAmount), fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي المشتريات:", fontWeight = FontWeight.Bold)
                            Text(Formatters.currency(invoice.totalAmount), fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        ConfirmDeleteDialog(
            title = "تأكيد حذف فاتورة الشراء",
            message = "هل أنت متأكد من رغبتك في حذف فاتورة المشتريات رقم ${invoice.billNumber}؟ سيتم خصم الكميات من المخزن وإلغاء القيد المحاسبي وتعديل رصيد المورد.",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
                onDismiss()
            }
        )
    }
}

// -------------------------------------------------------------
// 3. Sales Return Detail Dialog
// -------------------------------------------------------------
@Composable
fun SalesReturnDetailDialog(
    sReturn: SalesReturn,
    items: List<SalesReturnItem>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مردود مبيعات: ${sReturn.returnNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                        Text(
                            text = "العميل: ${sReturn.customerName} | التاريخ: ${Formatters.date(sReturn.date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onEdit != null) {
                            IconButton(onClick = {
                                onDismiss()
                                onEdit()
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل المردود", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (onDelete != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GrayLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.productName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayMedium
                                    )
                                }
                                Text(
                                    Formatters.currency(item.lineTotal),
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الإجمالي المسترجع:", fontWeight = FontWeight.Bold)
                            Text(Formatters.currency(sReturn.totalAmount), fontWeight = FontWeight.Bold, color = ErrorRed)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        ConfirmDeleteDialog(
            title = "تأكيد حذف مردود المبيعات",
            message = "هل أنت متأكد من حذف مردود المبيعات ${sReturn.returnNumber}؟",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
                onDismiss()
            }
        )
    }
}

// -------------------------------------------------------------
// 4. Purchase Return Detail Dialog
// -------------------------------------------------------------
@Composable
fun PurchaseReturnDetailDialog(
    pReturn: PurchaseReturn,
    items: List<PurchaseReturnItem>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مردود مشتريات: ${pReturn.returnNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                        Text(
                            text = "المورد: ${pReturn.supplierName} | التاريخ: ${Formatters.date(pReturn.date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onEdit != null) {
                            IconButton(onClick = {
                                onDismiss()
                                onEdit()
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل المردود", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (onDelete != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GrayLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.productName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${Formatters.number(item.quantity)} × ${Formatters.currency(item.unitPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrayMedium
                                    )
                                }
                                Text(
                                    Formatters.currency(item.lineTotal),
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي المردود المسترد:", fontWeight = FontWeight.Bold)
                            Text(Formatters.currency(pReturn.totalAmount), fontWeight = FontWeight.Bold, color = ErrorRed)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        ConfirmDeleteDialog(
            title = "تأكيد حذف مردود المشتريات",
            message = "هل أنت متأكد من حذف مردود المشتريات ${pReturn.returnNumber}؟",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
                onDismiss()
            }
        )
    }
}

// -------------------------------------------------------------
// 5. Journal Entry Detail Dialog
// -------------------------------------------------------------
@Composable
fun JournalEntryDetailDialog(
    entry: JournalEntry,
    lines: List<JournalEntryLine>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تفاصيل القيد: ${entry.entryNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${entry.description} | ${Formatters.date(entry.date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onDelete != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف القيد", tint = ErrorRed)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lines) { line ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GrayLight)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("${line.accountCode} - ${line.accountName}", fontWeight = FontWeight.Bold)
                                if (line.description.isNotBlank()) {
                                    Text(line.description, style = MaterialTheme.typography.bodySmall, color = GrayMedium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (line.debit > 0) Text("مدين: ${Formatters.currency(line.debit)}", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                    if (line.credit > 0) Text("دائن: ${Formatters.currency(line.credit)}", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي القيد المزدوج:", fontWeight = FontWeight.Bold)
                        Text(Formatters.currency(entry.totalDebit), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        ConfirmDeleteDialog(
            title = "تأكيد حذف القيد المحاسبي",
            message = "هل أنت متأكد من حذف القيد المحاسبي ${entry.entryNumber}؟ إذا كان القيد مرتبطاً بفاتورة أو سند فلن يتم حذفه إلا بعد حذف الأصل.",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
                onDismiss()
            }
        )
    }
}
