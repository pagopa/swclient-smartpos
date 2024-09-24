package it.pagopa.swc.smartpos.idpay.flow

import it.pagopa.swc.smartpos.idpay.confirm_cie_op.ConfirmCieOp
import it.pagopa.swc.smartpos.idpay.flow.cie_flow.CieFlowTest
import it.pagopa.swc.smartpos.idpay.flow.first_four.a_cie_pin.CiePinTest
import it.pagopa.swc.smartpos.idpay.flow.first_four.choose_import.ChooseImportTest
import it.pagopa.swc.smartpos.idpay.flow.first_four.choose_initiative.ChooseInitiativeTest
import it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.intro.IntroTest
import it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.outro.OutroFlow
import it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.receipt.ReceiptFlow
import it.pagopa.swc.smartpos.idpay.flow.last_three.pay_residual.PayResidualFlow
import it.pagopa.swc.smartpos.idpay.flow.last_three.qr_code.QrCodeFlow
import it.pagopa.swc.smartpos.idpay.flow.last_three.transaction_history.TransactionHistoryTest
import it.pagopa.swc.smartpos.idpay.flow.result.ResultFlow
import it.pagopa.swc.smartpos.idpay.flow.transaction_detail.TransactionDetailTest
import it.pagopa.swc.smartpos.idpay.login.LoginTest

class ShowFragments {
    val loginTest by lazy {
        LoginTest()
    }
    val introTest by lazy {
        IntroTest()
    }
    val transactionHistory by lazy {
        TransactionHistoryTest()
    }
    val transactionDetail by lazy {
        TransactionDetailTest()
    }
    val chooseInitiativeTest by lazy {
        ChooseInitiativeTest()
    }
    val chooseImportTest by lazy {
        ChooseImportTest()
    }
    val qrCodeFlow by lazy {
        QrCodeFlow()
    }
    val cieFlow by lazy {
        CieFlowTest()
    }
    val confirmCieOp by lazy {
        ConfirmCieOp()
    }
    val ciePin by lazy {
        CiePinTest()
    }
    val resultFlow by lazy {
        ResultFlow()
    }
    val receiptFlow by lazy {
        ReceiptFlow()
    }
    val outroFlow by lazy {
        OutroFlow()
    }
    val payResidualFlow by lazy {
        PayResidualFlow()
    }

    inner class DirectFlow {
        fun directFlowToChooseImport(action: Action) {
            introTest.goToChooseInitiative {
                chooseInitiativeTest.goToChooseImport(action)
            }
        }
    }
}
typealias Action = () -> Unit