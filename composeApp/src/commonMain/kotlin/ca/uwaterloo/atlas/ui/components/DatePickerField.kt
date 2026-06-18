package ca.uwaterloo.atlas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ── Helpers ───────────────────────────────────────────────────────────────

/** Expected typed format shown to users. */
const val DATE_INPUT_FORMAT = "MM/DD/YYYY"

/**
 * Parses a user-typed string in MM/DD/YYYY format into a [LocalDate],
 * returning null if the string is incomplete or invalid.
 */
fun parseTypedDate(input: String): LocalDate? {
    if (input.length != 10) return null
    return try {
        val parts = input.split("/")
        if (parts.size != 3) return null
        val month = parts[0].toInt()
        val day = parts[1].toInt()
        val year = parts[2].toInt()
        LocalDate(year, month, day)
    } catch (_: Exception) {
        null
    }
}

/**
 * Formats a [LocalDate] into the MM/DD/YYYY string shown in the text field.
 */
fun LocalDate.toDisplayString(): String =
    "${monthNumber.toString().padStart(2, '0')}/" +
            "${dayOfMonth.toString().padStart(2, '0')}/" +
            "$year"

/**
 * Converts a [DatePickerState] to a [LocalDate].
 * Returns null when nothing is selected yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
fun DatePickerState.toLocalDate(): LocalDate? {
    val millis = selectedDateMillis ?: return null
    return Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.UTC)
        .date
}

/**
 * Converts a [LocalDate] to the value expected by [DatePickerState].
 * Uses noon UTC to avoid timezone-boundary issues.
 */
fun LocalDate.toEpochMillis(): Long {
    // Build an ISO string and parse via kotlinx-datetime
    val isoString = "${year}-${monthNumber.toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}T12:00:00Z"
    return Instant.parse(isoString).toEpochMilliseconds()
}

// ── DatePickerField ───────────────────────────────────────────────────────

/**
 * A date input field that lets the user:
 *  - Type a date in MM/DD/YYYY format — the calendar selection updates automatically
 *    once a valid complete date is typed.
 *  - Tap the calendar icon to open a [DatePickerDialog] — the text field updates
 *    when a date is confirmed.
 *  - Tap the clear icon (shown when a date is selected) to reset.
 *
 * @param label         Field label displayed above the text field.
 * @param isRequired    Whether to show a red asterisk next to the label.
 * @param date          The currently selected [LocalDate], or null if none.
 * @param onDateChange  Called whenever the selected date changes (or is cleared).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Text the user is typing – kept in sync with `date`
    var textInput by remember(date) {
        mutableStateOf(date?.toDisplayString() ?: "")
    }
    var showPicker by remember { mutableStateOf(false) }

    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = date?.toEpochMillis()
    )

    Column(modifier = modifier) {
        // ── Optional Label ────────────────────────────────────────────────
        if (label.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                if (isRequired) {
                    Text(
                        " *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Red
                    )
                }
            }
        }

        // ── Text field ────────────────────────────────────────────────────
        OutlinedTextField(
            value = textInput,
            onValueChange = { raw ->
                textInput = raw
                val parsed = parseTypedDate(raw)
                // Only fire onDateChange when the typed string is a valid complete date
                // or when the field is cleared entirely
                if (parsed != null || raw.isEmpty()) {
                    onDateChange(parsed)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(DATE_INPUT_FORMAT, color = Color.Gray) },
            trailingIcon = {
                if (date != null) {
                    // Show clear button when a date is selected
                    IconButton(onClick = {
                        textInput = ""
                        onDateChange(null)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear date", tint = Color.Gray)
                    }
                } else {
                    // Show calendar icon to open picker
                    IconButton(onClick = { showPicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Open calendar", tint = Color.Gray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color(0xFFF5F5F5)
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            singleLine = true,
            isError = textInput.isNotEmpty() && textInput.length == 10 && parseTypedDate(textInput) == null
        )
    }

    // ── DatePickerDialog ──────────────────────────────────────────────────
    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val picked = pickerState.toLocalDate()
                    if (picked != null) {
                        textInput = picked.toDisplayString()
                        onDateChange(picked)
                    }
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
