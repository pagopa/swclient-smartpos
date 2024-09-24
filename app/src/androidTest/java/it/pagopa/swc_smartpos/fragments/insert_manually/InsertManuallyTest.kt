package it.pagopa.swc_smartpos.fragments.insert_manually

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.fragments.BaseFragmentTest
import it.pagopa.swc_smartpos.fragments.utils.toInputText
import it.pagopa.swc_smartpos.fragments.utils.withIndex
import it.pagopa.swc_smartpos.view.InsertManuallyFragment
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import it.pagopa.swc_smart_pos.ui_kit.R as RUikit

@RunWith(AndroidJUnit4::class)
class InsertManuallyTest : BaseFragmentTest() {
    @Test
    fun testInsertManuallyFragment() {
        testFragment<InsertManuallyFragment> {
            Thread.sleep(500L)
            val etInput11 = Espresso.onView(ViewMatchers.withId(R.id.inputText_insert_manually)).toInputText()
            var textToCompare = ""
            it?.requireActivity()?.runOnUiThread {
                etInput11?.setText("123456789012345678")
                textToCompare = etInput11?.getText().orEmpty()
            }
            Thread.sleep(500L)
            Assert.assertEquals(textToCompare, "123456789012345678")
            Thread.sleep(500L)
            Espresso.onView(withIndex(ViewMatchers.withId(RUikit.id.editTextInputText), 0)).perform(ViewActions.pressImeActionButton())
            Thread.sleep(500L)
            it?.requireActivity()?.runOnUiThread {
                etInput11?.setText("123456789012345678")
                textToCompare = etInput11?.getText().orEmpty()
            }
            Thread.sleep(500L)
            Assert.assertEquals(textToCompare, "12345678901")
            Thread.sleep(500L)
            it?.requireActivity()?.runOnUiThread {
                etInput11?.setText("123456")
                textToCompare = etInput11?.getText().orEmpty()
            }
            Espresso.onView(withIndex(ViewMatchers.withId(RUikit.id.editTextInputText), 0)).perform(ViewActions.pressImeActionButton())
            Assert.assertEquals(textToCompare, "123456")
        }
    }
}