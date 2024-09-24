package it.pagopa.swc_smartpos.flow

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc_smartpos.R

class PaymentAmountResumeFlow {
    fun flowBackToIntro(done: Action) {
        Espresso.onView(ViewMatchers.withId(it.pagopa.swc.smartpos.app_shared.R.id.iv_home)).perform(ViewActions.click())
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(it.pagopa.swc_smart_pos.ui_kit.R.id.second_action)).perform(ViewActions.click())
        Thread.sleep(2500L)
        done.invoke()
    }


    fun flow(done: Action) {
        Espresso.pressBack()
        FlowTest.setCurrentFragment()
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(R.id.btn_pay_amount)).perform(ViewActions.click())
        Thread.sleep(7000L)//three calls
        done.invoke()
    }
}