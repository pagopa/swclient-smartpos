package it.pagopa.swc_smartpos.flow

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc_smart_pos.ui_kit.R

class ReceiptFragmentFlow {
    fun flow(actionDone:Action){
        Espresso.pressBack()
        Espresso.onView(ViewMatchers.withId(R.id.third_btn_receipt)).perform(ViewActions.click())
        actionDone.invoke()
    }
}