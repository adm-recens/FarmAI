package com.farmai.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onNavigateToFarmers: () -> Unit,
    onNavigateToBrokers: () -> Unit,
    onNavigateToReceipts: () -> Unit,
    onNavigateToBatches: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSuppliers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "FarmAI",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 32.dp)
        )
        Text(
            "Agricultural Receipt Management",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        MenuCard(
            title = "Farmers",
            subtitle = "Manage farmer profiles and codes",
            icon = Icons.Filled.People,
            onClick = onNavigateToFarmers
        )
        MenuCard(
            title = "Brokers",
            subtitle = "Manage commission agents",
            icon = Icons.Filled.Business,
            onClick = onNavigateToBrokers
        )
        MenuCard(
            title = "Receipts",
            subtitle = "Create and manage produce receipts",
            icon = Icons.Filled.Receipt,
            onClick = onNavigateToReceipts
        )
        MenuCard(
            title = "Batch Processing",
            subtitle = "Queue and process receipt images",
            icon = Icons.Filled.Inventory,
            onClick = onNavigateToBatches
        )
        MenuCard(
            title = "Reports",
            subtitle = "Farmer summaries, broker settlements",
            icon = Icons.Filled.Assessment,
            onClick = onNavigateToReports
        )
        MenuCard(
            title = "Suppliers",
            subtitle = "Manage supplier matching rules",
            icon = Icons.Filled.Agriculture,
            onClick = onNavigateToSuppliers
        )
    }
}

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Text(
                subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, start = 44.dp)
            )
        }
    }
}