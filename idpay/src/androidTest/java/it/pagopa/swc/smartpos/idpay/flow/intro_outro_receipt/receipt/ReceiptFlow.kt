package it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.receipt

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseTestFragment
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc_smart_pos.ui_kit.R

class ReceiptFlow : BaseTestFragment() {
    fun test() {
        testBackPress {
            //for now no actions added
            click(R.id.first_btn_receipt)
            click(R.id.second_btn_receipt)
            click(R.id.third_btn_receipt)
            BaseFlowTest.sleepLess()
        }
    }

    fun clickShare(actionOutroReached: Action) {
        click(R.id.second_btn_receipt)
        BaseFlowTest.sleepLess()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressBack()
        actionOutroReached.invoke()
    }

    fun goToOutro(actionDone: Action) {
        click(R.id.third_btn_receipt)
        actionDone.invoke()
    }
}