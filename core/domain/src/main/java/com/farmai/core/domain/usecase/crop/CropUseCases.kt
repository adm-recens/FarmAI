package com.farmai.core.domain.usecase.crop

import com.farmai.core.domain.model.CropBox
import com.farmai.core.domain.model.CropBoxJson
import com.farmai.core.domain.model.ReceiptJob
import com.farmai.core.domain.model.SmartCropProfile
import com.farmai.core.domain.repository.BatchRepository
import com.farmai.core.domain.usecase.FlowUseCase
import com.farmai.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveJobByIdUseCase @Inject constructor(
    private val repository: BatchRepository
) : FlowUseCase<String, ReceiptJob?> {
    override operator fun invoke(id: String): Flow<ReceiptJob?> = repository.observeJobById(id)
}

class UpdateJobCropBoxUseCase @Inject constructor(
    private val repository: BatchRepository
) : UseCase<UpdateJobCropBoxParams, Unit> {
    override suspend operator fun invoke(params: UpdateJobCropBoxParams) {
        repository.updateJobCropBox(params.jobId, params.cropBoxJson, params.confidenceScore, params.croppedImagePath)
    }
}

class GenerateAutoCropBoxUseCase @Inject constructor() : UseCase<GenerateAutoCropBoxParams, CropBox> {
    override suspend operator fun invoke(params: GenerateAutoCropBoxParams): CropBox {
        return params.profile?.let { profile ->
            CropBoxJson.fromProfile(
                imageWidth = params.imageWidth,
                imageHeight = params.imageHeight,
                profile = profile,
                manualOverride = params.manualOverride
            )
        } ?: CropBoxJson.autoDetect(params.imageWidth, params.imageHeight, params.manualOverride)
    }
}

data class UpdateJobCropBoxParams(
    val jobId: String,
    val cropBoxJson: String,
    val confidenceScore: Double,
    val croppedImagePath: String? = null
)

data class GenerateAutoCropBoxParams(
    val imageWidth: Int,
    val imageHeight: Int,
    val profile: SmartCropProfile? = null,
    val manualOverride: Boolean = false
)
