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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptStatus
import com.farmai.feature.receipt.R
import com.farmai.feature.receipt.viewmodel.ReceiptDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    navController: NavController,
    receiptId: String,
    viewModel: ReceiptDetailViewModel = hiltViewModel()
) {
    val receipt by viewModel.receipt.collectAsState()
    val lineItems by viewModel.lineItems.collectAsState()
    val deductions by viewModel.deductions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val currentReceipt = receipt
    val currentError = error

    LaunchedEffect(receiptId) {
        viewModel.loadReceipt(receiptId)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.receipt_detail_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            actions = {
                IconButton(onClick = { /* TODO: Export PDF */ }) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.export_pdf))
                }
                IconButton(onClick = { /* TODO: Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
                }
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (currentReceipt == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_receipts_found),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val r = currentReceipt
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
                                stringResource(R.string.receipt_details),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text("Voucher: ${r.voucherNumber}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Status: ${r.status.name}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.line_items),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                items(lineItems) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Qty: ${item.quantity}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Price: ${item.pricePerUnit}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Amount: ${item.amount}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.deductions),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                items(deductions) { deduction ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(deduction.type.name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("- ${deduction.amount}", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (r.status == ReceiptStatus.DRAFT) {
                            Button(
                                onClick = {
                                    viewModel.updateReceiptStatus(r.id, ReceiptStatus.CONFIRMED)
                                },
                                modifier = Modifier.weight(1f, fill = true).height(48.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(stringResource(R.string.confirm_receipt))
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.deleteReceipt(r.id)
                                navController.popBackStack()
                            },
                            modifier = Modifier.weight(1f, fill = true).height(48.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text(stringResource(R.string.delete))
                        }
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
