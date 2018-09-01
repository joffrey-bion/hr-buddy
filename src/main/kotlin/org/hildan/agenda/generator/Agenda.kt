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

    val interview1 = Interview(10 h 45, 11 h 30, recSpe, "BE1683-London")
    val interview2 = Interview(11 h 30, 12 h 15, hrConsultant, "BE1683-London")
    val interview3 = Interview(14 h 0, 14 h 45, manager1, "BE1683-London")
    val interview4 = Interview(14 h 45, 15 h 30, manager2, "BE1683-London")
    val interview5 = Interview(15 h 30, 16 h 15, manager3, "BE1683-London")
    val interview6 = Interview(16 h 15, 17 h 0, director, "BE1683-London")

    val morningSlots = listOf(interview1, interview2)
    val afternoonSlots = listOf(interview3, interview4, interview5, interview6)

    return Agenda(date, candidate, 8 h 0, 17 h 0, morningSlots, afternoonSlots)
}

// needs to be open for docx-stamper proxy system to work
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
    val interviewer: Employee,
    val room: String
)

open class Person(
    val firstName: String,
    val lastName: String
) {
    val fullName: String = "$firstName ${lastName.toUpperCase()}"

    override fun toString(): String = fullName
}

class Employee(
    firstName: String,
    lastName: String,
    val jobTitle: String,
    val subdivision: String? = null,
    val division: String? = null
) : Person(firstName, lastName) {

    val description: String = jobTitle dash division dash subdivision

    private infix fun String.dash(suffix: String?) = if (suffix == null) {
        this
    } else {
        "$this - $suffix"
    }
}

infix fun Int.h(minutes: Int): LocalTime = LocalTime.of(this, minutes)
