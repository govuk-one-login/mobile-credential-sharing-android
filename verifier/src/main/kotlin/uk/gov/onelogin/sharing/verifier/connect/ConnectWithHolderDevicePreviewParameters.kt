package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal data class ConnectWithHolderDevicePreviewParameters(
    override val values: Sequence<String> =
        sequenceOf(
            VALID_CBOR,
            INVALID_CBOR
        )

) : PreviewParameterProvider<String> {

    companion object {
        private const val INVALID_CBOR =
            "gg8EaqoiWCA8Qib6bCfaav" +
                "-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap" +
                "-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8="
        private const val VALID_CBOR =
            "vwBjMS4wAZ8B2BhYTL8BAiABIVggk7wmKUmR5q" +
                "-ozZGB1uPAKfi8upiiA8JC88Ilgg8EaqoiWCA8Qib6bCfaav" +
                "-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap" +
                "-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8="
    }
}
