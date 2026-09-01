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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    onDismiss: () -> Unit,
    onSave: (storeName: String, storePhone: String, currencySymbol: String) -> Unit
) {
    var storeName by remember { mutableStateOf(currentStoreName) }
    var storePhone by remember { mutableStateOf(currentStorePhone) }
    var selectedCurrency by remember { mutableStateOf(currentCurrencySymbol) }
    var customCurrencyInput by remember { mutableStateOf("") }
    var isCustomCurrencySelected by remember {
        mutableStateOf(PRESET_CURRENCIES.none { it.symbol == currentCurrencySymbol })
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
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
                // Header with Gradient
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
                                text = "إعدادات النظام والعملة",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تهيئة بيانات النشاط والعملة الافتراضية",
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

                Divider(
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
                    // Section 1: Currency Selection
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
                                        text = "العملة الحالية: $selectedCurrency",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            Text(
                                text = "اختر العملة التي تريد أن تظهر في كافة الفواتير والقيود والتقارير:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Quick preset chips
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                                    .height(48.dp)
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
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = currency.flagEmoji,
                                                        fontSize = 16.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${currency.symbol}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                        // Fill remaining slots if row has fewer than 3
                                        repeat(3 - rowCurrencies.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            // Custom Currency Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        isCustomCurrencySelected = true
                                    }
                                    .background(
                                        if (isCustomCurrencySelected) EmeraldPrimary.copy(alpha = 0.1f) else Color.Transparent
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isCustomCurrencySelected,
                                    onClick = { isCustomCurrencySelected = true },
                                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                )
                                Text(
                                    text = "إدخال عملة مخصصة (رمز آخر):",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            AnimatedVisibility(visible = isCustomCurrencySelected) {
                                OutlinedTextField(
                                    value = customCurrencyInput,
                                    onValueChange = {
                                        customCurrencyInput = it
                                        if (it.isNotBlank()) {
                                            selectedCurrency = it.trim()
                                        }
                                    },
                                    label = { Text("رمز العملة المخصصة (مثال: USD, ل.س, دينار)") },
                                    placeholder = { Text("اكتب رمز العملة هنا...") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = EmeraldPrimary)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("custom_currency_field"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Section 2: Store / Business Info
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
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "بيانات المنشأة / المحل",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedTextField(
                                value = storeName,
                                onValueChange = { storeName = it },
                                label = { Text("اسم النشاط التجاري / المحل") },
                                placeholder = { Text("مثال: مؤسسة الأمانة للتجارة") },
                                leadingIcon = {
                                    Icon(Icons.Default.Business, contentDescription = null)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("store_name_field"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = storePhone,
                                onValueChange = { storePhone = it },
                                label = { Text("رقم هاتف التواصل / واتساب (اختياري)") },
                                placeholder = { Text("مثال: 0501234567") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("store_phone_field"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Section 3: System Status & Database info
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "قاعدة بيانات محلية آمنة 100%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "جميع العمليات والحسابات تُحفظ محلياً على جهازك بدقة فورية وسرعة فائقة.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val finalCurrency = if (isCustomCurrencySelected && customCurrencyInput.isNotBlank()) {
                                customCurrencyInput.trim()
                            } else {
                                selectedCurrency
                            }
                            onSave(storeName, storePhone, finalCurrency)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("save_settings_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حفظ الإعدادات والعملة",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
