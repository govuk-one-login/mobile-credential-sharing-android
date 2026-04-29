package uk.gov.onelogin.sharing.core.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.metrics.performance.PerformanceMetricsState

/**
 * Wrapper object for convenience functions relating to the 'JankStats' library provided by
 * `androidx-metrics:metrics-performance`.
 */
object JankStatsHelper {
    /**
     * Used for tracking app state for performance monitoring purposes. Composable functions
     * update the [PerformanceMetricsState.Holder.state] by putting key-value pairs into the holder.
     *
     * A [androidx.metrics.performance.JankStats.OnFrameListener] within consumers then handle
     * the updated state.
     *
     * Internally uses [LocalView] to obtain the current view for the composable.
     *
     * @return An instance of [PerformanceMetricsState.Holder] for use with the current view.
     */
    @Composable
    fun rememberMetricsStateHolder(): PerformanceMetricsState.Holder {
        val view = LocalView.current
        return remember(view) { PerformanceMetricsState.getHolderForHierarchy(view) }
    }
}