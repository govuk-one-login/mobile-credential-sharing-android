package uk.gov.onelogin.sharing.testapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.Logger
import uk.gov.logging.impl.v3.LogcatLogger
import uk.gov.logging.impl.v3.MultiLogger
import uk.gov.onelogin.sharing.analytics.SystemAnalyticsLogger

@InstallIn(SingletonComponent::class)
@Module
class LoggerModule {
    @Provides
    @Singleton
    fun provideLogger(): Logger = MultiLogger(
        LogcatLogger()
    )

    @Provides
    @Singleton
    fun provideAnalyticsLogger(logger: Logger): AnalyticsLogger = SystemAnalyticsLogger(logger)
}
