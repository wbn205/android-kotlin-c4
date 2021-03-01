package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.lang.Exception

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(shouldReturnError){
            return Result.Error("Exception getReminders")
        }

        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("ReminderDataItems not found!")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if(shouldReturnError){
            return Result.Error("Exception getReminder")
        }

        val reminder = reminders?.find { reminder -> reminder.id == id }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("ReminderDataItem not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}