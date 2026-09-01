package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.viewmodel.AccountStatementRow

// -------------------------------------------------------------
// 1. Add / Edit Account Dialog
// -------------------------------------------------------------
@Composable
fun AddEditAccountDialog(
    account: Account?,
    onDismiss: () -> Unit,
    onConfirm: (Account) -> Unit
) {
    var code by remember { mutableStateOf(account?.code ?: "") }
    var nameAr by remember { mutableStateOf(account?.nameAr ?: "") }
    var type by remember { mutableStateOf(account?.type ?: AccountType.ASSET) }
    var isGroup by remember { mutableStateOf(account?.isGroup ?: false) }
    var initialBalance by remember { mutableStateOf(account?.initialBalance?.toString() ?: "0.0") }
    var notes by remember { mutableStateOf(account?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (account == null) "إضافة حساب جديد" else "تعديل الحساب",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("رمز الحساب (الكود)") },
                    modifier = Modifier.fillMaxWidth().testTag("account_code_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم الحساب") },
                    modifier = Modifier.fillMaxWidth().testTag("account_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("نوع الحساب القوائمي:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AccountType.values().forEach { t ->
                        val selected = type == t
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { type = t },
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = t.arabicName.split(" ")[0],
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isGroup,
                        onCheckedChange = { isGroup = it }
                    )
                    Text("حساب رئيسي (تجميعي / غير قابل للترحيل المباشر)")
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (!isGroup && account == null) {
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("الرصيد الافتتاحي") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (code.isNotBlank() && nameAr.isNotBlank()) {
                                val initBal = initialBalance.toDoubleOrNull() ?: 0.0
                                onConfirm(
                                    Account(
                                        id = account?.id ?: 0,
                                        code = code.trim(),
                                        nameAr = nameAr.trim(),
                                        type = type,
                                        isGroup = isGroup,
                                        initialBalance = if (account == null) initBal else account.initialBalance,
                                        currentBalance = if (account == null) initBal else account.currentBalance,
                                        notes = notes
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_account_btn")
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Add / Edit Product Dialog
// -------------------------------------------------------------
@Composable
fun AddEditProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    var code by remember { mutableStateOf(product?.code ?: "PRD-${System.currentTimeMillis() % 10000}") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var nameAr by remember { mutableStateOf(product?.nameAr ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "عام") }
    var unit by remember { mutableStateOf(product?.unit ?: "قطعة") }
    var purchasePrice by remember { mutableStateOf(product?.purchasePrice?.toString() ?: "0.0") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "0.0") }
    var currentStock by remember { mutableStateOf(product?.currentStock?.toString() ?: "0.0") }
    var minStock by remember { mutableStateOf(product?.minStockLevel?.toString() ?: "5.0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (product == null) "إضافة صنف جديد بالمخزن" else "تعديل بيانات الصنف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم الصنف") },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("كود الصنف") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("الوحدة (قطعة/كرتون)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("سعر الشراء (التكلفة)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text("سعر البيع") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentStock,
                        onValueChange = { currentStock = it },
                        label = { Text("الكمية الحالية") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("حد الطلب الأدنى") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nameAr.isNotBlank()) {
                                onConfirm(
                                    Product(
                                        id = product?.id ?: 0,
                                        code = code.trim(),
                                        barcode = barcode.trim(),
                                        nameAr = nameAr.trim(),
                                        category = category.trim(),
                                        unit = unit.trim(),
                                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                        currentStock = currentStock.toDoubleOrNull() ?: 0.0,
                                        minStockLevel = minStock.toDoubleOrNull() ?: 5.0
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_product_btn")
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Add / Edit Customer Dialog
// -------------------------------------------------------------
@Composable
fun AddEditCustomerDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onConfirm: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var taxNumber by remember { mutableStateOf(customer?.taxNumber ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var initialBalance by remember { mutableStateOf(customer?.currentBalance?.toString() ?: "0.0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (customer == null) "إضافة عميل جديد" else "تعديل بيانات العميل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل / المؤسسة") },
                    modifier = Modifier.fillMaxWidth().testTag("customer_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف / الجوال") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = taxNumber,
                    onValueChange = { taxNumber = it },
                    label = { Text("الرقم الضريبي (إن وجد)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("العنوان / المدينة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    Customer(
                                        id = customer?.id ?: 0,
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        taxNumber = taxNumber.trim(),
                                        address = address.trim(),
                                        currentBalance = if (customer == null) (initialBalance.toDoubleOrNull() ?: 0.0) else customer.currentBalance
                                    )
                                )
                            }
                        }
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Add / Edit Supplier Dialog
// -------------------------------------------------------------
@Composable
fun AddEditSupplierDialog(
    supplier: Supplier?,
    onDismiss: () -> Unit,
    onConfirm: (Supplier) -> Unit
) {
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var phone by remember { mutableStateOf(supplier?.phone ?: "") }
    var taxNumber by remember { mutableStateOf(supplier?.taxNumber ?: "") }
    var address by remember { mutableStateOf(supplier?.address ?: "") }
    var initialBalance by remember { mutableStateOf(supplier?.currentBalance?.toString() ?: "0.0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (supplier == null) "إضافة مورد جديد" else "تعديل بيانات المورد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المورد / الشركة") },
                    modifier = Modifier.fillMaxWidth().testTag("supplier_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = taxNumber,
                    onValueChange = { taxNumber = it },
                    label = { Text("الرقم الضريبي للمورد") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("العنوان") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    Supplier(
                                        id = supplier?.id ?: 0,
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        taxNumber = taxNumber.trim(),
                                        address = address.trim(),
                                        currentBalance = if (supplier == null) (initialBalance.toDoubleOrNull() ?: 0.0) else supplier.currentBalance
                                    )
                                )
                            }
                        }
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. Stock Adjustment Dialog
// -------------------------------------------------------------
@Composable
fun StockAdjustmentDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, isAddition: Boolean, reason: String) -> Unit
) {
    var quantityText by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "تسوية جردية للمخزن",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "الصنف: ${product.nameAr} (الرصيد الحالي: ${Formatters.number(product.currentStock)} ${product.unit})",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isAddition = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAddition) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isAddition) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("إضافة كمية (+)")
                    }
                    Button(
                        onClick = { isAddition = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isAddition) ErrorRed else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!isAddition) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("خصم عجز (-)")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("الكمية المراد تسويتها") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب التسوية / ملاحظات الجرد") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = quantityText.toDoubleOrNull() ?: 0.0
                            if (qty > 0) {
                                onConfirm(qty, isAddition, reason)
                            }
                        }
                    ) {
                        Text("تأكيد التسوية")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. Account Statement Dialog (كشف حساب)
// -------------------------------------------------------------
@Composable
fun AccountStatementDialog(
    account: Account,
    statementRows: List<AccountStatementRow>,
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
                            text = "كشف حساب: ${account.nameAr}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "كود الحساب: ${account.code} | الرصيد الحالي: ${Formatters.currency(account.currentBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayMedium
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()

                if (statementRows.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا توجد حركات مسجلة على هذا الحساب", color = GrayMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(statementRows) { row ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = GrayLight)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = row.entryNumber, fontWeight = FontWeight.Bold)
                                        Text(text = Formatters.date(row.date), style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                    }
                                    if (row.description.isNotBlank()) {
                                        Text(text = row.description, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (row.debit > 0) {
                                            Text(text = "مدين: ${Formatters.currency(row.debit)}", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                        }
                                        if (row.credit > 0) {
                                            Text(text = "دائن: ${Formatters.currency(row.credit)}", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                                        }
                                        Text(text = "الرصيد: ${Formatters.currency(row.runningBalance)}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
