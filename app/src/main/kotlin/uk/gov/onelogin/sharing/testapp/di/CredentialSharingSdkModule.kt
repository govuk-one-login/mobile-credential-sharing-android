package uk.gov.onelogin.sharing.testapp.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.internal.shared.CredentialSharingSdkImpl

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

    @Provides
    @Singleton
    fun providePresentCredentialSdk(credentialSharingSdk: CredentialSharingSdk) =
        credentialSharingSdk.presentCredentialSdk

    @Provides
    @Singleton
    fun provideVerifyCredentialSdk(credentialSharingSdk: CredentialSharingSdk) =
        credentialSharingSdk.verifyCredentialSdk
}
