package it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.outro

import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseTestWithShowFragment
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc.smartpos.idpay.view.OutroFragment
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class OutroFlow : BaseTestWithShowFragment() {
    fun test() {
        backIntro {
        }
    }

    fun goToResidual(action: Action) {
        Thread.sleep(500L)
        clickWithParentMatch(RUiKit.id.ll_styled_dialog, RUiKit.id.first_action)
        action.invoke()
    }

    fun goToResidualWithoutDialog(action: Action){
        Thread.sleep(500L)
        clickWithParentMatch(RUiKit.id.main_background, RUiKit.id.close_styled_dialog)
        testBackPress {
            click(R.id.btn_pay_residual)
            action.invoke()
        }
    }
    fun acceptNewBonus(action: Action) {
        click(R.id.btn_accept_new_bonus)
        action.invoke()
    }

    fun backIntro(action: Action) {
        BaseFlowTest.setCurrentFragment()
        Thread.sleep(500L)
        val fragment = BaseFlowTest.getCurrentFragment() as OutroFragment
        val parentId = fragment.binding.root.id
        clickWithParentMatch(parentId, R.id.back_home)
        action.invoke()
    }
}