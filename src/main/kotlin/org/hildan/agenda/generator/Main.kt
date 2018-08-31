package org.hildan.agenda.generator

import java.io.File
import java.io.InputStream

fun main(args: Array<String>) {
    val agenda = fakeAgenda()
    val generator = AgendaGenerator(templateProvider(args))

    generator.generateAgenda(agenda)
}

private fun templateProvider(args: Array<String>) = if (args.isEmpty()) {
    defaultTemplateProvider()
} else {
    fileTemplateProvider(args[0])
}

const val defaultResource = "/agenda-template.docx"

private fun defaultTemplateProvider(): () -> InputStream = {
    AgendaGenerator::class.java.getResourceAsStream(defaultResource)
}

private fun fileTemplateProvider(templateFilename: String): () -> InputStream {
    val templateFile = File(templateFilename)
    if (!templateFile.exists()) {
        System.err.println("The given template file $templateFilename was not found")
        System.exit(1)
    } else if (templateFile.isDirectory) {
        System.err.println("The given template file path points to a directory, not a .docx file")
        System.exit(1)
    }
    return { templateFile.inputStream() }
}

