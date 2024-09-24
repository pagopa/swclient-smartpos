package it.pagopa.swc_smartpos.flow

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc.smartpos.app_shared.view.BaseBottomSheetMenu
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.databinding.ItemMenuBinding
import it.pagopa.swc_smartpos.databinding.ItemUiKitShowCaseBinding
import it.pagopa.swc_smartpos.fragments.utils.ScrollToBottomAction
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerView
import it.pagopa.swc_smartpos.view.UiKitShowCase
import org.hamcrest.Matcher

object Navigation {
    fun fromIntroToUiKitShowCase() {
        Espresso.onView(ViewMatchers.withId(R.id.main_menu)).perform(ViewActions.click())
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(it.pagopa.swc.smartpos.app_shared.R.id.rv_items))
            .perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>
                    .ViewHolder>(4, ViewActions.click())
            )
        Thread.sleep(500L)
        RecyclerViewActions
            .actionOnItemAtPosition<BaseRecyclerView<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>
            .ViewHolder>(3, ViewActions.click())
    }

    fun fromIntroToEnslavedMode() {
        Espresso.onView(ViewMatchers.withId(R.id.main_menu)).perform(ViewActions.click())
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(it.pagopa.swc.smartpos.app_shared.R.id.rv_items))
            .perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>
                    .ViewHolder>(3, ViewActions.click())
            )
        Thread.sleep(500L)
        RecyclerViewActions
            .actionOnItemAtPosition<BaseRecyclerView<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>
            .ViewHolder>(3, ViewActions.click())
    }

    fun fromIntroToScanCode() {
        Espresso.onView(ViewMatchers.withId(R.id.main_btn)).perform(ViewActions.click())
        Thread.sleep(500L)
    }

    fun fromIntroToInsertManually() {
        Espresso.onView(ViewMatchers.withId(R.id.secondary_btn)).perform(ViewActions.click())
    }

    fun fromLoginToIntro() {
        Espresso.onView(ViewMatchers.withId(R.id.main_menu)).perform(ViewActions.click())
    }

    object ShowCase {
        fun toButtonsShowCase(rvView: Matcher<View>) {
            Espresso.onView(rvView).perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<UiKitShowCase.UiKitCases, ItemUiKitShowCaseBinding>
                    .ViewHolder>(0, ViewActions.click())
            )
        }

        fun showDialog(rvView: Matcher<View>) {
            Espresso.onView(rvView).perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<UiKitShowCase.UiKitCases, ItemUiKitShowCaseBinding>
                    .ViewHolder>(1, ViewActions.click())
            )
        }

        fun showStyledDialogPopUpMenu(rvView: Matcher<View>) {
            Espresso.onView(rvView).perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<UiKitShowCase.UiKitCases, ItemUiKitShowCaseBinding>
                    .ViewHolder>(2, ViewActions.click())
            )
        }

        fun showResultPopUpMenu(rvView: Matcher<View>) {
            Espresso.onView(rvView).perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<UiKitShowCase.UiKitCases, ItemUiKitShowCaseBinding>
                    .ViewHolder>(3, ViewActions.click())
            )
        }

        fun showReceipt(rvView: Matcher<View>) {
            Espresso.onView(rvView).perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<UiKitShowCase.UiKitCases, ItemUiKitShowCaseBinding>
                    .ViewHolder>(4, ViewActions.click())
            )
        }

        fun showOutro(rvView: Matcher<View>) {
            Espresso.onView(rvView).perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<UiKitShowCase.UiKitCases, ItemUiKitShowCaseBinding>
                    .ViewHolder>(5, ViewActions.click())
            )
        }

        fun showInputShowCase(rvView: Matcher<View>) {
            Espresso.onView(rvView).perform(ScrollToBottomAction())
            Espresso.onView(rvView).perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<UiKitShowCase.UiKitCases, ItemUiKitShowCaseBinding>
                    .ViewHolder>(6, ViewActions.click())
            )
        }

        fun showToastShowCase(rvView: Matcher<View>) {
            Espresso.onView(rvView).perform(ScrollToBottomAction())
            Espresso.onView(rvView).perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<BaseRecyclerView<UiKitShowCase.UiKitCases, ItemUiKitShowCaseBinding>
                    .ViewHolder>(7, ViewActions.click())
            )
        }
    }
}