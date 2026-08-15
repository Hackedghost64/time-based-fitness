package com.timebasedfitness.app.di;

import com.timebasedfitness.app.data.local.CategorySelectionDao;
import com.timebasedfitness.app.data.repository.CategoryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class RepositoryModule_ProvideCategoryRepositoryFactory implements Factory<CategoryRepository> {
  private final Provider<CategorySelectionDao> daoProvider;

  public RepositoryModule_ProvideCategoryRepositoryFactory(
      Provider<CategorySelectionDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public CategoryRepository get() {
    return provideCategoryRepository(daoProvider.get());
  }

  public static RepositoryModule_ProvideCategoryRepositoryFactory create(
      Provider<CategorySelectionDao> daoProvider) {
    return new RepositoryModule_ProvideCategoryRepositoryFactory(daoProvider);
  }

  public static CategoryRepository provideCategoryRepository(CategorySelectionDao dao) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideCategoryRepository(dao));
  }
}
