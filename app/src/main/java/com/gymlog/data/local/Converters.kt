package com.gymlog.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun sessionStatusToString(status: SessionStatus): String {
        return status.name
    }

    @TypeConverter
    fun stringToSessionStatus(value: String): SessionStatus {
        return SessionStatus.valueOf(value)
    }
}
