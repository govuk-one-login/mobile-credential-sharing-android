package uk.gov.onelogin.sharing.verification.reader

import android.net.Uri
import io.github.classgraph.ClassInfo
import io.mockk.mockk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

class ReaderAuthenticationOutcomeTest {

    @Test
    fun `ReaderAuthenticationOutcome has expected inheritors`() {
        val expectedInheritors = setOf(
            "Success",
            "Unfulfillable"
        )

        val classInfo =
            scanResult.getClassesImplementing(ReaderAuthenticationOutcome::class.java.name)

        assertThat(
            classInfo.map(ClassInfo::getSimpleName).toSet(),
            equalTo(expectedInheritors)
        )
    }

    @Test
    fun `Success outcome supports equality and copy`() {
        val uri: Uri = mockk()
        val docRequest1 = DocRequest(
            itemsRequest = ItemsRequest(
                docType = "org.iso.18013.5.1.mDL",
                nameSpaces = emptyMap()
            )
        )
        val docRequest2 = DocRequest(
            itemsRequest = ItemsRequest(
                docType = "org.iso.18013.5.1.aamva",
                nameSpaces = emptyMap()
            )
        )

        val req1 = AuthenticatedReaderRequest(docRequest = docRequest1, privacyPolicyUrl = uri)
        val req2 = AuthenticatedReaderRequest(docRequest = docRequest2, privacyPolicyUrl = uri)

        val success1 = ReaderAuthenticationOutcome.Success(req1)
        val success2 = ReaderAuthenticationOutcome.Success(req1)
        val success3 = ReaderAuthenticationOutcome.Success(req2)

        assertEquals(req1, success1.authenticatedReaderRequest)
        assertEquals(success1, success2)
        assertEquals(success1.hashCode(), success2.hashCode())
        assertNotEquals(success1, success3)

        val copy = success1.copy()
        assertEquals(success1, copy)
    }
}
