package org.hildan.hrbuddy.server.controllers

import org.hildan.hrbuddy.agendagenerator.generateAgendas
import org.hildan.hrbuddy.agendagenerator.parser.PlanningFormatException
import org.hildan.hrbuddy.server.service.SessionFileMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpSession

@Controller
class AgendaGeneratorController @Autowired constructor(
    val sessionFileMap: SessionFileMap
) {

    data class SendPlanningResponse(
        val downloadUrl: String?, val error: String?
    )

    @PostMapping("/planning")
    @CrossOrigin(origins = ["*"])
    @ResponseBody
    fun sendPlanningFile(planningFile: MultipartFile, session: HttpSession): SendPlanningResponse {

        val agendasDir = createTempDir("agendas-", "")
        try {
            generateAgendas(planningFile.inputStream, agendasDir)
        } catch (e: PlanningFormatException) {
            return SendPlanningResponse(null, e.message)
        } catch (e: Exception) {
            return SendPlanningResponse(null, "An unexpected error occurred: ${e.message}")
        }

        val zipFile = createTempFile("agendas-", ".zip")
        zipFile.deleteOnExit()
        agendasDir.zipInto(zipFile)
        agendasDir.deleteRecursively()

        val fileId = sessionFileMap.addFile(zipFile)
        val fileUrl = getDownloadUrl(fileId)
        return SendPlanningResponse(fileUrl, null)
    }

    private fun getDownloadUrl(fileId: String) =
        linkTo(methodOn(AgendaGeneratorController::class.java).download(fileId)).toString()

    @GetMapping("/download/{fileId}", produces = ["application/octet-stream"])
    @CrossOrigin(origins = ["*"])
    @ResponseBody
    fun download(@PathVariable fileId: String): FileSystemResource {
        val zipFile = sessionFileMap.getFile(fileId) ?: throw ResourceNotFoundException(fileId)
        return FileSystemResource(zipFile)
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class ResourceNotFoundException(msg: String) : RuntimeException(msg)

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
