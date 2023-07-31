package ru.netology.laliapp.model

import ru.netology.laliapp.dto.Job

data class JobModel(
    val jobs: List<Job> = emptyList(),
    val empty: Boolean = false,
)