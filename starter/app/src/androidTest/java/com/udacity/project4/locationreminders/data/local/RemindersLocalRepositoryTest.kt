package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var remindersDatabase: RemindersDatabase
    lateinit var remindersDAO: RemindersDao
    lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().context,
                RemindersDatabase::class.java
        )
                .allowMainThreadQueries()
                .build()
        remindersDAO = remindersDatabase.reminderDao()
        repository =
                RemindersLocalRepository(
                        remindersDAO
                )
    }

    @After
    fun cleanUp() {
        remindersDatabase.close()
    }

    @Test
    fun saveReminder_getReminders() = runBlocking {
        // GIVEN - 2 reminders in the database
        val reminder1 = ReminderDTO("Drink some beer","Buy one or eight beers beer in nearly restaurant 60/40",
            "50.0673 | 8.2473",
            50.06733405133622,8.247373577847616)
        repository.saveReminder(reminder1)

        val reminder2 = ReminderDTO("Take a photo","Take a phote from Kurhaus",
            "Bowling Green",
            50.08473806716792, 8.245727317709253)
        repository.saveReminder(reminder2)

        // WHEN - all reminders are loaded
        val result = repository.getReminders()


        // THEN - Result is success and all reminders are loaded correctly
        Assert.assertThat(result, instanceOf(Result.Success::class.java))
        result as Result.Success
        val loadedReminders = result.data
        assert(loadedReminders.size == 2)

        val loadedReminder1 = loadedReminders.find { loadedReminder -> loadedReminder.id == reminder1.id }
        assertThat(loadedReminder1?.title, `is`(reminder1.title))
        assertThat(loadedReminder1?.description, `is`(reminder1.description))
        assertThat(loadedReminder1?.location, `is`(reminder1.location))
        assertThat(loadedReminder1?.latitude, `is`(reminder1.latitude))
        assertThat(loadedReminder1?.longitude, `is`(reminder1.longitude))

        val loadedReminder2 = loadedReminders.find { loadedReminder -> loadedReminder.id == reminder2.id }
        assertThat(loadedReminder2?.title, `is`(reminder2.title))
        assertThat(loadedReminder2?.description, `is`(reminder2.description))
        assertThat(loadedReminder2?.location, `is`(reminder2.location))
        assertThat(loadedReminder2?.latitude, `is`(reminder2.latitude))
        assertThat(loadedReminder2?.longitude, `is`(reminder2.longitude))
    }

    @Test
    fun saveReminder_getReminderById() = runBlocking {
        // GIVEN - 2 reminders in the database
        val reminder1 = ReminderDTO("Drink some beer","Buy one or eight beers beer in nearly restaurant 60/40",
            "50.0673 | 8.2473",
            50.06733405133622,8.247373577847616)
        repository.saveReminder(reminder1)

        val reminder2 = ReminderDTO("Take a photo","Take a phote from Kurhaus",
            "Bowling Green",
            50.08473806716792, 8.245727317709253)
        repository.saveReminder(reminder2)

        // WHEN - reminder is loaded by ID
        val result = repository.getReminder(reminder2.id)

        // THEN - Result is success and reminder was loaded correctly
        Assert.assertThat(result, instanceOf(Result.Success::class.java))
        result as Result.Success
        val loadedReminder = result.data

        assertThat<ReminderDTO>(loadedReminder, CoreMatchers.notNullValue())
        assertThat(loadedReminder.id, `is`(reminder2.id))
        assertThat(loadedReminder.title, `is`(reminder2.title))
        assertThat(loadedReminder.description, `is`(reminder2.description))
        assertThat(loadedReminder.location, `is`(reminder2.location))
        assertThat(loadedReminder.latitude, `is`(reminder2.latitude))
        assertThat(loadedReminder.longitude, `is`(reminder2.longitude))
    }


    @Test
    fun deleteAllReminders() = runBlocking {
        // GIVEN - Insert two reminders
        val reminder1 = ReminderDTO("title1", "description1", "location1", 13.37, 42.42)
        repository.saveReminder(reminder1)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 23.38, 52.82)
        repository.saveReminder(reminder2)

        // WHEN - Delete all reminders from the database
        repository.deleteAllReminders()

        // THEN - The loaded list contains 0 elements
        val result = repository.getReminders()
        Assert.assertThat(result, instanceOf(Result.Success::class.java))
        result as Result.Success
        val loadedReminders = result.data
        assert(loadedReminders.isEmpty())
    }

}