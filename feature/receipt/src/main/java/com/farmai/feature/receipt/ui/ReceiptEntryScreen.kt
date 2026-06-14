package com.farmai.feature.receipt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.DeductionType
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptLineItem
import com.farmai.core.domain.model.ReceiptStatus
import com.farmai.feature.receipt.R
import com.farmai.feature.receipt.viewmodel.ReceiptEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptEntryScreen(
    navController: NavController,
    viewModel: ReceiptEntryViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val lineItems by viewModel.lineItems.collectAsState()
    val deductions by viewModel.deductions.collectAsState()
    val currentError = error

    var voucherNumber by remember { mutableStateOf("") }
    var voucherDate by remember { mutableStateOf("") }
    var farmerCode by remember { mutableStateOf("") }
    var brokerName by remember { mutableStateOf("") }
    var rawOcrText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.add_receipt)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.receipt_basic_info),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = voucherNumber,
                            onValueChange = { voucherNumber = it },
                            label = { Text(stringResource(R.string.voucher_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = voucherDate,
                            onValueChange = { voucherDate = it },
                            label = { Text(stringResource(R.string.voucher_date)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = farmerCode,
                            onValueChange = { farmerCode = it.uppercase() },
                            label = { Text(stringResource(R.string.farmer_code)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = brokerName,
                            onValueChange = { brokerName = it },
                            label = { Text(stringResource(R.string.broker_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.line_items),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { viewModel.addLineItem() }) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_line_item))
                            }
                        }
                    }
                }
            }

            itemsIndexed(lineItems) { index, item ->
                LineItemRow(
                    item = item,
                    onRemove = { viewModel.removeLineItem(index) },
                    onUpdate = { newQty, newPrice ->
                        viewModel.updateLineItem(index, newQty, newPrice)
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.deductions),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { viewModel.addDeduction(DeductionType.OTHER, 0.0) }) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_deduction))
                            }
                        }
                    }
                }
            }

            itemsIndexed(deductions) { index, deduction ->
                DeductionRow(
                    deduction = deduction,
                    onRemove = { viewModel.removeDeduction(index) },
                    onUpdate = { newAmount ->
                        viewModel.updateDeduction(index, newAmount)
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.ocr_text),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = rawOcrText,
                            onValueChange = { rawOcrText = it },
                            label = { Text(stringResource(R.string.paste_ocr_text)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5
                        )
                        Button(
                            onClick = { viewModel.parseReceiptText(rawOcrText) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(stringResource(R.string.parse_text))
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val receipt = Receipt(
                            id = Receipt.generateId(),
                            farmerId = farmerCode,
                            brokerId = brokerName,
                            voucherNumber = voucherNumber,
                            voucherDate = System.currentTimeMillis(),
                            status = ReceiptStatus.DRAFT
                        )
                        viewModel.saveReceipt(receipt, lineItems, deductions)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.save_receipt))
                    }
                }
            }
        }

        if (currentError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(currentError, color = Color.Red, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun LineItemRow(
    item: ReceiptLineItem,
    onRemove: () -> Unit,
    onUpdate: (Double, Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = item.quantity.toString(),
            onValueChange = { onUpdate(it.toDoubleOrNull() ?: 0.0, item.pricePerUnit) },
            label = { Text(stringResource(R.string.qty)) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = item.pricePerUnit.toString(),
            onValueChange = { onUpdate(item.quantity, it.toDoubleOrNull() ?: 0.0) },
            label = { Text(stringResource(R.string.price)) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = item.amount.toString(),
            onValueChange = {},
            label = { Text(stringResource(R.string.amount)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = false
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove))
        }
    }
}

@Composable
fun DeductionRow(
    deduction: Deduction,
    onRemove: () -> Unit,
    onUpdate: (Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(deduction.type.name, modifier = Modifier.weight(1f), fontSize = 14.sp)
        OutlinedTextField(
            value = deduction.amount.toString(),
            onValueChange = { onUpdate(it.toDoubleOrNull() ?: 0.0) },
            label = { Text(stringResource(R.string.amount)) },
            modifier = Modifier.weight(2f),
            singleLine = true
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove))
        }
    }
}
