package ca.uwaterloo.atlas.ui.components

import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.domain.profile.TravelStyleTag
import ca.uwaterloo.atlas.platform.PlatformImage
import ca.uwaterloo.atlas.platform.rememberPhotoPickerLauncher
import kotlinx.datetime.LocalDate

// Atlas brand colour
private val AtlasPrimary = Color(0xFF4C5BD4)

/**
 * Data class to hold form data from the Create/Edit Trip dialog.
 */
data class TripFormData(
    val name: String,
    val destination: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val imageUrl: String,
    val isPublic: Boolean,
    val tags: List<String> = emptyList()
)

/**
 * Dialog for creating new trips or editing existing trips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTripDialog(
    isEdit: Boolean = false,
    existingTrip: TripData? = null,
    onDismiss: () -> Unit,
    onSave: (TripFormData) -> Unit
) {
    var tripName        by remember { mutableStateOf(existingTrip?.title    ?: "") }
    var mainDestination by remember { mutableStateOf(existingTrip?.location ?: "") }
    var startDate       by remember { mutableStateOf<LocalDate?>(existingTrip?.startDate) }
    var endDate         by remember { mutableStateOf<LocalDate?>(existingTrip?.endDate) }
    var isPublic        by remember { mutableStateOf(existingTrip?.isPublic ?: true) }
    var selectedTags    by remember { mutableStateOf(existingTrip?.tags?.toSet() ?: emptySet<String>()) }
    var coverPhotoUri   by remember { mutableStateOf(existingTrip?.imageUrl ?: "") }

    val launchPhotoPicker = rememberPhotoPickerLauncher { uriStrings ->
        uriStrings.firstOrNull()?.let { coverPhotoUri = it }
    }

    val start = startDate
    val end = endDate
    val hasInvalidDateRange = start != null && end != null && end < start
    val canSave = tripName.isNotBlank() &&
            mainDestination.isNotBlank() &&
            startDate != null &&
            endDate != null &&
            !hasInvalidDateRange

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // ── Header ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEdit) "Edit Trip" else "New Trip",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )
                        Text(
                            text = if (isEdit) "Update your adventure" else "Start planning your adventure",
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF6B7280))
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Cover photo ───────────────────────────────────────────
                CoverPhotoField(
                    uri = coverPhotoUri,
                    onPickPhoto = { launchPhotoPicker() },
                    onRemovePhoto = { coverPhotoUri = "" }
                )

                Spacer(Modifier.height(20.dp))

                // ── Trip name ─────────────────────────────────────────────
                TripFormField(
                    label = "Trip Name",
                    value = tripName,
                    onValueChange = { tripName = it },
                    isRequired = true,
                    placeholder = "e.g., Summer in Paris"
                )

                Spacer(Modifier.height(16.dp))

                // ── Main destination ──────────────────────────────────────
                TripFormField(
                    label = "Main Destination",
                    value = mainDestination,
                    onValueChange = { mainDestination = it },
                    isRequired = true,
                    placeholder = "e.g., Paris, France"
                )

                Spacer(Modifier.height(16.dp))

                // ── Dates ─────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DatePickerField(
                        label = "Start Date",
                        date = startDate,
                        onDateChange = { startDate = it },
                        isRequired = true,
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerField(
                        label = "End Date",
                        date = endDate,
                        onDateChange = { endDate = it },
                        isRequired = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (hasInvalidDateRange) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "End date must be on or after start date.",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(22.dp))

                // ── Tags ──────────────────────────────────────────────────
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            "Tags",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )
                    }
                    TravelStyleTag.entries.chunked(3).forEach { rowTags ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTags.forEach { tag ->
                                val selected = tag.label in selectedTags
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedTags = if (selected) selectedTags - tag.label
                                        else selectedTags + tag.label
                                    },
                                    label = { Text(tag.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AtlasPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF3F4F6),
                                        labelColor = Color(0xFF374151)
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(22.dp))

                // ── Visibility ────────────────────────────────────────────
                Column {
                    Text(
                        "Visibility",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F1F1F),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            VisibilityOption(
                                emoji = "🔒",
                                title = "Private",
                                subtitle = "Only you can see this trip",
                                selected = !isPublic,
                                onClick = { isPublic = false }
                            )
                            VisibilityOption(
                                emoji = "🌍",
                                title = "Public",
                                subtitle = "Share with the community",
                                selected = isPublic,
                                onClick = { isPublic = true }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Action Buttons ────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF374151))
                    ) {
                        Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            onSave(
                                TripFormData(
                                    name = tripName,
                                    destination = mainDestination,
                                    startDate = startDate,
                                    endDate = endDate,
                                    imageUrl = coverPhotoUri,
                                    isPublic = isPublic,
                                    tags = selectedTags.toList()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary),
                        enabled = canSave
                    ) {
                        Text(
                            if (isEdit) "Save Changes" else "Create Trip",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ── VisibilityOption ─────────────────────────────────────────────────────────

@Composable
private fun VisibilityOption(
    emoji: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color.White else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (selected) Modifier.shadow(4.dp, RoundedCornerShape(12.dp))
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (selected) Color(0xFF1F1F1F) else Color(0xFF6B7280)
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = AtlasPrimary)
            )
        }
    }
}

// ── CoverPhotoField ──────────────────────────────────────────────────────────

@Composable
private fun CoverPhotoField(
    uri: String,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Column {
        Text(
            "Cover Photo",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F1F1F),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        if (uri.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                PlatformImage(
                    uri = uri,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
                // Remove button
                IconButton(
                    onClick = onRemovePhoto,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.Black.copy(alpha = 0.55f)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp).size(16.dp)
                        )
                    }
                }
                // Change label
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    onClick = onPickPhoto
                ) {
                    Text(
                        "Change",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF9FAFB),
                onClick = onPickPhoto
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0F3FF),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("📷", fontSize = 24.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Add Cover Photo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasPrimary
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Tap to choose from your library",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

// ── TripFormField ────────────────────────────────────────────────────────────

@Composable
private fun TripFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean = false,
    placeholder: String = ""
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F1F1F))
            if (isRequired) {
                Text(" *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AtlasPrimary,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color(0xFFF9FAFB),
                unfocusedContainerColor = Color(0xFFF9FAFB),
                cursorColor = AtlasPrimary
            )
        )
    }
}
