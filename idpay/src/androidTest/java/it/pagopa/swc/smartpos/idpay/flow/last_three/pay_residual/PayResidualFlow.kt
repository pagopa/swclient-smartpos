package it.pagopa.swc.smartpos.idpay.flow.last_three.pay_residual

import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseHeaderTest
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class PayResidualFlow : BaseHeaderTest() {
    fun testBackHome(onDone: Action) {
        testBackPress {
            dialogTestAvoid {
                dialogTestBackHome(onDone)
            }
        }
    }

    fun testCancelOp(onDone: Action){
        dialogCancelOpTest(onDone)
    }

    fun testPayWithCash() {
        payWithCash()
    }

    private fun payWithCash() {
        click(R.id.btn_pay)
        Thread.sleep(500L)
        try {
            clickWithParentMatch(RUiKit.id.ll_main_dialog, RUiKit.id.second_action_custom_button)
            Thread.sleep(3000L)
        } catch (_: Exception) {//if is an android smartphone there is no dialog
            Thread.sleep(500L)
        }
    }

    private fun dialogCancelOpTest(action: Action) {
        click(R.id.btn_cancel_op)
        Thread.sleep(500L)
        clickWithParentMatch(RUiKit.id.ll_main_dialog, RUiKit.id.first_action)
        Thread.sleep(500L)
        click(R.id.btn_cancel_op)
        Thread.sleep(500L)
        clickWithParentMatch(RUiKit.id.ll_main_dialog, RUiKit.id.second_action)
        BaseFlowTest.networkSleep()
        action.invoke()
    }

    private fun dialogTestAvoid(action: Action) {
        testHeaderHome {
            Thread.sleep(500L)
            clickWithParentMatch(RUiKit.id.ll_main_dialog, RUiKit.id.first_action)
            action.invoke()
        }
    }

    private fun dialogTestBackHome(action: Action) {
        testHeaderHome {
            Thread.sleep(500L)
            clickWithParentMatch(RUiKit.id.ll_main_dialog, RUiKit.id.second_action)
            action.invoke()
        }
    }
}