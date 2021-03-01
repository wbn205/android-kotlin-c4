package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {


    private lateinit var dataSource: FakeDataSource

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        stopKoin()

        dataSource = FakeDataSource()

        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
                dataSource)

        saveReminderViewModel.reminderTitle.value = null
        saveReminderViewModel.reminderDescription.value = null
        saveReminderViewModel.reminderSelectedLocationStr.value = null
        saveReminderViewModel.selectedPOI.value = null
        saveReminderViewModel.latitude.value = null
        saveReminderViewModel.longitude.value = null
    }

    @Test
    fun onClear_isCleared() {
        // GIVEN - filled saveReminderViewModel
        saveReminderViewModel.reminderTitle.value = "title"
        saveReminderViewModel.reminderDescription.value = "desc"
        saveReminderViewModel.reminderSelectedLocationStr.value = "location"
        saveReminderViewModel.selectedPOI.value = PointOfInterest(null, null, null)
        saveReminderViewModel.latitude.value = 45.345456
        saveReminderViewModel.longitude.value = 24.2342

        // WHEN - calling onClear
        saveReminderViewModel.onClear()

        // THEN - the viewModel is cleared
        assert(saveReminderViewModel.reminderTitle.value == null)
        assert(saveReminderViewModel.reminderDescription.value == null)
        assert(saveReminderViewModel.reminderSelectedLocationStr.value == null)
        assert(saveReminderViewModel.selectedPOI.value == null)
        assert(saveReminderViewModel.latitude.value == null)
        assert(saveReminderViewModel.longitude.value == null)
    }

    @Test
    fun validateEnteredData_titleIsNotValid() {
        // GIVEN - ReminderDataItem with invalid title and "clear" snackbar livedata
        val reminderDataItem = ReminderDataItem("", "asd", "location", 45.345456, 24.2342)
        saveReminderViewModel.showSnackBarInt.value = null

        // WHEN - calling validateEnteredData
        saveReminderViewModel.validateEnteredData(reminderDataItem)

        // THEN - the snackbar event is triggered
        assert(saveReminderViewModel.showSnackBarInt.value != null)
    }

    @Test
    fun validateEnteredData_locationIsNotValid() {
        // GIVEN - ReminderDataItem with invalid location and "clear" snackbar livedata
        val reminderDataItem = ReminderDataItem("title", "asd", "", 45.345456, 24.2342)
        saveReminderViewModel.showSnackBarInt.value = null

        // WHEN - calling validateEnteredData
        saveReminderViewModel.validateEnteredData(reminderDataItem)

        // THEN - the snackbar event is triggered
        assert(saveReminderViewModel.showSnackBarInt.value != null)
    }


}