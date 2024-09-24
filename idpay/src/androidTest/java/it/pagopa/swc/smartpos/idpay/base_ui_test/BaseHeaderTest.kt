package it.pagopa.swc.smartpos.idpay.base_ui_test

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc.smartpos.app_shared.R
import it.pagopa.swc.smartpos.idpay.flow.Action

abstract class BaseHeaderTest : BaseTestWithShowFragment() {

    fun testHeaderBack(action: Action) {
        Espresso.onView(ViewMatchers.withId(R.id.iv_back)).perform(ViewActions.click())
        action.invoke()
    }

    fun testHeaderHome(action: Action) {
        Espresso.onView(ViewMatchers.withId(R.id.iv_home)).perform(ViewActions.click())
        action.invoke()
    }
}