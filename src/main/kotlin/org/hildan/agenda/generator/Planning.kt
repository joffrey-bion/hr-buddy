package org.hildan.agenda.generator

import java.time.LocalDate

class Planning(
    val date: LocalDate,
    val interviews: List<Interview>
) {
    fun toAgendas(): List<Agenda> {
        return emptyList()
    }
}
