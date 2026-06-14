package com.farmai.feature.receipt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.navigation.NavController
import com.farmai.feature.receipt.R
import com.farmai.feature.receipt.viewmodel.BatchListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchListScreen(
    navController: NavController,
    viewModel: BatchListViewModel = hiltViewModel()
) {
    val batches by viewModel.batches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var batchName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.batches)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = batchName,
                onValueChange = { batchName = it },
                label = { Text(stringResource(R.string.batch_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            TextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.batch_notes)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 2
            )
            Button(
                onClick = {
                    viewModel.createBatch(batchName, notes.ifBlank { null })
                    batchName = ""
                    notes = ""
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        if (batches.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_batches_found), fontSize = 18.sp)
            }
        } else if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(batches) { batch ->
                    Card(
                        onClick = { navController.navigate("batch/${batch.id}") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(batch.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Status: ${batch.status.name}", modifier = Modifier.padding(top = 4.dp))
                            Text("Jobs: ${batch.totalImages}", modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        error?.let { currentError ->
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
