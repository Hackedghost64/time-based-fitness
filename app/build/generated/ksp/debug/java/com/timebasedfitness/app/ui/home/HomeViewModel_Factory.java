package com.timebasedfitness.app.ui.home;

import com.timebasedfitness.app.data.repository.CategoryRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<CompletionRepository> completionRepositoryProvider;

  public HomeViewModel_Factory(Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<CompletionRepository> completionRepositoryProvider) {
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.completionRepositoryProvider = completionRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(categoryRepositoryProvider.get(), completionRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<CompletionRepository> completionRepositoryProvider) {
    return new HomeViewModel_Factory(categoryRepositoryProvider, completionRepositoryProvider);
  }

  public static HomeViewModel newInstance(CategoryRepository categoryRepository,
      CompletionRepository completionRepository) {
    return new HomeViewModel(categoryRepository, completionRepository);
  }
}
