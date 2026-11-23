// Defines the values used by the player form.

package com.formup.app.ui.team

data class AddPlayerFormState(
    val fullName: String = "",
    val dateOfBirth: String = "",                           
    val dateOfBirthIso: String = "",                                  
    val position: String = "",
    val nameError: String? = null,
    val positionError: String? = null
)

val PositionOptions = listOf("Goalkeeper", "Defender", "Midfielder", "Forward")
