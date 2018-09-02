package org.hildan.agenda.generator

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
    val division: String? = null,
    val subdivision: String? = null,
    val team: String? = null
) : Person(firstName, lastName) {

    val description: String = jobTitle dash team dash division dash subdivision

    private infix fun String.dash(suffix: String?) = if (suffix == null) {
        this
    } else {
        "$this - $suffix"
    }
}
