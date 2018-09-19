package org.hildan.hrbuddy.server.controllers

import org.hildan.hrbuddy.agendagenerator.generateAgendas
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpSession

@Controller
class AgendaGeneratorController {

    @PostMapping("/planning")
    @CrossOrigin(origins = ["*"])
    @ResponseBody
    fun sendPlanningFile(planningFile: MultipartFile, session: HttpSession): FileSystemResource {

        println("Received ${planningFile.originalFilename}")
        val agendasDir = createTempDir("agendas-", "")
        generateAgendas(planningFile.inputStream, agendasDir)

        val zipFile = createTempFile("agendas-", ".zip")
        zipFile.deleteOnExit()
        agendasDir.zipInto(zipFile)
        agendasDir.deleteRecursively()

        return FileSystemResource(zipFile)
    }

    private fun File.zipInto(zipFile: File) = ZipOutputStream(zipFile.outputStream()).use { it.putFile(this) }

    private fun ZipOutputStream.putFile(file: File, namePrefix: String = "") {
        if (file.isHidden) {
            return
        }
        val entryName = "$namePrefix${file.name}"
        if (file.isDirectory) {
            file.listFiles().forEach { putFile(it, "$entryName/") }
            return
        }
        val entry = ZipEntry(entryName)
        putNextEntry(entry)
        file.inputStream().use { it.copyTo(this) }
    }
}
