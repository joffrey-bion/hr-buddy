package org.hildan.agenda.generator

import com.xenomachina.argparser.SystemExitException
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun parsePlanning(planningExcelFile: File): Planning = XSSFWorkbook(planningExcelFile).use(::parsePlanning)

private fun parsePlanning(workbook: Workbook): Planning {
    val sheet = workbook.getSheetAt(0)
    val date = extractDateFromTitle(sheet)
    val interviews = extractInterviews(sheet)
    return Planning(date, interviews)
}

private fun extractDateFromTitle(sheet: Sheet): LocalDate {
    val title = extractTitleCellContent(sheet)
    try {
        return LocalDate.parse(title.takeLast(10), DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: DateTimeParseException) {
        throw PlanningFormatException("Expected date at the end of the title, with format yyyy-MM-dd")
    }
}

private fun extractTitleCellContent(sheet: Sheet): String {
    val titleRow = sheet.getRow(1) ?: throw PlanningFormatException("Expected row 2 to exist")
    val titleCell = titleRow.firstOrNull { it.stringCellValue?.contains("RECRUITMENT DAY") ?: false }
        ?: throw PlanningFormatException("Expected title cell containing 'RECRUITMENT DAT' in row 2")
    return titleCell.stringCellValue
}

private fun extractInterviews(sheet: Sheet): List<Interview> {
    val startRowNum = getNextInterviewTableStartRow(sheet, 0) ?: return emptyList()
    return readInterviewTableFrom(sheet, startRowNum)
}

private fun getNextInterviewTableStartRow(sheet: Sheet, startRowNum: Int) =
    sheet.drop(startRowNum).firstOrNull { row -> row.firstOrNull()?.stringCellValue == "Interviewer" }?.rowNum

private fun readInterviewTableFrom(sheet: Sheet, startRowNum: Int): List<Interview> {
    val names = listAfterHeader(sheet.getRow(startRowNum))
    val jobTitles = extractJobTitles(sheet, startRowNum + 1)
    val teams = extractTeamsIfPresent(sheet, startRowNum + 2)?.map { it.nullifyIfEmpty() }
    val interviewers = readInterviewers(startRowNum, names, jobTitles, teams)

    val roomsRowNum = if (teams == null) startRowNum + 2 else startRowNum + 3
    val rooms = extractRooms(sheet, roomsRowNum)

    return readInterviews(interviewers, rooms, sheet, roomsRowNum + 1)
}

private fun extractJobTitles(sheet: Sheet, rowNum: Int): List<String> {
    val row = sheet.getRow(rowNum)
    if (!row.startsWith("Job Title")) {
        throw PlanningFormatException("Expected 'Job Title' row at index ${rowNum + 1} (below Interviewers row)")
    }
    return listAfterHeader(row)
}

private fun extractTeamsIfPresent(sheet: Sheet, rowNum: Int): List<String>? {
    val teamRow = sheet.getRow(rowNum)
    return if (!teamRow.startsWith("Team")) null else listAfterHeader(teamRow)
}

private fun extractRooms(sheet: Sheet, rowNum: Int): List<String> {
    val row = sheet.getRow(rowNum)
    if (!row.startsWith("Room")) {
        throw PlanningFormatException("Expected 'Room' row at index ${rowNum + 1}")
    }
    return listAfterHeader(row)
}

private fun Row?.startsWith(firstCell: String) = this?.firstOrNull()?.stringCellValue == firstCell

private fun listAfterHeader(row: Row?): List<String> {
    return row?.drop(1)?.map { it.stringCellValue }.orEmpty()
}

private fun String?.nullifyIfEmpty(): String? = if (isNullOrBlank()) null else this

private fun readInterviewers(
    rowNum: Int,
    names: List<String>,
    jobTitles: List<String>,
    teams: List<String?>?
): List<Employee> {
    if (names.size != jobTitles.size) {
        throw PlanningFormatException("There are ${names.size} names for ${jobTitles.size} job titles (row $rowNum)")
    }
    if (teams != null && names.size != teams.size) {
        throw PlanningFormatException("There are ${names.size} names for ${teams.size} teams (row $rowNum)")
    }
    return names.indices.map {
        val (first, last) = splitNames(names[it])
        Employee(first, last, jobTitle = jobTitles[it], team = teams?.get(it))
    }
}

private fun splitNames(fullName: String): Pair<String, String> {
    val lastSpaceIndex = fullName.lastIndexOf(' ')
    val firstName = fullName.substring(0, lastSpaceIndex)
    val lastName = fullName.substring(lastSpaceIndex + 1)
    return Pair(firstName, lastName)
}

private fun readInterviews(interviewers: List<Employee>, rooms: List<String>, sheet: Sheet, startRowNum: Int):
        List<Interview> {
    val interviews = mutableListOf<Interview>()
    var rowNum = startRowNum
    while (true) {
        val row = sheet.getRow(rowNum)
        val timeSlotStr = row.firstOrNull()?.stringCellValue?.trim()
        if (timeSlotStr == null || !timeSlotStr.matches(Regex("""\d?\d:\d\d\s*-\s*\d?\d:\d\d"""))) {
            throw PlanningFormatException("Expecting time slot at row ${rowNum + 1} with format '00:00 - 00:00'")
        }
        val (start, end) = timeSlotStr.split(Regex("""\s*-\s*"""))
        val startTime = LocalTime.parse(start, DateTimeFormatter.ISO_LOCAL_TIME)
        val endTime = LocalTime.parse(start, DateTimeFormatter.ISO_LOCAL_TIME)
        rowNum++
    }
    return interviews
}

private class PlanningFormatException(message: String) : SystemExitException(message, 1)

