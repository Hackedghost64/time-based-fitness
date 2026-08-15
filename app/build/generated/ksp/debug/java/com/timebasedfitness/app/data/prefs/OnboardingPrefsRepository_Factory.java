package com.timebasedfitness.app.data.prefs;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class OnboardingPrefsRepository_Factory implements Factory<OnboardingPrefsRepository> {
  private final Provider<Context> contextProvider;

  public OnboardingPrefsRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public OnboardingPrefsRepository get() {
    return newInstance(contextProvider.get());
  }

  public static OnboardingPrefsRepository_Factory create(Provider<Context> contextProvider) {
    return new OnboardingPrefsRepository_Factory(contextProvider);
  }

  public static OnboardingPrefsRepository newInstance(Context context) {
    return new OnboardingPrefsRepository(context);
  }
}
