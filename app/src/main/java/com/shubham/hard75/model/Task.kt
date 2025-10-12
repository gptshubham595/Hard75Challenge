package com.shubham.hard75.model

/**
 * Represents a single task that the user needs to complete daily.
 *
 * @param id A unique identifier for the task (e.g., "gym", "read_10_pages").
 * @param name The display name of the task shown to the user (e.g., "Go to the Gym").
 */
data class Task(
    val id: String,
    val name: String
)

