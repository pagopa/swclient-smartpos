package it.pagopa.swc.smartpos.idpay.base_ui_test

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.viewbinding.ViewBinding
import it.pagopa.swc.smartpos.idpay.flow.Action
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerView
import org.hamcrest.Matchers

abstract class BaseTestFragment {
    fun testBackPress(action: Action) {
        Espresso.pressBack()
        action.invoke()
    }

    fun click(id: Int) {
        Espresso.onView(ViewMatchers.withId(id)).perform(ViewActions.click())
    }

    fun clickTv(id: Int) {
        Espresso.onView(ViewMatchers.withText(id)).perform(ViewActions.click())
    }

    fun <T, VB : ViewBinding> recyclerViewClick(id: Int, pos: Int) {
        Espresso.onView(ViewMatchers.withId(id)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<BaseRecyclerView<T, VB>
                .ViewHolder>(pos, ViewActions.click())
        )
    }

    fun clickWithParentMatch(parentId: Int, id: Int) {
        val parentMatch = ViewMatchers.withParent(ViewMatchers.withId(parentId))
        val mainBtn = ViewMatchers.withId(id)
        Espresso
            .onView(Matchers.allOf(parentMatch, mainBtn, ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
    }

    fun clickTvWithParentMatch(parentId: Int, @StringRes id: Int) {
        val parentMatch = ViewMatchers.withParent(ViewMatchers.withId(parentId))
        val tv = ViewMatchers.withText(id)
        Espresso
            .onView(Matchers.allOf(parentMatch, tv, ViewMatchers.isDisplayingAtLeast(1)))
            .perform(ViewActions.click())
    }
}