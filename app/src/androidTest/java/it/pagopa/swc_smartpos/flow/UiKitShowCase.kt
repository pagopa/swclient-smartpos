package it.pagopa.swc_smartpos.flow

import android.view.View
import android.widget.Toast
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.toast.UiKitToast
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class UiKitShowCase {
    fun flow(actionDone: Action) {
        val rvView = ViewMatchers.withId(R.id.rv)
        Navigation.ShowCase.toButtonsShowCase(rvView)
        Thread.sleep(500L)
        ButtonsShowCaseTestFlow().flow {
            Espresso.pressBack()//back to UiKit
            Navigation.ShowCase.showDialog(rvView)
            Thread.sleep(500L)
            Espresso.onView(ViewMatchers.withId(it.pagopa.swc_smart_pos.ui_kit.R.id.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Thread.sleep(500L)
            Espresso.onView(ViewMatchers.withId(it.pagopa.swc_smart_pos.ui_kit.R.id.first_action)).perform(ViewActions.click())
            Thread.sleep(Toast.LENGTH_LONG.toLong())
            Espresso.onView(ViewMatchers.withId(it.pagopa.swc_smart_pos.ui_kit.R.id.second_action)).perform(ViewActions.click())
            Thread.sleep(Toast.LENGTH_LONG.toLong())
            Espresso.onView(ViewMatchers.withId(it.pagopa.swc_smart_pos.ui_kit.R.id.close_dialog)).perform(ViewActions.click())
            rvView.showOneStyledDialog(Style.Success)
            rvView.showOneStyledDialog(Style.Warning)
            rvView.showOneStyledDialog(Style.Info)
            rvView.showOneStyledDialog(Style.Error, true)
            rvView.showOneResult(BaseResultFragment.State.Success)
            rvView.showOneResult(BaseResultFragment.State.Info)
            rvView.showOneResult(BaseResultFragment.State.Error)
            rvView.showOneResult(BaseResultFragment.State.Warning)
            Thread.sleep(400L)
            Navigation.ShowCase.showReceipt(rvView)
            Espresso.pressBack()//backToUiKit
            Navigation.ShowCase.showOutro(rvView)
            Espresso.pressBack()//backToUiKit
            Navigation.ShowCase.showInputShowCase(rvView)
            Espresso.pressBack()//backToUiKit
            rvView.showOneToast(UiKitToast.Value.Generic)
            rvView.showOneToast(UiKitToast.Value.Generic, true)
            rvView.showOneToast(UiKitToast.Value.Success)
            rvView.showOneToast(UiKitToast.Value.Error)
            rvView.showOneToast(UiKitToast.Value.Info)
            rvView.showOneToast(UiKitToast.Value.Warning)
            Navigation.ShowCase.showOutro(rvView)
            Espresso.onView(ViewMatchers.withId(RUiKit.id.btn_new_payment)).perform(ViewActions.click())
            Thread.sleep(500L)
            Navigation.fromIntroToUiKitShowCase()//backToUiKit
            Navigation.ShowCase.showOutro(rvView)
            Espresso.onView(ViewMatchers.withId(RUiKit.id.back_home)).perform(ViewActions.click())//backToIntro
            actionDone.invoke()
        }
    }

    private fun Matcher<View>.showOneStyledDialog(kind: Style, isLast: Boolean = false) {
        Thread.sleep(400L)
        Navigation.ShowCase.showStyledDialogPopUpMenu(this)
        Espresso.onView(withText(kind.name)).inRoot(RootMatchers.isPlatformPopup()).perform(ViewActions.click())
        Thread.sleep(400L)
        if (isLast) {
            val parentMatch = ViewMatchers.withParent(ViewMatchers.withId(RUiKit.id.ll_styled_dialog))
            val mainBtn = ViewMatchers.withId(RUiKit.id.first_action)
            val secondaryBtn = ViewMatchers.withId(RUiKit.id.second_action)
            Espresso
                .onView(Matchers.allOf(parentMatch, mainBtn, ViewMatchers.isDisplayed()))
                .perform(ViewActions.click())
            Thread.sleep(1000L)
            Espresso
                .onView(Matchers.allOf(parentMatch, secondaryBtn, ViewMatchers.isDisplayed()))
                .perform(ViewActions.click())
            Thread.sleep(3000L)
        }
        Espresso.pressBack()//dismiss Dialog
    }

    private fun Matcher<View>.showOneResult(kind: BaseResultFragment.State) {
        Thread.sleep(400L)
        Navigation.ShowCase.showResultPopUpMenu(this)
        Espresso.onView(withText(kind.name)).inRoot(RootMatchers.isPlatformPopup()).perform(ViewActions.click())
        Thread.sleep(400L)
        Espresso.pressBack()//back to show case
    }

    private fun Matcher<View>.showOneToast(kind: UiKitToast.Value, lastToast: Boolean = false) {
        Thread.sleep(400L)
        Navigation.ShowCase.showToastShowCase(this)
        Espresso.onView(withText(if (lastToast) kind.name + " without image" else kind.name)).inRoot(RootMatchers.isPlatformPopup())
            .perform(ViewActions.click())
        Thread.sleep(3500L)
    }
}