package org.hildan.hrbuddy.agendagenerator.parser

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import java.time.LocalDate
import java.time.ZoneId

internal fun getStringData(sheet: Sheet, key: String): String {
    val cell = getData(sheet, key)
    try {
        val data = cell.stringCellValue
        if (data.isNullOrBlank()) {
            formatError("Missing value for '$key'", cell.rowIndex, cell.columnIndex)
        }
        return data
    } catch (e: IllegalStateException) {
        formatError("Wrong type for '$key', expected text (${e.message})", cell.rowIndex, cell.columnIndex)
    }
}

internal fun getDateData(sheet: Sheet, key: String): LocalDate {
    val cell = getData(sheet, key)
    try {
        val date = cell.dateCellValue ?: formatError("Missing value for '$key'", cell.rowIndex, cell.columnIndex)
        // Excel stores local dates, and Apache POI uses the default time zone to convert to java.util.Date
        // We need to use the default time zone to get back a correct LocalDate
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (e: IllegalStateException) {
        formatError("Wrong type for '$key', expected date (${e.message})", cell.rowIndex, cell.columnIndex)
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
