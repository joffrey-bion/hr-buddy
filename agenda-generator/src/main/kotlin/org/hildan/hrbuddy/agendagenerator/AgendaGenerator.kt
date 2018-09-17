package org.hildan.hrbuddy.agendagenerator

import org.hildan.hrbuddy.agendagenerator.parser.parsePlanning
import java.io.File

interface Config {
    val planningFile: File
    val templateFile: File?
    val outputDir: File
}

fun generateAgendas(config: Config) {
    config.run {
        val planning = parsePlanning(planningFile)
        val agendas = planning.toAgendas()
        val agendaWriter = AgendaWriter(templateFile, outputDir)

        agendas.forEach { agendaWriter.write(it) }
    }
}
