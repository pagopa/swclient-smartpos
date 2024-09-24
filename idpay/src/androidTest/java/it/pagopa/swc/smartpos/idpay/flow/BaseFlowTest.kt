package it.pagopa.swc.smartpos.idpay.flow

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import io.mockk.every
import io.mockk.spyk
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc_smartpos.sharedutils.model.Business
import it.pagopa.swc_smartpos.sharedutils.model.Payment
import it.pagopa.swc_smartpos.sharedutils.model.PaymentStatus
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Test

abstract class BaseFlowTest {
    val showFragments by lazy {
        ShowFragments()
    }

    @Test
    @CallSuper
    open fun launchActivity() {
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
    }

    companion object {
        fun sleepLess() {
            Thread.sleep(500L)
        }

        fun networkSleep() {
            Thread.sleep(2200L)
        }

        var currentActivity: MainActivity? = null
        private val currentFragmentMutable = MutableStateFlow<BaseDataBindingFragment<*, *>?>(null)
        private val currentFragment = currentFragmentMutable.asStateFlow()
        fun getCurrentFragment() = currentFragment.value
        fun setCurrentFragment() {
            val navHostContainer =
                currentActivity?.supportFragmentManager?.findFragmentById(it.pagopa.swc.smartpos.idpay.R.id.nav_host_container)
            currentFragmentMutable.value =
                navHostContainer?.childFragmentManager?.fragments?.get(0) as BaseDataBindingFragment<*, *>
        }
    }
}