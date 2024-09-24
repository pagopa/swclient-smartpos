package it.pagopa.swc.smartpos.idpay.flow.transaction_detail

import androidx.appcompat.widget.AppCompatButton
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseHeaderTest
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class TransactionDetailTest : BaseHeaderTest() {
    fun test(onBackToIntro: Action) {
        val history = showFragments.transactionHistory
        val intro = showFragments.introTest
        testHeaderHome {
            intro.fromIntroToHistory {
                history.goToTransactionDetail {
                    testHeaderBack {
                        history.goToTransactionDetail {
                            Espresso.pressBack()
                            history.goToTransactionDetail {
                                val act = BaseFlowTest.currentActivity!!
                                val doReceipt = act.findViewById<AppCompatButton>(R.id.do_receipt)
                                act.runOnUiThread {
                                    doReceipt.performClick()
                                }
                                BaseFlowTest.sleepLess()
                                clickWithParentMatch(
                                    RUiKit.id.ll_main_dialog,
                                    RUiKit.id.first_action_custom_button
                                )
                                BaseFlowTest.sleepLess()
                                act.runOnUiThread {
                                    doReceipt.performClick()
                                }
                                BaseFlowTest.sleepLess()
                                clickWithParentMatch(
                                    RUiKit.id.ll_main_dialog,
                                    RUiKit.id.second_action_custom_button
                                )
                                BaseFlowTest.sleepLess()
                                val device =
                                    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                                device.pressBack()
                                BaseFlowTest.sleepLess()
                                onBackToIntro.invoke()
                            }
                        }
                    }
                }
            }
        }
    }
}