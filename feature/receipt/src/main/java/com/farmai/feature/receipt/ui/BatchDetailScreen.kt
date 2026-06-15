package com.farmai.feature.receipt.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.farmai.core.domain.model.ReceiptJobStatus
import com.farmai.feature.receipt.R
import com.farmai.feature.receipt.viewmodel.BatchDetailViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    navController: NavController,
    batchId: String,
    viewModel: BatchDetailViewModel = hiltViewModel()
) {
    val batch by viewModel.batch.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(batchId) {
        viewModel.loadBatch(batchId)
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch {
                val savedPath = saveReceiptImage(context, it)
                viewModel.addReceiptJob(batchId, savedPath)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.batch_detail)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        )

        if (batch == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val b = batch!!
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
                            Text(stringResource(R.string.batch_summary), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Name: ${b.name}", modifier = Modifier.padding(top = 8.dp))
                            Text("Status: ${b.status.name}", modifier = Modifier.padding(top = 4.dp))
                            Text("Jobs: ${jobs.size}", modifier = Modifier.padding(top = 4.dp))
                            Text("Processed: ${b.processedCount}", modifier = Modifier.padding(top = 4.dp))
                            Text("Validated: ${b.validatedCount}", modifier = Modifier.padding(top = 4.dp))
                            Text("Failed: ${b.failedCount}", modifier = Modifier.padding(top = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { imagePicker.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Button(
                                    onClick = { viewModel.shareBatchCsv(context, b, jobs) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                ) {
                                    Text(stringResource(R.string.batch_export_csv_excel))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.shareBatchPdf(context, b, jobs) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                ) {
                                    Text(stringResource(R.string.batch_export_pdf))
                                }
                                Button(
                                    onClick = { viewModel.deleteBatch(b.id); navController.popBackStack() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading,
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

                items(jobs) { job ->
                    BatchJobCard(
                        job = job,
                        onCrop = { navController.navigate("queue/job/${job.id}/crop") },
                        onMarkParsed = { viewModel.updateJobStatus(job.id, ReceiptJobStatus.PARSED) },
                        onNeedsValidation = { viewModel.updateJobStatus(job.id, ReceiptJobStatus.NEEDS_VALIDATION) },
                        onValidated = { viewModel.updateJobStatus(job.id, ReceiptJobStatus.VALIDATED) },
                        onRetry = { viewModel.updateJobStatus(job.id, ReceiptJobStatus.QUEUED) },
                        onFailed = { viewModel.markJobFailed(job.id, "Manual failure") },
                        onDelete = { viewModel.deleteJob(job.id) },
                        enabled = !isLoading
                    )
                }
            }
        }

        message?.let { validationMessage ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(validationMessage, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Medium)
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

@Composable
private fun BatchJobCard(
    job: com.farmai.core.domain.model.ReceiptJob,
    onCrop: () -> Unit,
    onMarkParsed: () -> Unit,
    onNeedsValidation: () -> Unit,
    onValidated: () -> Unit,
    onRetry: () -> Unit,
    onFailed: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Job: ${job.id.takeLast(8)}", fontWeight = FontWeight.Bold)
            Text("Status: ${job.status.name}", modifier = Modifier.padding(top = 4.dp))
            Text("Image: ${job.imagePath ?: "none"}", modifier = Modifier.padding(top = 4.dp), fontSize = 12.sp)
            if (job.error != null) {
                Text("Error: ${job.error}", modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onCrop, enabled = enabled && job.imagePath != null, modifier = Modifier.weight(1f)) { Text("Crop") }
                Button(onClick = onMarkParsed, enabled = enabled, modifier = Modifier.weight(1f)) { Text("Parsed") }
                Button(onClick = onNeedsValidation, enabled = enabled, modifier = Modifier.weight(1f)) { Text("Validate") }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onRetry) { Icon(Icons.Default.Refresh, contentDescription = "Retry") }
                IconButton(onClick = onValidated) { Text("OK") }
                IconButton(onClick = onFailed) { Text("Fail") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
            }
        }
    }
}

private suspend fun saveReceiptImage(context: Context, uri: Uri): String {
    val directory = context.getExternalFilesDir("receipt_images") ?: context.filesDir
    directory.mkdirs()
    val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
    val file = File(directory, "${UUID.randomUUID()}.$extension")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}
