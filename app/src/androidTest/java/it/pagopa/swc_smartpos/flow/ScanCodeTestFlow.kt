package it.pagopa.swc_smartpos.flow

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc_smartpos.sharedutils.qrCode.QrCode
import it.pagopa.swc_smartpos.view.ScanCodeFragment
import org.hamcrest.Matchers
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit
import it.pagopa.swc_smartpos.sharedUtils.R as RSharedUtils

class ScanCodeTestFlow {
    fun testBackPress() {
        Espresso.pressBack()
    }

    fun testClose() {
        Espresso.onView(ViewMatchers.withId(RSharedUtils.id.close_btn)).perform(ViewActions.click())
    }

    private fun someError(error: String, doFirstAction: Boolean = true, done: Action) {
        FlowTest.setCurrentFragment()
        Thread.sleep(1000L)
        val frag = FlowTest.getCurrentFragment() as? ScanCodeFragment
        frag?.actionScanned?.invoke(
            QrCode(
                 "PAGOPA|002|302051234567890124|$error|11741",//qr code not accepted
                "QRCODE"
            )
        )
        Thread.sleep(2500L)
        if(doFirstAction) {
            val parentMatch = ViewMatchers.withParent(ViewMatchers.withId(RUiKit.id.main_ll_result))
            Espresso
                .onView(Matchers.allOf(parentMatch, ViewMatchers.withId(RUiKit.id.btn_continue), ViewMatchers.isDisplayed()))
                .perform(ViewActions.click())
            Thread.sleep(1000L)
            Navigation.fromIntroToScanCode()
            Thread.sleep(1000L)
        }
        done.invoke()
    }

    fun scanCodeAllErrors(done: Action) {
        scanCodeNoticeGlitch {
            scanCodeWrongNoticeData {
                scanCodeCreditorProblems {
                    scanCodePaymentAlreadyInProgress {
                        scanCodeExpiredNotice {
                            scanCodeUnknownNotice {
                                scanCodeRevokedNotice {
                                    scanCodeNoticeAlreadyPaid {
                                        scanCodeUnExpectedError {
                                           done.invoke()
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

    private fun scanCodeNoticeGlitch(done: Action) {
        someError("00000000001", done = done)
    }

    private fun scanCodeWrongNoticeData(done: Action) {
        someError("00000000002", done = done)
    }

    private fun scanCodeCreditorProblems(done: Action) {
        someError("00000000003", done = done)
    }

    private fun scanCodePaymentAlreadyInProgress(done: Action) {
        someError("00000000004", done = done)
    }

    private fun scanCodeExpiredNotice(done: Action) {
        someError("00000000005", done = done)
    }

    private fun scanCodeUnknownNotice(done: Action) {
        someError("00000000006", done = done)
    }

    private fun scanCodeRevokedNotice(done: Action) {
        someError("00000000007", done = done)
    }

    private fun scanCodeNoticeAlreadyPaid(done: Action) {
        someError("00000000008", done = done)
    }

    private fun scanCodeUnExpectedError(done: Action) {
        someError("00000000009", done = done)
    }

    fun scanCode(done: Action) {
        FlowTest.setCurrentFragment()
        Thread.sleep(1000L)
        val frag = FlowTest.getCurrentFragment() as? ScanCodeFragment
        frag?.actionScanned?.invoke(
            QrCode(
                "PAGOPA|002|302051234567890111|00000000201|9999",
                "QRCODE"
            )
        )
        Thread.sleep(2500L)
        done.invoke()
    }
}