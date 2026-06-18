package ca.uwaterloo.atlas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate

data class TripFilters(
    val showPublic: Boolean = true,
    val showPrivate: Boolean = true,
    val selectedTags: Set<String> = emptySet(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val isActive: Boolean
        get() = !showPublic ||
                !showPrivate ||
                selectedTags.isNotEmpty() ||
                startDate != null ||
                endDate != null
}

/**
 * Bottom sheet for filtering trips by visibility, tags, and date range.
 *
 * Uses [MaterialTheme.colorScheme] tokens throughout so it automatically
 * inherits any future AtlasTheme changes without touching this file.
 *
 * @param filters        Current filter state to pre-populate the sheet.
 * @param availableTags  Tags present on the current trip list — only shown if non-empty.
 * @param showVisibility Whether to show the Public / Private toggles (My Trips only).
 * @param onApply        Called with the new [TripFilters] when the user taps Apply.
 * @param onDismiss      Called when the sheet should close without applying.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFilterSheet(
    filters: TripFilters,
    availableTags: Set<String> = emptySet(),
    showVisibility: Boolean = true,
    onApply: (TripFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var showPublic   by remember { mutableStateOf(filters.showPublic) }
    var showPrivate  by remember { mutableStateOf(filters.showPrivate) }
    var selectedTags by remember { mutableStateOf(filters.selectedTags) }
    var startDate    by remember { mutableStateOf<LocalDate?>(filters.startDate) }
    var endDate      by remember { mutableStateOf<LocalDate?>(filters.endDate) }

    // Count active filters for a summary pill on the header
    val activeCount = listOfNotNull(
        if (!showPublic) true else null,
        if (!showPrivate) true else null,
        if (startDate != null) true else null,
        if (endDate != null) true else null,
    ).size + selectedTags.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // White sheet surface matching the card / dialog style
        containerColor = MaterialTheme.colorScheme.surface,
        // Rounded top corners matching dialog radius
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            // Styled drag handle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── Sheet header ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Filter Trips",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (activeCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "$activeCount",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Visibility (My Trips only) ────────────────────────────────
            if (showVisibility) {
                FilterSection(title = "Visibility") {
                    // Card-style toggle matching CreateEditTripDialog's VisibilityOption
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            VisibilityToggleRow(
                                emoji    = "🌍",
                                label    = "Public",
                                selected = showPublic,
                                onClick  = { showPublic = !showPublic }
                            )
                            VisibilityToggleRow(
                                emoji    = "🔒",
                                label    = "Private",
                                selected = showPrivate,
                                onClick  = { showPrivate = !showPrivate }
                            )
                        }
                    }
                }
            }

            // ── Date range ────────────────────────────────────────────────
            FilterSection(title = "Date Range") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DatePickerField(
                        label      = "Start date",
                        date       = startDate,
                        onDateChange = { startDate = it },
                        modifier   = Modifier.weight(1f)
                    )
                    DatePickerField(
                        label      = "End date",
                        date       = endDate,
                        onDateChange = { endDate = it },
                        modifier   = Modifier.weight(1f)
                    )
                }
            }

            // ── Tags ──────────────────────────────────────────────────────
            if (availableTags.isNotEmpty()) {
                FilterSection(title = "Tags") {
                    availableTags.sorted().chunked(3).forEach { rowTags ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTags.forEach { tag ->
                                val isSelected = tag in selectedTags
                                FilterChip(
                                    selected = isSelected,
                                    onClick  = {
                                        selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                                    },
                                    label    = {
                                        Text(
                                            "#$tag",
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                                        containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ── Action buttons ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showPublic   = true
                        showPrivate  = true
                        selectedTags = emptySet()
                        startDate    = null
                        endDate      = null
                        onApply(TripFilters())
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Clear All", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        onApply(
                            TripFilters(
                                showPublic   = showPublic,
                                showPrivate  = showPrivate,
                                selectedTags = selectedTags,
                                startDate    = startDate,
                                endDate      = endDate
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Apply", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────

@Composable
private fun FilterSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text       = title,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}

/**
 * A single toggle row inside the card-style visibility selector.
 * Uses a checkbox-style indicator so multiple options can be selected
 * simultaneously (unlike radio buttons which are mutually exclusive).
 */
@Composable
private fun VisibilityToggleRow(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (selected) Modifier.shadow(3.dp, RoundedCornerShape(12.dp)) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(
                text       = label,
                fontSize   = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier   = Modifier.weight(1f)
            )
            Checkbox(
                checked         = selected,
                onCheckedChange = { onClick() },
                colors          = CheckboxDefaults.colors(
                    checkedColor   = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}
