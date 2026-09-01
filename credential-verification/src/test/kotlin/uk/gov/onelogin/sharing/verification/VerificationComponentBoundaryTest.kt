package uk.gov.onelogin.sharing.verification

import io.github.classgraph.ClassInfo
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

class VerificationComponentBoundaryTest {

    @Test
    fun `verification cose does not depend on prohibited packages`() {
        val violations = scanResult.allClasses
            .filter { isProductionCoseClass(it) }
            .flatMap { getCoseDependencyViolations(it) }

        assertTrue(violations.joinToString("\n"), violations.isEmpty())
    }

    private fun isProductionCoseClass(classInfo: ClassInfo): Boolean =
        classInfo.packageName.startsWith(PACKAGE_COSE) &&
            classInfo.isStandardClass &&
            !classInfo.name.contains("Test")

    private fun getCoseDependencyViolations(classInfo: ClassInfo): List<String> =
        classInfo.classDependencies
            .map { it.name }
            .filter { dep -> isProhibitedForCose(dep) }
            .map { "Class ${classInfo.name} depends on prohibited class $it" }

    private fun isProhibitedForCose(dependency: String): Boolean =
        PROHIBITED_FOR_COSE.any { prohibited -> dependency.startsWith(prohibited) }

    @Test
    fun `verification document and reader do not depend on each other or cose internal`() {
        val documentViolations = scanResult.allClasses
            .filter { isProductionClassInPackage(it, PACKAGE_DOCUMENT) }
            .flatMap { getDocumentDependencyViolations(it) }

        val readerViolations = scanResult.allClasses
            .filter { isProductionClassInPackage(it, PACKAGE_READER) }
            .flatMap { getReaderDependencyViolations(it) }

        val allViolations = documentViolations + readerViolations
        assertTrue(allViolations.joinToString("\n"), allViolations.isEmpty())
    }

    private fun isProductionClassInPackage(classInfo: ClassInfo, packageName: String): Boolean =
        classInfo.packageName.startsWith(packageName) &&
            classInfo.isStandardClass &&
            !classInfo.name.contains("Test")

    private fun getDocumentDependencyViolations(classInfo: ClassInfo): List<String> =
        classInfo.classDependencies
            .map { it.name }
            .filter { it.startsWith(PACKAGE_READER) || it.startsWith(PACKAGE_COSE_INTERNAL) }
            .map { "Document class ${classInfo.name} depends on prohibited class $it" }

    private fun getReaderDependencyViolations(classInfo: ClassInfo): List<String> =
        classInfo.classDependencies
            .map { it.name }
            .filter { it.startsWith(PACKAGE_DOCUMENT) || it.startsWith(PACKAGE_COSE_INTERNAL) }
            .map { "Reader class ${classInfo.name} depends on prohibited class $it" }

    @Test
    fun `verification trust can depend on cose but not vice versa`() {
        val trustClasses = scanResult.allClasses
            .filter { isProductionClassInPackage(it, PACKAGE_TRUST) }

        trustClasses.forEach { _ ->
            // During migration, trust can depend on cose (public or internal)
            // but cose must not depend on trust (enforced by other test)
        }
    }

    companion object {
        private const val PACKAGE_VERIFICATION = "uk.gov.onelogin.sharing.verification"
        private const val PACKAGE_COSE = "$PACKAGE_VERIFICATION.cose"
        private const val PACKAGE_COSE_INTERNAL = "$PACKAGE_COSE.internal"
        private const val PACKAGE_DOCUMENT = "$PACKAGE_VERIFICATION.document"
        private const val PACKAGE_READER = "$PACKAGE_VERIFICATION.reader"
        private const val PACKAGE_TRUST = "$PACKAGE_VERIFICATION.trust"

        private val PROHIBITED_FOR_COSE = listOf(
            PACKAGE_DOCUMENT,
            PACKAGE_READER,
            PACKAGE_TRUST,
            "uk.gov.onelogin.sharing.verification.format",
            "uk.gov.onelogin.sharing.models.mdoc",
            "uk.gov.logging"
        )
    }
}
