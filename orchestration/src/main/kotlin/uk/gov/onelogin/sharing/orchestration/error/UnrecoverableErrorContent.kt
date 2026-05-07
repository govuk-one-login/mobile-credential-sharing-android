package uk.gov.onelogin.sharing.orchestration.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.orchestration.session.SessionError

@Composable
fun UnrecoverableErrorContent(
    failureState: SessionError,
    modifier: Modifier = Modifier,
    onExitJourney: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacingSingle)
    ) {
        Text(failureState.reason::class.java.simpleName)
        Text(failureState.message)
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { coroutineScope.launch { onExitJourney() } }
        ) {
            Text("Exit journey")
        }
    }
}
