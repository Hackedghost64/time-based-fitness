package com.timebasedfitness.app.data.repository;

import com.timebasedfitness.app.data.local.CompletionLogDao;
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
public final class CompletionRepository_Factory implements Factory<CompletionRepository> {
  private final Provider<CompletionLogDao> daoProvider;

  public CompletionRepository_Factory(Provider<CompletionLogDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public CompletionRepository get() {
    return newInstance(daoProvider.get());
  }

  public static CompletionRepository_Factory create(Provider<CompletionLogDao> daoProvider) {
    return new CompletionRepository_Factory(daoProvider);
  }

  public static CompletionRepository newInstance(CompletionLogDao dao) {
    return new CompletionRepository(dao);
  }
}
