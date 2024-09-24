package it.pagopa.swc.smartpos.idpay.flow.intro_outro_receipt.intro

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc.smartpos.app_shared.databinding.ItemMenuBinding
import it.pagopa.swc.smartpos.app_shared.view.BaseBottomSheetMenu
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.base_ui_test.BaseTestFragment
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import org.hamcrest.Matchers
import it.pagopa.swc.smartpos.app_shared.R as RShared
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class IntroTest : BaseTestFragment() {
    fun testIntro(onChooseInitiativeReached: Action) {
        Espresso.pressBack()//showingDialog
        BaseFlowTest.sleepLess()
        clickWithParentMatch(RUiKit.id.ll_main_dialog, RUiKit.id.first_action)
        BaseFlowTest.sleepLess()
        openMenu {
            Espresso.pressBack()//delete menu
            goToChooseInitiative(onChooseInitiativeReached)
        }
    }


    fun goToChooseInitiative(onDone: Action) {
        val parent = ViewMatchers.withParent(ViewMatchers.withId(R.id.ll_intro_binding))
        val introBtn = ViewMatchers.withId(R.id.main_btn)
        Espresso
            .onView(Matchers.allOf(parent, introBtn, ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
        BaseFlowTest.networkSleep()
        onDone.invoke()
    }

    private fun openMenu(onDone: Action) {
        Thread.sleep(1500L)
        Espresso.onView(ViewMatchers.withId(R.id.menu)).perform(ViewActions.click())
        Thread.sleep(500L)
        onDone.invoke()
    }

    fun fromIntroToHistory(actionDone: Action) {
        openMenu {
            recyclerViewClick<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>(RShared.id.rv_items, 1)
            BaseFlowTest.networkSleep()
            actionDone.invoke()
        }
    }
}