package it.pagopa.swc_smartpos.model

import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentRequest
import org.junit.Test

class ClosePaymentRequestTest {
    @Test
    fun testFields() {
        val clazz = ClosePaymentRequest(
            outcome = "outcome",
            paymentTimestamp = "paymentTimestamp",
            paymentMethod = "PAYMENT_CARD"
        )
        assert(clazz.outcome == "outcome")
        assert(clazz.paymentTimestamp == "paymentTimestamp")
        assert(clazz.paymentMethod == "PAYMENT_CARD")
    }
}