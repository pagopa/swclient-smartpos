package it.pagopa.swc.smartpos.idpay.utils

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc_smartpos.ui_kit.custom_keyboard.CustomKeyboard
import org.hamcrest.Matcher

fun ViewInteraction.toCustomKeyboard(): CustomKeyboard? {
    var back: CustomKeyboard? = null
    this.perform(object : ViewAction {
        override fun getDescription(): String {
            return "getting text"
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(CustomKeyboard::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            back = view as? CustomKeyboard
        }
    })
    return back
}