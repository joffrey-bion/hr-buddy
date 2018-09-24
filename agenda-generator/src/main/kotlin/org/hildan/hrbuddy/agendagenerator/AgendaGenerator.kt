package org.hildan.hrbuddy.agendagenerator

import org.hildan.hrbuddy.agendagenerator.parser.PlanningParserOptions
import org.hildan.hrbuddy.agendagenerator.parser.parsePlanning
import java.io.File
import java.io.InputStream

interface Config {
    val planningFile: File
    val templateFile: File?
    val outputDir: File
}

fun generateAgendas(config: Config) {
    config.run {
        generateAgendas(planningFile.inputStream(), outputDir, templateFile)
    }
}

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
