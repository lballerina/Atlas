package ca.uwaterloo.atlas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.atlas.domain.place.CostLevel
import ca.uwaterloo.atlas.domain.place.MoodOptions
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.TimeOfDay
import ca.uwaterloo.atlas.platform.TripMap
import ca.uwaterloo.atlas.platform.getImageUploader
import ca.uwaterloo.atlas.ui.utils.parseMarkdown
import ca.uwaterloo.atlas.ui.utils.searchPlaces
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

// Atlas brand colour
private val AtlasPrimary = Color(0xFF4C5BD4)
private const val MAX_PIN_PHOTOS = 10

/**
 * Data class to hold form data for creating/editing a place
 */
data class PlaceFormData(
    val name: String = "",
    val category: PlaceCategory = PlaceCategory.CAFE,
    val address: String = "",
    val photos: List<String> = emptyList(),
    val thumbnailPhoto: String? = null,
    val notes: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val dateVisited: LocalDate? = null,
    val rating: Float? = null,
    val mood: String? = null,
    val tags: List<String> = emptyList(),
    val costIndicator: CostLevel? = null,
    val costAmount: Double? = null,
    val timeOfDay: TimeOfDay? = null,
    val photoCaptions: Map<String, String> = emptyMap(),
    val isFavorite: Boolean = false
)

/**
 * Dialog for adding a new place to a trip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onSave: suspend (PlaceFormData) -> Unit,
    existingPlace: PlaceFormData? = null
) {
    val scope = rememberCoroutineScope()
    var placeName        by remember { mutableStateOf(existingPlace?.name ?: "") }
    var searchQuery      by remember { mutableStateOf(placeName) }
    var searchResults    by remember { mutableStateOf<List<PlaceSearchResult>>(emptyList()) }
    var selectedAddress  by remember { mutableStateOf("") }
    var latitude         by remember { mutableStateOf(0.0) }
    var longitude        by remember { mutableStateOf(0.0) }
    var searchExpanded   by remember { mutableStateOf(false) }
    var searching        by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(existingPlace?.category ?: PlaceCategory.CAFE) }

    val previewPlace = remember(latitude, longitude, placeName) {
        if (latitude != 0.0 && longitude != 0.0) Place(
            id = "preview", tripId = "preview", name = placeName,
            latitude = latitude, longitude = longitude,
            category = selectedCategory, notes = "", address = selectedAddress
        ) else null
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length < 3) { searchResults = emptyList(); return@LaunchedEffect }
        kotlinx.coroutines.delay(300)
        searching = true
        searchResults = searchPlaces(searchQuery)
        searchExpanded = true
        searching = false
    }

    var notes            by remember { mutableStateOf(existingPlace?.notes ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var dateVisited      by remember { mutableStateOf(existingPlace?.dateVisited) }
    var rating           by remember { mutableStateOf<Float?>(existingPlace?.rating) }
    var selectedMood     by remember { mutableStateOf(existingPlace?.mood) }
    var tags             by remember { mutableStateOf(existingPlace?.tags ?: emptyList()) }
    var newTag           by remember { mutableStateOf("") }
    var costIndicator    by remember { mutableStateOf(existingPlace?.costIndicator) }
    var costAmount       by remember { mutableStateOf(existingPlace?.costAmount?.toString() ?: "") }
    var timeOfDay        by remember { mutableStateOf(existingPlace?.timeOfDay) }
    var timeExpanded     by remember { mutableStateOf(false) }
    var isFavorite       by remember { mutableStateOf(existingPlace?.isFavorite ?: false) }
    var selectedPhotos   by remember {
        mutableStateOf<List<SelectedPhoto>>(
            existingPlace?.photos?.map { SelectedPhoto(id = it, uri = it) } ?: emptyList()
        )
    }
    var coverPhotoId by remember { mutableStateOf<String?>(existingPlace?.thumbnailPhoto) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.95f)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header ────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (existingPlace != null) "Edit Place" else "Add Place",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )
                        Text(
                            text = "Search and pin a new location",
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Favourite toggle
                        IconButton(onClick = { isFavorite = !isFavorite }) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourite",
                                tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFFD1D5DB)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF6B7280))
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE5E7EB))

                // ── Scrollable form body ──────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {

                    // Place search
                    PlaceFormSection(title = "Search Place", isRequired = true) {
                        ExposedDropdownMenuBox(
                            expanded = searchExpanded && searchResults.isNotEmpty(),
                            onExpandedChange = {}
                        ) {
                            OutlinedTextField(
                                value = placeName,
                                onValueChange = { placeName = it; searchQuery = it },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                placeholder = { Text("Search for a place…", color = Color(0xFF9CA3AF)) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                trailingIcon = {
                                    if (searching) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = AtlasPrimary
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AtlasPrimary,
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color(0xFFF9FAFB),
                                    unfocusedContainerColor = Color(0xFFF9FAFB),
                                    cursorColor = AtlasPrimary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = searchExpanded && searchResults.isNotEmpty(),
                                onDismissRequest = { searchExpanded = false },
                                containerColor = Color.White
                            ) {
                                searchResults.forEach { result ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(result.name, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F1F1F))
                                                Text(result.address, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                            }
                                        },
                                        onClick = {
                                            placeName = result.name
                                            selectedAddress = result.address
                                            latitude = result.latitude
                                            longitude = result.longitude
                                            searchExpanded = false
                                            searchResults = emptyList()
                                        }
                                    )
                                }
                            }
                        }

                        // Selected location chip
                        if (selectedAddress.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF0F3FF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = AtlasPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("Selected Location", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                                        Text(selectedAddress, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF374151))
                                        Text("Lat: $latitude  Lon: $longitude", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                                    }
                                }
                            }
                        }

                        // Map preview
                        if (previewPlace != null) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Location Preview",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF374151)
                            )
                            Spacer(Modifier.height(6.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                TripMap(
                                    places = listOf(previewPlace!!),
                                    selectedPlace = previewPlace,
                                    modifier = Modifier.fillMaxSize(),
                                    onMapTapped = {},
                                    onMarkerTapped = {}
                                )
                            }
                        }
                    }

                    // Category
                    PlaceFormSection(title = "Category", isRequired = true) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory.displayName,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AtlasPrimary,
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color(0xFFF9FAFB),
                                    unfocusedContainerColor = Color(0xFFF9FAFB)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                containerColor = Color.White
                            ) {
                                PlaceCategory.values().forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.displayName, color = Color(0xFF374151)) },
                                        onClick = { selectedCategory = category; categoryExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // Date & Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DatePickerField(
                            label = "Date Visited",
                            date = dateVisited,
                            onDateChange = { dateVisited = it },
                            modifier = Modifier.weight(1f)
                        )
                        // Time of day dropdown in second column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Time of Day",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F1F1F),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = timeExpanded,
                                onExpandedChange = { timeExpanded = !timeExpanded }
                            ) {
                                OutlinedTextField(
                                    value = timeOfDay?.let { "${it.emoji} ${it.displayName}" } ?: "None",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AtlasPrimary,
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        focusedContainerColor = Color(0xFFF9FAFB),
                                        unfocusedContainerColor = Color(0xFFF9FAFB)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = timeExpanded,
                                    onDismissRequest = { timeExpanded = false },
                                    containerColor = Color.White
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("None", color = Color(0xFF9CA3AF)) },
                                        onClick = { timeOfDay = null; timeExpanded = false }
                                    )
                                    TimeOfDay.values().forEach { time ->
                                        DropdownMenuItem(
                                            text = { Text("${time.emoji} ${time.displayName}", color = Color(0xFF374151)) },
                                            onClick = { timeOfDay = time; timeExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Rating
                    PlaceFormSection(title = "Rating") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentRating = rating?.toInt() ?: 0
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= currentRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Rating $i",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable { rating = if (currentRating == i) null else i.toFloat() }
                                )
                            }
                        }
                    }

                    // Mood
                    PlaceFormSection(title = "How did you feel?") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MoodOptions.presets.forEach { (emoji, label) ->
                                FilterChip(
                                    selected = selectedMood == emoji,
                                    onClick = { selectedMood = if (selectedMood == emoji) null else emoji },
                                    label = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(emoji, fontSize = 22.sp)
                                            Text(label, fontSize = 10.sp)
                                        }
                                    },
                                    modifier = Modifier.padding(2.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFF0F3FF),
                                        selectedLabelColor = AtlasPrimary
                                    )
                                )
                            }
                        }
                    }

                    // Cost
                    PlaceFormSection(title = "Cost") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CostLevel.values().forEach { level ->
                                    FilterChip(
                                        selected = costIndicator == level,
                                        onClick = { costIndicator = if (costIndicator == level) null else level },
                                        label = { Text(level.symbol, fontWeight = FontWeight.SemiBold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFF0F3FF),
                                            selectedLabelColor = AtlasPrimary
                                        )
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = costAmount,
                                onValueChange = { costAmount = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Amount spent (optional)", color = Color(0xFF9CA3AF)) },
                                leadingIcon = {
                                    Text("$", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AtlasPrimary,
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color(0xFFF9FAFB),
                                    unfocusedContainerColor = Color(0xFFF9FAFB)
                                )
                            )
                        }
                    }

                    // Tags
                    PlaceFormSection(title = "Tags") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (tags.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    tags.forEach { tag ->
                                        AssistChip(
                                            onClick = { tags = tags.filter { it != tag } },
                                            label = { Text(tag, color = AtlasPrimary, fontSize = 13.sp) },
                                            trailingIcon = {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                            },
                                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF0F3FF))
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newTag,
                                    onValueChange = { newTag = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Add a tag…", color = Color(0xFF9CA3AF)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AtlasPrimary,
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        focusedContainerColor = Color(0xFFF9FAFB),
                                        unfocusedContainerColor = Color(0xFFF9FAFB)
                                    )
                                )
                                Button(
                                    onClick = {
                                        if (newTag.isNotBlank()) { tags = tags + newTag.trim(); newTag = "" }
                                    },
                                    enabled = newTag.isNotBlank(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                                }
                            }
                        }
                    }

                    // Photos
                    PhotoPickerGrid(
                        photos = selectedPhotos,
                        coverPhotoId = coverPhotoId,
                        onPhotosSelected = { selectedPhotos = it.take(MAX_PIN_PHOTOS) },
                        onPhotoRemoved = { photo ->
                            selectedPhotos = selectedPhotos.filter { it.id != photo.id }
                            if (coverPhotoId == photo.id) coverPhotoId = selectedPhotos.firstOrNull()?.id
                        },
                        onCoverPhotoSelected = { coverPhotoId = it },
                        maxPhotos = MAX_PIN_PHOTOS,
                        showCoverSelection = true
                    )

                    // Notes & Memories
                    PlaceFormSection(title = "Notes & Memories") {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                placeholder = { Text("Share your experience… (Markdown supported)", color = Color(0xFF9CA3AF)) },
                                maxLines = 8,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AtlasPrimary,
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color(0xFFF9FAFB),
                                    unfocusedContainerColor = Color(0xFFF9FAFB)
                                )
                            )
                            HorizontalDivider(color = Color(0xFFE5E7EB))
                            Text(
                                "Live Preview",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF374151)
                            )
                            val previewAnnotated = parseMarkdown(notes)
                            val uriHandler = LocalUriHandler.current
                            Surface(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF9FAFB)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    if (notes.isBlank()) {
                                        Text("Preview will appear here…", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                                    } else {
                                        ClickableText(
                                            text = previewAnnotated,
                                            style = MaterialTheme.typography.bodyLarge,
                                            onClick = { offset ->
                                                previewAnnotated.getStringAnnotations("URL", offset, offset)
                                                    .firstOrNull()?.let { uriHandler.openUri(it.item) }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Action Buttons ────────────────────────────────────────
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF374151))
                    ) {
                        Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            if (placeName.isNotBlank()) {
                                scope.launch {
                                    val uploader = getImageUploader()
                                    val limitedPhotos = selectedPhotos.take(MAX_PIN_PHOTOS)
                                    val uploadedPhotos = limitedPhotos.map { photo ->
                                        if (photo.uri.startsWith("content://"))
                                            uploader.uploadImage(photo.uri, "photos") ?: photo.uri
                                        else photo.uri
                                    }
                                    var updatedThumbnail = coverPhotoId
                                    if (coverPhotoId?.startsWith("content://") == true) {
                                        val index = limitedPhotos.indexOfFirst { it.id == coverPhotoId }
                                        if (index != -1) updatedThumbnail = uploadedPhotos[index]
                                    }
                                    val updatedCaptions = limitedPhotos.mapIndexed { index, photo ->
                                        uploadedPhotos[index] to photo.caption
                                    }.toMap()
                                    onSave(
                                        PlaceFormData(
                                            name = placeName,
                                            category = selectedCategory,
                                            notes = notes,
                                            photos = uploadedPhotos,
                                            photoCaptions = updatedCaptions,
                                            thumbnailPhoto = updatedThumbnail,
                                            latitude = latitude,
                                            longitude = longitude,
                                            address = selectedAddress,
                                            dateVisited = dateVisited,
                                            rating = rating,
                                            mood = selectedMood,
                                            tags = tags,
                                            costIndicator = costIndicator,
                                            costAmount = costAmount.toDoubleOrNull(),
                                            timeOfDay = timeOfDay,
                                            isFavorite = isFavorite
                                        )
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(54.dp),
                        enabled = placeName.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary)
                    ) {
                        Text(
                            if (existingPlace != null) "Save Changes" else "Add Place",
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

// ---------------------------------------------------------------------------
// FormSection — shared label + content layout used across all forms
// ---------------------------------------------------------------------------
@Composable
fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F1F1F)
        )
        content()
    }
}

// Internal helper — same layout but allows isRequired asterisk
@Composable
private fun PlaceFormSection(
    title: String,
    isRequired: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F1F1F)
            )
            if (isRequired) {
                Text(" *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
            }
        }
        content()
    }
}
