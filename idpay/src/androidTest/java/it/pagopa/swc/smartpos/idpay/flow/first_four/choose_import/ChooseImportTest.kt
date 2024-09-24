package it.pagopa.swc.smartpos.idpay.flow.first_four.choose_import

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseHeaderTest
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc.smartpos.idpay.flow.first_four.choose_initiative.ChooseInitiativeTest
import it.pagopa.swc.smartpos.idpay.utils.toCustomKeyboard
import it.pagopa.swc.smartpos.idpay.view.ChooseImportFragment
import org.hamcrest.Matchers

class ChooseImportTest : BaseHeaderTest() {
    private val chooseInitiative by lazy {
        ChooseInitiativeTest()
    }

    fun testAndGoToQrCode(action: Action) {
        testHeaderHome {
            showFragments.DirectFlow().directFlowToChooseImport {
                testBackPress {
                    chooseInitiative.goToChooseImport {
                        testHeaderBack {
                            chooseInitiative.goToChooseImport {
                                startQrCodeFlow(action)
                            }
                        }
                    }
                }
            }
        }
    }

    fun startQrCodeFlow(action: Action) {
        goAhead {
            clickWithParentMatch(
                it.pagopa.swc_smart_pos.ui_kit.R.id.ll_main_dialog,
                it.pagopa.swc_smart_pos.ui_kit.R.id.second_action_custom_button
            )
            action.invoke()
        }
    }

    fun startCieFlow(action: Action) {
        goAhead {
            clickWithParentMatch(
                it.pagopa.swc_smart_pos.ui_kit.R.id.ll_main_dialog,
                it.pagopa.swc_smart_pos.ui_kit.R.id.first_action
            )
            action.invoke()
        }
    }

    private fun goAhead(action: Action) {
        insertImport {
            click(R.id.btn_calculate_sale)
            BaseFlowTest.networkSleep()
            action.invoke()
        }
    }

    private fun insertImport(action: Action) {
        BaseFlowTest.setCurrentFragment()
        Thread.sleep(500L)
        val fragment = BaseFlowTest.getCurrentFragment() as ChooseImportFragment
        val parentId = fragment.binding.root.id
        val parentMatch = ViewMatchers.withParent(ViewMatchers.withId(parentId))
        val keyboard = ViewMatchers.withId(R.id.custom_keyboard)
        val customKeyboard = Espresso
            .onView(Matchers.allOf(parentMatch, keyboard, ViewMatchers.isDisplayed()))
            .toCustomKeyboard()
        customKeyboard?.textWritten?.postValue("12345")
        Thread.sleep(1000L)
        action.invoke()
    }
}