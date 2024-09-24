package it.pagopa.swc_smartpos.model

import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentResponse
import org.junit.Test

class ClosePaymentResponseTest {
    @Test
    fun testFields() {
        val clazz = ClosePaymentResponse(
            location = listOf("bach"),
            retryAfter = listOf(10),
            maxRetries = listOf(3)
        )
        assert(clazz.location!![0] == "bach")
        assert(clazz.retryAfter!![0] == 10)
        assert(clazz.maxRetries!![0] == 3)
    }
}