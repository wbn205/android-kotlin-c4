package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepositoryTest
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4ClassRunner::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest :
        AutoCloseKoinTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()
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
    fun noData_showNoDataTextView() = runBlockingTest {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun loadData_showRecyclerView() {

         runBlocking {

             // GIVEN - 2 reminders in the database
             val reminder1 = ReminderDTO("Drink some beer", "Buy one or eight beers beer in nearly restaurant 60/40",
                    "50.0673 | 8.2473",
                    50.06733405133622, 8.247373577847616)
             repository.saveReminder(reminder1)

             val reminder2 = ReminderDTO("Take a photo", "Take a phote from Kurhaus",
                    "Bowling Green",
                    50.08473806716792, 8.245727317709253)
             repository.saveReminder(reminder2)

             // WHEN - Fragment is launched
             launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

             // THEN - correct views and strings are displayed
             onView(ViewMatchers.withText("Drink some beer")).check(matches(isDisplayed()))
             onView(ViewMatchers.withText("Buy one or eight beers beer in nearly restaurant 60/40")).check(matches(isDisplayed()))
             onView(ViewMatchers.withText("50.0673 | 8.2473")).check(matches(isDisplayed()))
             onView(ViewMatchers.withText("Take a photo")).check(matches(isDisplayed()))
             onView(ViewMatchers.withText("Take a phote from Kurhaus")).check(matches(isDisplayed()))
             onView(ViewMatchers.withText("Bowling Green")).check(matches(isDisplayed()))

             onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
         }
    }

    @Test
    fun navigateToAddReminder_showSelectLocationFragment() {
        // GIVEN - launched fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - SelectLocationFragment is displayed
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}