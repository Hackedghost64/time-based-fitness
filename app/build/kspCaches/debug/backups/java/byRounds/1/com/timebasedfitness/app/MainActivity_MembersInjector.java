package com.timebasedfitness.app;

import com.timebasedfitness.app.data.prefs.OnboardingPrefsRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<OnboardingPrefsRepository> prefsRepositoryProvider;

  public MainActivity_MembersInjector(Provider<OnboardingPrefsRepository> prefsRepositoryProvider) {
    this.prefsRepositoryProvider = prefsRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<OnboardingPrefsRepository> prefsRepositoryProvider) {
    return new MainActivity_MembersInjector(prefsRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectPrefsRepository(instance, prefsRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.timebasedfitness.app.MainActivity.prefsRepository")
  public static void injectPrefsRepository(MainActivity instance,
      OnboardingPrefsRepository prefsRepository) {
    instance.prefsRepository = prefsRepository;
  }
}
