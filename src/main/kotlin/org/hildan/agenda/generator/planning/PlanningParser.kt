package org.hildan.agenda.generator.planning

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hildan.agenda.generator.Employee
import org.hildan.agenda.generator.Interview
import org.hildan.agenda.generator.Person
import org.hildan.agenda.generator.Room
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun parsePlanning(planningExcelFile: File): Planning = XSSFWorkbook(planningExcelFile).use(::parsePlanning)

private fun parsePlanning(workbook: Workbook): Planning {
    val globalInfo = parseGlobalInfo(workbook)
    val (interviews, debriefing) = parseInterviews(workbook, globalInfo)
    return Planning(globalInfo, interviews, debriefing)
}

private fun parseGlobalInfo(workbook: Workbook): GlobalInfo {
    val globalInfoSheet = workbook.getSheet("Global info") ?: formatError("Missing sheet named 'Global info'")
    val date = getDateData(globalInfoSheet, "Date")
    val divisionCode = getStringData(globalInfoSheet, "Division Code")
    val divisionName = getStringData(globalInfoSheet, "Division Name")
    val subdivisionCode = getStringData(globalInfoSheet, "Subdivision Code")
    val subdivisionName = getStringData(globalInfoSheet, "Subdivision Name")
    return GlobalInfo(date, divisionCode, divisionName, subdivisionCode, subdivisionName)
}

private fun parseInterviews(workbook: Workbook, globalInfo: GlobalInfo): Pair<List<Interview>, Debriefing> {
    val planningSheet = workbook.getSheetAt(0) ?: formatError("No sheet in the given excel file!")

    val interviews = mutableListOf<Interview>()
    var startRowNum = 0
    var lastDebriefing: Debriefing? = null
    while (true) {
        startRowNum = planningSheet.nextRowStartingWith("Interviewer", startRowNum)?.rowNum ?: break
        val (tableInteviews, debrief) = parseInterviewTable(planningSheet, startRowNum, globalInfo)
        if (lastDebriefing != null && lastDebriefing != debrief) {
            formatError("Debriefings don't match: $lastDebriefing VS $debrief")
        }
        lastDebriefing = debrief
        interviews.addAll(tableInteviews)
    }
    if (interviews.isEmpty()) {
        formatError("No interview table found in first sheet")
    }
    return Pair(interviews, lastDebriefing!!)
}

private fun parseInterviewTable(
    sheet: Sheet,
    startRowNum: Int,
    globalInfo: GlobalInfo
): Pair<List<Interview>, Debriefing> {
    val (firstNames, lastNames) = parseInterviewerNames(sheet, startRowNum)
    val nbInterviewers = firstNames.size
    val jobTitles = parseJobTitles(sheet, startRowNum + 1, nbInterviewers)
    val teams = parseTeamsIfPresent(sheet, startRowNum + 2, nbInterviewers)
    val interviewers = buildInterviewers(firstNames, lastNames, jobTitles, teams, globalInfo)

    val roomsRowNum = if (teams == null) startRowNum + 2 else startRowNum + 3
    val rooms = parseRooms(sheet, roomsRowNum, nbInterviewers)

    return buildInterviews(sheet, roomsRowNum + 1, interviewers, rooms)
}

private fun parseInterviewerNames(sheet: Sheet, rowNum: Int): Pair<List<String>, List<String>> {
    val fullNames = listAfterHeader(sheet.getRow(rowNum)).takeWhile { it.isNotBlank() }
    return fullNames.mapIndexed { i, name -> splitFullName(name, rowNum, i + 1) }.unzip()
}

private fun parseJobTitles(sheet: Sheet, rowNum: Int, nbInterviewers: Int): List<String> {
    val row = sheet.getRow(rowNum)
    if (!row.startsWith("Job Title")) {
        formatError("Expected 'Job Title' row below interviewers row", rowNum, 0)
    }
    val jobTitles = listAfterHeader(row).take(nbInterviewers)
    if (jobTitles.size < nbInterviewers) {
        formatError("There are only ${jobTitles.size} job titles, but $nbInterviewers interviewers", rowNum)
    }
    jobTitles.forEachIndexed { index, s ->
        if (s.isBlank()) {
            formatError("Missing job title", rowNum, index + 1)
        }
    }
    return jobTitles
}

private fun parseTeamsIfPresent(sheet: Sheet, rowNum: Int, nbInterviewers: Int): List<String?>? {
    val teamRow = sheet.getRow(rowNum)
    if (!teamRow.startsWith("Team")) {
        return null
    }
    val teams = listAfterHeader(teamRow).map { it.nullifyIfEmpty() }.take(nbInterviewers)
    if (teams.size < nbInterviewers) {
        formatError("There are only ${teams.size} teams, but $nbInterviewers interviewers", rowNum)
    }
    return teams
}

private fun String?.nullifyIfEmpty(): String? = if (isNullOrBlank()) null else this

private fun parseRooms(sheet: Sheet, rowNum: Int, nbInterviewers: Int): List<Room> {
    val row = sheet.getRow(rowNum)
    if (!row.startsWith("Room")) {
        formatError("Expected 'Room' in the first cell, found '${row.firstContent()}'", rowNum)
    }
    val rooms = listAfterHeader(row).take(nbInterviewers).mapIndexed { col, text -> parseRoom(text, rowNum, col) }
    if (rooms.size < nbInterviewers) {
        formatError("There are only ${rooms.size} rooms, but $nbInterviewers interviewers", rowNum)
    }
    return rooms
}

private fun parseRoom(text: String, row: Int, col: Int): Room {
    if (!text.contains("-")) {
        formatError("Expected room name in the form 'code-name', found '$text'", row, col)
    }
    val (code, name) = text.split("-")
    return Room(code, name)
}

private fun buildInterviewers(
    firstNames: List<String>,
    lastNames: List<String>,
    jobTitles: List<String>,
    teams: List<String?>?,
    globalInfo: GlobalInfo
): List<Employee> = firstNames.indices.map {
    Employee(
        firstNames[it],
        lastNames[it],
        jobTitle = jobTitles[it],
        division = globalInfo.divisionName,
        subdivision = globalInfo.subdivisionName,
        team = teams?.get(it)
    )
}

private fun splitFullName(fullName: String, row: Int, col: Int): Pair<String, String> {
    val lastSpaceIndex = fullName.lastIndexOf(' ')
    if (lastSpaceIndex < 0) {
        formatError("Expected full name in the form 'FirstName LastName', got '$fullName'", row, col)
    }
    val firstName = fullName.substring(0, lastSpaceIndex)
    val lastName = fullName.substring(lastSpaceIndex + 1)
    return Pair(firstName, lastName)
}

private fun buildInterviews(
    sheet: Sheet,
    startRowNum: Int,
    interviewers: List<Employee>,
    rooms: List<Room>
): Pair<List<Interview>, Debriefing> {
    val interviews = mutableListOf<Interview>()
    var rowNum = startRowNum
    while (true) {
        val row = sheet.getRow(rowNum) ?: error("Reached the end of the document without finding the DEBRIEFING")
        val timeSlot = getTimeSlotText(row)
        val firstData = row.secondContent()
        if (timeSlot == "LUNCH" || firstData == "LUNCH") {
            rowNum++
            continue
        }
        val (startTime, endTime) = parseTimeSlot(timeSlot, rowNum)
        if (firstData?.startsWith("DEBRIEFING") == true) {
            val debrief = parseDebrief(startTime, endTime, firstData, rowNum)
            return Pair(interviews, debrief)
        }

        val candidateNames = listAfterHeader(row).map { it.nullifyIfEmpty() }.take(interviewers.size)

        candidateNames.forEachIndexed { i, name ->
            if (name != null) {
                val (firstName, lastName) = splitFullName(name, rowNum, i + 1)
                val candidate = Person(firstName, lastName)
                val interviewer = interviewers[i]
                val room = rooms[i]
                val interview = Interview(startTime, endTime, candidate, interviewer, room)
                interviews.add(interview)
            }
        }
        rowNum++
    }
}

private fun getTimeSlotText(row: Row): String? {
    val cell = row.getCell(0) ?: formatError("Missing time slot information", row.rowNum, 0)
    try {
        return cell.stringCellValue
    } catch (e: IllegalStateException) {
        formatError("Invalid time slot data, it must be some text of the form '00:00 - 00:00'", row.rowNum, 0)
    }
}

private fun parseTimeSlot(timeSlotStr: String?, rowNum: Int): Pair<LocalTime, LocalTime> {
    if (timeSlotStr == null || !timeSlotStr.matches(Regex("""\d\d:\d\d\s*-\s*\d\d:\d\d"""))) {
        formatError("Expected time slot with format '00:00 - 00:00'", rowNum, 0)
    }
    val (start, end) = timeSlotStr.split(Regex("""\s*-\s*"""))
    try {
        val startTime = LocalTime.parse(start, DateTimeFormatter.ISO_LOCAL_TIME)
        val endTime = LocalTime.parse(end, DateTimeFormatter.ISO_LOCAL_TIME)
        return Pair(startTime, endTime)
    } catch (e: DateTimeParseException) {
        formatError("Expected time slot with format '00:00 - 00:00'", rowNum, 0)
    }
}

private fun parseDebrief(start: LocalTime, end: LocalTime, firstData: String, rowNum: Int): Debriefing {
    val regex = Regex("""DEBRIEFING \((.*)\)""")
    val roomMatch = regex.matchEntire(firstData)
        ?: formatError("Expected debriefing data like 'DEBRIEFING (room)', got '$firstData'", rowNum, 1)
    val roomText = roomMatch.groupValues[1]
    val room = parseRoom(roomText, rowNum, 1)
    return Debriefing(start, room)
}
