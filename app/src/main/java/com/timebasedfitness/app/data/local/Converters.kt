package com.timebasedfitness.app.data.local

import androidx.room.TypeConverter
import com.timebasedfitness.app.data.model.Category
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromCategory(category: Category): String = category.name

    @TypeConverter
    fun toCategory(value: String): Category = Category.valueOf(value)

    @TypeConverter
    fun fromLocalTime(time: LocalTime): Int = time.toSecondOfDay() / 60

    @TypeConverter
    fun toLocalTime(minutes: Int): LocalTime = LocalTime.ofSecondOfDay(minutes.toLong() * 60)

    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    @TypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun toInstant(epochMillis: Long): Instant = Instant.ofEpochMilli(epochMillis)
}
