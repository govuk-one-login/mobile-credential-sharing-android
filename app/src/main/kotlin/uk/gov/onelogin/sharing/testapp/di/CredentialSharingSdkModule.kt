package uk.gov.onelogin.sharing.testapp.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.CredentialSharingSdkImpl

@Module
@InstallIn(SingletonComponent::class)
object CredentialSharingSdkModule {
    @Provides
    @Singleton
    fun provideCredentialSharingSdk(
        application: Application,
        logger: Logger
    ): CredentialSharingSdk = CredentialSharingSdkImpl(
        applicationContext = application,
        logger = logger
    )
}
