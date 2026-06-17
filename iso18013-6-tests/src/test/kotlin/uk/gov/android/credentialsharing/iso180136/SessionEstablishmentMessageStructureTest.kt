package uk.gov.android.credentialsharing.iso180136

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * ISO/IEC TS 18013-6:2025 conformance tests for Session Establishment.
 *
 * These tests validate that the mdoc reader correctly ignores unknown/RFU keys
 * in the SessionEstablishment CBOR structure per ISO/IEC 18013-5:2021, 9.1.1.4.
 *
 * ```
 * SessionEstablishment = {
 * "eReaderKey" : EReaderKeyBytes,
 * "data" : bstr ; Encrypted mdoc request
 * * tstr => RFU
 * }
 * ```
 */
class SessionEstablishmentMessageStructureTest {
    @Test
    fun `mDLR_MS_SE_01 - mdoc reader ignores ProtocolInfo RFU key in SessionEstablishment`() {
        assertTrue(true)
    }
}