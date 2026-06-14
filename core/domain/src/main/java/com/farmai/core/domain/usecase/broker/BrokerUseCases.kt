package com.farmai.core.domain.usecase.broker

import com.farmai.core.domain.model.Broker
import com.farmai.core.domain.repository.BrokerRepository
import com.farmai.core.domain.usecase.FlowUseCase
import com.farmai.core.domain.usecase.NoParams
import com.farmai.core.domain.usecase.UseCase
import javax.inject.Inject

class GetAllBrokersUseCase @Inject constructor(
    private val repository: BrokerRepository
) : UseCase<NoParams, List<Broker>> {
    override suspend operator fun invoke(params: NoParams): List<Broker> {
        return repository.getAllBrokers()
    }
}

class ObserveAllBrokersUseCase @Inject constructor(
    private val repository: BrokerRepository
) : FlowUseCase<NoParams, List<Broker>> {
    override operator fun invoke(params: NoParams) = repository.observeAllBrokers()
}

class GetBrokerByIdUseCase @Inject constructor(
    private val repository: BrokerRepository
) : UseCase<String, Broker?> {
    override suspend operator fun invoke(id: String): Broker? {
        return repository.getBrokerById(id)
    }
}

class ObserveBrokerUseCase @Inject constructor(
    private val repository: BrokerRepository
) : FlowUseCase<String, Broker?> {
    override operator fun invoke(id: String) = repository.observeBroker(id)
}

class SaveBrokerUseCase @Inject constructor(
    private val repository: BrokerRepository
) : UseCase<Broker, Unit> {
    override suspend operator fun invoke(broker: Broker): Unit {
        repository.saveBroker(broker)
    }
}

class DeleteBrokerUseCase @Inject constructor(
    private val repository: BrokerRepository
) : UseCase<String, Unit> {
    override suspend operator fun invoke(id: String): Unit {
        repository.deleteBroker(id)
    }
}

class SearchBrokersUseCase @Inject constructor(
    private val repository: BrokerRepository
) : UseCase<String, List<Broker>> {
    override suspend operator fun invoke(query: String): List<Broker> {
        return repository.searchBrokers(query)
    }
}