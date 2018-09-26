package org.hildan.hrbuddy.agendagenerator

import org.hildan.hrbuddy.agendagenerator.parser.PlanningParserOptions
import org.hildan.hrbuddy.agendagenerator.parser.parsePlanning
import java.io.File
import java.io.InputStream

fun generateAgendas(
    planningExcel: InputStream,
    outputDir: File,
    templateFile: File? = null,
    parserOptions: PlanningParserOptions? = null
): List<File> {
    val planning = parsePlanning(planningExcel, parserOptions)
    val agendas = planning.toAgendas()
    val agendaWriter = AgendaWriter(templateFile, outputDir)

    return agendas.map { agendaWriter.write(it) }
}
