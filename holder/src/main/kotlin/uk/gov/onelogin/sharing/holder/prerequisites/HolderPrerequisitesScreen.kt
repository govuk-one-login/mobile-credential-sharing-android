package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.gov.android.ui.theme.m3.GdsTheme
@Composable
internal fun HolderPrerequisitesScreen(
    modifier: Modifier = Modifier,
) {
    HolderPrerequisitesContent(
        modifier = modifier
    )
}

@Composable
internal fun HolderPrerequisitesContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column {
            CircularProgressIndicator()
        }
    }
}

@Composable
@Preview(showBackground = true)
internal fun HolderPrerequisitesScreenPreview() {
    GdsTheme {
        HolderPrerequisitesContent(
            modifier = Modifier.fillMaxSize()
        )
    }
}