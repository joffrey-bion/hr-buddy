package org.hildan.agenda.generator

import java.time.LocalDate
import java.time.LocalTime

fun fakeAgenda(): Agenda {
    val date = LocalDate.of(2018, 9, 5)
    val candidate = Person("Noob", "Des Familles")

    val hrConsultant = Employee("Vielle", "Peau", "HR Consultant")
    val recSpe = Employee("Miss", "Relou", "Recruitment Specialist")

    val manager1 = Employee("John", "Bogoss", "Manager")
    val manager2 = Employee("Bob", "Lee Swagger", "Manager", "AIR", "DGS")
    val manager3 = Employee("Chuck", "Norris", "Manager")
    val director = Employee("Tony", "Stark", "Director")

    val room = Room("BE1683", "London")
    val interview1 = Interview(10 h 45, 11 h 30, candidate, recSpe, room)
    val interview2 = Interview(11 h 30, 12 h 15, candidate, hrConsultant, room)
    val interview3 = Interview(14 h 0, 14 h 45, candidate, manager1, room)
    val interview4 = Interview(14 h 45, 15 h 30, candidate, manager2, room)
    val interview5 = Interview(15 h 30, 16 h 15, candidate, manager3, room)
    val interview6 = Interview(16 h 15, 17 h 0, candidate, director, room)

    val morningSlots = listOf(interview1, interview2)
    val afternoonSlots = listOf(interview3, interview4, interview5, interview6)

    return Agenda(date, candidate, 8 h 0, 17 h 0, morningSlots, afternoonSlots)
}

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
    val candidate: Person,
    val interviewer: Employee,
    val room: Room
)

class Room(
    val code: String,
    val name: String
) {
    override fun toString() = "$code-$name"
}

infix fun Int.h(minutes: Int): LocalTime = LocalTime.of(this, minutes)
