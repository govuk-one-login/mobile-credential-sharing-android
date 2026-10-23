package uk.gov.onelogin.sharing.holder.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

/**
 * Test data used with [StringsXmlTest] for the Welsh (cy) translations of the consent-card
 * master string resources.
 *
 * Ensures that tests would fail when the Welsh strings update without updating this test data.
 */
enum class WelshStringsXmlData(val expected: String, @param:StringRes val resourceId: Int) {
    CONSENT_CARD_HEADING(
        expected = "Byddwch yn rhannu manylion %1\$d o\u2019ch %2\$s",
        resourceId = R.string.consent_card_heading
    ),
    CONSENT_CARD_WILL_KEEP(
        expected = "Bydd %1\$s yn cadw\u2019r manylion hyn fel cofnod o\u2019r gwiriad hwn:",
        resourceId = R.string.consent_card_will_keep
    ),
    CONSENT_CARD_WILL_NOT_KEEP(
        expected = "Ni fydd %1\$s yn cadw:",
        resourceId = R.string.consent_card_will_not_keep
    ),
    CONSENT_CARD_ALL_KEPT(
        expected = "Bydd %1\$s yn cadw\u2019r manylion hyn fel cofnod o\u2019r gwiriad hwn.",
        resourceId = R.string.consent_card_all_kept
    ),
    CONSENT_CARD_NONE_KEPT(
        expected =
            "Dim ond ar gyfer y gwiriad hwn y defnyddir y manylion hyn. " +
                "Ni fyddant yn cael eu harbed.",
        resourceId = R.string.consent_card_none_kept
    ),
    CONSENT_DOC_MDL(
        expected = "trwydded yrru\u2019r DU",
        resourceId = R.string.consent_doc_mdl
    ),
    CONSENT_ATTR_DOCUMENT_NUMBER(
        expected = "Rhif y drwydded",
        resourceId = R.string.consent_attr_document_number
    ),
    CONSENT_ATTR_PORTRAIT(
        expected = "Llun",
        resourceId = R.string.consent_attr_portrait
    ),
    CONSENT_ATTR_TITLE(
        expected = "Teitl",
        resourceId = R.string.consent_attr_title
    ),
    CONSENT_ATTR_FAMILY_NAME(
        expected = "Cyfenw",
        resourceId = R.string.consent_attr_family_name
    ),
    CONSENT_ATTR_GIVEN_NAME(
        expected = "Enw(au) cyntaf",
        resourceId = R.string.consent_attr_given_name
    ),
    CONSENT_ATTR_EXPIRY_DATE(
        expected = "Dyddiad daw i ben",
        resourceId = R.string.consent_attr_expiry_date
    ),
    CONSENT_ATTR_BIRTH_DATE(
        expected = "Dyddiad geni",
        resourceId = R.string.consent_attr_birth_date
    ),
    CONSENT_ATTR_AGE_OVER(
        expected = "Os ydych dros %1\$d",
        resourceId = R.string.consent_attr_age_over
    ),
    CONSENT_ATTR_BIRTH_PLACE(
        expected = "Lleoliad geni",
        resourceId = R.string.consent_attr_birth_place
    ),
    CONSENT_ATTR_RESIDENT_ADDRESS(
        expected = "Cyfeiriad stryd",
        resourceId = R.string.consent_attr_resident_address
    ),
    CONSENT_ATTR_RESIDENT_CITY(
        expected = "Tref neu ddinas (cyfeiriad)",
        resourceId = R.string.consent_attr_resident_city
    ),
    CONSENT_ATTR_RESIDENT_POSTAL_CODE(
        expected = "Cod post",
        resourceId = R.string.consent_attr_resident_postal_code
    ),
    CONSENT_ATTR_ISSUE_DATE(
        expected = "Dyddiad cyhoeddi",
        resourceId = R.string.consent_attr_issue_date
    ),
    CONSENT_ATTR_ISSUING_AUTHORITY(
        expected = "Awdurdod cyhoeddi",
        resourceId = R.string.consent_attr_issuing_authority
    ),
    CONSENT_ATTR_ISSUING_COUNTRY(
        expected = "Gwlad gyhoeddi",
        resourceId = R.string.consent_attr_issuing_country
    ),
    CONSENT_ATTR_UN_DISTINGUISHING_SIGN(
        expected =
            "Gwlad gyhoeddi (arwydd gwahaniaethu\u2019r Cenhedloedd Unedig)",
        resourceId = R.string.consent_attr_un_distinguishing_sign
    ),
    CONSENT_ATTR_DRIVING_PRIVILEGES(
        expected = "Cerbydau y gallwch eu gyrru",
        resourceId = R.string.consent_attr_driving_privileges
    ),
    CONSENT_ATTR_PROVISIONAL_DRIVING_PRIVILEGES(
        expected = "Cerbydau y gallwch eu gyrru gyda\u2019ch trwydded dros dro",
        resourceId = R.string.consent_attr_provisional_driving_privileges
    ),
    CONSENT_ATTR_WELSH_LICENCE(
        expected = "Os yw\u2019ch trwydded yn ddwyieithog (Saesneg a Chymraeg)",
        resourceId = R.string.consent_attr_welsh_licence
    )
}
