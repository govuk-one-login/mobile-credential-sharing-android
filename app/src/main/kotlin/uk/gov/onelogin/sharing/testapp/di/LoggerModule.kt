package uk.gov.onelogin.sharing.testapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.impl.v2.AndroidLogger
import uk.gov.onelogin.sharing.testapp.logger.SystemCrashLogger

@InstallIn(SingletonComponent::class)
@Module
class LoggerModule {
    @Provides
    @Singleton
    fun provideLogger(): Logger = AndroidLogger(SystemCrashLogger())
}
