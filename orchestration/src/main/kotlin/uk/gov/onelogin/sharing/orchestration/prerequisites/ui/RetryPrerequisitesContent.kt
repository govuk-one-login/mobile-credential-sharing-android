package uk.gov.onelogin.sharing.orchestration.prerequisites.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite

@Composable
fun RetryPrerequisitesContent(
    missingPrerequisites: List<Prerequisite>?,
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        missingPrerequisites?.let { prerequisites ->
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacingSingle)
            ) {
                Text("Additional actions required for:")
                prerequisites.forEach { prerequisite ->
                    Text(prerequisite.toString())
                }

                Spacer(Modifier.Companion.height(spacingSingle))

                Button(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    onClick = onButtonClick
                ) {
                    Text("Resolve actions")
                }
            }
        } ?: CircularProgressIndicator()
    }
}
