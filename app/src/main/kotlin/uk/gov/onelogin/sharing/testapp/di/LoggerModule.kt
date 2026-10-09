package uk.gov.onelogin.sharing.testapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.v2.CrashLogger
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.impl.v2.AndroidLogger
import uk.gov.onelogin.sharing.analytics.SystemAnalyticsLogger
import uk.gov.onelogin.sharing.testapp.logger.SystemCrashLogger

@InstallIn(SingletonComponent::class)
@Module
class LoggerModule {
    @Provides
    @Singleton
    fun provideCrashLogger(): CrashLogger = SystemCrashLogger()

    @Provides
    @Singleton
    fun provideLogger(logger: CrashLogger): Logger = AndroidLogger(logger)

    @Provides
    @Singleton
    fun provideAnalyticsLogger(logger: Logger, crashLogger: CrashLogger): AnalyticsLogger =
        SystemAnalyticsLogger(logger = logger, crashLogger = crashLogger)
}
