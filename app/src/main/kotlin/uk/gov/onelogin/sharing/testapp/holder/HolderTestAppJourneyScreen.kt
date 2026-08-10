package uk.gov.onelogin.sharing.testapp.holder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.sdk.api.presenter.SharingSession
import uk.gov.onelogin.sharing.ui.impl.ShareCredential

/**
 * Suppresses `ktlint:compose:vm-forwarding-check` due to the naming convention of the
 * [SharingSession].
 */
@Composable
@Suppress("ktlint:compose:vm-forwarding-check")
internal fun HolderTestAppJourneyScreen(
    session: SharingSession,
    modifier: Modifier = Modifier
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderTestAppJourneyScreen")
    }

    ShareCredential(
        session = session,
        modifier = modifier
    )
}
