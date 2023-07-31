package ru.netology.laliapp.errors

import java.io.IOException
import java.sql.SQLException

sealed class AppError(message: String) : Exception(message) {

    companion object {
        fun from(e: Throwable) =
            when (e) {
                is IOException -> NetworkError
                is SQLException -> DbError
                is ApiError -> e
                else -> UnknownError
            }
    }
}

class ApiError(val code: Int, message: String) : AppError(message)

object NetworkError : AppError("Network Error")

object DbError : AppError("DB Error")

object UnknownError : AppError("Unknown Error")
