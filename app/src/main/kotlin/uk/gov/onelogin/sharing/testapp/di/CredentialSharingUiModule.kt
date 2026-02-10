package uk.gov.onelogin.sharing.testapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uk.gov.onelogin.sharing.ui.api.CredentialSharingUi
import uk.gov.onelogin.sharing.ui.impl.CredentialSharingUiImpl

@Module
@InstallIn(SingletonComponent::class)
object CredentialSharingUiModule {
    @Provides
    @Singleton
    fun provideCredentialSharingUi(): CredentialSharingUi = CredentialSharingUiImpl()
}
