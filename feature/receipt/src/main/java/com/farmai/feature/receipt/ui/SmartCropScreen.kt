package com.farmai.feature.receipt.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.farmai.core.domain.model.CropBox
import com.farmai.feature.receipt.R
import com.farmai.feature.receipt.viewmodel.SmartCropViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCropScreen(
    navController: NavController,
    jobId: String,
    viewModel: SmartCropViewModel = hiltViewModel()
) {
    val job by viewModel.job.collectAsState()
    val cropBox by viewModel.cropBox.collectAsState()
    val imageWidth by viewModel.imageWidth.collectAsState()
    val imageHeight by viewModel.imageHeight.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(jobId) {
        viewModel.loadJob(jobId)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.smart_crop)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.smart_crop_subtitle), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.smart_crop_hint), modifier = Modifier.padding(top = 8.dp))
                        if (imageWidth > 0 && imageHeight > 0) {
                            Text(
                                stringResource(R.string.smart_crop_dimensions, imageWidth, imageHeight),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    job == null -> Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    job?.imagePath.isNullOrBlank() -> Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            stringResource(R.string.smart_crop_no_image),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    cropBox != null && imageWidth > 0 && imageHeight > 0 -> SmartCropCanvas(
                        imagePath = job!!.imagePath!!,
                        cropBox = cropBox!!,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        onCropBoxChange = viewModel::setCropBox
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = viewModel::runAutoCrop,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && !job?.imagePath.isNullOrBlank()
                    ) {
                        Text(stringResource(R.string.smart_crop_auto))
                    }
                    Button(
                        onClick = viewModel::skipCrop,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && !job?.imagePath.isNullOrBlank()
                    ) {
                        Text(stringResource(R.string.smart_crop_skip))
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = { viewModel.saveCropBox(manualOverride = true) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && cropBox != null && !job?.imagePath.isNullOrBlank()
                ) {
                    Text(stringResource(R.string.smart_crop_save))
                }
            }

            cropBox?.let { currentCropBox ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.smart_crop_values), fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.smart_crop_value_x, currentCropBox.x.toInt()), modifier = Modifier.padding(top = 8.dp))
                            Text(stringResource(R.string.smart_crop_value_y, currentCropBox.y.toInt()), modifier = Modifier.padding(top = 4.dp))
                            Text(stringResource(R.string.smart_crop_value_size, currentCropBox.width.toInt(), currentCropBox.height.toInt()), modifier = Modifier.padding(top = 4.dp))
                            Text(stringResource(R.string.smart_crop_value_confidence, (currentCropBox.confidence * 100).toInt()), modifier = Modifier.padding(top = 4.dp))
                            Text(stringResource(R.string.smart_crop_value_manual, if (currentCropBox.manualOverride) "Manual override" else "Auto crop"), modifier = Modifier.padding(top = 4.dp))
                        }
                    }
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
private fun SmartCropCanvas(
    imagePath: String,
    cropBox: CropBox,
    imageWidth: Int,
    imageHeight: Int,
    onCropBoxChange: (CropBox) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val density = LocalDensity.current
        val primaryColor = MaterialTheme.colorScheme.primary
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val imageAspect = imageWidth.toFloat() / imageHeight.toFloat()
        val availableAspect = maxWidthPx / maxHeightPx
        val displayedWidth = if (imageAspect > availableAspect) maxWidthPx else maxHeightPx * imageAspect
        val displayedHeight = if (imageAspect > availableAspect) maxWidthPx / imageAspect else maxHeightPx

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(
                    with(density) { displayedWidth.toDp() },
                    with(density) { displayedHeight.toDp() }
                )
            ) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = stringResource(R.string.smart_crop_image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
                )
                CropOverlay(
                    cropBox = cropBox,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    displayedWidth = displayedWidth,
                    displayedHeight = displayedHeight,
                    primaryColor = primaryColor,
                    onCropBoxChange = onCropBoxChange
                )
            }
        }
    }
}

@Composable
private fun CropOverlay(
    cropBox: CropBox,
    imageWidth: Int,
    imageHeight: Int,
    displayedWidth: Float,
    displayedHeight: Float,
    primaryColor: Color,
    onCropBoxChange: (CropBox) -> Unit
) {
    var dragMode by remember { mutableStateOf<CropDragMode>(CropDragMode.None) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(cropBox, imageWidth, imageHeight, displayedWidth, displayedHeight) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val rect = cropBox.toUiRect(imageWidth, imageHeight, displayedWidth, displayedHeight)
                        dragMode = when {
                            rect.handleRects(CROP_HANDLE_SIZE_PX).any { it.containsOffset(offset) } -> CropDragMode.Resize
                            rect.containsOffset(offset) -> CropDragMode.Move
                            else -> CropDragMode.None
                        }
                    },
                    onDragEnd = { dragMode = CropDragMode.None },
                    onDragCancel = { dragMode = CropDragMode.None },
                    onDrag = { _, dragAmount ->
                        if (dragMode != CropDragMode.None) {
                            val dx = dragAmount.x * imageWidth / displayedWidth
                            val dy = dragAmount.y * imageHeight / displayedHeight
                            val updatedCropBox = when (dragMode) {
                                CropDragMode.Move -> cropBox.move(dx, dy, imageWidth, imageHeight)
                                CropDragMode.Resize -> cropBox.resize(dx, dy, imageWidth, imageHeight)
                                CropDragMode.None -> cropBox
                            }
                            onCropBoxChange(updatedCropBox.clamped(imageWidth, imageHeight))
                        }
                    }
                )
            }
    ) {
        val rect = cropBox.toUiRect(imageWidth, imageHeight, displayedWidth, displayedHeight)
        val handleSize = CROP_HANDLE_SIZE_PX
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(0f, 0f),
                size = Size(rect.left, size.height)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(rect.left, 0f),
                size = Size(rect.right - rect.left, rect.top)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(rect.right, 0f),
                size = Size(size.width - rect.right, size.height)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(rect.left, rect.bottom),
                size = Size(rect.right - rect.left, size.height - rect.bottom)
            )
            drawRect(
                color = primaryColor,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Square)
            )
            drawRect(
                color = primaryColor,
                topLeft = rect.topLeft,
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = primaryColor,
                topLeft = Offset(rect.right - handleSize, rect.top),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = primaryColor,
                topLeft = Offset(rect.left, rect.bottom - handleSize),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = primaryColor,
                topLeft = Offset(rect.right - handleSize, rect.bottom - handleSize),
                size = Size(handleSize, handleSize)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.75f),
                start = rect.topLeft,
                end = rect.bottomRight,
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.75f),
                start = Offset(rect.left, rect.bottom),
                end = Offset(rect.right, rect.top),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

private fun CropBox.toUiRect(
    imageWidth: Int,
    imageHeight: Int,
    displayedWidth: Float,
    displayedHeight: Float
): Rect {
    val left = x / imageWidth * displayedWidth
    val top = y / imageHeight * displayedHeight
    return Rect(
        left = left,
        top = top,
        right = (x + width) / imageWidth * displayedWidth,
        bottom = (y + height) / imageHeight * displayedHeight
    )
}

private fun CropBox.move(
    dx: Float,
    dy: Float,
    imageWidth: Int,
    imageHeight: Int
): CropBox {
    return copy(
        x = x + dx,
        y = y + dy
    ).clamped(imageWidth, imageHeight)
}

private fun CropBox.resize(
    dx: Float,
    dy: Float,
    imageWidth: Int,
    imageHeight: Int
): CropBox {
    return copy(
        width = width + dx,
        height = height + dy
    ).clamped(imageWidth, imageHeight)
}

private fun Rect.handleRects(handleSize: Float): List<Rect> {
    return listOf(
        Rect(left = left, top = top, right = left + handleSize, bottom = top + handleSize),
        Rect(left = right - handleSize, top = top, right = right, bottom = top + handleSize),
        Rect(left = left, top = bottom - handleSize, right = left + handleSize, bottom = bottom),
        Rect(left = right - handleSize, top = bottom - handleSize, right = right, bottom = bottom)
    )
}

private fun Rect.containsOffset(offset: Offset): Boolean {
    return offset.x in left..right && offset.y in top..bottom
}

private sealed interface CropDragMode {
    data object None : CropDragMode
    data object Move : CropDragMode
    data object Resize : CropDragMode
}

private const val CROP_HANDLE_SIZE_PX = 36f
