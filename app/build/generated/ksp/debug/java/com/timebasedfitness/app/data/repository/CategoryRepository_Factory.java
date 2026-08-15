package com.timebasedfitness.app.data.repository;

import com.timebasedfitness.app.data.local.CategorySelectionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class CategoryRepository_Factory implements Factory<CategoryRepository> {
  private final Provider<CategorySelectionDao> daoProvider;

  public CategoryRepository_Factory(Provider<CategorySelectionDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public CategoryRepository get() {
    return newInstance(daoProvider.get());
  }

  public static CategoryRepository_Factory create(Provider<CategorySelectionDao> daoProvider) {
    return new CategoryRepository_Factory(daoProvider);
  }

  public static CategoryRepository newInstance(CategorySelectionDao dao) {
    return new CategoryRepository(dao);
  }
}
