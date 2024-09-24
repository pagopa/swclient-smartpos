package it.pagopa.swc.smartpos.idpay.view

import androidx.navigation.fragment.NavHostFragment
import it.pagopa.swc.smartpos.app_shared.view.BaseBottomSheetMenu
import it.pagopa.swc.smartpos.idpay.BuildConfig
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.R

class MenuBottomSheet(private val activity: MainActivity) : BaseBottomSheetMenu(activity) {
    override val isPoynt: Boolean get() = BuildConfig.FLAVOR.contains("poynt", true)
    override val listItemMenu: List<ItemMenu> get() = createList()
    private fun createList(): List<ItemMenu> {
        val navController = (activity.supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment).navController
        return listOf(
            ItemMenu(R.string.title_intro, R.drawable.bonus_black) {
                if (navController.currentDestination?.id != R.id.introFragment)
                    navController.navigate(R.id.action_global_introFragment)
            }, ItemMenu(R.string.cta_menu_paymentHistory, R.drawable.storico) {
                if (navController.currentDestination?.id != R.id.transactionHistoryFragment)
                    navController.navigate(R.id.action_global_transactionHistory)
            }, ItemMenu(R.string.cta_menu_support, R.drawable.assistenza) {
                if (navController.currentDestination?.id != R.id.webViewFragment)
                    navController.navigate(R.id.action_global_WebViewFragment)
            }
        )
    }
}