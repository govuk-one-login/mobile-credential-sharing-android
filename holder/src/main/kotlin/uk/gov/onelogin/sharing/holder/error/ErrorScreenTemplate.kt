package uk.gov.onelogin.sharing.holder.error

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreenIcon
import uk.gov.onelogin.sharing.holder.R

@Composable
fun ErrorScreenTemplate(
    modifier: Modifier = Modifier,
    titleText: String,
    body: (LazyListScope.(Dp) -> Unit) = { _ -> },
    onGoBackToDocumentsClick: () -> Unit = {},
) {
    ErrorScreen(
        modifier = modifier,
        icon = { horizontalPadding ->
            GdsIcon(
                image = ImageVector.vectorResource(ErrorScreenIcon.ErrorIcon.icon),
                contentDescription = stringResource(ErrorScreenIcon.ErrorIcon.description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        title = { horizontalPadding ->
            GdsHeading(
                text = titleText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.CenterAligned
            )
        },
        body = body,
        supportingText = null,
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_generic_error_button),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onGoBackToDocumentsClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

internal fun LazyListScope.createErrorBodyText(
    @StringRes resourceId: Int,
    padding: Dp
) {
    item {
        Text(
            stringResource(resourceId),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padding),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
