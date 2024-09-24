package it.pagopa.swc.smartpos.idpay.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.BuildConfig
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.IntroBindingBinding
import it.pagopa.swc.smartpos.idpay.second_screen.showSecondScreenIntro
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import it.pagopa.swc.smartpos.idpay.view_model.IntroViewModel
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.AnimationEndListener
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc.smartpos.app_shared.R as RShared
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class IntroFragment : BaseDataBindingFragmentApp<IntroBindingBinding>() {
    private val viewModel: IntroViewModel by viewModels()
    override val layoutId: Int get() = R.layout.intro_binding
    override fun viewBinding() = binding(IntroBindingBinding::inflate)
    override val backPress: () -> Unit
        get() = {
            UiKitDialog
                .withTitle(getTextSafely(R.string.title_exitAppDialog))
                .withMainBtn(getTextSafely(R.string.cta_cancel))
                .withSecondaryBtn(getTextSafely(R.string.cta_exitApp)) {
                    mainActivity?.finishAndRemoveTask()
                }
                .showDialog(mainActivity?.supportFragmentManager)
        }
    override val header: HeaderView? = null

    private fun callList() {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(RShared.string.feedback_loading_generic))
        mainActivity?.viewModel?.showLoader(true to false)
        accessTokenLambda { bearer ->
            viewModel.callList(
                mainActivity!!,
                bearer,
                mainActivity?.sdkUtils?.getCurrentBusiness()?.value
            ).observe(viewLifecycleOwner, BaseWrapper(mainActivity, successAction = {
                findNavController().navigate(
                    R.id.action_introFragment_to_chooseInitiative,
                    bundleOf(ChooseInitiative.initiativesArg to it)
                )
            }, errorAction = {
                if (it == BaseWrapper.tokenRefreshed)
                    callList()
                else
                    findNavController().navigate(R.id.action_global_resultFragment, Bundle().apply {
                        putBoolean(ResultFragment.backHome, true)
                        this.putSerializable(
                            BaseResultFragment.stateArg,
                            BaseResultFragment.State.Error
                        )
                        this.putInt(
                            BaseResultFragment.titleArg,
                            R.string.title_errorLoadingTransactionList
                        )
                        this.putInt(BaseResultFragment.descriptionArg, R.string.paragraph_tryAgain)
                        this.putSerializable(
                            BaseResultFragment.firstButtonArg,
                            CustomBtnCustomizer(getTextSafely(RShared.string.cta_goToHomepage)) { frag ->
                                frag.findNavController().popBackStack(R.id.introFragment, false)
                            })
                    })
            }, showLoader = true, showSecondScreenLoader = false, showDialog = false))
        }
    }

    override fun setupListeners() {
        binding.menu.setOnClickListener {
            mainActivity?.showMenuBottomSheet()
        }
        binding.mainBtn.setOnClickListener {
            callList()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun setupUI() {
        super.setupUI()
        if (mainActivity?.isPoynt != true)
            mainActivity?.showSecondScreenIntro()
        binding.mainBtn.visibility = View.INVISIBLE
        binding.llTitleAndDescription.visibility = View.INVISIBLE
        val fadeIn = AnimationUtils.loadAnimation(context, RUiKit.anim.animation_fade_in)
        val fadeIn2 = AnimationUtils.loadAnimation(context, RUiKit.anim.animation_fade_in)
        val fadeIn3 = AnimationUtils.loadAnimation(context, RUiKit.anim.animation_fade_in)
        fadeIn?.setAnimationListener(AnimationEndListener {
            binding.llTitleAndDescription.startAnimation(fadeIn2)
            binding.llTitleAndDescription.isVisible = true
        })
        fadeIn2?.setAnimationListener(AnimationEndListener {
            binding.mainBtn.startAnimation(fadeIn3)
            binding.mainBtn.isVisible = true
        })
        binding.logo.startAnimation(fadeIn)

        if (BuildConfig.DEBUG) {
            binding.version.visibility = View.VISIBLE
            binding.version.text = "V. ${BuildConfig.testName}"
        }
    }
}