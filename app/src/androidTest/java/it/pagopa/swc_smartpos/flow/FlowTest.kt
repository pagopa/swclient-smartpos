package it.pagopa.swc_smartpos.flow

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import io.mockk.every
import io.mockk.spyk
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.sharedutils.model.Business
import it.pagopa.swc_smartpos.sharedutils.model.Payment
import it.pagopa.swc_smartpos.sharedutils.model.PaymentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FlowTest {
    private val showFragments by lazy {
        ShowFragments()
    }

    @Test
    fun launchActivity() {
        ActivityScenario.launch(MainActivity::class.java).onActivity { mainActivity ->
            currentActivity = mainActivity
        }
        val spySdkUtils = spyk(currentActivity!!.sdkUtils!!)
        every { spySdkUtils.launchPayment(any(), "payment_app") } answers {
            currentActivity!!.viewModel.setPayment(Payment("", PaymentStatus.COMPLETED))
        }
        every { spySdkUtils.getCurrentBusiness() } answers {
            MutableLiveData(Business("fakeId", "30390022", "999999600307", listOf("4585625")))
        }
        currentActivity!!.sdkUtils = spySdkUtils
        Thread.sleep(3000L)
        showFragments.flow(currentActivity!!)
    }


    companion object {
        private val currentFragmentMutable = MutableStateFlow<Fragment?>(null)
        private val currentFragment = currentFragmentMutable.asStateFlow()
        var currentActivity: MainActivity? = null
        fun setCurrentFragment() {
            val navHostContainer =
                currentActivity?.supportFragmentManager?.findFragmentById(R.id.nav_host_container)
            currentFragmentMutable.value = navHostContainer?.childFragmentManager?.fragments?.get(0)
        }

        fun manageAskedPermission(allow: Boolean): Boolean {
            val device = UiDevice.getInstance(getInstrumentation())
            val allowPermissions = device.findObject(
                UiSelector()
                    .clickable(true)
                    .checkable(false)
                    .index(if (allow) 0 else 2)
            )
            if (allowPermissions.exists()) {
                return try {
                    allowPermissions.click()
                    true
                } catch (e: UiObjectNotFoundException) {
                    Log.e(e.javaClass.name, "There is no permissions dialog to interact with ")
                    false
                }
            } else if (!allow) {
                val denyPermission = device.findObject(
                    UiSelector()
                        .clickable(true)
                        .checkable(false)
                        .index(1)
                )
                if (denyPermission.exists()) {
                    return try {
                        allowPermissions.click()
                        true
                    } catch (e: UiObjectNotFoundException) {
                        Log.e(e.javaClass.name, "There is no permissions dialog to interact with ")
                        false
                    }
                }
            }
            return false
        }

        fun getCurrentFragment() = currentFragment.value
    }
}