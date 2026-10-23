package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

enum class WelshStringsXmlData(val expected: String, @param:StringRes val resourceId: Int) {
    TURN_ON_BLUETOOTH(
        expected = "Troi Bluetooth ymlaen",
        resourceId = R.string.turn_on_bluetooth
    ),
    TURN_ON_BLUETOOTH_BEFORE_SHARING(
        expected = "Cyn y gallwch rannu, mae angen i chi droi Bluetooth ymlaen.",
        resourceId = R.string.turn_on_bluetooth_before_sharing
    ),
    USE_QR_CODE_TO_CONNECT_NEXT(
        expected = "Ni fyddwch yn cysylltu â dyfais ar hyn o bryd. Byddwch yn defnyddio cod QR i " +
            "wneud hynny nesaf.",
        resourceId = R.string.use_qr_code_to_connect_next
    )
}
