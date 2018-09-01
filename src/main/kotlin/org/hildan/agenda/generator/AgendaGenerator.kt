package org.hildan.agenda.generator

import org.wickedsource.docxstamper.DocxStamper
import org.wickedsource.docxstamper.DocxStamperConfiguration
import java.io.File
import java.io.InputStream
import java.time.format.DateTimeFormatter

class AgendaGenerator(val templateProvider: () -> InputStream) {

    private val stamper: DocxStamper<Any> = DocxStamperConfiguration().build()

    fun generateAgenda(agenda: Agenda, outputFilename: String = filename(agenda)) {
       File(outputFilename).outputStream().use {
           val context = JContext().apply { this.a = agenda }
           stamper.stamp(templateProvider(), context, it)
           println("Agenda generated as $outputFilename")
       }
    }

    private fun filename(agenda: Agenda): String =
        "${agenda.date.format(DateTimeFormatter.ISO_DATE)}-${agenda.candidate.firstName}-${agenda.candidate.lastName}.docx"
}
