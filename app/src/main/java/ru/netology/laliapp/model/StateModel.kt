package ru.netology.laliapp.model

data class StateModel(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val loginError: Boolean = false,
    val registrationError: Boolean = false,
)