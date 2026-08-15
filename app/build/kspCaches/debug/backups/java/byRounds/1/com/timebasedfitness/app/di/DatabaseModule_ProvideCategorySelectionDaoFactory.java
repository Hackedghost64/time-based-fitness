package com.timebasedfitness.app.di;

import com.timebasedfitness.app.data.local.AppDatabase;
import com.timebasedfitness.app.data.local.CategorySelectionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideCategorySelectionDaoFactory implements Factory<CategorySelectionDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideCategorySelectionDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CategorySelectionDao get() {
    return provideCategorySelectionDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideCategorySelectionDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideCategorySelectionDaoFactory(dbProvider);
  }

  public static CategorySelectionDao provideCategorySelectionDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCategorySelectionDao(db));
  }
}
