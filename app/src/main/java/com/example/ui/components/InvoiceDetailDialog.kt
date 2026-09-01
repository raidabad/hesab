package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onDismiss: () -> Unit
) {
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
                            text = "فاتورة مبيعات: ${invoice.invoiceNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "العميل: ${invoice.customerName} | التاريخ: ${Formatters.dateTime(invoice.date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayMedium
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الضريبة (${invoice.taxRate}%):")
                            Text(Formatters.currency(invoice.taxAmount), fontWeight = FontWeight.Bold)
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
}

// -------------------------------------------------------------
// 2. Purchase Invoice Detail Dialog
// -------------------------------------------------------------
@Composable
fun PurchaseInvoiceDetailDialog(
    invoice: PurchaseInvoice,
    items: List<PurchaseInvoiceItem>,
    onDismiss: () -> Unit
) {
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
                            text = "فاتورة مشتريات: ${invoice.billNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "المورد: ${invoice.supplierName} | التاريخ: ${Formatters.dateTime(invoice.date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayMedium
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الضريبة (${invoice.taxRate}%):")
                            Text(Formatters.currency(invoice.taxAmount), fontWeight = FontWeight.Bold)
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
}

// -------------------------------------------------------------
// 3. Journal Entry Detail Dialog
// -------------------------------------------------------------
@Composable
fun JournalEntryDetailDialog(
    entry: JournalEntry,
    lines: List<JournalEntryLine>,
    onDismiss: () -> Unit
) {
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
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
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
}
