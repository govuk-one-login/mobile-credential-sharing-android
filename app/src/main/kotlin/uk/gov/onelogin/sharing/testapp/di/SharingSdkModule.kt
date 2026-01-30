package uk.gov.onelogin.sharing.testapp.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.SharingSdk
import uk.gov.onelogin.sharing.SharingSdkImpl

@Module
@InstallIn(SingletonComponent::class)
object SharingSdkModule {
    @Provides
    @Singleton
    fun provideSharingSdk(application: Application, logger: Logger): SharingSdk = SharingSdkImpl(
        applicationContext = application,
        logger = logger
    )
}
