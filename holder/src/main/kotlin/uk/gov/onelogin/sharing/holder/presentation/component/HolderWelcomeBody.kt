package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.holder.R

private val paragraphs = listOf(
    R.string.holder_welcome_body_1,
    R.string.holder_welcome_body_2,
    R.string.holder_welcome_body_3
)

@Composable
internal fun HolderWelcomeBody(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacingDouble)
    ) {
        paragraphs.forEach {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
