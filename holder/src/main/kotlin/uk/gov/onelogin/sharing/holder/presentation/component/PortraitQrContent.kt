package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreen

@Composable
internal fun PortraitQrContent(qrPayload: String?, modifier: Modifier = Modifier) {
    LeftAlignedScreen(
        modifier = modifier,
        title = { padding ->
            QrCodeScreenHeading(Modifier.padding(horizontal = padding))
        },
        body = { padding ->
            item {
                QrCodeScreenImage(
                    qrPayload = qrPayload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                )
            }
            item {
                QrCodeScreenBody(Modifier.padding(horizontal = padding))
            }
        }
    )
}
