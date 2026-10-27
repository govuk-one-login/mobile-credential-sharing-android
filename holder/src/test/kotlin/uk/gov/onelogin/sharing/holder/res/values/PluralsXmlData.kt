package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.PluralsRes
import uk.gov.onelogin.sharing.holder.R

/**
 * Test data used with [StringsXmlTest] for plural resource IDs.
 *
 * Ensures that tests would fail when plurals update without updating this test data.
 *
 * [quantity] is the count passed to `getQuantityString` for both selection and `%1$d`
 * substitution, and [documentName] is substituted for `%2$s`.
 */
enum class PluralsXmlData(
    val expected: String,
    @param:PluralsRes val resourceId: Int,
    val quantity: Int,
    val documentName: String
) {
    CONSENT_CARD_HEADING_ONE(
        expected = "You\u2019ll share 1 detail from your UK driving licence",
        resourceId = R.plurals.consent_card_heading,
        quantity = 1,
        documentName = "UK driving licence"
    ),
    CONSENT_CARD_HEADING_OTHER(
        expected = "You\u2019ll share 5 details from your UK driving licence",
        resourceId = R.plurals.consent_card_heading,
        quantity = 5,
        documentName = "UK driving licence"
    ),
    CONSENT_CARD_HEADING_EMPHASIS_ONE(
        expected = "1 detail",
        resourceId = R.plurals.consent_card_heading_emphasis,
        quantity = 1,
        documentName = "UK driving licence"
    ),
    CONSENT_CARD_HEADING_EMPHASIS_OTHER(
        expected = "5 details",
        resourceId = R.plurals.consent_card_heading_emphasis,
        quantity = 5,
        documentName = "UK driving licence"
    )
}

/**
 * Welsh (cy) translations of the plural consent-card resources.
 */
enum class WelshPluralsXmlData(
    val expected: String,
    @param:PluralsRes val resourceId: Int,
    val quantity: Int,
    val documentName: String
) {
    CONSENT_CARD_HEADING_ZERO(
        expected = "Byddwch yn rhannu 0 manylion o\u2019ch trwydded yrru\u2019r DU",
        resourceId = R.plurals.consent_card_heading,
        quantity = 0,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_ONE(
        expected = "Byddwch yn rhannu 1 manylyn o\u2019ch trwydded yrru\u2019r DU",
        resourceId = R.plurals.consent_card_heading,
        quantity = 1,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_TWO(
        expected = "Byddwch yn rhannu manylion 2 o\u2019ch trwydded yrru\u2019r DU",
        resourceId = R.plurals.consent_card_heading,
        quantity = 2,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_FEW(
        expected = "Byddwch yn rhannu manylion 3 o\u2019ch trwydded yrru\u2019r DU",
        resourceId = R.plurals.consent_card_heading,
        quantity = 3,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_MANY(
        expected = "Byddwch yn rhannu manylion 6 o\u2019ch trwydded yrru\u2019r DU",
        resourceId = R.plurals.consent_card_heading,
        quantity = 6,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_OTHER(
        expected = "Byddwch yn rhannu manylion 5 o\u2019ch trwydded yrru\u2019r DU",
        resourceId = R.plurals.consent_card_heading,
        quantity = 5,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_EMPHASIS_ZERO(
        expected = "manylion 0",
        resourceId = R.plurals.consent_card_heading_emphasis,
        quantity = 0,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_EMPHASIS_ONE(
        expected = "manylion 1",
        resourceId = R.plurals.consent_card_heading_emphasis,
        quantity = 1,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_EMPHASIS_TWO(
        expected = "manylion 2",
        resourceId = R.plurals.consent_card_heading_emphasis,
        quantity = 2,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_EMPHASIS_FEW(
        expected = "manylion 3",
        resourceId = R.plurals.consent_card_heading_emphasis,
        quantity = 3,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_EMPHASIS_MANY(
        expected = "manylion 6",
        resourceId = R.plurals.consent_card_heading_emphasis,
        quantity = 6,
        documentName = "trwydded yrru\u2019r DU"
    ),
    CONSENT_CARD_HEADING_EMPHASIS_OTHER(
        expected = "manylion 5",
        resourceId = R.plurals.consent_card_heading_emphasis,
        quantity = 5,
        documentName = "trwydded yrru\u2019r DU"
    )
}
