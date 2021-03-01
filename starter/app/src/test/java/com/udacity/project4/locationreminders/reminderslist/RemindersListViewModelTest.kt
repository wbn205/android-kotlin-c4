package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    private lateinit var dataSource: FakeDataSource

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setupViewModel() {
        stopKoin()

        val reminder1 = ReminderDTO("Drink some beer","Buy one or eight beers beer in nearly restaurant 60/40",
                "50.0673 | 8.2473",
                50.06733405133622,8.247373577847616)

        val reminder2 = ReminderDTO("Take a photo","Take a phote from Kurhaus",
                "Bowling Green",
                50.08473806716792, 8.245727317709253)

        dataSource = FakeDataSource()
        dataSource.reminders = mutableListOf(reminder1, reminder2)

        remindersListViewModel = RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                dataSource)
    }

    @Test
    fun loadReminders_loadingDataSuccessful()= runBlockingTest {

        // GIVEN - 2 reminders

        // WHEN - calling loadReminders
        remindersListViewModel.loadReminders()

        // THEN - the viewModel has two reminders
        assert(remindersListViewModel.remindersList.value?.size!! == dataSource.reminders?.size)
    }

    @Test
    fun loadReminders_withError() = runBlockingTest {
        // GIVEN - datasource should return error
        dataSource.setReturnError(true)

        // WHEN - calling loadReminders
        remindersListViewModel.loadReminders()

        // THEN - datasource returns error
        val error = remindersListViewModel.showSnackBar.getOrAwaitValue()
        assert(error.contains("Exception"))
    }

    @Test
    fun showLoading() {
        // GIVEN - 2 reminders
        mainCoroutineRule.pauseDispatcher()

        // WHEN - loading reminders
        remindersListViewModel.loadReminders()

        // THEN - show loading
        assert(remindersListViewModel.showLoading.getOrAwaitValue() == true)

        // WHEN - loading is finished
        mainCoroutineRule.resumeDispatcher()

        // THEN - hide loading
        assert(remindersListViewModel.showLoading.getOrAwaitValue() == false)
    }

}