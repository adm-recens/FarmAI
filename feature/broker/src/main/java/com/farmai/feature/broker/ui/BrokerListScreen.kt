package com.farmai.feature.broker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.farmai.feature.broker.R
import com.farmai.feature.broker.viewmodel.BrokerListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrokerListScreen(
    navController: NavController,
    viewModel: BrokerListViewModel = hiltViewModel()
) {
    val brokers by viewModel.brokers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var queryText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.brokers_title)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            actions = {
                IconButton(onClick = { navController.navigate("broker/add") }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_broker))
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextField(
                value = queryText,
                onValueChange = { queryText = it; viewModel.onSearchQueryChange(it) },
                label = { Text(stringResource(R.string.search_brokers)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (brokers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.no_brokers_found), fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.add_first_broker), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
            ) {
                items(brokers) { broker ->
                    BrokerItemCard(
                        broker = broker,
                        onClick = { navController.navigate("broker/${broker.id}") },
                        onEditClick = { navController.navigate("broker/edit/${broker.id}") },
                        onDeleteClick = { viewModel.deleteBroker(broker) }
                    )
                }
            }
        }

        error?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(msg, color = Color.Red, fontWeight = FontWeight.Medium)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ExtendedFloatingActionButton(
            onClick = { navController.navigate("broker/add") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.add_broker)) }
        )
    }
}

@Composable
fun BrokerItemCard(
    broker: com.farmai.core.domain.model.Broker,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        broker.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Commission: ${broker.defaultCommissionPercent}%",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Phone: ${broker.phone}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        broker.address,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}