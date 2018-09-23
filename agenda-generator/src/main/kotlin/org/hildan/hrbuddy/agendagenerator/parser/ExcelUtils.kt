package org.hildan.hrbuddy.agendagenerator.parser

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

internal fun Workbook.getMandatorySheet(name: String): Sheet =
    getSheet(name) ?: formatError("Missing sheet named '$name'")

internal fun Row.expect(values: List<String>) {
    this.forEachIndexed { col, cell -> cell.expect(values[col]) }
}

private fun Cell.expect(content: String) {
    if (stringCellValue != content) {
        formatError("Expected value '$content'", this)
    }
}

internal fun getStringData(sheet: Sheet, key: String): String {
    val cell = getData(sheet, key)
    try {
        val data = cell.stringCellValue
        if (data.isNullOrBlank()) {
            formatError("Missing value for '$key'", cell)
        }
        return data
    } catch (e: IllegalStateException) {
        formatError("Wrong type for '$key', expected text (${e.message})", cell)
    }
}

internal fun getDateData(sheet: Sheet, key: String): LocalDate = getData(sheet, key).localDateValue()

internal fun Cell.localTimeValue(): LocalTime {
    var cellText = DataFormatter().formatCellValue(this)
    try {
        if (cellText.substringBefore(':').length < 2) {
            cellText = "0$cellText"
        }
        return LocalTime.parse(cellText, DateTimeFormatter.ISO_LOCAL_TIME)
    } catch (e: DateTimeParseException) {
        formatError("Expected time with format 00:00 or 00:00:00, got '$cellText'", this)
    }
}

internal fun Cell.localDateValue(): LocalDate {
    try {
        val date = dateCellValue ?: formatError("Missing date value", this)
        // Excel stores local dates, and Apache POI uses the default time zone to convert to java.util.Date
        // We need to use the default time zone to get back a correct LocalDate
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (e: IllegalStateException) {
        formatError("Wrong cell type, expected date, got '$stringCellValue'", this)
    }
}

private fun getData(sheet: Sheet, key: String): Cell {
    val row = sheet.nextRowStartingWith(key)
        ?: formatError("Missing row starting with '$key' in the '${sheet.sheetName}' sheet")
    val rowNum = row.rowNum
    return row.getCell(1) ?: formatError("Missing value for '$key'", rowNum, 1)
}

internal fun Iterable<Cell>?.firstContent(): String? = this?.firstOrNull()?.stringCellValue?.trim()

internal fun Iterable<Cell>?.secondContent(): String? = this?.drop(1)?.firstContent()

internal fun Row?.startsWith(firstCell: String): Boolean = this?.firstContent() == firstCell

internal fun Sheet.nextRowStartingWith(headerCell: String, fromInclusive: Int = 0) =
    drop(fromInclusive).firstOrNull { row -> row.startsWith(headerCell) }

internal fun listAfterHeader(row: Row?): List<String> = row?.drop(1)?.map { it.stringCellValue }.orEmpty()
