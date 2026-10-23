package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

enum class WelshStringsXmlData(val expected: String, @param:StringRes val resourceId: Int) {
    DETAILS_SHARED(
        expected = "Manylion wedi'u rhannu",
        resourceId = R.string.details_shared
    ),
    PERSON_WILL_CHECK_DETAILS_SHARED(
        expected = "Bydd y person yn gwirio'r manylion rydych wedi'u rhannu.",
        resourceId = R.string.person_will_check_details_shared
    ),
    BLUETOOTH_CONNECTION_WILL_END(
        expected = "Bydd eich cysylltiad Bluetooth gyda hwy nawr yn dod i ben.",
        resourceId = R.string.bluetooth_connection_will_end
    ),
    GO_BACK_TO_DOCUMENTS(
        expected = "Ewch yn ôl i dogfennau",
        resourceId = R.string.go_back_to_documents
    )
}
