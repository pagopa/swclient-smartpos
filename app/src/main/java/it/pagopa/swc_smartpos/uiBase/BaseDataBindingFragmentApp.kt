package it.pagopa.swc_smartpos.uiBase

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment

/**This class customize BaseDataBinding fragment from UiKit and gives a specified Base Fragment for the app.
 * Here you can put base logic like back to a certain fragment which is obviously into stack, normally a LoginFragment could be an example.*/
abstract class BaseDataBindingFragmentApp<T : ViewBinding> : BaseDataBindingFragment<T, MainActivity>() {
    open val header: HeaderView? = null
    fun backToIntroFragment() {
        findNavController().popBackStack(R.id.introFragment, false)
    }

    @CallSuper
    override fun setupOnCreate() {
        super.setupOnCreate()
        mainActivity?.viewModel?.setHeaderView(header)
        mainActivity?.viewModel?.setKeepScreenOn(false)
    }

    @CallSuper
    override fun setupOnResume() {
        super.setupOnResume()
        mainActivity?.viewModel?.setHeaderView(header)
    }

    fun navigate(action: Int, args: Bundle? = null) {
        findNavController().navigate(action, args)
    }

    @CallSuper
    override fun setupDestroyView() {
        super.setupDestroyView()
        mainActivity?.viewModel?.setHeaderView(null)
    }
}