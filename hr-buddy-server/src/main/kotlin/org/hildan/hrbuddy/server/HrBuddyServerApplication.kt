package org.hildan.hrbuddy.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HrBuddyServerApplication

fun main(args: Array<String>) {
    runApplication<HrBuddyServerApplication>(*args)
}
