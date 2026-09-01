package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.Formatters
import com.example.ui.components.ModernStatCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel
import com.example.ui.viewmodel.AppSection
import com.example.ui.viewmodel.DashboardStats

@Composable
fun DashboardScreen(
    viewModel: AccountingViewModel,
    onNavigateSection: (AppSection) -> Unit,
    onNewSaleClick: () -> Unit,
    onNewPurchaseClick: () -> Unit,
    onNewJournalClick: () -> Unit,
    onNewProductClick: () -> Unit,
    onOpenSettingsClick: () -> Unit = {},
    onViewSaleDetail: (SalesInvoice) -> Unit,
    onViewPurchaseDetail: (PurchaseInvoice) -> Unit,
    onViewJournalDetail: (JournalEntry) -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val storeName by viewModel.storeName.collectAsState()
    val recentSales by viewModel.salesInvoices.collectAsState()
    val recentPurchases by viewModel.purchaseInvoices.collectAsState()
    val recentJournals by viewModel.journalEntries.collectAsState()
    val lowStockItems by viewModel.lowStockProducts.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Hero Financial Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(EmeraldPrimary, EmeraldDark, Color(0xFF042017))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "المركز المالي العام",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "صافي أرباح الفترة",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "متوازن",
                                        color = GoldAccent,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = Formatters.currency(stats.netProfit),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Small row inside hero
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("السيولة (نقد وبنوك)", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                Text(Formatters.currency(stats.cashAndBankBalance), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("قيمة المخزون (تكلفة)", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                Text(Formatters.currency(stats.inventoryCostValue), color = GoldAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // Quick Settings & Currency Strip
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onOpenSettingsClick() }
                    .testTag("dashboard_settings_banner"),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تهيئة العملة وبيانات المحل",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "العملة الحالية: $currencySymbol | $storeName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onOpenSettingsClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "تغيير العملة",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Actions Speed Bar
        item {
            Text(
                text = "الإجراءات والعمليات السريعة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = "فاتورة بيع",
                    icon = Icons.Default.PointOfSale,
                    color = EmeraldPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_sale_btn"),
                    onClick = onNewSaleClick
                )
                QuickActionButton(
                    title = "فاتورة شراء",
                    icon = Icons.Default.ShoppingBag,
                    color = WarningOrange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_purchase_btn"),
                    onClick = onNewPurchaseClick
                )
                QuickActionButton(
                    title = "قيد يومية",
                    icon = Icons.Default.MenuBook,
                    color = InfoBlue,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_journal_btn"),
                    onClick = onNewJournalClick
                )
                QuickActionButton(
                    title = "إضافة صنف",
                    icon = Icons.Default.AddBox,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_product_btn"),
                    onClick = onNewProductClick
                )
            }
        }

        // Low stock warning strip (if any)
        if (lowStockItems.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateSection(AppSection.INVENTORY) },
                    colors = CardDefaults.cardColors(containerColor = ExpenseRedBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("تنبيه انخفاض المخزون!", fontWeight = FontWeight.Bold, color = ExpenseRed, style = MaterialTheme.typography.bodyMedium)
                                Text("يوجد ${lowStockItems.size} أصناف وصلت لحد إعادة الطلب", color = ExpenseRed.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // 4 Core Financial KPI Grid Cards
        item {
            Text("مؤشرات الأنظمة الرئيسية", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModernStatCard(
                        title = "إجمالي المبيعات",
                        value = Formatters.currency(stats.totalSales),
                        subtitle = "نظام المبيعات",
                        icon = Icons.Default.TrendingUp,
                        gradientColors = listOf(IncomeGreen, Color(0xFF059669)),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateSection(AppSection.SALES) }
                    )

                    ModernStatCard(
                        title = "إجمالي المشتريات",
                        value = Formatters.currency(stats.totalPurchases),
                        subtitle = "نظام المشتريات",
                        icon = Icons.Default.ShoppingCart,
                        gradientColors = listOf(WarningOrange, Color(0xFFD97706)),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateSection(AppSection.PURCHASES) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModernStatCard(
                        title = "الذمم المدينة (العملاء)",
                        value = Formatters.currency(stats.totalReceivables),
                        subtitle = "مستحقات للشركة",
                        icon = Icons.Default.People,
                        gradientColors = listOf(InfoBlue, Color(0xFF2563EB)),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateSection(AppSection.SALES) }
                    )

                    ModernStatCard(
                        title = "الذمم الدائنة (الموردون)",
                        value = Formatters.currency(stats.totalPayables),
                        subtitle = "مستحقات على الشركة",
                        icon = Icons.Default.LocalShipping,
                        gradientColors = listOf(ExpenseRed, Color(0xFFDC2626)),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateSection(AppSection.PURCHASES) }
                    )
                }
            }
        }

        // Financial Ratio & Balance Chart Visual Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مقارنة الإيرادات بالمصروفات والمشتريات",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val maxVal = maxOf(stats.totalSales, stats.totalPurchases, 1.0)
                    val salesRatio = (stats.totalSales / maxVal).toFloat().coerceIn(0.05f, 1f)
                    val purchaseRatio = (stats.totalPurchases / maxVal).toFloat().coerceIn(0.05f, 1f)

                    // Sales bar
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المبيعات", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(Formatters.currency(stats.totalSales), style = MaterialTheme.typography.labelSmall, color = IncomeGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(salesRatio)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IncomeGreen)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Purchases bar
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المشتريات", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(Formatters.currency(stats.totalPurchases), style = MaterialTheme.typography.labelSmall, color = WarningOrange, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(purchaseRatio)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WarningOrange)
                            )
                        }
                    }
                }
            }
        }

        // Recent Invoices and Operations
        item {
            SectionHeader(
                title = "آخر فواتير المبيعات",
                actionText = "عرض الكل",
                onActionClick = { onNavigateSection(AppSection.SALES) }
            )
        }

        if (recentSales.isEmpty()) {
            item {
                Text(
                    text = "لا توجد فواتير مبيعات مسجلة حتى الآن",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(recentSales.take(3)) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewSaleDetail(sale) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IncomeGreenBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = IncomeGreen)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(sale.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("${sale.invoiceNumber} • ${Formatters.date(sale.date)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(Formatters.currency(sale.totalAmount), fontWeight = FontWeight.Bold, color = IncomeGreen, style = MaterialTheme.typography.bodyMedium)
                            StatusBadge(
                                text = sale.paymentType.arabicName,
                                backgroundColor = if (sale.paymentType == PaymentType.CASH) IncomeGreenBg else InfoBlueBg,
                                textColor = if (sale.paymentType == PaymentType.CASH) IncomeGreen else InfoBlue
                            )
                        }
                    }
                }
            }
        }

        // Recent Journal entries
        item {
            SectionHeader(
                title = "آخر قيود اليومية (الأستاذ العام)",
                actionText = "عرض الكل",
                onActionClick = { onNavigateSection(AppSection.GENERAL_LEDGER) }
            )
        }

        if (recentJournals.isEmpty()) {
            item {
                Text(
                    text = "لا توجد قيود يومية مسجلة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(recentJournals.take(3)) { jv ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewJournalDetail(jv) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(jv.description, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                Text("${jv.entryNumber} • ${Formatters.date(jv.date)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(Formatters.currency(jv.totalDebit), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                            Text("متوازن", color = IncomeGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
