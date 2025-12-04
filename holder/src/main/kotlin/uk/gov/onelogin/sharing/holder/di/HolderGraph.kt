package uk.gov.onelogin.sharing.holder.di

import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import dev.zacsweers.metrox.viewmodel.ViewModelScope

@DependencyGraph(ViewModelScope::class)
interface HolderGraph : ViewModelGraph {
    val context: Context

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): HolderGraph
    }
}
