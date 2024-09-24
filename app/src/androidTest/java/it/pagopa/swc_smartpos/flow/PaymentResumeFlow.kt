package it.pagopa.swc_smartpos.flow

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.databinding.ItemPaymentBinding
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerView
import it.pagopa.swc_smartpos.utils.ClickRvChild
import it.pagopa.swc_smartpos.view.PaymentResumeFragment
import org.hamcrest.Matchers

class PaymentResumeFlow {
    fun flowBackToIntro(actionDoneBackToIntro: Action) {
        Espresso.onView(ViewMatchers.withId(it.pagopa.swc.smartpos.app_shared.R.id.iv_home)).perform(ViewActions.click())
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(it.pagopa.swc_smart_pos.ui_kit.R.id.first_action)).perform(ViewActions.click())
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(it.pagopa.swc.smartpos.app_shared.R.id.iv_home)).perform(ViewActions.click())
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(it.pagopa.swc_smart_pos.ui_kit.R.id.second_action)).perform(ViewActions.click())
        actionDoneBackToIntro.invoke()
    }

    fun flow(actionDone: Action) {
        Espresso.pressBack()
        Espresso.onView(ViewMatchers.withId(R.id.rv_payment_resume)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<BaseRecyclerView<PaymentResumeFragment.ListItem, ItemPaymentBinding>
                .ViewHolder>(2, ClickRvChild.clickChildViewWithId(R.id.iv_item_info))
        )
        Thread.sleep(500L)
        Espresso.pressBack()
        Thread.sleep(500L)
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.btn_pay),ViewMatchers.isDisplayed())).perform(ViewActions.click())
        Thread.sleep(4500L)//two network call
        actionDone.invoke()
    }
}