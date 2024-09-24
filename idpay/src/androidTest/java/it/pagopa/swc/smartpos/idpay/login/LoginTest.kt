package it.pagopa.swc.smartpos.idpay.login

import androidx.appcompat.widget.LinearLayoutCompat
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseTestFragment
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc_smartpos.ui_kit.buttons.CustomLoadingButton
import it.pagopa.swc_smartpos.ui_kit.input.InputText
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class LoginTest : BaseTestFragment() {
    fun test(onIntroReached: Action) {
        testBackPress {
            Thread.sleep(500L)
            val act = BaseFlowTest.currentActivity!!
            val llInput = act.findViewById<LinearLayoutCompat>(RUiKit.id.ll_inputs)
            val etName: InputText = llInput.getChildAt(0) as InputText
            val etPassword: InputText = llInput.getChildAt(1) as InputText
            val btnForm = act.findViewById<CustomLoadingButton>(RUiKit.id.btn_form)
            act.runOnUiThread {
                etName.setText("123456789012345678")
                etPassword.setText("blablabla")
                btnForm.performClick()
            }
            BaseFlowTest.networkSleep()
            act.runOnUiThread {
                etPassword.setText("access")
                btnForm.performClick()
            }
            BaseFlowTest.networkSleep()
            onIntroReached.invoke()
        }
    }

    fun goToIntro(onIntroReached: Action) {
        val act = BaseFlowTest.currentActivity!!
        val llInput = act.findViewById<LinearLayoutCompat>(RUiKit.id.ll_inputs)
        val etName: InputText = llInput.getChildAt(0) as InputText
        val etPassword: InputText = llInput.getChildAt(1) as InputText
        val btnForm = act.findViewById<CustomLoadingButton>(RUiKit.id.btn_form)
        act.runOnUiThread {
            etName.setText("123456789012345678")
            etPassword.setText("access")
            btnForm.performClick()
        }
        BaseFlowTest.networkSleep()
        onIntroReached.invoke()
    }
}