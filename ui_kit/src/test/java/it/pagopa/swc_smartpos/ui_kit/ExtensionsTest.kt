package it.pagopa.swc_smartpos.ui_kit

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPx
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExtensionsTest {
    private var ctx: Context? = null
    private var res: Resources? = null
    private var displayMetrics: DisplayMetrics? = null
    private var typedValue: TypedValue? = null

    @Before
    fun before() {
        ctx = mockk()
        res = mockk()
        displayMetrics = mockk()
        typedValue = mockk()
    }

    @After
    fun after() {
        ctx = null
        res = null
        displayMetrics = null
        typedValue = null
    }

    @Test
    fun dpToPxTest() {
        mockkStatic(TypedValue::class)
        every { TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, displayMetrics) } returns 3.5f
        every { res!!.displayMetrics } returns displayMetrics
        every { ctx!!.resources } returns res
        assert((ctx!! dpToPx 4f) != 0)
    }
}