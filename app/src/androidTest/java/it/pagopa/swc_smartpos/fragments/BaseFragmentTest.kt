package it.pagopa.swc_smartpos.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import it.pagopa.swc_smartpos.R

abstract class BaseFragmentTest {
    inline fun <reified Frag : Fragment> testFragment(args: Bundle? = null, whatToDo: (Frag?) -> Unit) {
        var fragmentHere: Frag? = null
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val scenario = FragmentScenario.launchInContainer(Frag::class.java, args, R.style.Theme_SWCSMARTPOS)
        scenario.onFragment { fragment ->
            fragmentHere = fragment
            navController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        whatToDo.invoke(fragmentHere)
    }
}