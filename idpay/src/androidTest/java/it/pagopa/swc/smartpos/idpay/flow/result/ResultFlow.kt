package it.pagopa.swc.smartpos.idpay.flow.result

import androidx.test.espresso.Espresso
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseTestFragment
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterfaceMocked
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class ResultFlow : BaseTestFragment() {
    fun goToNeedReceipt(action: Action) {
        Espresso.pressBack()
        Thread.sleep(1000L)
        HttpServiceInterfaceMocked.cntCallForFakePoll = 0
        click(RUiKit.id.btn_continue)
        action.invoke()
    }
}