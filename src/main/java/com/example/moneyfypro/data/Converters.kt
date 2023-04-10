package com.example.moneyfypro.data

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.util.*

class DateConverter {

    @TypeConverter
    fun fromLong(date: Long): Date {
        return Date(date)
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
}