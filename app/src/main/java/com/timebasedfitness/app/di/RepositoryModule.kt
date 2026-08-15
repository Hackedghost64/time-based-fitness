package com.timebasedfitness.app.di

import com.timebasedfitness.app.data.local.CategorySelectionDao
import com.timebasedfitness.app.data.local.CompletionLogDao
import com.timebasedfitness.app.data.repository.CategoryRepository
import com.timebasedfitness.app.data.repository.CompletionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCategoryRepository(dao: CategorySelectionDao): CategoryRepository {
        return CategoryRepository(dao)
    }

    @Provides
    @Singleton
    fun provideCompletionRepository(dao: CompletionLogDao): CompletionRepository {
        return CompletionRepository(dao)
    }
}
