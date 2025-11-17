package com.example.tictaclearn.di

import com.example.tictaclearn.data.repository.AIEngineRepositoryImpl
import com.example.tictaclearn.domain.repository.AIEngineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que define cómo inyectar las interfaces de los repositorios a sus implementaciones.
 *
 * Utilizamos @Binds para enlazar la interfaz (abstract fun) con la implementación concreta
 * (el parámetro), ya que la implementación ya tiene un constructor @Inject.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindAiEngineRepository(
        aiEngineRepositoryImpl: AIEngineRepositoryImpl
    ): AIEngineRepository
}