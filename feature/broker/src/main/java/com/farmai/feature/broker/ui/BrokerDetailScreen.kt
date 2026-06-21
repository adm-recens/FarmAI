package com.farmai.feature.broker.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.farmai.core.domain.model.Broker
import com.farmai.feature.broker.R
import com.farmai.feature.broker.viewmodel.BrokerDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrokerDetailScreen(
    navController: NavController,
    brokerId: String?,
    viewModel: BrokerDetailViewModel = hiltViewModel()
) {
    val brokerState = viewModel.broker.collectAsState()
    val broker = brokerState.value
    val isLoadingState = viewModel.isLoading.collectAsState()
    val isLoading = isLoadingState.value
    val isSavingState = viewModel.isSaving.collectAsState()
    val isSaving = isSavingState.value
    val errorState = viewModel.error.collectAsState()
    val error = errorState.value

    val isEditing = brokerId != null && brokerId != "add"
    val nameAddressPhoneRequiredMessage = stringResource(R.string.name_address_phone_required)
    val brokerUpdatedMessage = stringResource(R.string.broker_updated)
    val brokerAddedMessage = stringResource(R.string.broker_added)
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(brokerId) {
        viewModel.loadBroker(brokerId)
    }

    var name by remember { mutableStateOf(broker?.name ?: "") }
    var address by remember { mutableStateOf(broker?.address ?: "") }
    var phone by remember { mutableStateOf(broker?.phone ?: "") }
    var commission by remember { mutableStateOf(broker?.defaultCommissionPercent?.toString() ?: "4.0") }

    if (broker != null) {
        name = broker.name
        address = broker.address
        phone = broker.phone
        commission = broker.defaultCommissionPercent.toString()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(if (isEditing) stringResource(R.string.edit_broker) else stringResource(R.string.add_broker)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            actions = {
                if (isEditing) {
                    IconButton(onClick = {
                        viewModel.loadBroker(brokerId)
                        name = broker?.name ?: ""
                        address = broker?.address ?: ""
                        phone = broker?.phone ?: ""
                        commission = broker?.defaultCommissionPercent?.toString() ?: "4.0"
                    }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.reset))
                    }
                }
            }
        )

        if (isLoading && broker == null) {
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
                            stringResource(R.string.broker_details),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.broker_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = name.isBlank()
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(stringResource(R.string.broker_address)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            isError = address.isBlank()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(stringResource(R.string.broker_phone)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = phone.isBlank()
                        )

                        OutlinedTextField(
                            value = commission,
                            onValueChange = { commission = it },
                            label = { Text(stringResource(R.string.default_commission_percent)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = commission.toDoubleOrNull() == null
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                                snackbarMessage = nameAddressPhoneRequiredMessage
                                scope.launch { snackbarHostState.showSnackbar(snackbarMessage) }
                                return@Button
                            }
                            val commissionValue = commission.toDoubleOrNull() ?: 4.0
                            val newBroker = Broker(
                                id = broker?.id ?: Broker.Companion.generateId(name),
                                name = name,
                                address = address,
                                phone = phone,
                                defaultCommissionPercent = commissionValue
                            )
                            viewModel.saveBroker(newBroker)
                            snackbarMessage = if (isEditing) brokerUpdatedMessage else brokerAddedMessage
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
                                viewModel.loadBroker(brokerId)
                                name = broker?.name ?: ""
                                address = broker?.address ?: ""
                                phone = broker?.phone ?: ""
                                commission = broker?.defaultCommissionPercent?.toString() ?: "4.0"
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