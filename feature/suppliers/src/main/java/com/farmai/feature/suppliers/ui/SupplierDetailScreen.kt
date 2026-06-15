package com.farmai.feature.suppliers.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.farmai.core.domain.model.Supplier
import com.farmai.feature.suppliers.R
import com.farmai.feature.suppliers.viewmodel.SupplierDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailScreen(
    navController: NavController,
    supplierId: String?,
    viewModel: SupplierDetailViewModel = hiltViewModel()
) {
    val supplierState = viewModel.supplier.collectAsState()
    val supplier = supplierState.value
    val isLoadingState = viewModel.isLoading.collectAsState()
    val isLoading = isLoadingState.value
    val isSavingState = viewModel.isSaving.collectAsState()
    val isSaving = isSavingState.value
    val errorState = viewModel.error.collectAsState()
    val error = errorState.value

    val isEditing = supplierId != null && supplierId != "add"
    val supplierNameRequiredMessage = stringResource(R.string.supplier_name_required)
    val supplierUpdatedMessage = stringResource(R.string.supplier_updated)
    val supplierAddedMessage = stringResource(R.string.supplier_added)
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(supplierId) {
        viewModel.loadSupplier(supplierId)
    }

    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var farmerCode by remember { mutableStateOf(supplier?.farmerCode ?: "") }
    var aliases by remember { mutableStateOf(supplier?.aliases?.joinToString(", ") ?: "") }
    var threshold by remember { mutableStateOf((supplier?.confidenceThreshold ?: 0.82f).toString()) }

    if (supplier != null) {
        name = supplier.name
        farmerCode = supplier.farmerCode ?: ""
        aliases = supplier.aliases.joinToString(", ")
        threshold = supplier.confidenceThreshold.toString()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(if (isEditing) stringResource(R.string.edit_supplier) else stringResource(R.string.add_supplier)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            actions = {
                if (isEditing) {
                    IconButton(onClick = {
                        viewModel.loadSupplier(supplierId)
                        name = supplier?.name ?: ""
                        farmerCode = supplier?.farmerCode ?: ""
                        aliases = supplier?.aliases?.joinToString(", ") ?: ""
                        threshold = supplier?.confidenceThreshold?.toString() ?: "0.82"
                    }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.reset))
                    }
                }
            }
        )

        if (isLoading && supplier == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.supplier_details),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.supplier_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = name.isBlank()
                        )

                        OutlinedTextField(
                            value = farmerCode,
                            onValueChange = { farmerCode = it.uppercase() },
                            label = { Text(stringResource(R.string.supplier_farmer_code)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = aliases,
                            onValueChange = { aliases = it },
                            label = { Text(stringResource(R.string.supplier_aliases)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        OutlinedTextField(
                            value = threshold,
                            onValueChange = { threshold = it },
                            label = { Text(stringResource(R.string.supplier_threshold)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                snackbarMessage = supplierNameRequiredMessage
                                scope.launch { snackbarHostState.showSnackbar(snackbarMessage) }
                                return@Button
                            }
                            val thresholdValue = threshold.toFloatOrNull()?.coerceIn(0.0f, 1.0f) ?: 0.82f
                            val newSupplier = Supplier(
                                id = supplier?.id ?: Supplier.generateId(name),
                                name = name,
                                farmerCode = farmerCode.takeIf { it.isNotBlank() },
                                aliases = aliases
                                    .split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                    .distinct(),
                                confidenceThreshold = thresholdValue
                            )
                            viewModel.saveSupplier(newSupplier)
                            snackbarMessage = if (isEditing) supplierUpdatedMessage else supplierAddedMessage
                            scope.launch { snackbarHostState.showSnackbar(snackbarMessage) }
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f, fill = true).height(48.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (isEditing) stringResource(R.string.save) else stringResource(R.string.add))
                        }
                    }

                    if (isEditing) {
                        Button(
                            onClick = {
                                viewModel.loadSupplier(supplierId)
                                name = supplier?.name ?: ""
                                farmerCode = supplier?.farmerCode ?: ""
                                aliases = supplier?.aliases?.joinToString(", ") ?: ""
                                threshold = supplier?.confidenceThreshold?.toString() ?: "0.82"
                            },
                            modifier = Modifier.weight(1f, fill = true).height(48.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(stringResource(R.string.reset))
                        }
                    }
                }
            }
        }

        error?.let { msg ->
            snackbarMessage = msg
            scope.launch { snackbarHostState.showSnackbar(snackbarMessage) }
        }
    }

    SnackbarHost(snackbarHostState) { data ->
        Snackbar(
            snackbarData = data,
            modifier = Modifier.padding(16.dp)
        )
    }
}
