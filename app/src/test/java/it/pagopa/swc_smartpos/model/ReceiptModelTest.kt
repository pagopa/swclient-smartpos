package it.pagopa.swc_smartpos.model

import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import org.junit.Test

class ReceiptModelTest {
    @Test
    fun testFields() {
        val model = ReceiptModel()
        with(model){
            state = BaseResultFragment.State.Info
            labelDateAndTime = "labelDateAndTime"
            labelPayee = "labelPayee"
            labelPayeeTaxCode = "labelPayeeTaxCode"
            labelNoticeCode = "labelNoticeCode"
            labelPaymentReason = "labelPaymentReason"
            labelAmount = "labelAmount"
            labelFee = "labelFee"
            labelTotalAmount = "labelTotalAmount"
            transactionID = "transactionID"
            labelTerminalCode = "labelTerminalCode"
            paymentToken = "paymentToken"
        }
        assert(model.state == BaseResultFragment.State.Info)
        assert(model.labelDateAndTime == "labelDateAndTime")
        assert(model.labelPayee == "labelPayee")
        assert(model.labelPayeeTaxCode == "labelPayeeTaxCode")
        assert(model.labelNoticeCode == "labelNoticeCode")
        assert(model.labelPaymentReason == "labelPaymentReason")
        assert(model.labelAmount == "labelAmount")
        assert(model.labelFee == "labelFee")
        assert(model.labelTotalAmount == "labelTotalAmount")
        assert(model.transactionID == "transactionID")
        assert(model.labelTerminalCode == "labelTerminalCode")
        assert(model.paymentToken == "paymentToken")
    }
}