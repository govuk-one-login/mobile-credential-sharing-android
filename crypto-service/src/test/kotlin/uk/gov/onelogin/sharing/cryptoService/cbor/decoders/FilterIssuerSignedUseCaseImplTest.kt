package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import kotlin.test.assertFailsWith
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.FilterIssuerSignedUseCaseImpl
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.NoMatchingAttributesException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.ParsedRawCredential
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import java.io.ByteArrayOutputStream
import kotlin.collections.iterator

class FilterIssuerSignedUseCaseImplTest {

    private val logger = object : Logger {
        override fun debug(tag: String, message: String) = Unit
        override fun error(tag: String, message: String, throwable: Throwable?) = Unit
        override fun info(tag: String, message: String) = Unit
        override fun warn(tag: String, message: String) = Unit
    }

    private val useCase = FilterIssuerSignedUseCaseImpl(logger)
    private val cborMapper = ObjectMapper(CBORFactory())

    private val docType = "org.iso.18013.5.1.mDL"
    private val namespace = "org.iso.18013.5.1"
    private val gbNamespace = "org.iso.18013.5.1.GB"
    private val issuerAuth = byteArrayOf(0x01)

    private fun buildItemBytes(
        digestId: Long,
        elementIdentifier: String,
        elementValue: Any
    ): ByteArray = cborMapper.writeValueAsBytes(
        mapOf(
            "digestID" to digestId,
            "random" to byteArrayOf(0x01),
            "elementIdentifier" to elementIdentifier,
            "elementValue" to elementValue
        )
    )

    private fun buildNameSpacesBytes(nameSpaces: Map<String, List<ByteArray>>): ByteArray {
        val out = ByteArrayOutputStream()
        val gen = cborMapper.factory.createGenerator(out)
        gen.writeStartObject()
        for ((ns, items) in nameSpaces) {
            gen.writeFieldName(ns)
            gen.writeStartArray()
            for (item in items) gen.writeBinary(item)
            gen.writeEndArray()
        }
        gen.writeEndObject()
        gen.close()
        return out.toByteArray()
    }

    private fun deviceRequest(nameSpaces: Map<String, Map<String, Boolean>>) = DeviceRequest(
        version = "1.0",
        docRequests = listOf(DocRequest(ItemsRequest(docType = docType, nameSpaces = nameSpaces)))
    )

    private fun parsedCredential(nameSpacesBytes: ByteArray) = ParsedRawCredential(
        nameSpaces = nameSpacesBytes,
        issuerAuth = issuerAuth,
        msoDocType = docType
    )

    @Test
    fun `filter retains only requested elements from single namespace`() {
        val familyNameBytes = buildItemBytes(0, "family_name", "Smith")
        val givenNameBytes = buildItemBytes(1, "given_name", "John")
        val birthDateBytes = buildItemBytes(2, "birth_date", "1990-01-01")

        val credentialBytes = buildNameSpacesBytes(
            mapOf(namespace to listOf(familyNameBytes, givenNameBytes, birthDateBytes))
        )
        val request = deviceRequest(
            mapOf(namespace to mapOf("family_name" to true, "given_name" to true))
        )

        val result = useCase.filter(parsedCredential(credentialBytes), request)

        val items = result.nameSpaces!![namespace]!!
        assertEquals(2, items.size)
        assertArrayEquals(familyNameBytes, items[0])
        assertArrayEquals(givenNameBytes, items[1])
    }

    @Test
    fun `filter retains elements from multiple namespaces separately`() {
        val familyNameBytes = buildItemBytes(0, "family_name", "Smith")
        val drivingPrivilegesBytes = buildItemBytes(0, "driving_privileges", "B")

        val credentialBytes = buildNameSpacesBytes(
            mapOf(
                namespace to listOf(familyNameBytes),
                gbNamespace to listOf(drivingPrivilegesBytes)
            )
        )
        val request = deviceRequest(
            mapOf(
                namespace to mapOf("family_name" to true),
                gbNamespace to mapOf("driving_privileges" to true)
            )
        )

        val result = useCase.filter(parsedCredential(credentialBytes), request)

        assertEquals(2, result.nameSpaces!!.size)
        assertArrayEquals(familyNameBytes, result.nameSpaces[namespace]!![0])
        assertArrayEquals(drivingPrivilegesBytes, result.nameSpaces[gbNamespace]!![0])
    }

    @Test
    fun `filter preserves issuerAuth bytes unchanged`() {
        val itemBytes = buildItemBytes(0, "family_name", "Smith")
        val credentialBytes = buildNameSpacesBytes(mapOf(namespace to listOf(itemBytes)))
        val request = deviceRequest(mapOf(namespace to mapOf("family_name" to true)))

        val result = useCase.filter(parsedCredential(credentialBytes), request)

        assertArrayEquals(issuerAuth, result.issuerAuth)
    }

    @Test
    fun `filter throws when no requested namespace exists in credential`() {
        val itemBytes = buildItemBytes(0, "family_name", "Smith")
        val credentialBytes = buildNameSpacesBytes(mapOf(namespace to listOf(itemBytes)))
        val request = deviceRequest(mapOf("org.iso.18013.5.1.OTHER" to mapOf("family_name" to true)))

        val ex = assertFailsWith<NoMatchingAttributesException> {
            useCase.filter(parsedCredential(credentialBytes), request)
        }
        assertTrue(ex.message!!.contains("no matching NameSpaces"))
    }

    @Test
    fun `filter throws when namespace matches but no elements match`() {
        val itemBytes = buildItemBytes(0, "family_name", "Smith")
        val credentialBytes = buildNameSpacesBytes(mapOf(namespace to listOf(itemBytes)))
        val request = deviceRequest(mapOf(namespace to mapOf("portrait" to true)))

        val ex = assertFailsWith<NoMatchingAttributesException> {
            useCase.filter(parsedCredential(credentialBytes), request)
        }
        assertTrue(ex.message!!.contains("no matching attributes"))
    }

    @Test
    fun `filter returns closest TRUE age_over when requested age is 19`() {
        val age18Bytes = buildItemBytes(0, "age_over_18", true)
        val age21Bytes = buildItemBytes(1, "age_over_21", true)
        val age25Bytes = buildItemBytes(2, "age_over_25", false)

        val credentialBytes = buildNameSpacesBytes(
            mapOf(namespace to listOf(age18Bytes, age21Bytes, age25Bytes))
        )
        val request = deviceRequest(mapOf(namespace to mapOf("age_over_19" to true)))

        val result = useCase.filter(parsedCredential(credentialBytes), request)

        val items = result.nameSpaces!![namespace]!!
        assertEquals(1, items.size)
        assertArrayEquals(age21Bytes, items[0])
    }

    @Test
    fun `filter returns closest FALSE age_over when no TRUE exists for requested age 23`() {
        val age18Bytes = buildItemBytes(0, "age_over_18", true)
        val age21Bytes = buildItemBytes(1, "age_over_21", false)
        val age25Bytes = buildItemBytes(2, "age_over_25", false)

        val credentialBytes = buildNameSpacesBytes(
            mapOf(namespace to listOf(age18Bytes, age21Bytes, age25Bytes))
        )
        val request = deviceRequest(mapOf(namespace to mapOf("age_over_23" to true)))

        val result = useCase.filter(parsedCredential(credentialBytes), request)

        val items = result.nameSpaces!![namespace]!!
        assertEquals(1, items.size)
        assertArrayEquals(age21Bytes, items[0])
    }

    @Test
    fun `filter returns no age element when gap scenario produces no match`() {
        val age18Bytes = buildItemBytes(0, "age_over_18", true)
        val age21Bytes = buildItemBytes(1, "age_over_21", false)
        val age25Bytes = buildItemBytes(2, "age_over_25", false)

        val credentialBytes = buildNameSpacesBytes(
            mapOf(namespace to listOf(age18Bytes, age21Bytes, age25Bytes))
        )

        val request = deviceRequest(mapOf(namespace to mapOf("age_over_20" to true)))

        val ex = assertFailsWith<NoMatchingAttributesException> {
            useCase.filter(parsedCredential(credentialBytes), request)
        }
        assertTrue(ex.message!!.contains("no matching attributes"))
    }

    @Test
    fun `filter drops age_over with 1-digit number`() {
        val credentialBytes = buildNameSpacesBytes(
            mapOf(namespace to listOf(buildItemBytes(0, "family_name", "Smith")))
        )
        val request = deviceRequest(mapOf(namespace to mapOf("age_over_1" to true)))

        val ex = assertFailsWith<NoMatchingAttributesException> {
            useCase.filter(parsedCredential(credentialBytes), request)
        }
        assertTrue(ex.message!!.contains("no matching attributes"))
    }

    @Test
    fun `filter drops age_over with 3-digit number`() {
        val credentialBytes = buildNameSpacesBytes(
            mapOf(namespace to listOf(buildItemBytes(0, "family_name", "Smith")))
        )
        val request = deviceRequest(mapOf(namespace to mapOf("age_over_100" to true)))

        val ex = assertFailsWith<NoMatchingAttributesException> {
            useCase.filter(parsedCredential(credentialBytes), request)
        }
        assertTrue(ex.message!!.contains("no matching attributes"))
    }
}
