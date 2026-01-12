package uk.gov.onelogin.sharing.verifier.connect.error

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreenIcon
import uk.gov.onelogin.sharing.verifier.R

@Composable
fun BluetoothConnectionErrorScreen(
    title: String,
    modifier: Modifier = Modifier,
    onTryAgainClick: () -> Unit = {}
) {
    ErrorScreen(
        modifier = modifier.fillMaxWidth(),
        icon = { horizontalPadding ->
            GdsIcon(
                image = ImageVector.vectorResource(ErrorScreenIcon.ErrorIcon.icon),
                contentDescription = stringResource(ErrorScreenIcon.ErrorIcon.description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                color = colorScheme.onBackground
            )
        },
        title = { horizontalPadding ->
            GdsHeading(
                text = title,
                modifier = Modifier
                    .padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.CenterAligned
            )
        },
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.bluetooth_connection_error_try_again),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onTryAgainClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
