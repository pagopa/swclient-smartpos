package it.pagopa.swc_smartpos.flow

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class AllowCameraAccessFlow {
    fun flow() {
        try {
            Espresso.onView(ViewMatchers.withId(RUiKit.id.close_btn_allow_camera_access)).perform(ViewActions.click())
            Navigation.fromIntroToScanCode()
            Thread.sleep(500L)
            Espresso.pressBack()
            Navigation.fromIntroToScanCode()
            Thread.sleep(500L)
            Espresso.onView(ViewMatchers.withId(RUiKit.id.white_button_allow_camera)).perform(ViewActions.click())
            FlowTest.manageAskedPermission(false)
            Thread.sleep(500L)
            Espresso.onView(ViewMatchers.withId(RUiKit.id.white_button_allow_camera)).perform(ViewActions.click())//we are in insert manually
            Thread.sleep(500L)
            Espresso.pressBack()//delete keyboard
            Thread.sleep(1000L)
            Espresso.pressBack()//intro
            Navigation.fromIntroToScanCode()
            Espresso.onView(ViewMatchers.withId(RUiKit.id.white_button_allow_camera)).perform(ViewActions.click())
            FlowTest.manageAskedPermission(true)
            Thread.sleep(500L)
        } catch (_: Exception) {

        }
    }
}