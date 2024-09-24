package it.pagopa.swc_smartpos.model

import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentPollingResponse
import org.junit.Test

class ClosePaymentPollingResponseTest {
    @Test
    fun testFields() {
        val clazz = ClosePaymentPollingResponse(
            acquirerId = "acquirerId",
            channel = "channel",
            fee = 10,
            insertTimestamp = "insertTimestamp",
            merchantId = "merchantId",
            notices = listOf(
                Notice(
                    100,
                    "company",
                    "description",
                    "noticeNumber",
                    "office",
                    "paTaxCode",
                    "paymentToken"
                )
            ),
            status = Status.PRE_CLOSE.status,
            terminalId = "terminalId",
            totalAmount = 100,
            transactionId = "transactionId"
        )
        assert(clazz.acquirerId == "acquirerId")
        assert(clazz.channel == "channel")
        assert(clazz.fee == 10)
        assert(clazz.insertTimestamp == "insertTimestamp")
        assert(clazz.merchantId == "merchantId")
        assert(clazz.terminalId == "terminalId")
        assert(clazz.transactionId == "transactionId")
        assert(clazz.status == Status.PRE_CLOSE.status)
        assert(clazz.totalAmount == 100)
        assert(clazz.notices[0].amount == 100)
    }
}