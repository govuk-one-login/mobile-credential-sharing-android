package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal data class ConnectWithHolderDevicePreviewParameters(
    override val values: Sequence<String> = sequenceOf(
        // valid CBOR
        "vwBjMS4wAZ8B2BhYTL8BAiABIVggk7wmKUmR5q-ozZGB1uPAKfi8upiiA8JC88Ilgg8EaqoiWCA8Qib6bCfaav" +
            "-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8=",
        // invalid CBOR
        "gg8EaqoiWCA8Qib6bCfaav-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap-fAgG_APUB9A" +
            "pQERERESIiMzNERFVVVVVVVf____8="
    )
) : PreviewParameterProvider<String>
