package com.timebasedfitness.app.ui.routine;

import androidx.lifecycle.SavedStateHandle;
import com.timebasedfitness.app.data.content.ContentRepository;
import com.timebasedfitness.app.data.repository.CompletionRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class RoutineDetailViewModel_Factory implements Factory<RoutineDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ContentRepository> contentRepositoryProvider;

  private final Provider<CompletionRepository> completionRepositoryProvider;

  public RoutineDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ContentRepository> contentRepositoryProvider,
      Provider<CompletionRepository> completionRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.contentRepositoryProvider = contentRepositoryProvider;
    this.completionRepositoryProvider = completionRepositoryProvider;
  }

  @Override
  public RoutineDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), contentRepositoryProvider.get(), completionRepositoryProvider.get());
  }

  public static RoutineDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ContentRepository> contentRepositoryProvider,
      Provider<CompletionRepository> completionRepositoryProvider) {
    return new RoutineDetailViewModel_Factory(savedStateHandleProvider, contentRepositoryProvider, completionRepositoryProvider);
  }

  public static RoutineDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      ContentRepository contentRepository, CompletionRepository completionRepository) {
    return new RoutineDetailViewModel(savedStateHandle, contentRepository, completionRepository);
  }
}
