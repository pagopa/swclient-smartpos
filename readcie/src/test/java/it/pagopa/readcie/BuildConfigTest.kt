package it.pagopa.readcie

import org.junit.Test

class BuildConfigTest {
    @Test
    fun test_it() {
        assert(BuildConfig.LIBRARY_PACKAGE_NAME == "it.pagopa.readcie")
        assert(BuildConfig.BUILD_TYPE == if (BuildConfig.DEBUG) "debug" else "release")
    }
}