package ru.netology.laliapp.dto

data class Job(
    val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
) {
    companion object {
        val emptyJob = Job(
            id = 0,
            name = "",
            position = "",
            start = "",
            finish = null,
        )
    }
}

const val DATE_WORK_NOW = "2099-01-01T00:00:00Z"
const val STRING_FOR_LINK = "."