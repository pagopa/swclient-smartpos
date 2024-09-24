package it.pagopa.swc_smartpos.flow

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc_smartpos.R
import org.hamcrest.Matchers

class ButtonsShowCaseTestFlow {
    fun flow(actionDone: Action) {
        val scrollView = ViewMatchers.withId(R.id.nested_scroll_view_buttons_show_case)
        val parentMatch = ViewMatchers.withParent(ViewMatchers.withId(R.id.btn_show_case_main_ll))
        val infoDarkButton = ViewMatchers.withId(R.id.info_dark_btn)
        val infoLightButton = ViewMatchers.withId(R.id.info_light_btn)
        val successDarkButton = ViewMatchers.withId(R.id.success_dark_btn)
        val successLightButton = ViewMatchers.withId(R.id.success_light_btn)
        val errorDarkButton = ViewMatchers.withId(R.id.error_dark_btn)
        val errorLightButton = ViewMatchers.withId(R.id.error_light_btn)
        Espresso
            .onView(Matchers.allOf(parentMatch, infoDarkButton, ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
        Espresso
            .onView(Matchers.allOf(parentMatch, infoLightButton, ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
        Espresso
            .onView(Matchers.allOf(parentMatch, successDarkButton, ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
        Espresso
            .onView(Matchers.allOf(parentMatch, successLightButton, ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
        Espresso.onView(scrollView).perform(ViewActions.swipeUp())
        Thread.sleep(400L)
        Espresso
            .onView(Matchers.allOf(parentMatch, errorDarkButton, ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
        Espresso
            .onView(Matchers.allOf(parentMatch, errorLightButton, ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
        Thread.sleep(400L)
        actionDone.invoke()
    }
}