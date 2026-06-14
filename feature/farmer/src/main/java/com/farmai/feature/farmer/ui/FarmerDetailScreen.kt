package com.farmai.feature.farmer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.farmai.core.domain.model.Farmer
import com.farmai.feature.farmer.R
import com.farmai.feature.farmer.viewmodel.FarmerDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDetailScreen(
    navController: NavController,
    farmerId: String?,
    viewModel: FarmerDetailViewModel = hiltViewModel()
) {
    val farmerState = viewModel.farmer.collectAsState()
    val farmer = farmerState.value
    val isLoadingState = viewModel.isLoading.collectAsState()
    val isLoading = isLoadingState.value
    val isSavingState = viewModel.isSaving.collectAsState()
    val isSaving = isSavingState.value
    val errorState = viewModel.error.collectAsState()
    val error = errorState.value

    val isEditing = farmerId != null && farmerId != "add"
    val nameCodeRequiredMessage = stringResource(R.string.name_code_required)
    val farmerUpdatedMessage = stringResource(R.string.farmer_updated)
    val farmerAddedMessage = stringResource(R.string.farmer_added)
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    var name by remember { mutableStateOf(farmer?.name ?: "") }
    var code by remember { mutableStateOf(farmer?.code ?: "") }
    var phone by remember { mutableStateOf(farmer?.phone ?: "") }
    var village by remember { mutableStateOf(farmer?.village ?: "") }
    var primaryCrop by remember { mutableStateOf(farmer?.primaryCrop ?: "LEMON") }

    if (farmer != null) {
        name = farmer.name
        code = farmer.code
        phone = farmer.phone ?: ""
        village = farmer.village ?: ""
        primaryCrop = farmer.primaryCrop
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(if (isEditing) stringResource(R.string.edit_farmer) else stringResource(R.string.add_farmer)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            actions = {
                if (isEditing) {
                    IconButton(onClick = {
                        viewModel.loadFarmer(farmerId)
                        name = farmer?.name ?: ""
                        code = farmer?.code ?: ""
                        phone = farmer?.phone ?: ""
                        village = farmer?.village ?: ""
                        primaryCrop = farmer?.primaryCrop ?: "LEMON"
                    }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.reset))
                    }
                }
            }
        )

        if (isLoading && farmer == null) {
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
                            stringResource(R.string.farmer_details),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.farmer_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = name.isBlank()
                        )

                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase() },
                            label = { Text(stringResource(R.string.farmer_code)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = code.isBlank()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(stringResource(R.string.phone_optional)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text(stringResource(R.string.village_optional)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = primaryCrop,
                            onValueChange = { primaryCrop = it.uppercase() },
                            label = { Text(stringResource(R.string.primary_crop)) },
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
                            if (name.isBlank() || code.isBlank()) {
                                snackbarMessage = nameCodeRequiredMessage
                                showSnackbar = true
                                return@Button
                            }
                            val newFarmer = Farmer(
                                id = farmer?.id ?: Farmer.Companion.generateId(code),
                                name = name,
                                code = code.uppercase(),
                                phone = phone.takeIf { it.isNotBlank() },
                                village = village.takeIf { it.isNotBlank() },
                                primaryCrop = primaryCrop.uppercase()
                            )
                            viewModel.saveFarmer(newFarmer)
                            snackbarMessage = if (isEditing) farmerUpdatedMessage else farmerAddedMessage
                            showSnackbar = true
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
                                viewModel.loadFarmer(farmerId)
                                name = farmer?.name ?: ""
                                code = farmer?.code ?: ""
                                phone = farmer?.phone ?: ""
                                village = farmer?.village ?: ""
                                primaryCrop = farmer?.primaryCrop ?: "LEMON"
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
            showSnackbar = true
        }
    }

    SnackbarHost(snackbarHostState) { data ->
        androidx.compose.material3.Snackbar(
            snackbarData = data,
            modifier = Modifier.padding(16.dp)
        )
    }
}