package com.timebasedfitness.app.ui.settings;

import com.timebasedfitness.app.data.repository.CategoryRepository;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<CategoryRepository> categoryRepositoryProvider;

  public SettingsViewModel_Factory(Provider<CategoryRepository> categoryRepositoryProvider) {
    this.categoryRepositoryProvider = categoryRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(categoryRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<CategoryRepository> categoryRepositoryProvider) {
    return new SettingsViewModel_Factory(categoryRepositoryProvider);
  }

  public static SettingsViewModel newInstance(CategoryRepository categoryRepository) {
    return new SettingsViewModel(categoryRepository);
  }
}
