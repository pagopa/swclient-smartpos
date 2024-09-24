package it.pagopa.swc_smartpos.fragments.payment_amount_resume

import android.os.Bundle
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.fragments.BaseFragmentTest
import it.pagopa.swc_smartpos.view.PaymentReceiptFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentAmountResumeTest : BaseFragmentTest() {
    @Test
    fun paymentAmountResumeTest() {
        testFragment<PaymentReceiptFragment>(Bundle().apply {
            putInt(PaymentReceiptFragment.amountArg, 10000)
            putInt(PaymentReceiptFragment.feeArg, 100)
            putString(PaymentReceiptFragment.paymentObjectArg, "Test")
        }) {
            Thread.sleep(1000L)
            Espresso.onView(ViewMatchers.withId(R.id.total_value)).check(ViewAssertions.matches(ViewMatchers.withText("101,00 €")))
        }
    }

    @Test
    fun paymentAmountResumeTestNoArg() {
        testFragment<PaymentReceiptFragment> {
        }
    }
}