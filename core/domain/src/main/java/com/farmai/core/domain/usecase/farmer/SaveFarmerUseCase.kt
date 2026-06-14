package com.farmai.core.domain.usecase.farmer

import com.farmai.core.domain.model.Farmer
import com.farmai.core.domain.repository.FarmerRepository
import com.farmai.core.domain.usecase.UseCase
import javax.inject.Inject

class SaveFarmerUseCase @Inject constructor(
    private val repository: FarmerRepository
) : UseCase<Farmer, Unit> {
    override suspend operator fun invoke(farmer: Farmer): Unit {
        repository.saveFarmer(farmer)
    }
}

class DeleteFarmerUseCase @Inject constructor(
    private val repository: FarmerRepository
) : UseCase<String, Unit> {
    override suspend operator fun invoke(id: String): Unit {
        repository.deleteFarmer(id)
    }
}

class SearchFarmersUseCase @Inject constructor(
    private val repository: FarmerRepository
) : UseCase<String, List<Farmer>> {
    override suspend operator fun invoke(query: String): List<Farmer> {
        return repository.searchFarmers(query)
    }
}