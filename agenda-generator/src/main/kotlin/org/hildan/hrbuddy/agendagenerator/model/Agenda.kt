package org.hildan.hrbuddy.agendagenerator.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

class Agenda(
    val date: LocalDate,
    val candidate: Person,
    val morningTaxiTime: LocalTime?,
    val eveningTaxiTime: LocalTime?,
    val morningSlots: List<Interview>,
    val afternoonSlots: List<Interview>
) {
    val fullDate = formatter.format(date)
}

class Interview(
    val start: LocalTime,
    val end: LocalTime,
    val candidate: Candidate,
    val interviewer: Employee,
    val room: Room,
    val isBeforeLunch: Boolean
)

class Room(
    val name: String
) {
    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
