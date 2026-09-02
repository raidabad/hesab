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
import com.example.data.model.MovementType
import com.example.data.model.Product
import com.example.data.model.StockMovement
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountingViewModel

@Composable
fun InventoryScreen(
    viewModel: AccountingViewModel,
    onNewProductClick: () -> Unit,
    onEditProductClick: (Product) -> Unit,
    onStockAdjustmentClick: (Product) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val products by viewModel.products.collectAsState()
    val movements by viewModel.stockMovements.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter { it.nameAr.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery) }
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
                text = { Text("بطاقات الأصناف (${products.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("حركات المخزون (${movements.size})") }
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
                placeholder = { Text("بحث باسم الصنف أو الكود...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (selectedTab == 0) {
                Button(
                    onClick = onNewProductClick,
                    modifier = Modifier.testTag("new_product_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("صنف جديد")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Products List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (lowStockProducts.isNotEmpty() && searchQuery.isBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تنبيه: يوجد (${lowStockProducts.size}) أصناف وصلت أو قاربت على النفاد من المستودع!",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }

                items(filteredProducts, key = { it.id }) { prod ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (prod.isLowStock) ErrorRed else EmeraldPrimary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = prod.code,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = prod.nameAr,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "الشراء: ${Formatters.currency(prod.purchasePrice)} • البيع: ${Formatters.currency(prod.sellingPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayMedium
                                )
                                Text(
                                    text = "قيمة مخزون الصنف: ${Formatters.currency(prod.totalCostValue)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${Formatters.number(prod.currentStock)} ${prod.unit}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (prod.isLowStock) ErrorRed else EmeraldPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row {
                                    IconButton(onClick = { onEditProductClick(prod) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { onStockAdjustmentClick(prod) }) {
                                        Icon(Icons.Default.Tune, contentDescription = "تسوية جردية", modifier = Modifier.size(18.dp), tint = EmeraldPrimary)
                                    }
                                    IconButton(onClick = { productToDelete = prod }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف الصنف", modifier = Modifier.size(18.dp), tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Stock Movements
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movements, key = { it.id }) { mov ->
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
                                Text(mov.productName, fontWeight = FontWeight.Bold)
                                Text(
                                    "${mov.movementType.arabicName} • ${Formatters.date(mov.date)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayMedium
                                )
                                if (mov.notes.isNotBlank()) {
                                    Text(mov.notes, style = MaterialTheme.typography.labelSmall, color = GrayMedium)
                                }
                            }
                            Text(
                                text = "${Formatters.number(mov.quantity)}",
                                fontWeight = FontWeight.Bold,
                                color = if (mov.movementType == MovementType.PURCHASE || mov.movementType == MovementType.ADJUSTMENT_ADD || mov.movementType == MovementType.RETURN_IN) SuccessGreen else ErrorRed,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // Product Delete Confirmation
    productToDelete?.let { prod ->
        ConfirmDeleteDialog(
            title = "تأكيد حذف الصنف",
            message = "هل أنت متأكد من حذف الصنف \"${prod.nameAr}\"؟ تنبيه: لن يتم الحذف إذا كانت هناك فواتير أو حركات مرتبطة بهذا الصنف.",
            onDismiss = { productToDelete = null },
            onConfirm = {
                viewModel.deleteProductSafe(prod)
                productToDelete = null
            }
        )
    }
}
