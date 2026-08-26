package uk.gov.onelogin.sharing.testapp.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.internal.shared.CredentialSharingSdkImpl

@Module
@InstallIn(ActivityComponent::class)
object CredentialSharingSdkModule {
    @Provides
    @ActivityScoped
    fun provideCredentialSharingSdk(
        application: Application,
        logger: Logger,
        permissionChecker: PermissionChecker,
        readerAuthCredentialFactory: ReaderAuthCredentialProvider.Factory
    ): CredentialSharingSdk = CredentialSharingSdkImpl(
        applicationContext = application,
        logger = logger,
        permissionChecker = permissionChecker,
        readerAuthCredentialFactory = readerAuthCredentialFactory
    )

    @Provides
    @ActivityScoped
    fun providePresentCredentialSdk(credentialSharingSdk: CredentialSharingSdk) =
        credentialSharingSdk.presentCredentialSdk

    @Provides
    @ActivityScoped
    fun provideVerifyCredentialSdk(credentialSharingSdk: CredentialSharingSdk) =
        credentialSharingSdk.verifyCredentialSdk
}
