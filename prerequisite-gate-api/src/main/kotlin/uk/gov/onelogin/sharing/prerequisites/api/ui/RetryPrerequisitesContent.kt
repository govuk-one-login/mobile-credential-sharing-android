package uk.gov.onelogin.sharing.prerequisites.api.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite

@Composable
fun RetryPrerequisitesContent(
    missingPrerequisites: List<Prerequisite>?,
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        missingPrerequisites?.let { prerequisites ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacingSingle)
            ) {
                Text("Additional actions required for:")
                prerequisites.forEach { prerequisite ->
                    Text(prerequisite.toString())
                }

                Spacer(Modifier.height(spacingSingle))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { coroutineScope.launch { onButtonClick() } }
                ) {
                    Text("Resolve actions")
                }
            }
        } ?: CircularProgressIndicator()
    }
}
