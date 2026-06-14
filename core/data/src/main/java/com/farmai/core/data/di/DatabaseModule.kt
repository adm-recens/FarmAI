package com.farmai.core.data.di

import android.content.Context
import com.farmai.core.data.local.AppDatabase
import com.farmai.core.data.repository.BatchRepositoryImpl
import com.farmai.core.data.repository.BrokerRepositoryImpl
import com.farmai.core.data.repository.FarmerRepositoryImpl
import com.farmai.core.data.repository.ReceiptParserRepositoryImpl
import com.farmai.core.data.repository.ReceiptRepositoryImpl
import com.farmai.core.data.repository.ReportRepositoryImpl
import com.farmai.core.domain.repository.BatchRepository
import com.farmai.core.domain.repository.BrokerRepository
import com.farmai.core.domain.repository.FarmerRepository
import com.farmai.core.domain.repository.ReceiptParserRepository
import com.farmai.core.domain.repository.ReceiptRepository
import com.farmai.core.domain.repository.ReportRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFarmerRepository(impl: FarmerRepositoryImpl): FarmerRepository = impl

    @Provides
    @Singleton
    fun provideBatchRepository(impl: BatchRepositoryImpl): BatchRepository = impl

    @Provides
    @Singleton
    fun provideBrokerRepository(impl: BrokerRepositoryImpl): BrokerRepository = impl

    @Provides
    @Singleton
    fun provideReceiptRepository(impl: ReceiptRepositoryImpl): ReceiptRepository = impl

    @Provides
    @Singleton
    fun provideReceiptParserRepository(impl: ReceiptParserRepositoryImpl): ReceiptParserRepository = impl

    @Provides
    @Singleton
    fun provideReportRepository(impl: ReportRepositoryImpl): ReportRepository = impl
}