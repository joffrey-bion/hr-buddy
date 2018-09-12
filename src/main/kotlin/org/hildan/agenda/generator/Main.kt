package org.hildan.agenda.generator

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.hildan.agenda.generator.planning.parsePlanning
import java.io.File

class Config(parser: ArgParser) {

    val planningFile: File by parser.positional("PLANNING", "planning file") { File(this) }
        .addValidator {
            if (!value.exists()) {
                throw SystemExitException("Planning Excel file not found", 1)
            }
        }

    val templateFile: File? by parser.storing("-t", "--template", help = "template for agendas") { File(this) }
        .default<File?>(null)
        .addValidator {
            val file = if (value == null) return@addValidator else value!!
            if (!file.exists()) {
                throw SystemExitException("Agenda template file not found", 1)
            }
            if (file.isDirectory) {
                throw SystemExitException("Invalid agenda template file, the given path points to a directory", 1)
            }
            if (file.extension != "docx") {
                throw SystemExitException("Invalid agenda template file type, expected .docx", 1)
            }
        }
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::Config).run {
        val planning = parsePlanning(planningFile)
        val agendas = planning.toAgendas()
        val agendaWriter = AgendaWriter(templateFile)
        agendas.forEach { agendaWriter.write(it) }
    }
}
