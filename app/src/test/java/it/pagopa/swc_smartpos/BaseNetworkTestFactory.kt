@file:Suppress("DEPRECATION")

package it.pagopa.swc_smartpos

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito
import kotlin.properties.Delegates

open class BaseNetworkTestFactory {
    var ctx: Context? = null
    private var version by Delegates.notNull<Int>()
    private var connectivityManager: ConnectivityManager? = null
    private var activeNetwork: NetworkCapabilities? = null
    private var activeNetworkInfo: NetworkInfo? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var coroutinesTestRule = CoroutineHelper()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockServer = MockWebServer()

    @Before
    fun before() {
        ctx = mockk()
        connectivityManager = mockk()
        activeNetwork = mockk()
        activeNetworkInfo = mockk()
        version = Build.VERSION.SDK_INT
    }

    @After
    fun after() {
        ctx = null
    }

    fun prepareNetwork(wifi: Boolean = true, cellular: Boolean = true, ethernet: Boolean = true) {
        every { activeNetworkInfo!!.isConnected } returns true
        every { activeNetwork!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns wifi
        every { activeNetwork!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns cellular
        every { activeNetwork!!.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns ethernet
        every { connectivityManager!!.getNetworkCapabilities(connectivityManager!!.activeNetwork) } returns activeNetwork
        every { connectivityManager!!.activeNetworkInfo } returns activeNetworkInfo
        every { ctx!!.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    }

    fun provideLifecycleOwner(): LifecycleOwner {
        val lifecycleOwner: LifecycleOwner = Mockito.mock(LifecycleOwner::class.java)
        val lifecycle = LifecycleRegistry(Mockito.mock(LifecycleOwner::class.java))
        lifecycle.currentState = Lifecycle.State.RESUMED
        Mockito.`when`(lifecycleOwner.lifecycle).thenReturn(lifecycle)
        return lifecycleOwner
    }
}