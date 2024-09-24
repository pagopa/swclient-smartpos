package it.pagopa.swc_smartpos.model

import it.pagopa.swc_smartpos.model.activate_payment.ActivatePaymentRequest
import org.junit.Test

class ActivatePaymentRequestTest {
    @Test
    fun testFields() {
        val clazz = ActivatePaymentRequest(
            idempotencyKey = "idempotencyKey",
            amount = 100
        )
        assert(clazz.idempotencyKey == "idempotencyKey")
        assert(clazz.amount == 100)
    }
}