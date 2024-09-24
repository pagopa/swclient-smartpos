package it.pagopa.swc_smartpos.view

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import it.pagopa.swc.smartpos.app_shared.login.LoginUtility
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequest
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import it.pagopa.swc_smartpos.second_screen.showSecondScreenWelcome
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseFormFragment
import it.pagopa.swc_smartpos.ui_kit.input.InputText
import it.pagopa.swc_smartpos.ui_kit.toast.UiKitToast
import it.pagopa.swc_smartpos.ui_kit.utils.disablePrimaryButton
import it.pagopa.swc_smartpos.ui_kit.utils.enablePrimaryButton
import it.pagopa.swc_smartpos.ui_kit.utils.hideKeyboard
import it.pagopa.swc_smartpos.ui_kit.utils.setupUiKitToast
import it.pagopa.swc_smartpos.view_model.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit
import it.pagopa.swc.smartpos.app_shared.R as RShared

class LoginFragment : BaseFormFragment<MainActivity>() {
    private val viewModel: LoginViewModel by viewModels()
    override val backPress: () -> Unit get() = { mainActivity?.finishAndRemoveTask() }
    private val inputOne by lazy {
        InputText(requireContext()).apply {
            setInputType(InputText.InputTypeInputText.Text)
            inputTypeOnlyAlphaNumeric()
            setHint(getTextSafely(R.string.label_username))
            setTextChunk(0)
            onAction = { text, _ ->
                viewModel.setInputOne(text)
            }
            actionDone = { _, _ ->
                inputTwo.setFocus(true)
            }
        }
    }
    private val inputTwo by lazy {
        InputText(requireContext()).apply {
            setInputType(InputText.InputTypeInputText.Password)
            inputTypeOnlyAlphaNumeric()
            setHint(getTextSafely(R.string.label_password))
            setTextChunk(0)
            onAction = { text, _ ->
                viewModel.setInputTwo(text)
            }
            actionDone = { _, _ ->
                if (binding.btnForm.isEnabled)
                    binding.btnForm.performClick()
            }
        }
    }
    override val inputTextArrays: Array<InputText> get() = arrayOf(inputOne, inputTwo)
    override val image: Int get() = RUiKit.drawable.logo_blu
    override val title: Int get() = R.string.title_loginTestEnv
    override val buttonText: Int get() = R.string.cta_login
    override val buttonAction: () -> Unit
        get() = {
            if (!binding.btnForm.isLoading) {
                val map = LoginUtility(
                    mainActivity,
                    HttpServiceInterface(CoroutineScope(Dispatchers.IO))
                ).getLoginValidationMap()
                viewModel.doLogin(
                    mainActivity!!,
                    LoginRequest(
                        username = inputOne.getText().orEmpty(),
                        password = inputTwo.getText().orEmpty(),
                        clientId = map?.get(LoginUtility.LoginValidation.ClientId).orEmpty(),
                        grantType = map?.get(LoginUtility.LoginValidation.GrantType).orEmpty(),
                        scope = map?.get(LoginUtility.LoginValidation.Scope).orEmpty()
                    ),
                    mainActivity?.sdkUtils?.getCurrentBusiness()?.value
                ).observe(viewLifecycleOwner) {
                    when (it.status) {
                        Status.ERROR -> {
                            this.backPressActionEnabled.value = true
                            loginError(it.message, it.code)
                        }

                        Status.LOADING -> {
                            this.backPressActionEnabled.value = false
                            binding.btnForm.showLoading(true)
                        }

                        Status.SUCCESS -> {
                            it.data?.let { response ->
                                mainActivity?.encrypt(response.accessToken) { listEncrypted ->
                                    mainActivity!!.viewModel.setAccessToken(listEncrypted)
                                    mainActivity!!.encrypt(response.refreshToken) { listRefreshEncrypted ->
                                        mainActivity!!.viewModel.setRefreshToken(
                                            listRefreshEncrypted
                                        )
                                        binding.btnForm.showLoading(false)
                                        inputOne.setText("")
                                        inputTwo.setText("")
                                        this.backPressActionEnabled.value = true
                                        findNavController().navigate(R.id.action_loginFragment_to_introFragment)
                                    }
                                }
                            } ?: run {
                                loginError("", null)
                            }
                        }
                    }
                }
                context.hideKeyboard()
            }
        }

    private fun loginError(message: String?, code: Int?) {
        if (code == null && message.equals("no network available", true))
            viewModel.setToast(
                UiKitToast(
                    UiKitToast.Value.Warning,
                    getTextSafely(RShared.string.feedback_no_network),
                    Snackbar.LENGTH_LONG
                )
            )
        else
            viewModel.setToast(
                UiKitToast(
                    UiKitToast.Value.Error,
                    getTextSafely(R.string.feedback_loginFailed),
                    Snackbar.LENGTH_LONG
                )
            )
        binding.btnForm.showLoading(false)
    }

    override fun setupObservers() {
        binding.root.setupUiKitToast(viewLifecycleOwner, viewModel.toastMutable, viewModel.toast)
        viewModel.inputOne.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty() && !viewModel.inputTwo.value.isNullOrEmpty())
                binding.btnForm.enablePrimaryButton(context)
            else
                binding.btnForm.disablePrimaryButton(context)
        }
        viewModel.inputTwo.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty() && !viewModel.inputOne.value.isNullOrEmpty())
                binding.btnForm.enablePrimaryButton(context)
            else
                binding.btnForm.disablePrimaryButton(context)
        }
    }

    override fun setupUI() {
        super.setupUI()
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            delay(1500)//waiting for splash animation
            inputOne.setFocus(true)
        }
        binding.btnForm.disablePrimaryButton(context)
        mainActivity?.showSecondScreenWelcome()
    }

    override fun setupDestroyView() {
        super.setupDestroyView()
        viewModel.clear()
    }
}