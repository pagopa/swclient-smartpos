package it.pagopa.swc.smartpos.idpay.confirm_cie_op

import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseHeaderTest
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class ConfirmCieOp : BaseHeaderTest() {
    fun gotToCiePin(action: Action) {
        click(R.id.btn_auth)
        action.invoke()
    }

    fun denyOpAndBackToCieReader(action: Action){
        click(R.id.btn_deny)
        BaseFlowTest.networkSleep()//we are in result
        click(RUiKit.id.second_btn)
        BaseFlowTest.networkSleep()
        action.invoke()
    }
}