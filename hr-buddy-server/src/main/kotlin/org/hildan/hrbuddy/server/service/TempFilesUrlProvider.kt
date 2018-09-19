package org.hildan.hrbuddy.server.service

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.File
import java.util.UUID

@Component
class SessionFileMap {

    val fileIds = mutableMapOf<String, File>()

    fun addFile(file: File): String {
        val id = UUID.randomUUID().toString()
        fileIds[id] = file
        return id
    }

    fun getFile(id: String): File? = fileIds[id]
}
