package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a reminder
        val reminder = ReminderDTO("title", "description", "location", 13.37, 42.42)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loadedReminder = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values
        assertThat<ReminderDTO>(loadedReminder as ReminderDTO, notNullValue())
        assertThat(loadedReminder.id, `is`(reminder.id))
        assertThat(loadedReminder.title, `is`(reminder.title))
        assertThat(loadedReminder.description, `is`(reminder.description))
        assertThat(loadedReminder.location, `is`(reminder.location))
        assertThat(loadedReminder.latitude, `is`(reminder.latitude))
        assertThat(loadedReminder.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveRemindersAndGetReminders() = runBlockingTest {
        // GIVEN - Insert two reminders
        val reminder1 = ReminderDTO("title1", "description1", "location1", 13.37, 42.42)
        database.reminderDao().saveReminder(reminder1)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 23.38, 52.82)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Get all reminders from the database
        val loadedReminders = database.reminderDao().getReminders()

        // THEN - The loaded list contains the expected reminders
        assertThat(loadedReminders.size, `is`(2))

        val loadedReminder1 = loadedReminders?.find { loadedReminder -> loadedReminder.id == reminder1.id }
        assertThat(loadedReminder1?.title, `is`(reminder1.title))
        assertThat(loadedReminder1?.description, `is`(reminder1.description))
        assertThat(loadedReminder1?.location, `is`(reminder1.location))
        assertThat(loadedReminder1?.latitude, `is`(reminder1.latitude))
        assertThat(loadedReminder1?.longitude, `is`(reminder1.longitude))

        val loadedReminder2 = loadedReminders?.find { loadedReminder -> loadedReminder.id == reminder2.id }
        assertThat(loadedReminder2?.title, `is`(reminder2.title))
        assertThat(loadedReminder2?.description, `is`(reminder2.description))
        assertThat(loadedReminder2?.location, `is`(reminder2.location))
        assertThat(loadedReminder2?.latitude, `is`(reminder2.latitude))
        assertThat(loadedReminder2?.longitude, `is`(reminder2.longitude))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        // GIVEN - Insert two reminders
        val reminder1 = ReminderDTO("title1", "description1", "location1", 13.37, 42.42)
        database.reminderDao().saveReminder(reminder1)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 23.38, 52.82)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Delete all reminders from the database
        database.reminderDao().deleteAllReminders()

        // THEN - The loaded list contains 0 elements
        val loadedReminders = database.reminderDao().getReminders()
        assertThat(loadedReminders.size, `is`(0))
    }

}