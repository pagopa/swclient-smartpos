package it.pagopa.swc_smartpos.view

import androidx.navigation.fragment.NavHostFragment
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.app_shared.view.BaseBottomSheetMenu
import it.pagopa.swc_smartpos.BuildConfig
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.network.HttpServiceInterface
import it.pagopa.swc_smartpos.ui_kit.utils.findActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MenuBottomSheet(
    private val activity: MainActivity,
    private val subscriberId: String
) : BaseBottomSheetMenu(activity) {
    override val isPoynt: Boolean get() = BuildConfig.FLAVOR.contains("poynt", true)
    override val listItemMenu: List<ItemMenu> get() = createListItemsMenu()
    private fun createListItemsMenu(): List<ItemMenu> {
        val navController = (activity.supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment).navController
        val list = if (subscriberId.isEmpty()) arrayListOf(ItemMenu(R.string.cta_menu_payNotice, R.drawable.paga_un_avviso) {
            if (navController.currentDestination?.id != R.id.introFragment)
                navController.navigate(R.id.action_global_introFragment)
        }, ItemMenu(R.string.cta_menu_paymentHistory, R.drawable.storico) {
            if (navController.currentDestination?.id != R.id.transactionHistoryFragment)
                navController.navigate(R.id.action_global_storicoFragment)
        }, ItemMenu(R.string.cta_menu_support, R.drawable.assistenza) {
            if (navController.currentDestination?.id != R.id.webviewFragment)
                navController.navigate(R.id.action_global_WebViewFragment)
        }, ItemMenu(R.string.helped_way, R.drawable.paga_un_avviso) {
            if (navController.currentDestination?.id != R.id.helpedWaySubscribeFragment)
                navController.navigate(R.id.action_global_helpedWay)
        }) else arrayListOf(ItemMenu(R.string.helped_way_unsubscribe, R.drawable.paga_un_avviso) {
            val activity = context.findActivity() as? MainActivity
            activity?.viewModel?.setLoaderText(activity.resources.getString(R.string.helped_way_unsubscribe_loading))
            activity?.viewModel?.showLoader(true to false)
            activity?.decrypt(activity.viewModel.accessToken.value) {
                deactivateHelpedWayTerminal(it, activity.sdkUtils?.getCurrentBusiness()?.value?.paTaxCode.orEmpty())
            }
        })
        if (BuildConfig.DEBUG) {
            list.add(ItemMenu(R.string.uikit, R.drawable.uikit) {
                if (navController.currentDestination?.id != R.id.uiKitShowCase)
                    navController.navigate(R.id.action_global_UiKitShowcase)
            })
        }
        return list
    }

    private fun deactivateHelpedWayTerminal(accessToken: String, paTaxCode: String) {
        HttpServiceInterface(CoroutineScope(Dispatchers.IO)).unSubScribeTerminal(
            context, accessToken, paTaxCode, subscriberId,
            activity.sdkUtils?.getCurrentBusiness()?.value
        ).observe(activity, BaseWrapper(activity, successAction = {
            activity.setSubscriberId("")
            activity.viewModel.setHelpedWayDeactivated(true)
        }, errorAction = {
            if (it == BaseWrapper.tokenRefreshed)
                deactivateHelpedWayTerminal(accessToken, paTaxCode)
        }, showSecondScreenLoader = false))
    }
}