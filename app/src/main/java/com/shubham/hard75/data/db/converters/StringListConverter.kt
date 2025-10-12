package com.shubham.hard75.data.db.converters
import androidx.room.TypeConverter

/**
 * Type converter for Room to store a list of Strings.
 * It converts a List<String> to a single comma-separated String and back.
 * This allows the database to handle the 'completedTaskIds' field in the ChallengeDay entity.
 */
class StringListConverter {
    /**
     * Converts a comma-separated String into a List of Strings.
     * @param value The String from the database.
     * @return A List of Strings.
     */
    @TypeConverter
    fun fromString(value: String?): List<String> {
        // If the value from the DB is null or empty, return an empty list.
        // Otherwise, split the string by commas.
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    /**
     * Converts a List of Strings into a single comma-separated String.
     * @param list The List of Strings to convert.
     * @return A single String representation of the list.
     */
    @TypeConverter
    fun fromList(list: List<String>?): String {
        // If the list is null, return an empty string.
        // Otherwise, join the elements with a comma.
        return list?.joinToString(",") ?: ""
    }
}
