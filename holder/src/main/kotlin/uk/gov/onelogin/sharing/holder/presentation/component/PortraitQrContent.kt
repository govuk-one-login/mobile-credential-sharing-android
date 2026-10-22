package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreen

@Composable
internal fun PortraitQrContent(qrData: String, modifier: Modifier = Modifier) {
    LeftAlignedScreen(
        modifier = modifier,
        title = { padding ->
            HolderWelcomeHeading(Modifier.padding(horizontal = padding))
        },
        body = { padding ->
            item {
                HolderWelcomeQrCode(
                    qrData = qrData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                )
            }
            item {
                HolderWelcomeBody(Modifier.padding(horizontal = padding))
            }
        }
    )
}
