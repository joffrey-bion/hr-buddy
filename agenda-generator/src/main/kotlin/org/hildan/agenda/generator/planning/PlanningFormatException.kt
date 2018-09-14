package org.hildan.agenda.generator.planning

import com.xenomachina.argparser.SystemExitException

open class PlanningFormatException(message: String, val row: Int?, val col: Int?) : SystemExitException(message, 1)

internal fun formatError(message: String, row: Int? = null, col: Int? = null): Nothing {
    val positionInfo = positionInfo(row, col)
    val fullMsg = if (positionInfo == null) message else "$message $positionInfo"
    throw PlanningFormatException(fullMsg, row, col)
}

private fun positionInfo(row: Int?, col: Int?): String? = when {
    row != null && col != null -> "(cell ${toLetter(col)}${row + 1})"
    row != null && col == null -> "(row ${row + 1})"
    row == null && col != null -> "(column ${toLetter(col)})"
    else -> null
}

private fun toLetter(col: Int): Char {
    return 'A' + col
}
