package it.pagopa.swc_smartpos

import SdkUtility
import android.animation.ObjectAnimator
import android.content.Context
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import it.pagopa.swc.smartpos.app_shared.BaseMainActivity
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.login.LoginUtility
import it.pagopa.swc.smartpos.app_shared.permission.PermissionHandler
import it.pagopa.swc_smartpos.databinding.ActivityMainBinding
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.network.coroutines.utils.NetworkLogger
import it.pagopa.swc_smartpos.second_screen.showSecondScreenIntro
import it.pagopa.swc_smartpos.second_screen.showSecondScreenLoader
import it.pagopa.swc_smartpos.second_screen.showSecondScreenWelcome
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.view.HelpedWaySubscribeFragment
import it.pagopa.swc_smartpos.view.MenuBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Mockable
class MainActivity : BaseMainActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override val navHostContainer: Int get() = R.id.nav_host_container
    override val mockEnv: Boolean = NetworkLogger.isMockEnv
    override val isPoynt: Boolean = BuildConfig.FLAVOR.contains("poynt", true)
    private val vm: MainViewModel by viewModels()
    override fun setupSdkUtils() {
        sdkUtils = SdkUtility(this, mockEnv)
    }

    override val viewModel: MainViewModel get() = vm
    override fun setTheme() {
        setTheme(R.style.Theme_SWCSMARTPOS)
    }

    override val loginUtility: LoginUtility
        get() = LoginUtility(
            this,
            HttpServiceInterface(CoroutineScope(Dispatchers.IO + SupervisorJob()))
        )

    override fun onShowingLoader(pair: Pair<Boolean, Boolean>) {
        val (merchantLoader, userLoader) = pair
        binding.loaderView.tvLoader.text = viewModel.loaderText.value
        binding.loaderView.root.isVisible = merchantLoader
        this.showSecondScreenLoader(viewModel.loaderText.value, userLoader)
    }

    override fun headerViewLogic(header: HeaderView?) {
        header?.let {
            with(binding.header) {
                root.isVisible = true
                it.bind(this@MainActivity, this@with)
            }
        } ?: run {
            binding.header.root.isVisible = false
        }
    }

    override fun setupOnCreate() {
        super.setupOnCreate()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { viewModel.keepSplashOnScreen }
        splashScreen.animate()
        this.observeLiveDataToken {
            viewModel.keepSplashOnScreen = false
            val sharedPreference =
                this.getSharedPreferences(PermissionHandler.PREFS_FILE_NAME, Context.MODE_PRIVATE)
            val subscriber =
                sharedPreference?.getString(HelpedWaySubscribeFragment.subscriberId, "").orEmpty()
            this.viewModel.setHelpedWay(subscriber)
        }
        lifecycleScope.launch {
            this@MainActivity.viewModel.hasSecondScreen.collectLatest {
                if (it) {
                    this@MainActivity.showSecondScreenWelcome()
                }
            }
        }
        if (isPoynt) {
            val navHost =
                this.supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
            val inflater = navHost.navController.navInflater
            val navGraph = inflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.introFragment)
            navHost.navController.graph = navGraph
        }
        NetworkLogger.enabled = BuildConfig.BUILD_TYPE.equals("debug", true)
        WrapperLogger.enabled = BuildConfig.BUILD_TYPE.equals("debug", true)
    }

    override fun backToLogin() {
        super.backToLogin()
        this.findNavController(R.id.nav_host_container).popBackStack(R.id.loginFragment, false)
    }

    private fun androidx.core.splashscreen.SplashScreen.animate() {
        this.setOnExitAnimationListener { splashViewProvider ->
            if (viewModel.animateIcon) {
                val fadeOut = ObjectAnimator.ofFloat(
                    if(mockEnv) splashViewProvider.view else splashViewProvider.iconView,
                    "alpha",
                    0f
                )
                fadeOut.interpolator = DecelerateInterpolator()
                fadeOut.duration = 1000L
                fadeOut.doOnEnd {
                    splashViewProvider.remove()
                    viewModel.animateIcon = false
                    if (isPoynt)
                        this@MainActivity.showSecondScreenIntro()
                }
                fadeOut.start()
            } else
                splashViewProvider.remove()
        }
    }

    fun setSubscriberId(value: String) {
        val sharedPreference =
            this.getSharedPreferences(PermissionHandler.PREFS_FILE_NAME, Context.MODE_PRIVATE)
        sharedPreference?.edit()?.putString(HelpedWaySubscribeFragment.subscriberId, value)?.apply()
        this.viewModel.setHelpedWay(value)
    }

    fun showMenuBottomSheet() {
        MenuBottomSheet(this, viewModel.subscriberId.value).showMenu()
    }
}