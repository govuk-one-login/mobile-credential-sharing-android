package uk.gov.onelogin.sharing.holder.di

import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.bluetooth.internal.central.AndroidGattWriter
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattWriter
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

@DependencyGraph(ViewModelScope::class)
interface HolderGraph : ViewModelGraph {
    val context: Context

    @Provides
    fun provideGattWriter(): GattWriter = AndroidGattWriter()

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Includes appGraph: CredentialSharingAppGraph): HolderGraph
    }
}
