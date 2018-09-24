package org.hildan.hrbuddy.agendagenerator.parser

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hildan.hrbuddy.agendagenerator.model.Candidate
import org.hildan.hrbuddy.agendagenerator.model.Debriefing
import org.hildan.hrbuddy.agendagenerator.model.Division
import org.hildan.hrbuddy.agendagenerator.model.Employee
import org.hildan.hrbuddy.agendagenerator.model.GlobalInfo
import org.hildan.hrbuddy.agendagenerator.model.Interview
import org.hildan.hrbuddy.agendagenerator.model.Planning
import org.hildan.hrbuddy.agendagenerator.model.Room
import java.io.InputStream
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class PlanningParserOptions(
    val noDivisionJobTitles: List<String> = listOf("RH Consultant", "Recruitment specialist")
)

fun parsePlanning(planningExcel: InputStream, options: PlanningParserOptions = PlanningParserOptions()): Planning =
    getWorkbook(planningExcel).use { parsePlanning(it, options) }

private fun getWorkbook(planningExcel: InputStream): XSSFWorkbook {
    try {
        return XSSFWorkbook(planningExcel)
    } catch (e: Exception) {
        formatError("Invalid file format: ${e.message}")
    }
}

fun parsePlanning(workbook: Workbook, options: PlanningParserOptions = PlanningParserOptions()): Planning {
    val globalInfo = parseGlobalInfo(workbook)
    val candidates = parseCandidates(workbook)
    val interviewParser = InterviewParser(globalInfo, candidates, options)
    val (interviews, debriefing) = interviewParser.parseInterviews(workbook)
    return Planning(globalInfo, interviews, debriefing)
}

private fun parseGlobalInfo(workbook: Workbook): GlobalInfo {
    val globalInfoSheet = workbook.getMandatorySheet("Global info")

    val divisionCode = getStringData(globalInfoSheet, "Division Code")
    val divisionName = getStringData(globalInfoSheet, "Division Name")
    val division = Division(divisionCode, divisionName)

    val subdivisionCode = getStringData(globalInfoSheet, "Subdivision Code")
    val subdivisionName = getStringData(globalInfoSheet, "Subdivision Name")
    val subdivision = Division(subdivisionCode, subdivisionName)

    return GlobalInfo(
        date = getDateData(globalInfoSheet, "Date"),
        division = division,
        subdivision = subdivision
    )
}

private fun parseCandidates(workbook: Workbook): Map<String, Candidate> {
    val globalInfoSheet = workbook.getMandatorySheet("Candidates")
    val headerRow = globalInfoSheet.getRow(0) ?: formatError("Missing header row in candidates sheet", 0)
    headerRow.expect(listOf("Candidate name", "Morning taxi", "Evening taxi"))

    return globalInfoSheet.drop(1).mapNotNull(::createCandidate).map { it.fullName to it }.toMap()
}

private fun createCandidate(row: Row?): Candidate? {
    val cell = row?.getCell(0)
    if (cell == null || cell.stringCellValue.isNullOrBlank()) {
        // this means we have reached the last row, but blank rows may exist anyway
        return null
    }
    val name = cell.stringCellValue ?: formatError("Missing candidate name", row.rowNum, 0)
    val (firstName, lastName) = splitFullName(name, row.rowNum, 0)

    val cell1 = row.getCell(1) ?: formatError("Missing morning taxi time for candidate '$name'", row.rowNum, 1)
    val morningTaxi = cell1.localTimeValue()

    val cell2 = row.getCell(2) ?: formatError("Missing evening taxi time for candidate '$name'", row.rowNum, 2)
    val eveningTaxi = cell2.localTimeValue()

    return Candidate(firstName, lastName, morningTaxi, eveningTaxi)
}

private class InterviewParser(
    private val globalInfo: GlobalInfo,
    private val candidates: Map<String, Candidate>,
    private val options: PlanningParserOptions
) {
    fun parseInterviews(workbook: Workbook): Pair<List<Interview>, Debriefing> {
        val planningSheet = workbook.getSheetAt(0) ?: formatError("No sheet in the given excel file!")

        val interviews = mutableListOf<Interview>()
        var startRowNum = 0
        var lastDebriefing: Debriefing? = null
        while (true) {
            startRowNum = planningSheet.nextRowStartingWith("Interviewer", startRowNum)?.rowNum ?: break
            val (tableInteviews, debrief) = parseInterviewTable(
                planningSheet, startRowNum
            )
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

    private fun parseInterviewTable(sheet: Sheet, startRowNum: Int): Pair<List<Interview>, Debriefing> {
        val (firstNames, lastNames) = parseInterviewerNames(sheet, startRowNum)
        val nbInterviewers = firstNames.size
        val jobTitles = parseJobTitles(sheet, startRowNum + 1, nbInterviewers)
        val teams = parseTeamsIfPresent(sheet, startRowNum + 2, nbInterviewers)
        val interviewers = buildInterviewers(firstNames, lastNames, jobTitles, teams)

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
        val teams = listAfterHeader(teamRow).take(nbInterviewers).map { it.nullifyIfEmpty() }
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
        val rooms = listAfterHeader(row).take(nbInterviewers).map(::Room)
        if (rooms.size < nbInterviewers) {
            formatError("There are only ${rooms.size} rooms, but $nbInterviewers interviewers", rowNum)
        }
        return rooms
    }

    private fun buildInterviewers(
        firstNames: List<String>, lastNames: List<String>, jobTitles: List<String>, teams: List<String?>?
    ): List<Employee> = firstNames.indices.map {
        val jobTitle = jobTitles[it]
        Employee(
            firstName = firstNames[it],
            lastName = lastNames[it],
            jobTitle = jobTitle,
            division = ifInDivision(jobTitle) { globalInfo.division },
            subdivision = ifInDivision(jobTitle) { globalInfo.subdivision },
            team = teams?.get(it)
        )
    }

    private fun ifInDivision(jobTitle: String, getValue: () -> Division): Division? =
        if (options.noDivisionJobTitles.contains(jobTitle)) {
            null
        } else {
            getValue()
        }

    private fun buildInterviews(
        sheet: Sheet, startRowNum: Int, interviewers: List<Employee>, rooms: List<Room>
    ): Pair<List<Interview>, Debriefing> {
        val interviews = mutableListOf<Interview>()
        var rowNum = startRowNum
        var beforeLunch = true
        while (true) {
            val row = sheet.getRow(rowNum) ?: error("Reached the end of the document without finding the DEBRIEFING")
            val timeSlot = getTimeSlotText(row)
            val firstData = row.secondContent()
            if (timeSlot == "LUNCH" || firstData == "LUNCH") {
                rowNum++
                beforeLunch = false
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
                    val candidate = candidates[name]
                        ?: formatError("Candidate '$name' not found in candidates list", rowNum, i)
                    val interview = Interview(
                        start = startTime,
                        end = endTime,
                        candidate = candidate,
                        interviewer = interviewers[i],
                        room = rooms[i],
                        isBeforeLunch = beforeLunch
                    )
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
        val room = Room(roomText)
        return Debriefing(start, end, room)
    }
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
