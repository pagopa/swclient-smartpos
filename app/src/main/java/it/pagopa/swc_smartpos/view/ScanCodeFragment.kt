package it.pagopa.swc_smartpos.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc_smartpos.BuildConfig
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.network.NetworkWrapper
import it.pagopa.swc_smartpos.second_screen.showQrCodeSample
import it.pagopa.swc_smartpos.sharedutils.camera.CustomScannerFragment
import it.pagopa.swc_smartpos.sharedutils.qrCode.QrCode
import it.pagopa.swc_smartpos.sharedutils.vibrate.VibrateManager
import it.pagopa.swc_smartpos.ui_kit.utils.findActivity
import it.pagopa.swc_smartpos.view.view_shared.accessTokenLambda
import it.pagopa.swc_smartpos.view_model.ScanCodeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScanCodeFragment : CustomScannerFragment() {
    private val viewModel: ScanCodeViewModel by viewModels()
    override val backPress: () -> Unit
        get() = {
            findNavController().popBackStack(
                R.id.introFragment,
                false
            )
        }
    override val closeAction: () -> Unit get() = backPress
    override val actionScanned: (QrCode) -> Unit
        get() = {
            activity?.runOnUiThread {
                it.launchQrCodeAction()
            }
        }

    override fun setupOnCreate() {
        super.setupOnCreate()
        (activity as? MainActivity)?.viewModel?.setKeepScreenOn(false)
    }

    private fun QrCode.launchQrCodeAction() {
        VibrateManager().vibrate(context, 500)
        val mainActivity = activity as? MainActivity ?: context.findActivity() as? MainActivity
        mainActivity?.viewModel?.setLoaderText(
            getStringSafely(
                mainActivity,
                R.string.feedback_loading_verifyNotice
            )
        )
        mainActivity?.viewModel?.showLoader(true to true)
        accessTokenLambda { accessToken ->
            viewModel.verifyQrCode(
                mainActivity!!, this.code, accessToken,
                mainActivity.sdkUtils?.getCurrentBusiness()?.value
            ).observe(viewLifecycleOwner, NetworkWrapper(mainActivity, {
                findNavController().navigate(
                    R.id.action_scanCodeFragment_to_paymentResumeFragment,
                    Bundle().apply {
                        putSerializable(PaymentResumeFragment.qrCodeParam, it.apply {
                            this?.originalCode = this@launchQrCodeAction.code
                        })
                    })
            }, {
                VibrateManager().vibrate(context, 1000)
                if (it != 200) {//already managed by NetworkWrapper
                    when (it) {
                        BaseWrapper.tokenRefreshed -> this.launchQrCodeAction()
                        400 -> {
                            resumeScannerView()
                            mainActivity.showQrCodeSample()
                        }

                        5050 -> findNavController().navigate(R.id.action_scanCodeFragment_to_insertManuallyFragment)
                        else -> findNavController().popBackStack(R.id.introFragment, false)
                    }
                }
            }, isForQrCode = true))
        }
    }

    private fun getStringSafely(mainActivity: MainActivity?, id: Int) =
        mainActivity?.resources?.getString(id).orEmpty()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as? MainActivity ?: context.findActivity() as? MainActivity
        activity.showQrCodeSample()
        //Remember: Comment these lines for Android Tests
        if (activity?.mockEnv == true) {
            viewModel.viewModelScope.launch {
                delay(3000L)
                actionScanned.invoke(
                    QrCode(
                        "PAGOPA|002|302051234567890111|00000000201|9999",
                        "QRCODE"
                    )
                )
            }
        }
    }
}