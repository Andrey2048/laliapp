package ru.netology.laliapp.entity

import ru.netology.laliapp.enumeration.EventType

data class EventTypeEmbeddable(
    val eventType: String,
) {
    fun toDto() = EventType.valueOf(eventType)

    companion object {
        fun fromDto(dto: EventType) = EventTypeEmbeddable(dto.name)
    }
}