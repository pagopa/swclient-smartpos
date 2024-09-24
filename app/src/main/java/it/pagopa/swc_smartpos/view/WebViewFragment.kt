package it.pagopa.swc_smartpos.view

import android.annotation.SuppressLint
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.databinding.WebviewFragmentBinding
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp

class WebViewFragment : BaseDataBindingFragmentApp<WebviewFragmentBinding>() {
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
            R.color.primary
        )

    override fun viewBinding() = binding(WebviewFragmentBinding::inflate)

    @SuppressLint("SetJavaScriptEnabled")
    override fun setupUI() {
        binding.wv.settings.javaScriptEnabled = true
        binding.wv.settings.domStorageEnabled = true
        binding.wv.loadUrl("https://www.pagopa.gov.it/it/assistenza/")
    }
}