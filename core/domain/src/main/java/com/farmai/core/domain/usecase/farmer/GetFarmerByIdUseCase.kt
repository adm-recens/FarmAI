package com.farmai.core.domain.usecase.farmer

import com.farmai.core.domain.model.Farmer
import com.farmai.core.domain.repository.FarmerRepository
import com.farmai.core.domain.usecase.FlowUseCase
import com.farmai.core.domain.usecase.UseCase
import javax.inject.Inject

class GetFarmerByIdUseCase @Inject constructor(
    private val repository: FarmerRepository
) : UseCase<String, Farmer?> {
    override suspend operator fun invoke(id: String): Farmer? {
        return repository.getFarmerById(id)
    }
}

class GetFarmerByCodeUseCase @Inject constructor(
    private val repository: FarmerRepository
) : UseCase<String, Farmer?> {
    override suspend operator fun invoke(code: String): Farmer? {
        return repository.getFarmerByCode(code)
    }
}

class ObserveFarmerUseCase @Inject constructor(
    private val repository: FarmerRepository
) : FlowUseCase<String, Farmer?> {
    override operator fun invoke(id: String) = repository.observeFarmer(id)
}