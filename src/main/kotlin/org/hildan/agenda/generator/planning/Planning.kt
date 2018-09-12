package org.hildan.agenda.generator.planning

import org.hildan.agenda.generator.Agenda
import org.hildan.agenda.generator.Interview
import org.hildan.agenda.generator.Room
import java.time.LocalDate
import java.time.LocalTime

data class Planning(
    val globalInfo: GlobalInfo,
    val interviews: List<Interview>,
    val debriefing: Debriefing
) {
    fun toAgendas(): List<Agenda> {
        TODO()
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
