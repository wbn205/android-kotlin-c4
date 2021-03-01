package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun createReminderAndCheckDetails() {
        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Check that "No data" is displayed
        onView(withId(R.id.noDataTextView)).check(matches(withText("No Data")))

        // Click on FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Insert reminder values
        onView(withId(R.id.reminderTitle)).perform(replaceText("NEW TITLE"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("NEW DESCRIPTION"))

        // Click on FAB to save
        onView(withId(R.id.saveReminder)).perform(click())

        // check whether saved reminder exists
        onView(withText("NEW TITLE")).check(matches(isDisplayed()))
        onView(withText("NEW DESCRIPTION")).check(matches(isDisplayed()))
        onView(withText("Lat: 50.08494, Long: 8.24753")).check(matches(isDisplayed()))

        // Click on reminder
        onView(withText("NEW TITLE")).perform(click())

        // check description fragment
        onView(withId(R.id.title)).check(matches(withText("NEW TITLE")))
        onView(withId(R.id.description)).check(matches(withText("NEW DESCRIPTION")))
        onView(withId(R.id.location)).check(matches(withText("Lat: 50.08494, Long: 8.24753")))
        onView(withId(R.id.latitude)).check(matches(withText("50.084937")))
        onView(withId(R.id.longitude)).check(matches(withText("8.247529")))

        // go back
        Espresso.pressBack()
        onView(withText("NEW TITLE")).check(matches(isDisplayed()))
        onView(withText("NEW DESCRIPTION")).check(matches(isDisplayed()))
        onView(withText("Lat: 50.08494, Long: 8.24753")).check(matches(isDisplayed()))

        activityScenario.close()
    }

}
