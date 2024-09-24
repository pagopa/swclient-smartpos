package it.pagopa.swc_smartpos.model

import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import org.junit.Test

class TransactionAndNoticeTest {
    @Test
    fun testModel() {
        val transaction = Transaction(
            "acquirerId",
            "channel",
            fee = 10,
            "insertTimestamp",
            "merchantId",
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
            status = Status.CLOSED.status,
            "terminalId",
            totalAmount = 110,
            "transactionId"
        )
        assert(transaction.acquirerId == "acquirerId")
        assert(transaction.channel == "channel")
        assert(transaction.fee == 10)
        assert(transaction.insertTimestamp == "insertTimestamp")
        assert(transaction.merchantId == "merchantId")
        val notice = transaction.notices!![0]
        assert(notice.amount == 100)
        assert(notice.company == "company")
        assert(notice.description == "description")
        assert(notice.noticeNumber == "noticeNumber")
        assert(notice.office == "office")
        assert(notice.paTaxCode == "paTaxCode")
        assert(notice.paymentToken == "paymentToken")
        assert(transaction.status == Status.CLOSED.status)
        assert(transaction.getStato() == Status.CLOSED)
        assert(transaction.amountPlusFee() == 120)
        assert(transaction.terminalId == "terminalId")
        assert(transaction.totalAmount == 110)
        assert(transaction.transactionId == "transactionId")
    }

    @Test
    fun testAmountPlusFee() {
        val transaction = Transaction(
            totalAmount = null,
            status = Status.CLOSED.status
        )
        assert(transaction.amountPlusFee() == null)
        val transaction2 = Transaction(
            totalAmount = 110,
            fee = null,
            status = Status.CLOSED.status
        )
        assert(transaction2.amountPlusFee() == null)
        assert(transaction2.getStato().state == BaseResultFragment.State.Success)
    }
}