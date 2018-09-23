package org.hildan.hrbuddy.agendagenerator.parser

import org.apache.poi.ss.usermodel.Cell

open class PlanningFormatException(
    override val message: String,
    val row: Int?,
    val col: Char?,
    val cell: String?
) : Exception(message)

internal fun formatError(message: String, cell: Cell): Nothing = formatError(message, cell.rowIndex, cell.columnIndex)

internal fun formatError(message: String, rowIndex: Int? = null, colIndex: Int? = null): Nothing {
    val row: Int? = if (rowIndex == null) null else rowIndex + 1
    val col: Char? = if (colIndex == null) null else toLetter(colIndex)
    val cell: String? = if (row != null && col != null) "$col$row" else null

    val position = positionInfoSuffix(row, col, cell)
    val msgWithPosition = "$message$position"

    throw PlanningFormatException(msgWithPosition, row, col, cell)
}

private fun positionInfoSuffix(row: Int?, col: Char?, cell: String?): String = when {
    cell != null -> " (cell $cell)"
    row != null -> " (row $row)"
    col != null -> " (column $col)"
    else -> ""
}

private fun toLetter(colIndex: Int): Char = 'A' + colIndex
