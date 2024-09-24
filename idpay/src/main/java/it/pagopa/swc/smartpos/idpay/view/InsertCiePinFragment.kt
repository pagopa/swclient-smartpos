package it.pagopa.swc.smartpos.idpay.view

import android.util.Base64
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.InsertCiePinBinding
import it.pagopa.swc.smartpos.idpay.model.request.AuthorizeRequest
import it.pagopa.swc.smartpos.idpay.model.response.VerifyCieResponse
import it.pagopa.swc.smartpos.idpay.network.NetworkErrorMessageWrapper
import it.pagopa.swc.smartpos.idpay.second_screen.showCieCodeSecondScreen
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.utils.AesEncrypt
import it.pagopa.swc.smartpos.idpay.view.view_shared.acceptNewBonus
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import it.pagopa.swc.smartpos.idpay.view.view_shared.errorDialogWithMoreDescr
import it.pagopa.swc.smartpos.idpay.view.view_shared.genericErrorDialog
import it.pagopa.swc.smartpos.idpay.view.view_shared.idPayOpNavigateSuccess
import it.pagopa.swc.smartpos.idpay.view_model.InsertCiePinViewModel
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.disablePrimaryButton
import it.pagopa.swc_smartpos.ui_kit.utils.enablePrimaryButton
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.Serializable
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import it.pagopa.swc.smartpos.app_shared.R as RShared

class InsertCiePinFragment : BaseDataBindingFragmentApp<InsertCiePinBinding>() {
    private val viewModel: InsertCiePinViewModel by viewModels()
    override val layoutId: Int get() = R.layout.insert_cie_pin
    override val backPress: () -> Unit get() = {}
    private val pinAttemptsDialog by lazy {
        UiKitStyledDialog
            .withStyle(Style.Warning)
            .withTitle(getTextSafely(R.string.dialog_pin_attempts_title))
            .withMainBtn(getTextSafely(it.pagopa.swc.smartpos.app_shared.R.string.cta_retry)) {
                viewModel.setPin("")
                binding.customKeyboard.clearText()
            }
            .withSecondaryBtn(getTextSafely(R.string.exit_payment)) {
                cancelOp {
                    this.backToIntroFragment()
                }
            }
    }

    override fun viewBinding() = binding(InsertCiePinBinding::inflate)
    private val exitDialog by lazy {
        UiKitDialog
            .withTitle(getTextSafely(R.string.exit_from_current_op))
            .withDescription(getTextSafely(R.string.exit_from_current_op_description))
            .withMainBtn(getTextSafely(R.string.cta_cancel))
            .withSecondaryBtn(getTextSafely(R.string.exit_payment)) {
                cancelOp {
                    this.backToIntroFragment()
                }
            }
    }
    override val header: HeaderView = HeaderView(
        null,
        null,
        HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary) {
            exitDialog.showDialog(mainActivity?.supportFragmentManager)
        },
        RShared.color.grey_light
    )

    private fun cancelOp(actionDone: () -> Unit) {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(RShared.string.feedback_loading_generic))
        mainActivity?.viewModel?.showLoader(true to true)
        accessTokenLambda { bearer ->
            viewModel.cancelOp(
                mainActivity!!, bearer, mainActivity?.sdkUtils?.getCurrentBusiness()?.value, viewModel.uiModel.value?.transactionId.orEmpty()
            ).observe(
                viewLifecycleOwner, BaseWrapper(mainActivity, successAction = {
                    actionDone.invoke()
                }, errorAction = {
                    if (it == BaseWrapper.tokenRefreshed)
                        cancelOp(actionDone)
                }, showLoader = true, showDialog = true)
            )
        }
    }

    override fun setupOnCreate() {
        super.setupOnCreate()
        viewModel.setUiModel(arguments?.getSerializableExtra(uiModel, UiModel::class.java))
    }

    override fun setupUI() {
        super.setupUI()
        mainActivity?.viewModel?.getKeyBackStack<Pair<String, Boolean>>()?.let {
            val (key, value) = it
            if (key == "clearPin" && value) {
                viewModel.setPin("")
                viewModel.pinAttempts = 3
                binding.customKeyboard.clearText()
            }
        }
        mainActivity?.showCieCodeSecondScreen()
        binding.customKeyboard.doAfterTextChanged = {
            WrapperLogger.i("pin changing", it)
            viewModel.setPin(it)
        }
    }

    override fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pin.collectLatest {
                    if (it.length >= 4)
                        binding.btnConfirm.enablePrimaryButton(mainActivity)
                    else
                        binding.btnConfirm.disablePrimaryButton(mainActivity)
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiModel.collectLatest {
                    binding.btnConfirm.isEnabled = it != null
                }
            }
        }
    }


    private fun callAuthorize(data: AuthorizeRequest.AuthCodeData) {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.auth_now))
        mainActivity?.viewModel?.showLoader(true to true)
        accessTokenLambda { bearer ->
            viewModel.authorize(
                mainActivity!!, bearer,
                mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
                AuthorizeRequest(data),
                viewModel.uiModel.value?.transactionId.orEmpty()
            ).observe(viewLifecycleOwner, NetworkErrorMessageWrapper(mainActivity,
                successAction = {
                    this.idPayOpNavigateSuccess(mainActivity?.viewModel?.model?.value?.availableSale?.toAmountFormatted().orEmpty())
                }, errorAction = {
                    if (it == BaseWrapper.tokenRefreshed)
                        callAuthorize(data)
                }, error400 = {
                    if (it?.contains("00A000053") == true) {
                        viewModel.pinAttempts--
                        if (viewModel.pinAttempts > 0) {
                            pinAttemptsDialog
                                .withDescription(
                                    getStringSafelyWithOneArg(
                                        if (viewModel.pinAttempts > 1)
                                            R.string.dialog_pin_attempts_description
                                        else
                                            R.string.dialog_pin_attempt_description, "${viewModel.pinAttempts}"
                                    )
                                )
                                .showDialog(mainActivity?.supportFragmentManager)
                        } else
                            navigateToAttemptsExhausted()
                    } else
                        mainActivity?.genericErrorDialog()
                })
            )
        }
    }

    private fun navigateToAttemptsExhausted() {
        val bundle = bundleOf(
            BaseResultFragment.stateArg to BaseResultFragment.State.Warning,
            BaseResultFragment.titleArg to R.string.pin_attempts_exhausted_title,
            BaseResultFragment.descriptionArg to R.string.pin_attempts_exhausted_description,
            BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                getTextSafely(R.string.accept_new_bonus)
            ) {
                val frag = it as ResultFragment
                frag.viewModel.cancelOp(frag){
                    frag.acceptNewBonus(frag.viewModel)
                }
            },
            ResultFragment.operationState to ResultFragment.OperationState.PIN_ATTEMPTS_EXHAUSTED
        )
        navigate(R.id.action_global_resultFragment, bundle)
    }

    override fun setupListeners() {
        binding.btnConfirm.setOnClickListener {
            viewModel.uiModel.value?.let { uiModel ->
                WrapperLogger.i("PIN inserted", viewModel.pin.value)
                val eDecoded = Base64.decode(uiModel.response.e, Base64.DEFAULT)
                WrapperLogger.d("eDecoded", "$eDecoded")
                val publicExponent = BigInteger(1, eDecoded)
                WrapperLogger.d("publicExponent", "$publicExponent")
                val nDecoded = Base64.decode(uiModel.response.n, Base64.DEFAULT)
                WrapperLogger.d("nDecoded", "$nDecoded")
                val modulus = BigInteger(1, nDecoded)
                WrapperLogger.d("modulus", "$modulus")
                val pubKeySpec = RSAPublicKeySpec(modulus, publicExponent)
                val keyFactory = KeyFactory.getInstance("RSA")
                val public = keyFactory.generatePublic(pubKeySpec) as RSAPublicKey
                WrapperLogger.d("PublicKey", "$public")
                val encryptClass = AesEncrypt()
                val (generated, pinBlock) = encryptClass.PinBlock(viewModel.uiModel.value?.secondFactor.orEmpty()).generate(viewModel.pin.value)
                WrapperLogger.d("PinBlock", "generated: $generated; pinBlock: $pinBlock")
                if (!generated)
                    this.errorDialogWithMoreDescr(pinBlock) {
                        cancelOp {
                            this.backToIntroFragment()
                        }
                    }
                else {
                    val data = AuthorizeRequest.AuthCodeData(
                        kid = uiModel.response.kid,
                        authCodeBlock = encryptClass.encrypt(pinBlock),
                        encSessionKey = encryptClass.RsaWithSameSecretKey().encryptSessionKeyWithRsa(public)
                    )
                    callAuthorize(data)
                }
            }
        }
    }

    data class UiModel(
        val euroCents: Long, val epochMillis: Long, val nis: String,
        val secondFactor: String,
        val response: VerifyCieResponse,
        val transactionId: String
    ) : Serializable

    companion object {
        const val uiModel = "uiModel"
    }
}