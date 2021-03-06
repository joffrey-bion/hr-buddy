package org.hildan.hrbuddy.agendagenerator

import org.hildan.hrbuddy.agendagenerator.model.Agenda
import org.wickedsource.docxstamper.DocxStamper
import org.wickedsource.docxstamper.DocxStamperConfiguration
import java.io.File
import java.io.InputStream
import java.time.format.DateTimeFormatter

class AgendaWriter(
    private val templateProvider: () -> InputStream,
    private val outputDir: File
) {

    private val stamper: DocxStamper<Any> = DocxStamperConfiguration().build()

    constructor(templateFile: File?, outputDir: File) : this(templateProvider(templateFile), outputDir)

    fun write(agenda: Agenda, outputFilename: String = filename(agenda)): File {
        val outputFile = outputDir.resolve(outputFilename)
        outputFile.outputStream().use {
            val context = JContext().apply { this.a = agenda }
            stamper.stamp(templateProvider(), context, it)
        }
        return outputFile
    }

    private fun filename(agenda: Agenda): String =
        "${agenda.date.format(DateTimeFormatter.ISO_DATE)}-${agenda.candidate.firstName}-${agenda.candidate.lastName}.docx"
}

private fun templateProvider(templateFile: File?) = if (templateFile == null) {
    defaultTemplateProvider()
} else {
    fileTemplateProvider(templateFile)
}

private fun defaultTemplateProvider(): () -> InputStream = {
    AgendaWriter::class.java.getResourceAsStream("/agenda-template.docx")
}

private fun fileTemplateProvider(templateFile: File): () -> InputStream = { templateFile.inputStream() }
