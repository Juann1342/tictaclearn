package com.example.tictaclearn.di

import com.example.tictaclearn.data.repository.AIEngineRepositoryImpl
import com.example.tictaclearn.domain.repository.AIEngineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
// di/RepositoryModule.kt

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Le decimos a Hilt: cuando alguien pida AIEngineRepository, dale AIEngineRepositoryImpl
    @Binds
    @Singleton
    abstract fun bindAIEngineRepository(
        aiEngineRepositoryImpl: AIEngineRepositoryImpl
    ): AIEngineRepository
}