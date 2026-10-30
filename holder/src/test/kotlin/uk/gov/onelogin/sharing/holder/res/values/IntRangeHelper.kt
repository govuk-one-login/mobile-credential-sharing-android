package uk.gov.onelogin.sharing.holder.res.values

object IntRangeHelper {
    internal fun IntRange.toPlaceholderStrings(): Array<String> = map { "<Argument $it>" }
        .toTypedArray()
}