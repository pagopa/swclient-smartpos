package it.pagopa.swc_smartpos.view

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.permission.PermissionCallBack
import it.pagopa.swc.smartpos.app_shared.permission.PermissionHandler
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseAllowCameraAccessFragment
import it.pagopa.swc_smartpos.view_model.AllowCameraAccessViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AllowCameraAccessFragment : BaseAllowCameraAccessFragment<MainActivity>() {
    private val viewModel: AllowCameraAccessViewModel by viewModels()
    override val backPress: () -> Unit get() = { findNavController().popBackStack(R.id.introFragment, false) }
    override val closeAction: () -> Unit get() = { findNavController().popBackStack(R.id.introFragment, false) }
    override fun mainBtnAction(): () -> Unit = {
        when (viewModel.state.value) {
            State.First -> PermissionHandler().requestPermissions(mainActivity, arrayOf(Manifest.permission.CAMERA), object : PermissionCallBack {
                override fun permissionGranted() {
                    super.permissionGranted()
                    WrapperLogger.i("CameraPermission", "Allowed")
                    findNavController().navigate(R.id.action_allowCameraAccessFragment_to_scanCodeFragment)
                }

                override fun permissionDenied() {
                    super.permissionDenied()
                    viewModel.setState(State.Second)
                    WrapperLogger.e("CameraPermission", "Denied")
                }

                override fun neverAskAgainClicked() {
                    super.neverAskAgainClicked()
                    viewModel.setState(State.Second)
                    WrapperLogger.e("CameraPermission", "Never ask again")
                }
            })
            State.Second -> findNavController().navigate(R.id.action_allowCameraAccessFragment_to_insertManuallyFragment)
            else -> {//nothing to do
            }
        }
    }

    override fun setupOnCreate() {
        super.setupOnCreate()
        mainActivity?.viewModel?.setKeepScreenOn(false)
    }
    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest {
                buildUi(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setupUI() {
        PermissionHandler().checkPermission(context, Manifest.permission.CAMERA) {
            when (it) {
                PermissionHandler.CheckPermissionResult.PermissionDisabled -> viewModel.setState(State.Second)
                else -> viewModel.setState(State.First)
            }
        }
    }
}