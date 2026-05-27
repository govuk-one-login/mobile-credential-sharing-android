package uk.gov.onelogin.sharing.verification.format

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat

internal object ClassInfoExt {
    /**
     * @return A [io.github.classgraph.ScanResult] instance for the entire gradle module.
     */
    val scanResult by lazy {
        ClassGraph()
            .enableAllInfo()
            .scan()
    }

    internal fun assertInterfaceReturnTypes(
        expectedMethods: List<Pair<String, Class<out Any>>>,
        classInfo: ClassInfo
    ) {
        expectedMethods.forEach { (expectedName, expectedType) ->
            val methodInfo = classInfo.methodInfo.getSingleMethod(expectedName)
            assertThat(
                methodInfo.typeDescriptorStr,
                containsString(expectedType.name.replace(".", "/"))
            )
        }
    }
}
