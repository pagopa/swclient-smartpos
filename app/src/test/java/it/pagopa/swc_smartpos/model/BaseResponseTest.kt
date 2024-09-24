package it.pagopa.swc_smartpos.model

import org.junit.Test

class BaseResponseTest {
    class ForTest : BaseResponse()

    @Test
    fun toStringTest() {
        val test = ForTest()
        assert(test.toString() == "outcome:${test.outcome.orEmpty()}")
    }
}