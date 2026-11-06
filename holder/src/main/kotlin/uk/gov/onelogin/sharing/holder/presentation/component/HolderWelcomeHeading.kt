package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.onelogin.sharing.holder.R

@Composable
internal fun HolderWelcomeHeading(modifier: Modifier = Modifier) {
    GdsHeading(
        text = stringResource(R.string.holder_welcome_heading),
        modifier = modifier,
        textAlign = GdsHeadingAlignment.LeftAligned
    )
}
