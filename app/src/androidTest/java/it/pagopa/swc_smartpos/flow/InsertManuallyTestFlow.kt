package it.pagopa.swc_smartpos.flow

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.fragments.utils.toInputText
import it.pagopa.swc_smartpos.view.InsertManuallyFragment

class InsertManuallyTestFlow {
    fun flow(actionDone: () -> Unit) {
        FlowTest.setCurrentFragment()
        Thread.sleep(1000L)
        val frag = FlowTest.getCurrentFragment() as? InsertManuallyFragment
        val etInput11 = Espresso.onView(ViewMatchers.withId(R.id.inputText_insert_manually)).toInputText()
        frag?.requireActivity()?.runOnUiThread {
            etInput11?.setText("302051234567890111")
        }
        Thread.sleep(1000L)
        
        Thread.sleep(1000L)

        frag?.requireActivity()?.runOnUiThread {
            etInput11?.setText("0000000020112121212")
        }
        Thread.sleep(1000L)


        Thread.sleep(2500L)
        actionDone.invoke()
    }
}