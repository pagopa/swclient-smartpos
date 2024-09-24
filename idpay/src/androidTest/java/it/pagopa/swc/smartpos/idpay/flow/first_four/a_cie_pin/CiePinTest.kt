package it.pagopa.swc.smartpos.idpay.flow.first_four.a_cie_pin

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseHeaderTest
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterfaceMocked
import it.pagopa.swc.smartpos.idpay.utils.toCustomKeyboard
import it.pagopa.swc.smartpos.idpay.view.InsertCiePinFragment
import org.hamcrest.Matchers
import it.pagopa.swc.smartpos.app_shared.R as RSHared
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class CiePinTest : BaseHeaderTest() {
    fun testAndBackHomeWithDialog(action: Action) {
        testBackPress {
            exitDialogTest(action)
        }
    }

    private fun exitDialogTest(onDone: Action) {
        testHeaderHome {
            clickWithParentMatch(RUiKit.id.ll_main_dialog, RUiKit.id.first_action)
            testHeaderHome {
                clickWithParentMatch(RUiKit.id.ll_main_dialog, RUiKit.id.second_action)
                BaseFlowTest.networkSleep()
                onDone.invoke()
            }
        }
    }

    private fun insertPinAndConfirm(action: Action) {
        BaseFlowTest.setCurrentFragment()
        Thread.sleep(500L)
        val fragment = BaseFlowTest.getCurrentFragment() as InsertCiePinFragment
        val parentId = fragment.binding.root.id
        val parentMatch = ViewMatchers.withParent(ViewMatchers.withId(parentId))
        val keyboard = ViewMatchers.withId(R.id.custom_keyboard)
        val customKeyboard = Espresso
            .onView(Matchers.allOf(parentMatch, keyboard, ViewMatchers.isDisplayed()))
            .toCustomKeyboard()
        customKeyboard?.textWritten?.postValue("123456")
        Thread.sleep(1000L)
        BaseFlowTest.sleepLess()
        //hack because of btn not found by espresso
        val text = BaseFlowTest.currentActivity?.resources?.getString(RSHared.string.cta_confirm)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val btnConfirm: UiObject =
            device.findObject(UiSelector().text(text!!))//this is for POYNT with second screen
        btnConfirm.click()
        action.invoke()
    }


    fun goToTooMuch(onDone: Action) {
        insertPinAndConfirm {
            BaseFlowTest.sleepLess()
            BaseFlowTest.networkSleep()
            clickWithParentMatch(RUiKit.id.ll_styled_dialog, RUiKit.id.first_action)
            insertPinAndConfirm {
                BaseFlowTest.sleepLess()
                BaseFlowTest.networkSleep()
                HttpServiceInterfaceMocked.cntPinExhausted++
                clickWithParentMatch(RUiKit.id.ll_styled_dialog, RUiKit.id.first_action)
                insertPinAndConfirm {
                    BaseFlowTest.sleepLess()
                    BaseFlowTest.networkSleep()
                    onDone.invoke()
                }
            }
        }
    }

    fun goToResult(onDone: Action) {
        HttpServiceInterfaceMocked.cntPinExhausted = 3
        insertPinAndConfirm {
            BaseFlowTest.sleepLess()
            BaseFlowTest.networkSleep()
            clickWithParentMatch(RUiKit.id.ll_styled_dialog, RUiKit.id.first_action)
            insertPinAndConfirm {
                BaseFlowTest.sleepLess()
                BaseFlowTest.networkSleep()
                clickWithParentMatch(RUiKit.id.ll_styled_dialog, RUiKit.id.first_action)
                insertPinAndConfirm {
                    BaseFlowTest.sleepLess()
                    BaseFlowTest.networkSleep()
                    onDone.invoke()
                }
            }
        }
    }
}