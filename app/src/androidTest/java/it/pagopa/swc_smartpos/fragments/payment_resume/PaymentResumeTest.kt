package it.pagopa.swc_smartpos.fragments.payment_resume

import android.os.Bundle
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.fragments.BaseFragmentTest
import it.pagopa.swc_smartpos.fragments.utils.withIndex
import it.pagopa.swc_smartpos.model.QrCodeVerifyResponse
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.view.PaymentResumeFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentResumeTest : BaseFragmentTest() {
    @Test
    fun paymentResumeTest() {
        testFragment<PaymentResumeFragment>(Bundle().apply {
            putSerializable(
                PaymentResumeFragment.qrCodeParam, QrCodeVerifyResponse(
                    1000,
                    "companyTest",
                    "descriptionTest",
                    "paTaxCodeTest",
                    "NoticeNumberTest",
                    "NoteTest",
                    "OfficeTest"
                )
            )
        }) {
            Espresso
                .onView(withIndex(ViewMatchers.withId(R.id.item_description), 0))
                .check(ViewAssertions.matches(ViewMatchers.withText("companyTest")))
            Espresso
                .onView(withIndex(ViewMatchers.withId(R.id.item_description), 1))
                .check(ViewAssertions.matches(ViewMatchers.withText("descriptionTest")))
            Espresso
                .onView(withIndex(ViewMatchers.withId(R.id.item_description), 2))
                .check(ViewAssertions.matches(ViewMatchers.withText(1000.toAmountFormatted())))
            Espresso
                .onView(withIndex(ViewMatchers.withId(R.id.item_description), 3))
                .check(ViewAssertions.matches(ViewMatchers.withText("NoticeNumberTest")))
            Espresso
                .onView(withIndex(ViewMatchers.withId(R.id.item_description), 4))
                .check(ViewAssertions.matches(ViewMatchers.withText("paTaxCodeTest")))
            Thread.sleep(1000L)
        }
    }

    @Test
    fun paymentResumeTestNoArg() {
        testFragment<PaymentResumeFragment> {
        }
    }
}