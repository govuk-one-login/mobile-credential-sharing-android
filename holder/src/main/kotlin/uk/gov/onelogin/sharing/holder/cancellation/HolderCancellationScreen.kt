package uk.gov.onelogin.sharing.holder.cancellation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble

@Composable
fun HolderCancellationScreen(
    modifier: Modifier = Modifier,
    onCancelJourney: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacingDouble),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Are you sure you want to cancel?")

        Button(
            onClick = onCancelJourney,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Yes")
        }
        Button(
            onClick = onDismiss,
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("No")
        }
    }
}

@Preview
@Composable
internal fun HolderCancellationScreenPreview() {
    GdsTheme {
        HolderCancellationScreen(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(spacingDouble)
        )
    }
}