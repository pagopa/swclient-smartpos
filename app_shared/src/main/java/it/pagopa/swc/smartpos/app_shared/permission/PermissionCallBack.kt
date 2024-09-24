package it.pagopa.swc.smartpos.app_shared.permission

import androidx.annotation.CallSuper
import it.pagopa.swc.smartpos.app_shared.login.LoginUtility

interface PermissionCallBack {
    @CallSuper
    fun permissionGranted() {
        LoginUtility.shouldVerifySession = false
        PermissionHandler.pCallback = null
    }

    @CallSuper
    fun permissionDenied() {
        LoginUtility.shouldVerifySession = false
        PermissionHandler.pCallback = null
    }

    @CallSuper
    fun neverAskAgainClicked() {
        LoginUtility.shouldVerifySession = false
        PermissionHandler.pCallback = null
    }
}