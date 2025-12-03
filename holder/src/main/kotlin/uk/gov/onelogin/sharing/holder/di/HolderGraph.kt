package uk.gov.onelogin.sharing.holder.di

import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionFactory
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeViewModel
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl

@DependencyGraph
interface HolderGraph {
    val context: Context
    val holderViewModel: HolderWelcomeViewModel

    @Provides
    fun providesSessionSecurity(): SessionSecurity = SessionSecurityImpl()

    @Provides
    fun providesEngagementGenerator(): Engagement = EngagementGenerator()

    @Provides
    fun providesMdocBleSession(): MdocSessionManager = MdocSessionFactory.create(context = context)

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): HolderGraph
    }
}
