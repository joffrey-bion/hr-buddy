package org.hildan.hrbuddy.agendagenerator.model

import java.time.LocalTime

open class Person(
    val firstName: String, val lastName: String
) {
    val fullName: String = "$firstName ${lastName.toUpperCase()}"

    override fun toString(): String = fullName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (fullName != other.fullName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + fullName.hashCode()
        return result
    }
}

class Candidate(
    firstName: String,
    lastName: String,
    val morningTaxiTime: LocalTime?,
    val eveningTaxiTime: LocalTime?
) : Person(firstName, lastName) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Candidate

        if (morningTaxiTime != other.morningTaxiTime) return false
        if (eveningTaxiTime != other.eveningTaxiTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (morningTaxiTime?.hashCode() ?: 0)
        result = 31 * result + (eveningTaxiTime?.hashCode() ?: 0)
        return result
    }
}

class Employee(
    firstName: String,
    lastName: String,
    val jobTitle: String,
    val division: Division? = null,
    val subdivision: Division? = null,
    val team: String? = null
) : Person(firstName, lastName) {

    private val divCodes: String? =
        if (division != null && subdivision != null) " (${division.code}/${subdivision.code})" else ""

    val description: String = (jobTitle dash team dash division?.name dash subdivision?.name) + divCodes

    private infix fun String.dash(suffix: String?) = if (suffix == null) {
        this
    } else {
        "$this - $suffix"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Employee

        if (jobTitle != other.jobTitle) return false
        if (division != other.division) return false
        if (subdivision != other.subdivision) return false
        if (team != other.team) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + jobTitle.hashCode()
        result = 31 * result + (division?.hashCode() ?: 0)
        result = 31 * result + (subdivision?.hashCode() ?: 0)
        result = 31 * result + (team?.hashCode() ?: 0)
        result = 31 * result + description.hashCode()
        return result
    }
}
