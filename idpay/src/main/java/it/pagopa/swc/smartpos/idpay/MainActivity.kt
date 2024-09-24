package it.pagopa.swc.smartpos.idpay

import SdkUtility
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import it.pagopa.swc.smartpos.app_shared.BaseMainActivity
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.login.LoginUtility
import it.pagopa.swc.smartpos.idpay.databinding.ActivityMainBinding
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterface
import it.pagopa.swc.smartpos.idpay.second_screen.showSecondScreenIntro
import it.pagopa.swc.smartpos.idpay.second_screen.showSecondScreenLoader
import it.pagopa.swc.smartpos.idpay.second_screen.showSecondScreenWelcome
import it.pagopa.swc.smartpos.idpay.view.MenuBottomSheet
import it.pagopa.swc_smartpos.network.coroutines.utils.NetworkLogger
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.ui_kit.utils.FoldableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import it.pagopa.swc.smartpos.app_shared.R as RShared

class MainActivity : BaseMainActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override val navHostContainer: Int get() = R.id.nav_host_container
    override val mockEnv: Boolean = NetworkLogger.isMockEnv
    override val isPoynt: Boolean = BuildConfig.FLAVOR.contains("poynt", true)
    private val vm: MainViewModel by viewModels()

    @SuppressLint("InflateParams")
    override var updateActivityView: (foldableState: FoldableState) -> Unit = {
        val newMainParent: ConstraintLayout =
            LayoutInflater.from(this).inflate(RShared.layout.app_header, null) as ConstraintLayout
        updateHeaderAndLoader(binding.header.root, newMainParent)
        val newLoader: FrameLayout =
            LayoutInflater.from(this).inflate(RShared.layout.loader_layout, null) as FrameLayout
        updateHeaderAndLoader(binding.loaderView.root, newLoader, true)
    }

    override fun setupSdkUtils() {
        sdkUtils = SdkUtility(this, mockEnv)
    }

    override val viewModel: MainViewModel get() = vm
    override val loginUtility: LoginUtility =
        LoginUtility(this, HttpServiceInterface(CoroutineScope(Dispatchers.IO + SupervisorJob())))

    override fun setTheme() {
        setTheme(R.style.Theme_SWCSMARTPOS)
    }

    override fun setupOnCreate() {
        super.setupOnCreate()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { viewModel.keepSplashOnScreen }
        splashScreen.animate()
        this.observeLiveDataToken {
            viewModel.keepSplashOnScreen = false
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
        NetworkLogger.enabled = BuildConfig.DEBUG
        WrapperLogger.enabled = BuildConfig.DEBUG
    }

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

    override fun backToLogin() {
        super.backToLogin()
        this.findNavController(R.id.nav_host_container).popBackStack(R.id.loginFragment, false)
    }

    override fun setupOnResume() {
        super.setupOnResume()
        sdkUtils?.bindCardReaderService {
            WrapperLogger.i("CardReaderService", "available")
        }
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

    override fun setupOnPause() {
        super.setupOnPause()
        sdkUtils?.unBindCardReaderService()
    }

    fun showMenuBottomSheet() {
        MenuBottomSheet(this).showMenu()
    }
}