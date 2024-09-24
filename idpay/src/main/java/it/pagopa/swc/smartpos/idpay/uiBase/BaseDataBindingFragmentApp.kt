package it.pagopa.swc.smartpos.idpay.uiBase

import android.os.Build
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import it.pagopa.swc_smartpos.ui_kit.utils.FoldableInterface

/**This class customize BaseDataBinding fragment from UiKit and gives a specified Base Fragment for the app.
 * Here you can put base logic like back to a certain fragment which is obviously into stack, normally a LoginFragment could be an example.*/
abstract class BaseDataBindingFragmentApp<T : ViewBinding> : BaseDataBindingFragment<T, MainActivity>(), FoldableInterface {
    open val header: HeaderView? = null
    open val layoutId: Int = 0
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
    override fun setupUI() {
        mainActivity?.onUpdatingView = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                this.updateView(this, binding.root, layoutId)
        }
    }

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