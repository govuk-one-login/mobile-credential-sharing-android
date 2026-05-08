package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder

@OptIn(ExperimentalPermissionsApi::class, UnstableDesignSystemAPI::class)
@Composable
internal fun VerifierPrerequisitesScreen(modifier: Modifier = Modifier) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("VerifierPrerequisitesScreen")
    }

    CircularProgressIndicator(
        modifier = modifier
            .then(Modifier.testTag("progressIndicator"))
    )
}
