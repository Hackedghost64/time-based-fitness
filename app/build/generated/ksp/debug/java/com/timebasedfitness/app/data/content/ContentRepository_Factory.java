package com.timebasedfitness.app.data.content;

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
public final class ContentRepository_Factory implements Factory<ContentRepository> {
  private final Provider<Context> contextProvider;

  public ContentRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ContentRepository get() {
    return newInstance(contextProvider.get());
  }

  public static ContentRepository_Factory create(Provider<Context> contextProvider) {
    return new ContentRepository_Factory(contextProvider);
  }

  public static ContentRepository newInstance(Context context) {
    return new ContentRepository(context);
  }
}
