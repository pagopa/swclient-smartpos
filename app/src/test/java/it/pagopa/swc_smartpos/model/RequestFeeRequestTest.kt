package it.pagopa.swc_smartpos.model

import it.pagopa.swc_smartpos.model.fee.RequestFeeRequest
import org.junit.Test

class RequestFeeRequestTest {
    @Test
    fun testFields() {
        val clazz = RequestFeeRequest(
            notices = listOf(
                RequestFeeRequest.Notice(
                    amount = 100,
                    paTaxCode = "paTaxCode",
                    transfers = listOf(Transfer(paTaxCode = "paTaxCode", category = "category"))
                )
            ),
            paymentMethod = "PAYMENT_CARD"
        )
        assert(clazz.paymentMethod == "PAYMENT_CARD")
        val notice = clazz.notices[0]
        assert(notice.amount == 100)
        assert(notice.paTaxCode == "paTaxCode")
        val transfer = notice.transfers!![0]
        assert(transfer.paTaxCode == "paTaxCode")
        assert(transfer.category == "category")
    }
}