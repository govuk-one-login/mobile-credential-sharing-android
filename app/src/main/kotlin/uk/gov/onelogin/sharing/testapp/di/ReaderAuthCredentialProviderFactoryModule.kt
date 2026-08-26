package uk.gov.onelogin.sharing.testapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.TestAppReaderAuthCredentialProviderFactory

@InstallIn(SingletonComponent::class)
@Module
object ReaderAuthCredentialProviderFactoryModule {
    @Provides
    @Singleton
    fun providesReaderAuthCredentialFactory(
        factory: TestAppReaderAuthCredentialProviderFactory
    ): ReaderAuthCredentialProvider.Factory = factory
}