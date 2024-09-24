package it.pagopa.swc_smartpos.flow

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class ResultFragmentFlow {
    fun flow(action: Action){
        Espresso.pressBack()
        Espresso.onView(ViewMatchers.withId(RUiKit.id.btn_continue)).perform(ViewActions.click())
        action.invoke()
    }
}