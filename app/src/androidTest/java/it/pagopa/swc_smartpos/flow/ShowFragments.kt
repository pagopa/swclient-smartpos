package it.pagopa.swc_smartpos.flow

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc.smartpos.app_shared.view.BaseBottomSheetMenu
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.databinding.ItemMenuBinding
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerView
import it.pagopa.swc.smartpos.app_shared.R as RShared
import it.pagopa.swc_smart_pos.ui_kit.R as RuiKit

class ShowFragments {

    fun flow(currentActivity: MainActivity) {
        Thread.sleep(3000L)
        testLoginFragment(currentActivity) {
            fromIntroToAssistanceWebView {
                fromIntroToStoricoOperazioni {
                    fromIntroToPaymentResumeByInsertManually {
                        //we are in payment resume
                        PaymentResumeFlow().flowBackToIntro {
                            Thread.sleep(500L)
                            fromIntroToPaymentResumeByInsertManually {
                                //we are in payment resume
                                fromPaymentResumeToOutro {
                                    //we are back to intro
                                    fromScanCodeToEnd {
                                        uiKitShowCaseTestsByIntro {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fromScanCodeToEnd(backToIntro: Action) {
        scanCodeErrorTestsByIntro {
            ScanCodeTestFlow().scanCode {
                //we are in payment resume
                PaymentResumeFlow().flow {
                    PaymentAmountResumeFlow().flowBackToIntro {
                        //we are back to intro
                        Navigation.fromIntroToScanCode()
                        ScanCodeTestFlow().scanCode {
                            fromPaymentResumeToOutro {
                                //done back to intro
                                backToIntro.invoke()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fromPaymentResumeToAmountResume(onDone: Action) {
        PaymentResumeFlow().flow {
            PaymentAmountResumeFlow().flow(onDone)
        }
    }

    private fun fromPaymentResumeToOutro(onDone: Action) {
        fromPaymentResumeToAmountResume {
            //we are in result
            fromResultToOutro(onDone)
        }
    }

    private fun fromIntroToPaymentResumeByInsertManually(onDone: Action) {
        introFragment {
            Navigation.fromIntroToInsertManually()
            InsertManuallyTestFlow().flow {
                //we are in payment resume
                onDone.invoke()
            }
        }
    }

    private fun fromResultToOutro(onDone: Action) {
        ResultFragmentFlow().flow {
            ReceiptFragmentFlow().flow {
                OutroFragmentTest().flow(onDone)
            }
        }
    }

    private fun uiKitShowCaseTestsByIntro(onDone: Action) {
        Navigation.fromIntroToUiKitShowCase()
        UiKitShowCase().flow(onDone)
    }

    private fun scanCodeErrorTestsByIntro(onDone: Action) {
        Navigation.fromIntroToScanCode()
        AllowCameraAccessFlow().flow()
        val scanCodeTestFlow = ScanCodeTestFlow()
        scanCodeTestFlow.testBackPress()
        Navigation.fromIntroToScanCode()
        scanCodeTestFlow.testClose()
        Navigation.fromIntroToScanCode()
        scanCodeTestFlow.scanCodeAllErrors(onDone)
    }

    private fun introFragment(actionClick: Action) {
        Espresso.onView(ViewMatchers.withId(RuiKit.id.main_menu)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Thread.sleep(1000L)
        Espresso.onView(ViewMatchers.withId(RuiKit.id.main_intro)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Thread.sleep(1000L)
        Espresso.onView(ViewMatchers.withId(RuiKit.id.ll_not_working)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        actionClick.invoke()
    }


    private fun testLoginFragment(currentActivity: MainActivity, actionClick: Action) {
        Espresso.onView(ViewMatchers.withId(RuiKit.id.btn_form)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Thread.sleep(500L)

        val inputOneHint = currentActivity.getString(R.string.label_username)
        val inputSecondHint = currentActivity.getString(R.string.label_password)

        Espresso.onView(ViewMatchers.withHint(inputOneHint)).perform(
            ViewActions.replaceText(
                "user"
            )
        )
        Espresso.onView(ViewMatchers.withHint(inputSecondHint)).perform(
            ViewActions.replaceText(
                "test"
            )
        )

        Espresso.onView(ViewMatchers.withId(RuiKit.id.btn_form)).perform(ViewActions.click())

        Thread.sleep(2000L)
        Espresso.onView(ViewMatchers.withId(RuiKit.id.toast_text)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))


        Thread.sleep(1000L)

        Espresso.onView(ViewMatchers.withHint(inputOneHint)).perform(
            ViewActions.replaceText(
                "user"
            )
        )
        Espresso.onView(ViewMatchers.withHint(inputSecondHint)).perform(
            ViewActions.replaceText(
                "access"
            )
        )
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(RuiKit.id.btn_form)).perform(ViewActions.click())

        Thread.sleep(2000L)
        actionClick.invoke()
    }

    private fun fromIntroToStoricoOperazioni(function: Action) {
        Thread.sleep(3000L)
        Espresso.onView(ViewMatchers.withId(RuiKit.id.main_menu)).perform(ViewActions.click())
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(RShared.id.rv_items)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<BaseRecyclerView<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>
                .ViewHolder>(1, ViewActions.click())
        )
        Thread.sleep(3000L)
        Espresso.onView(ViewMatchers.withId(R.id.rw_storico)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<BaseRecyclerView<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>
                .ViewHolder>(1, ViewActions.click())
        )
        Thread.sleep(2000L)
        Espresso.onView(ViewMatchers.withId(R.id.timestamp)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.pressBack()
        Thread.sleep(1000L)
        Espresso.onView(ViewMatchers.withId(R.id.rw_storico)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<BaseRecyclerView<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>
                .ViewHolder>(0, ViewActions.click())
        )
        Espresso.onView(ViewMatchers.withId(R.id.btn_ricevuta)).perform(ViewActions.click())
        Thread.sleep(500L)
        Espresso.pressBack()
        Espresso.pressBack()
        Thread.sleep(2000L)
        Espresso.onView(ViewMatchers.withId(it.pagopa.swc.smartpos.app_shared.R.id.iv_back)).perform(ViewActions.click())//showing Menu
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(RShared.id.rv_items)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<BaseRecyclerView<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>
                .ViewHolder>(0, ViewActions.click())
        )
        Thread.sleep(500L)
        function.invoke()
    }

    private fun fromIntroToAssistanceWebView(actionDone: Action) {
        Thread.sleep(3000L)
        Espresso.onView(ViewMatchers.withId(RuiKit.id.main_menu)).perform(ViewActions.click())
        Thread.sleep(500L)
        Espresso.onView(ViewMatchers.withId(RShared.id.rv_items)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<BaseRecyclerView<BaseBottomSheetMenu.ItemMenu, ItemMenuBinding>
                .ViewHolder>(2, ViewActions.click())
        )
        Thread.sleep(1000L)
        Espresso.pressBack()
        actionDone.invoke()
    }
}

typealias Action = () -> Unit