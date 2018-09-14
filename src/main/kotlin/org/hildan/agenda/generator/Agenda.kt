package org.hildan.agenda.generator

import java.time.LocalDate
import java.time.LocalTime

class Agenda(
    val date: LocalDate,
    val candidate: Person,
    val morningTaxiTime: LocalTime,
    val eveningTaxiTime: LocalTime,
    val morningSlots: List<Interview>,
    val afternoonSlots: List<Interview>
)

class Interview(
    val start: LocalTime,
    val end: LocalTime,
    val candidate: Candidate,
    val interviewer: Employee,
    val room: Room,
    val halfDay: HalfDay
)

enum class HalfDay {
    MORNING,
    AFTERNOON
}

class Room(
    val code: String,
    val name: String
) {
    override fun toString() = "$code-$name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (code != other.code) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

infix fun Int.h(minutes: Int): LocalTime = LocalTime.of(this, minutes)
