package com.agvahealthcare.ventilator_ext

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test

class MainActivityTest {
    private lateinit var scenario0: ActivityScenario<SplashActivity>
    private lateinit var scenario1:ActivityScenario<MainActivity>
    var mainActivity: MainActivity? = null
    var systemDialogFragment: SystemDialogFragment? = null
    @Before
    fun setup() {
        scenario0 = launchActivity()
        scenario0.moveToState(Lifecycle.State.STARTED)
        scenario1 = launchActivity()
        scenario1.moveToState(Lifecycle.State.STARTED)

    }

    @Test
    fun checkFemaleVersion(){
        onView(allOf(withId(R.id.buttonFemale), withParent(withId(R.id.includeFemale))))
            .perform(click())
    //onView(allOf(withId(R.id.buttonFemale), withParent(withId(R.id.includeFemale), 2))).perform(click());
    }
}