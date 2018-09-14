package org.hildan.agenda.generator.planning

import org.hildan.agenda.generator.Agenda
import org.hildan.agenda.generator.Candidate
import org.hildan.agenda.generator.HalfDay
import org.hildan.agenda.generator.Interview
import org.hildan.agenda.generator.Room
import java.time.LocalDate
import java.time.LocalTime

data class Planning(
    val globalInfo: GlobalInfo,
    val interviews: List<Interview>,
    val debriefing: Debriefing
) {
    fun toAgendas(): List<Agenda> = interviews.groupBy { it.candidate }.map { (c, slots) -> createAgenda(c, slots) }

    private fun createAgenda(candidate: Candidate, interviews: List<Interview>): Agenda {
        val (morningInts, afternoonInts) = interviews.partition { it.halfDay == HalfDay.MORNING }
        return Agenda(
            globalInfo.date,
            candidate,
            candidate.morningTaxiTime,
            candidate.eveningTaxiTime,
            morningInts,
            afternoonInts
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
    val time: LocalTime,
    val room: Room
)
