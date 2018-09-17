package org.hildan.hrbuddy.server.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.multipart.MultipartFile

@Controller
class AgendaGeneratorController {

    @PostMapping("/planning")
    fun sendPlanningFile(file: MultipartFile) {

    }
}
