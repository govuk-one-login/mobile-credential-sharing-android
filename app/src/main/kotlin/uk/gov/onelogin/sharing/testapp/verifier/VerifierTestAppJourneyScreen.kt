package uk.gov.onelogin.sharing.testapp.verifier

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.sdk.api.verifier.VerificationSession
import uk.gov.onelogin.sharing.ui.impl.VerifyCredential

@Composable
internal fun VerifierTestAppJourneyScreen(
    session: VerificationSession,
    modifier: Modifier = Modifier
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("VerifierTestAppJourneyScreen")
    }

    Surface(modifier = modifier) {
        VerifyCredential(
            session = session,
            modifier = Modifier.fillMaxSize()
        )
    }
}
