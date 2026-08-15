package com.timebasedfitness.app.ui.onboarding;

import com.timebasedfitness.app.data.prefs.OnboardingPrefsRepository;
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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<OnboardingPrefsRepository> prefsRepositoryProvider;

  public OnboardingViewModel_Factory(Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<OnboardingPrefsRepository> prefsRepositoryProvider) {
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.prefsRepositoryProvider = prefsRepositoryProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(categoryRepositoryProvider.get(), prefsRepositoryProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<OnboardingPrefsRepository> prefsRepositoryProvider) {
    return new OnboardingViewModel_Factory(categoryRepositoryProvider, prefsRepositoryProvider);
  }

  public static OnboardingViewModel newInstance(CategoryRepository categoryRepository,
      OnboardingPrefsRepository prefsRepository) {
    return new OnboardingViewModel(categoryRepository, prefsRepository);
  }
}
