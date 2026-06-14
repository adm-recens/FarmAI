package com.farmai.core.domain.usecase.farmer

import com.farmai.core.domain.model.Farmer
import com.farmai.core.domain.repository.FarmerRepository
import com.farmai.core.domain.usecase.FlowUseCase
import com.farmai.core.domain.usecase.NoParams
import com.farmai.core.domain.usecase.UseCase
import javax.inject.Inject

class GetAllFarmersUseCase @Inject constructor(
    private val repository: FarmerRepository
) : UseCase<NoParams, List<Farmer>> {
    override suspend operator fun invoke(params: NoParams): List<Farmer> {
        return repository.getAllFarmers()
    }
}

class ObserveAllFarmersUseCase @Inject constructor(
    private val repository: FarmerRepository
) : FlowUseCase<NoParams, List<Farmer>> {
    override operator fun invoke(params: NoParams) = repository.observeAllFarmers()
}