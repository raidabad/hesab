package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import com.example.ui.theme.*

data class CurrencyOption(
    val nameAr: String,
    val symbol: String,
    val flagEmoji: String
)

val PRESET_CURRENCIES = listOf(
    CurrencyOption("ريال سعودي", "ر.س", "🇸🇦"),
    CurrencyOption("ريال يمني", "ر.ي", "🇾🇪"),
    CurrencyOption("درهم إماراتي", "د.إ", "🇦🇪"),
    CurrencyOption("دينار كويتي", "د.ك", "🇰🇼"),
    CurrencyOption("جنيه مصري", "ج.م", "🇪🇬"),
    CurrencyOption("ريال عماني", "ر.ع", "🇴🇲"),
    CurrencyOption("ريال قطري", "ر.ق", "🇶🇦"),
    CurrencyOption("دينار بحريني", "د.ب", "🇧🇭"),
    CurrencyOption("دينار أردني", "د.أ", "🇯🇴"),
    CurrencyOption("دينار عراقي", "د.ع", "🇮🇶"),
    CurrencyOption("ليرة سورية", "ل.س", "🇸🇾"),
    CurrencyOption("ليرة لبنانية", "ل.ل", "🇱🇧"),
    CurrencyOption("شيكل فلسطيني", "₪", "🇵🇸"),
    CurrencyOption("دينار ليبي", "د.ل", "🇱🇾"),
    CurrencyOption("درهم مغربي", "د.م", "🇲🇦"),
    CurrencyOption("دينار جزائري", "د.ج", "🇩🇿"),
    CurrencyOption("دينار تونسي", "د.ت", "🇹🇳"),
    CurrencyOption("جنيه سوداني", "ج.س", "🇸🇩"),
    CurrencyOption("دولار أمريكي", "$", "🇺🇸"),
    CurrencyOption("يورو أوروبي", "€", "🇪🇺"),
    CurrencyOption("جنيه إسترليني", "£", "🇬🇧"),
    CurrencyOption("ليرة تركية", "₺", "🇹🇷")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentStoreName: String,
    currentStorePhone: String,
    currentCurrencySymbol: String,
    isTaxActive: Boolean,
    currentTaxRate: Double,
    currentShowDecimals: Boolean = false,
    currentMinStock: Double = 5.0,
    onDismiss: () -> Unit,
    onSave: (storeName: String, storePhone: String, currencySymbol: String, isTaxEnabled: Boolean, defaultTaxRate: Double, showDecimals: Boolean, defaultMinStock: Double) -> Unit,
    onClearTransactions: () -> Unit = {},
    onResetAllData: () -> Unit = {},
    onExportBackup: (suspend () -> String)? = null,
    onRestoreBackup: ((String, () -> Unit) -> Unit)? = null,
    onRepairCOGS: (() -> Unit)? = null,
    onApplyMinStockToAll: ((Double) -> Unit)? = null
) {
    var storeName by remember { mutableStateOf(currentStoreName) }
    var storePhone by remember { mutableStateOf(currentStorePhone) }
    var selectedCurrency by remember { mutableStateOf(currentCurrencySymbol) }
    var isTaxEnabled by remember { mutableStateOf(isTaxActive) }
    var taxRateInput by remember { mutableStateOf(currentTaxRate.toString()) }
    var showDecimalsState by remember { mutableStateOf(currentShowDecimals) }
    var minStockInput by remember { mutableStateOf(if (currentMinStock % 1.0 == 0.0) currentMinStock.toInt().toString() else currentMinStock.toString()) }

    var customCurrencyInput by remember { mutableStateOf("") }
    var isCustomCurrencySelected by remember {
        mutableStateOf(PRESET_CURRENCIES.none { it.symbol == currentCurrencySymbol })
    }

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showResetAllConfirmDialog by remember { mutableStateOf(false) }
    var showApplyMinStockConfirmDialog by remember { mutableStateOf(false) }
    var showRepairCOGSConfirmDialog by remember { mutableStateOf(false) }

    var showExportBackupDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }
    var isCopiedToClipboard by remember { mutableStateOf(false) }

    var showImportBackupDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("settings_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(EmeraldPrimary, GoldAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "إعدادات النظام والضريبة والعملة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تخصيص الضريبة، العملة، وبيانات النشاط",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section 1: Tax Settings (إعدادات الضريبة)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Percent,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "ضريبة القيمة المضافة (VAT)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "إظهار أو إخفاء حقول الضريبة وتحديد النسبة الافتراضية",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isTaxEnabled,
                                    onCheckedChange = { isTaxEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                                )
                            }

                            AnimatedVisibility(visible = isTaxEnabled) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = taxRateInput,
                                        onValueChange = { taxRateInput = it },
                                        label = { Text("نسبة الضريبة الافتراضية للفواتير (%)") },
                                        placeholder = { Text("مثال: 15 أو 5 أو 0") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Calculate, contentDescription = null)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Quick Presets
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("0.0" to "0% (معفاة)", "5.0" to "5% ضريبة", "15.0" to "15% قياسية").forEach { (rate, label) ->
                                            OutlinedButton(
                                                onClick = { taxRateInput = rate },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(label, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    Text(
                                        text = "* ملاحظة: يمكنك دائماً تغيير نسبة أو قيمة الضريبة يدوياً داخل كل فاتورة أثناء إصدارها.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Section 2: Currency Selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "العملة المستخدمة في النظام",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "العملة: $selectedCurrency",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            // Presets grid
                            val chunkedCurrencies = PRESET_CURRENCIES.chunked(3)
                            chunkedCurrencies.forEach { rowCurrencies ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowCurrencies.forEach { currency ->
                                        val isSelected = selectedCurrency == currency.symbol && !isCustomCurrencySelected
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    selectedCurrency = currency.symbol
                                                    isCustomCurrencySelected = false
                                                }
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ),
                                            color = if (isSelected) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(text = currency.flagEmoji, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = currency.symbol,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                    repeat(3 - rowCurrencies.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            // Custom Currency
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { isCustomCurrencySelected = true }
                                    .background(if (isCustomCurrencySelected) EmeraldPrimary.copy(alpha = 0.1f) else Color.Transparent)
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isCustomCurrencySelected,
                                    onClick = { isCustomCurrencySelected = true },
                                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                )
                                Text(
                                    text = "إدخال رمز عملة مخصص:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            AnimatedVisibility(visible = isCustomCurrencySelected) {
                                OutlinedTextField(
                                    value = customCurrencyInput,
                                    onValueChange = {
                                        customCurrencyInput = it
                                        if (it.isNotBlank()) selectedCurrency = it.trim()
                                    },
                                    label = { Text("رمز العملة المخصصة") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Section 3: Store Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "بيانات النشاط التجاري", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            OutlinedTextField(
                                value = storeName,
                                onValueChange = { storeName = it },
                                label = { Text("اسم المحل أو المؤسسة") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = storePhone,
                                onValueChange = { storePhone = it },
                                label = { Text("رقم الهاتف أو الجوال") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Section 4: Numbers & Decimal Digits Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Pin,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تنسيق الأرقام والكسور العشرية",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Switch(
                                    checked = showDecimalsState,
                                    onCheckedChange = { showDecimalsState = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EmeraldPrimary
                                    )
                                )
                            }

                            Text(
                                text = if (showDecimalsState)
                                    "تنسيق المبالغ مفعّل بالكسور العشرية بدقة خانتين (مثال: 1,500.25 $selectedCurrency)."
                                else
                                    "تنسيق المبالغ كأرقام صحيحة بدون كسور عشرية لتسهيل القراءة السريعة (مثال: 1,500 $selectedCurrency).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Section 5: Inventory & Low Stock Alerts
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تنبيهات المخزون والحد الأدنى للطلب",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "يتم تنبيهك في لوحة التحكم والمخازن عندما تنخفض كمية الصنف عن هذا الحد لتفادي نفاد البضائع:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = minStockInput,
                                    onValueChange = { minStockInput = it },
                                    label = { Text("الحد الأدنى الافتراضي") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                if (onApplyMinStockToAll != null) {
                                    OutlinedButton(
                                        onClick = { showApplyMinStockConfirmDialog = true },
                                        modifier = Modifier.padding(top = 6.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("تعميم على الأصناف", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    // Section 6: COGS & Profit Recalculation
                    if (onRepairCOGS != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = EmeraldPrimary.copy(alpha = 0.08f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoFixHigh,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "فحص ومطابقة تكلفة المبيعات والأرباح",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }

                                Text(
                                    text = "يفحص فواتير المشتريات ويربط تكلفة الشراء آلياً مع كافة بنود فواتير المبيعات ومردوداتها للتأكد من دقة حساب الأرباح في قائمة الدخل بنسبة 100%:",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Button(
                                    onClick = { showRepairCOGSConfirmDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("فحص وتحديث تكلفة المبيعات الآن", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // Section 7: Backup & Restore (JSON)
                    if (onExportBackup != null || onRestoreBackup != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "النسخ الاحتياطي واستعادة البيانات",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "تصدير نسخة كاملة من قاعدة البيانات (الدليل المحاسبي، الأصناف، الفواتير، السندات، القيود، المخزون) لحفظها بأمان، أو استعادتها بأي وقت:",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (onExportBackup != null) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isExporting = true
                                                    try {
                                                        exportedJsonText = onExportBackup()
                                                        isCopiedToClipboard = false
                                                        showExportBackupDialog = true
                                                    } finally {
                                                        isExporting = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isExporting,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isExporting) "جاري التصدير..." else "تصدير نسخة JSON", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    if (onRestoreBackup != null) {
                                        OutlinedButton(
                                            onClick = {
                                                importJsonText = ""
                                                importErrorMessage = null
                                                showImportBackupDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("استعادة نسخة", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 8: Reset & Clean Data
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "تهيئة وتصفير النظام", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ErrorRed)
                            }

                            Text(
                                text = "حذف كافة الفواتير والسندات والقيود للبدء من الصفر على نظافة:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showClearConfirmDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("تصفير الفواتير والقيود", style = MaterialTheme.typography.labelSmall)
                                }

                                OutlinedButton(
                                    onClick = { showResetAllConfirmDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("إعادة ضبط المصنع", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Footer Save Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedTax = if (isTaxEnabled) (taxRateInput.toDoubleOrNull() ?: 0.0) else 0.0
                            val parsedMinStock = minStockInput.toDoubleOrNull() ?: 5.0
                            onSave(storeName, storePhone, selectedCurrency, isTaxEnabled, parsedTax, showDecimalsState, parsedMinStock)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ التغييرات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        ConfirmDeleteDialog(
            title = "تأكيد تصفير العمليات",
            message = "هل أنت متأكد من رغبتك في حذف جميع فواتير المبيعات والمشتريات والسندات والقيود وتصفير الأرصدة؟",
            onDismiss = { showClearConfirmDialog = false },
            onConfirm = {
                showClearConfirmDialog = false
                onClearTransactions()
                onDismiss()
            }
        )
    }

    if (showResetAllConfirmDialog) {
        ConfirmDeleteDialog(
            title = "تأكيد إعادة ضبط المصنع بالكامل",
            message = "سيتم حذف كافة الأصناف والعملاء والموردين وجميع العمليات وإعادة دليل الحسابات الافتراضي النظيف. هل تريد المتابعة؟",
            onDismiss = { showResetAllConfirmDialog = false },
            onConfirm = {
                showResetAllConfirmDialog = false
                onResetAllData()
                onDismiss()
            }
        )
    }

    if (showApplyMinStockConfirmDialog && onApplyMinStockToAll != null) {
        val qty = minStockInput.toDoubleOrNull() ?: 5.0
        AlertDialog(
            onDismissRequest = { showApplyMinStockConfirmDialog = false },
            title = { Text("تأكيد تعميم الحد الأدنى", fontWeight = FontWeight.Bold) },
            text = { Text("هل تريد ضبط الحد الأدنى للطلب إلى ($qty) لجميع الأصناف المسجلة في المخزن؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showApplyMinStockConfirmDialog = false
                        onApplyMinStockToAll(qty)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("نعم، تطبيق")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyMinStockConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showRepairCOGSConfirmDialog && onRepairCOGS != null) {
        AlertDialog(
            onDismissRequest = { showRepairCOGSConfirmDialog = false },
            title = { Text("فحص وتحديث تكلفة المبيعات والأرباح", fontWeight = FontWeight.Bold) },
            text = { Text("سيتم فحص سجلات المشتريات وربط تكاليف الأصناف المفقودة في فواتير المبيعات وحركات المخزون لتحديث حساب الأرباح بدقة. هل ترغب في المتابعة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showRepairCOGSConfirmDialog = false
                        onRepairCOGS()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("بدء الفحص والتحديث")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepairCOGSConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showExportBackupDialog) {
        AlertDialog(
            onDismissRequest = { showExportBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تم إنشاء النسخة الاحتياطية بنجاح", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تم تجهيز ملف النسخة الاحتياطية بصيغة JSON. يمكنك نسخه إلى الحافظة وحفظه في ملاحظاتك أو إرساله لحفظه بأمان.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = exportedJsonText.take(600) + if (exportedJsonText.length > 600) "\n... [المزيد]" else "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        label = { Text("معاينة نص النسخة الاحتياطية") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (isCopiedToClipboard) {
                        Text(
                            text = "تم النسخ إلى الحافظة بنجاح!",
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportedJsonText))
                        isCopiedToClipboard = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نسخ إلى الحافظة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportBackupDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    if (showImportBackupDialog && onRestoreBackup != null) {
        AlertDialog(
            onDismissRequest = { showImportBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استعادة نسخة احتياطية (JSON)", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "الصق نص النسخة الاحتياطية (JSON) في الحقل أدناه لاستعادة كافة الحسابات والفواتير والأصناف والبيانات بالكامل:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = {
                            importJsonText = it
                            importErrorMessage = null
                        },
                        placeholder = { Text("الصق بيانات النسخة الاحتياطية هنا...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    importJsonText = clip
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("لصق من الحافظة", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (importErrorMessage != null) {
                        Text(
                            text = importErrorMessage!!,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isBlank()) {
                            importErrorMessage = "يرجى لصق نص النسخة الاحتياطية أولاً"
                            return@Button
                        }
                        isImporting = true
                        onRestoreBackup(importJsonText) {
                            isImporting = false
                            showImportBackupDialog = false
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    enabled = !isImporting && importJsonText.isNotBlank()
                ) {
                    Text(if (isImporting) "جاري الاستعادة..." else "استعادة البيانات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportBackupDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
