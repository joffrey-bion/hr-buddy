package org.hildan.agenda.generator

import java.io.File
import java.io.InputStream

fun main(args: Array<String>) {
    val templateFilename = if (args.isEmpty()) "agenda-template.docx" else args[0]
    val agenda = fakeAgenda()
    val generator = AgendaGenerator { templateInputStream(templateFilename) }

    generator.generateAgenda(agenda)
}

fun templateInputStream(templateFilename: String): InputStream {
    val templateFile = File(templateFilename)
    if (!templateFile.exists()) {
        System.err.println("The given template file was not found")
        System.exit(1)
    } else if (templateFile.isDirectory) {
        System.err.println("The given template file path points to a directory, not a .docx file")
        System.exit(1)
    }
    return templateFile.inputStream()
}

