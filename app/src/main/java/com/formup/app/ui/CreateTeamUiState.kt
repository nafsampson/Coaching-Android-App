// Defines team setup values and the available age groups.

package com.formup.app.ui.auth

data class TeamFormState(
    val teamName: String = "",
    val ageGroup: String = "",
    val nameError: String? = null,
    val ageError: String? = null
)

val AgeGroupOptions = (8..21).map { "U$it" }

data class CreatedTeam(
    val teamName: String,
    val ageGroup: Int
)
