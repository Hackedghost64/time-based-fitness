package com.timebasedfitness.app.di;

import com.timebasedfitness.app.data.local.AppDatabase;
import com.timebasedfitness.app.data.local.CompletionLogDao;
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
public final class DatabaseModule_ProvideCompletionLogDaoFactory implements Factory<CompletionLogDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideCompletionLogDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CompletionLogDao get() {
    return provideCompletionLogDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideCompletionLogDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideCompletionLogDaoFactory(dbProvider);
  }

  public static CompletionLogDao provideCompletionLogDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCompletionLogDao(db));
  }
}
