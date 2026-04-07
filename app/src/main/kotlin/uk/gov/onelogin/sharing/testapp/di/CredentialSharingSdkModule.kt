package uk.gov.onelogin.sharing.testapp.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2
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
        permissionChecker: PermissionCheckerV2
    ): CredentialSharingSdk = CredentialSharingSdkImpl(
        applicationContext = application,
        logger = logger,
        permissionChecker = permissionChecker
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
