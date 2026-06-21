package com.farmai.feature.receipt.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.farmai.core.domain.model.ReceiptJob
import com.farmai.core.domain.model.ReceiptJobStatus
import com.farmai.feature.receipt.viewmodel.BatchDetailViewModel
import com.farmai.feature.receipt.work.BatchWorkflowWorker
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

    val queued = jobs.count { it.status == ReceiptJobStatus.QUEUED }
    val cropped = jobs.count { it.status == ReceiptJobStatus.CROPPED }
    val parsed = jobs.count { it.status == ReceiptJobStatus.PARSED }
    val needsValidation = jobs.count { it.status == ReceiptJobStatus.NEEDS_VALIDATION }
    val validated = jobs.count { it.status == ReceiptJobStatus.VALIDATED }
    val failed = jobs.count { it.status == ReceiptJobStatus.FAILED }
    val total = jobs.size
    val progress = if (total > 0) (validated.toFloat() + needsValidation.toFloat()) / total.toFloat() else 0f

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(if (batch != null) batch!!.name else "Batch Detail") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            Text("Batch Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Name: ${b.name}", modifier = Modifier.padding(top = 8.dp))
                            Text("Status: ${b.status.name}", modifier = Modifier.padding(top = 4.dp))

                            Text("Pipeline Progress", fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 12.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatusChip("Queued", queued, MaterialTheme.colorScheme.outline)
                                StatusChip("Cropped", cropped, MaterialTheme.colorScheme.tertiaryContainer)
                                StatusChip("Parsed", parsed, MaterialTheme.colorScheme.secondaryContainer)
                                StatusChip("Review", needsValidation, MaterialTheme.colorScheme.primaryContainer)
                                StatusChip("OK", validated, Color(0xFF4CAF50))
                                StatusChip("Failed", failed, MaterialTheme.colorScheme.errorContainer)
                            }

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
                                    Text(" Add Images")
                                }
                                Button(
                                    onClick = {
                                        BatchWorkflowWorker.enqueue(context, b.id)
                                        viewModel.loadBatch(b.id)
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading && queued + cropped + parsed > 0
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(" Process")
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.shareBatchCsv(context, b, jobs) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading && validated > 0
                                ) {
                                    Text("Export CSV")
                                }
                                Button(
                                    onClick = { viewModel.shareBatchPdf(context, b, jobs) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading && validated > 0
                                ) {
                                    Text("Export PDF")
                                }
                            }
                            Button(
                                onClick = { viewModel.deleteBatch(b.id); navController.popBackStack() },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                enabled = !isLoading,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text("Delete Batch")
                            }
                        }
                    }
                }

                items(jobs) { job ->
                    BatchJobCard(
                        job = job,
                        onCrop = { navController.navigate("queue/job/${job.id}/crop") },
                        onValidate = {
                            viewModel.updateJobStatus(job.id, ReceiptJobStatus.VALIDATED)
                        },
                        onRetry = { viewModel.updateJobStatus(job.id, ReceiptJobStatus.QUEUED) },
                        onFailed = { viewModel.markJobFailed(job.id, "Manual failure") },
                        onDelete = { viewModel.deleteJob(job.id) },
                        enabled = !isLoading
                    )
                }
            }
        }

        message?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(it, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Medium)
            }
        }

        error?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(it, color = Color.Red, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BatchJobCard(
    job: ReceiptJob,
    onCrop: () -> Unit,
    onValidate: () -> Unit,
    onRetry: () -> Unit,
    onFailed: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean
) {
    val statusColor = when (job.status) {
        ReceiptJobStatus.QUEUED -> MaterialTheme.colorScheme.outline
        ReceiptJobStatus.CROPPED -> MaterialTheme.colorScheme.tertiaryContainer
        ReceiptJobStatus.PARSED -> MaterialTheme.colorScheme.secondaryContainer
        ReceiptJobStatus.NEEDS_VALIDATION -> MaterialTheme.colorScheme.primaryContainer
        ReceiptJobStatus.VALIDATED -> Color(0xFF4CAF50)
        ReceiptJobStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Job: ${job.id.takeLast(8)}", fontWeight = FontWeight.Bold)
                Text(job.status.name, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(statusColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp))
            }

            job.imagePath?.let {
                Text("Image: ${it.substringAfterLast("/").takeLast(20)}",
                    modifier = Modifier.padding(top = 4.dp), fontSize = 12.sp)
            }
            job.ocrRawText?.let {
                Text("OCR: ${it.take(60)}...", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (job.error != null) {
                Text("Error: ${job.error}", modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onCrop, enabled = enabled && job.imagePath != null,
                    modifier = Modifier.weight(1f)) {
                    Text("Crop", fontSize = 12.sp)
                }
                when (job.status) {
                    ReceiptJobStatus.NEEDS_VALIDATION -> {
                        Button(onClick = onValidate, enabled = enabled,
                            modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(" Validate", fontSize = 12.sp)
                        }
                    }
                    ReceiptJobStatus.FAILED -> {
                        Button(onClick = onRetry, enabled = enabled,
                            modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(" Retry", fontSize = 12.sp)
                        }
                    }
                    else -> {
                        Button(onClick = onRetry, enabled = enabled,
                            modifier = Modifier.weight(1f)) {
                            Text("Retry", fontSize = 12.sp)
                        }
                    }
                }
                IconButton(onClick = onDelete, enabled = enabled) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
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