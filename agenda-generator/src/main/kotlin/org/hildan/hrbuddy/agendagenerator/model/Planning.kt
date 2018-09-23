package org.hildan.hrbuddy.agendagenerator.model

import java.time.LocalDate
import java.time.LocalTime

data class Planning(
    val globalInfo: GlobalInfo,
    val interviews: List<Interview>,
    val debriefing: Debriefing
) {
    fun toAgendas(): List<Agenda> = interviews.groupBy { it.candidate }.map { (c, slots) -> createAgenda(c, slots) }

    private fun createAgenda(candidate: Candidate, interviews: List<Interview>): Agenda {
        val (morningInts, afternoonInts) = interviews.sortedBy { it.start }.partition { it.isBeforeLunch }
        return Agenda(
            globalInfo.date, candidate, candidate.morningTaxiTime, candidate.eveningTaxiTime, morningInts, afternoonInts
        )
    }
}

data class GlobalInfo(
    val date: LocalDate,
    val divisionCode: String,
    val divisionName: String,
    val subdivisionCode: String,
    val subdivisionName: String
)

data class Debriefing(
    val start: LocalTime,
    val end: LocalTime,
    val room: Room
)
