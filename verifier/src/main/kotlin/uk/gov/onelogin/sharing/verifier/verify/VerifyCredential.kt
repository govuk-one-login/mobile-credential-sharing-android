package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VerifyCredential(
    modifier: Modifier = Modifier,
) {
    // first check permissions for Bluetooth

    // TODO: then decide which attributes will be requested for verification
    // DCMAW-XXXXX |

    // then display scan screen
    Text("Test")
}