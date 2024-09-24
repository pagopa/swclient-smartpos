package it.pagopa.swc_smartpos.fragments.input_field

import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.fragments.BaseFragmentTest
import it.pagopa.swc_smartpos.fragments.utils.toInputText
import it.pagopa.swc_smartpos.fragments.utils.withIndex
import it.pagopa.swc_smartpos.ui_kit.input.InputText
import it.pagopa.swc_smartpos.view.InputFieldShowCase
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit


@RunWith(AndroidJUnit4::class)
class InputFieldShowCaseTest : BaseFragmentTest() {
    @Test
    fun testInputFieldShowCase() {
        testFragment<InputFieldShowCase> {
            val etInput11 = Espresso.onView(ViewMatchers.withId(R.id.input_field_11)).toInputText()
            val etInput5 = Espresso.onView(ViewMatchers.withId(R.id.input_field_5)).toInputText()
            val etInput18 = Espresso.onView(ViewMatchers.withId(R.id.input_field_18)).toInputText()
            val etInput11NoCtrl = Espresso.onView(ViewMatchers.withId(R.id.input_field_11_no_ctrl)).toInputText()
            Thread.sleep(1000L)
            etInput11.testWith(it, 0)
            Thread.sleep(1000L)
            etInput5.testWith(it, 1)
            Thread.sleep(1000L)
            etInput18.testWith(it, 2)
            Thread.sleep(1000L)
            etInput11NoCtrl.testWith(it, 3)
            Assert.assertEquals(etInput5?.getText(), "12345")
            Thread.sleep(1000L)
        }
    }

    private fun InputText?.testWith(frag: Fragment?, index: Int) {
        frag?.requireActivity()?.runOnUiThread {
            this?.setFocus(true)
            this?.setText("12345678901")
        }
        Espresso.onView(withIndex(ViewMatchers.withId(RUiKit.id.editTextInputText), index)).perform(ViewActions.pressImeActionButton())
    }
}