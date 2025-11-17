package uk.gov.onelogin.sharing.testapp

import org.junit.Assert
import org.junit.Test
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination

class MainActivityContentPreviewParametersTest {

    private val parameters = MainActivityContentPreviewParameters().values.toList()

    @Test
    fun parametersHoldDestinationEntriesOnly() {
        Assert.assertEquals(
            PrimaryTabDestination.Companion.entries(),
            parameters
        )
    }

    @Test
    fun sizeMatchesHardcodedValue() {
        Assert.assertEquals(
            PARAMETER_LIST_SIZE,
            parameters.size
        )
    }

    companion object {
        private const val PARAMETER_LIST_SIZE = 2
    }
}
