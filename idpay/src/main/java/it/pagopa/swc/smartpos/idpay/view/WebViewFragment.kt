package it.pagopa.swc.smartpos.idpay.view

import android.annotation.SuppressLint
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.WebViewFragmentBinding
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc_smart_pos.ui_kit.R as RUikit

class WebViewFragment : BaseDataBindingFragmentApp<WebViewFragmentBinding>() {
    override val layoutId: Int get() = R.layout.web_view_fragment
    override fun viewBinding() = binding(WebViewFragmentBinding::inflate)
    override val backPress: () -> Unit
        get() = {
            if (binding.wv.canGoBack())
                binding.wv.goBack()
            else
                backToIntroFragment()
        }
    override val header: HeaderView
        get() = HeaderView(
            HeaderView.HeaderElement(R.drawable.arrow_back, backPress),
            HeaderView.HeaderString(R.string.cta_menu_support, R.color.white),
            null,
            RUikit.color.primary
        )

    @SuppressLint("SetJavaScriptEnabled")
    override fun setupUI() {
        super.setupUI()
        binding.wv.settings.javaScriptEnabled = true
        binding.wv.settings.domStorageEnabled = true
        binding.wv.loadUrl("https://www.pagopa.gov.it/it/assistenza/")
    }
}