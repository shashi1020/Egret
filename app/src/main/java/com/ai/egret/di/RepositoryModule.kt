package com.ai.egret.di


import com.ai.egret.repository.FarmInsightsRepository
import com.ai.egret.repository.FarmInsightsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFarmInsightsRepository(
        impl: FarmInsightsRepositoryImpl
    ): FarmInsightsRepository
}