package uk.gov.onelogin.sharing.orchestration.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.orchestration.session.SessionError

@Composable
fun UnrecoverableErrorContent(
    failureState: SessionError,
    modifier: Modifier = Modifier,
    onExitJourney: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacingSingle)
    ) {
        Text(failureState.reason::class.java.simpleName)
        Text(failureState.message)
        Button(
            modifier = Modifier.Companion.fillMaxWidth(),
            onClick = onExitJourney
        ) {
            Text("Exit journey")
        }
    }
}
