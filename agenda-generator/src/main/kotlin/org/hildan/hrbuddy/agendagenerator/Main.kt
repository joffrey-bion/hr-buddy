package org.hildan.hrbuddy.agendagenerator

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.hildan.hrbuddy.agendagenerator.planningparser.PlanningFormatException
import java.io.File

class CliConfig(parser: ArgParser) : Config {

    override val planningFile: File by parser.positional("PLANNING", "planning file") { File(this) }
        .addValidator {
            if (!value.exists()) {
                throw SystemExitException("Planning Excel file not found: ${value.absolutePath}", 1)
            }
        }

    override val templateFile: File? by parser.storing("-t", "--template", help = "template for agendas") { File(this) }
        .default<File?>(null)
        .addValidator {
            val file = if (value == null) return@addValidator else value!!
            if (!file.exists()) {
                throw SystemExitException("Agenda template file not found: ${file.absolutePath}", 1)
            }
            if (file.isDirectory) {
                throw SystemExitException("Invalid agenda template file, the given path points to a directory", 1)
            }
            if (file.extension != "docx") {
                throw SystemExitException("Invalid agenda template file type, expected .docx", 1)
            }
        }

    override val outputDir: File by parser.storing("-o", "--outDir", help = "agendas output directory") { File(this) }
        .default(File("agendas"))
        .addValidator {
            if (!value.exists()) {
                value.mkdir()
            }
            if (!value.isDirectory) {
                val msg = "Invalid output directory, the given path points to an existing file: ${value.absolutePath}"
                throw SystemExitException(msg, 1)
            }
        }
}

fun main(args: Array<String>) = mainBody {
    try {
        generateAgendas(ArgParser(args).parseInto(::CliConfig))
    } catch (e: PlanningFormatException) {
        throw SystemExitException(e.message, 1)
    }
}
