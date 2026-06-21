package com.farmai.feature.receipt.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.farmai.core.domain.model.Broker
import com.farmai.core.domain.model.Deduction
import com.farmai.core.domain.model.DeductionType
import com.farmai.core.domain.model.Farmer
import com.farmai.core.domain.model.ParsedReceiptData
import com.farmai.core.domain.model.Receipt
import com.farmai.core.domain.model.ReceiptLineItem
import com.farmai.core.domain.model.ReceiptStatus
import com.farmai.feature.receipt.viewmodel.ReceiptEntryViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptEntryScreen(
    navController: NavController,
    receiptId: String? = null,
    viewModel: ReceiptEntryViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val receipt by viewModel.receipt.collectAsState()
    val lineItems by viewModel.lineItems.collectAsState()
    val deductions by viewModel.deductions.collectAsState()
    val parsedReceiptData by viewModel.parsedReceiptData.collectAsState()
    val parseMessage by viewModel.parseMessage.collectAsState()
    val farmers by viewModel.farmers.collectAsState()
    val brokers by viewModel.brokers.collectAsState()
    val currentError = error
    val isEditing = receiptId != null
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var voucherNumber by remember { mutableStateOf(receipt?.voucherNumber ?: "") }
    var voucherDate by remember { mutableStateOf(formatVoucherDate(receipt?.voucherDate)) }
    var selectedFarmer by remember { mutableStateOf<Farmer?>(null) }
    var selectedBroker by remember { mutableStateOf<Broker?>(null) }
    var rawOcrText by remember { mutableStateOf(receipt?.ocrRawText ?: "") }
    var imagePaths by remember { mutableStateOf(receipt?.imagePaths ?: emptyList()) }
    var ocrErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(receiptId) {
        if (receiptId != null) viewModel.loadReceipt(receiptId)
        viewModel.loadFarmersAndBrokers()
    }

    LaunchedEffect(receipt) {
        receipt?.let {
            voucherNumber = it.voucherNumber
            voucherDate = formatVoucherDate(it.voucherDate)
            rawOcrText = it.ocrRawText ?: ""
            imagePaths = it.imagePaths
            ocrErrorMessage = null
        }
    }

    LaunchedEffect(farmers, receipt) {
        val r = receipt
        if (r != null && selectedFarmer == null) {
            selectedFarmer = farmers.find { it.id == r.farmerId }
        }
    }

    LaunchedEffect(brokers, receipt) {
        val r = receipt
        if (r != null && selectedBroker == null) {
            selectedBroker = brokers.find { it.id == r.brokerId }
        }
    }

    fun applyParsedReceiptData(parsed: ParsedReceiptData) {
        if (parsed.voucherNumber != null) voucherNumber = parsed.voucherNumber!!
        if (parsed.voucherDate != null) voucherDate = formatVoucherDate(parsed.voucherDate)
        if (parsed.supplierCode != null) {
            val code = parsed.supplierCode!!
            val matchedFarmer = farmers.find { it.code.equals(code, ignoreCase = true) }
            if (matchedFarmer != null) selectedFarmer = matchedFarmer
        }
        if (parsed.brokerName != null) {
            val bname = parsed.brokerName!!
            val matchedBroker = brokers.find { it.name.contains(bname, ignoreCase = true) }
            if (matchedBroker != null) selectedBroker = matchedBroker
        }
        viewModel.applyParsedReceiptData(parsed)
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch {
                val savedPath = saveReceiptImage(context, it)
                imagePaths = imagePaths + savedPath
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(if (isEditing) "Edit Receipt" else "Add Receipt") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        Text("Receipt Info", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface)

                        FarmerDropdown(
                            farmers = farmers,
                            selectedFarmer = selectedFarmer,
                            onFarmerSelected = { selectedFarmer = it }
                        )

                        BrokerDropdown(
                            brokers = brokers,
                            selectedBroker = selectedBroker,
                            onBrokerSelected = { selectedBroker = it }
                        )

                        OutlinedTextField(
                            value = voucherNumber,
                            onValueChange = { voucherNumber = it },
                            label = { Text("Voucher Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = voucherDate,
                            onValueChange = { voucherDate = it },
                            label = { Text("Voucher Date (DD/MM/YYYY)") },
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
                            Text("Receipt Image", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            IconButton(onClick = { imagePicker.launch("image/*") }) {
                                Icon(Icons.Default.Add, contentDescription = "Import Image")
                            }
                        }
                        if (imagePaths.isNotEmpty()) {
                            AsyncImage(
                                model = File(imagePaths.last()),
                                contentDescription = "Receipt image preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .padding(top = 12.dp)
                            )
                            Button(
                                onClick = {
                                    runOcrOnImage(
                                        context = context,
                                        imagePath = imagePaths.last(),
                                        onSuccess = { text ->
                                            rawOcrText = text
                                            ocrErrorMessage = null
                                            viewModel.parseReceiptText(text)
                                        },
                                        onError = { msg -> ocrErrorMessage = msg }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                enabled = !isLoading
                            ) {
                                Text("Run Image OCR")
                            }
                        }
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
                            Text("Line Items", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            IconButton(onClick = { viewModel.addLineItem() }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Line Item")
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
                            Text("Deductions", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            IconButton(onClick = { viewModel.addDeduction(DeductionType.OTHER, 0.0) }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Deduction")
                            }
                        }
                    }
                }
            }

            itemsIndexed(deductions) { index, deduction ->
                DeductionRow(
                    deduction = deduction,
                    onRemove = { viewModel.removeDeduction(index) },
                    onUpdate = { newAmount -> viewModel.updateDeduction(index, newAmount) }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("OCR Text", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface)

                        OutlinedTextField(
                            value = rawOcrText,
                            onValueChange = { rawOcrText = it },
                            label = { Text("Paste or edit OCR text") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5
                        )
                        Button(
                            onClick = { viewModel.parseReceiptText(rawOcrText) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Parse Text")
                            }
                        }
                    }
                }
            }

            parsedReceiptData?.let { parsed ->
                item {
                    ParsedReceiptSummaryCard(
                        parsed = parsed,
                        message = parseMessage,
                        onApply = { applyParsedReceiptData(parsed) }
                    )
                }
            }

            if (ocrErrorMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(ocrErrorMessage.orEmpty(), color = Color.Red, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val parsedVoucherDate = parseVoucherDate(voucherDate) ?: System.currentTimeMillis()
                        val farmerId = selectedFarmer?.id ?: ""
                        val brokerId = selectedBroker?.id ?: ""
                        val updatedReceipt = Receipt(
                            id = receipt?.id ?: Receipt.generateId(),
                            farmerId = farmerId,
                            brokerId = brokerId,
                            voucherNumber = voucherNumber,
                            voucherDate = parsedVoucherDate,
                            imagePaths = imagePaths,
                            ocrRawText = rawOcrText.ifBlank { receipt?.ocrRawText },
                            status = receipt?.status ?: ReceiptStatus.DRAFT,
                            createdAt = receipt?.createdAt ?: System.currentTimeMillis()
                        )
                        viewModel.saveReceipt(updatedReceipt, lineItems, deductions)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Receipt")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FarmerDropdown(
    farmers: List<Farmer>,
    selectedFarmer: Farmer?,
    onFarmerSelected: (Farmer) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedFarmer?.let { "${it.code} - ${it.name}" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Farmer") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            farmers.forEach { farmer ->
                DropdownMenuItem(
                    text = { Text("${farmer.code} - ${farmer.name}") },
                    onClick = {
                        onFarmerSelected(farmer)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrokerDropdown(
    brokers: List<Broker>,
    selectedBroker: Broker?,
    onBrokerSelected: (Broker) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedBroker?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Broker") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            brokers.forEach { broker ->
                DropdownMenuItem(
                    text = { Text(broker.name) },
                    onClick = {
                        onBrokerSelected(broker)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ParsedReceiptSummaryCard(
    parsed: ParsedReceiptData,
    message: String?,
    onApply: () -> Unit
) {
    val brokerName = parsed.brokerName
    val voucherNumber = parsed.voucherNumber
    val supplierCode = parsed.supplierCode
    val voucherDate = parsed.voucherDate
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Parsed Receipt Data", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                message ?: "Confidence: ${(parsed.confidenceScore * 100).toInt()}%",
                modifier = Modifier.padding(top = 8.dp)
            )
            if (brokerName != null) Text("Broker: $brokerName", modifier = Modifier.padding(top = 4.dp))
            if (voucherNumber != null) Text("Voucher: $voucherNumber", modifier = Modifier.padding(top = 4.dp))
            if (voucherDate != null) Text("Date: ${formatVoucherDate(voucherDate)}", modifier = Modifier.padding(top = 4.dp))
            if (supplierCode != null) Text("Farmer Code: $supplierCode", modifier = Modifier.padding(top = 4.dp))
            Text("Line Items: ${parsed.lineItems.size}", modifier = Modifier.padding(top = 4.dp))
            val deductionCount = parsed.otherDeductions.size + listOfNotNull(
                parsed.commissionPercent, parsed.damagesAmount, parsed.unloadingAmount, parsed.advanceAmount
            ).size
            Text("Deductions: $deductionCount", modifier = Modifier.padding(top = 4.dp))
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                enabled = parsed.lineItems.isNotEmpty() || parsed.deductionsPresent()
            ) {
                Text("Apply Parsed Data")
            }
        }
    }
}

private fun ParsedReceiptData.deductionsPresent(): Boolean {
    return commissionPercent != null || damagesAmount != null || unloadingAmount != null ||
        advanceAmount != null || otherDeductions.isNotEmpty()
}

private fun runOcrOnImage(
    context: Context,
    imagePath: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromFilePath(context, Uri.parse(imagePath))

    recognizer.process(image)
        .addOnSuccessListener { visionText -> onSuccess(visionText.text) }
        .addOnFailureListener { error -> onError(error.message ?: "OCR failed") }
        .addOnCompleteListener { recognizer.close() }
}

private fun parseVoucherDate(value: String): Long? {
    if (value.isBlank()) return null
    val trimmed = value.trim()
    val formats = listOf("dd/MM/yyyy", "dd-MM-yyyy", "ddMMyyyy", "yyyy-MM-dd")
    return formats.firstNotNullOfOrNull { format ->
        runCatching {
            java.text.SimpleDateFormat(format, java.util.Locale.getDefault()).parse(trimmed)?.time
        }.getOrNull()
    }
}

private fun formatVoucherDate(value: Long?): String {
    if (value == null) return ""
    return java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(value))
}

private suspend fun saveReceiptImage(context: Context, uri: Uri): String {
    val directory = context.getExternalFilesDir("receipt_images") ?: context.filesDir
    directory.mkdirs()
    val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
    val file = File(directory, "${UUID.randomUUID()}.$extension")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output -> input.copyTo(output) }
    }
    return file.absolutePath
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
            label = { Text("Qty") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = item.pricePerUnit.toString(),
            onValueChange = { onUpdate(item.quantity, it.toDoubleOrNull() ?: 0.0) },
            label = { Text("Price") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = item.amount.toString(),
            onValueChange = {},
            label = { Text("Amount") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = false
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove")
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
            label = { Text("Amount") },
            modifier = Modifier.weight(2f),
            singleLine = true
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove")
        }
    }
}