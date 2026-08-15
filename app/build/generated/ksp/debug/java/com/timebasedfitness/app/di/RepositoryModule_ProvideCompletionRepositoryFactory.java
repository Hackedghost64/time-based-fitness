package com.timebasedfitness.app.di;

import com.timebasedfitness.app.data.local.CompletionLogDao;
import com.timebasedfitness.app.data.repository.CompletionRepository;
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
public final class RepositoryModule_ProvideCompletionRepositoryFactory implements Factory<CompletionRepository> {
  private final Provider<CompletionLogDao> daoProvider;

  public RepositoryModule_ProvideCompletionRepositoryFactory(
      Provider<CompletionLogDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public CompletionRepository get() {
    return provideCompletionRepository(daoProvider.get());
  }

  public static RepositoryModule_ProvideCompletionRepositoryFactory create(
      Provider<CompletionLogDao> daoProvider) {
    return new RepositoryModule_ProvideCompletionRepositoryFactory(daoProvider);
  }

  public static CompletionRepository provideCompletionRepository(CompletionLogDao dao) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideCompletionRepository(dao));
  }
}
