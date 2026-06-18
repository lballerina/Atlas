package ca.uwaterloo.atlas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.atlas.domain.place.CostLevel
import ca.uwaterloo.atlas.domain.place.MoodOptions
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.TimeOfDay
import ca.uwaterloo.atlas.platform.PlatformImage
import ca.uwaterloo.atlas.ui.components.DatePickerField
import ca.uwaterloo.atlas.ui.components.FormSection
import ca.uwaterloo.atlas.ui.components.PhotoPickerGrid
import ca.uwaterloo.atlas.ui.components.SelectedPhoto
import ca.uwaterloo.atlas.ui.utils.parseMarkdown
import ca.uwaterloo.atlas.viewmodel.SinglePinViewModel

// Atlas brand colours
private val AtlasPrimary   = Color(0xFF4C5BD4)
private val AtlasGradStart = Color(0xFF4C5BD4)
private val AtlasGradMid   = Color(0xFF8D6E95)
private val AtlasGradEnd   = Color(0xFFC79AA1)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SinglePinScreen(
    placeId: String,
    vm: SinglePinViewModel,
    onBackClick: () -> Unit,
    onDeletePin: () -> Unit
) {
    LaunchedEffect(placeId) { vm.loadPlace(placeId) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var addressExpanded by remember { mutableStateOf(false) }
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }
    val place = vm.place

    if (place == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AtlasPrimary)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF7F7FB),
            topBar = {
            // Gradient header — matches Login / OtherProfile / ViewSingleTrip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AtlasGradStart, AtlasGradMid, AtlasGradEnd)
                        )
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        vm.exit()
                        onBackClick()
                    }, modifier = Modifier.testTag("backButton"), // for ui unit test!
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !place.address.isNullOrBlank()) { 
                                addressExpanded = !addressExpanded 
                            }
                    ) {
                        Text(
                            text = place.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!place.address.isNullOrBlank()) {
                            Text(
                                text = place.address!!,
                                maxLines = if (addressExpanded) 5 else 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                        }
                    }
                    // Action buttons
                    if (vm.isEditable && !vm.isEditing) {
                        IconButton(
                            onClick = { vm.startEditing(place.notes) },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .testTag("editButton"), // for ui unit test!
                            ) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                    } else if (vm.isEditable && vm.isEditing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TextButton(onClick = { showDeleteDialog = true }) {
                                Text("Delete", color = Color(0xFFFF8A80), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            TextButton(onClick = { vm.cancelEditing() }) {
                                Text("Cancel", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                            }
                            Surface(
                                onClick = {
                                    vm.updateNotes(vm.editingText)
                                    vm.updateMetadata(
                                        vm.dateVisited,
                                        vm.price,
                                        vm.rating,
                                        vm.selectedCategory,
                                        vm.tags,
                                        vm.timeOfDay,
                                        vm.isFavorite,
                                        newMood = vm.selectedMood,
                                        newAmount = vm.costAmount
                                    )
                                    vm.finishEditing()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White
                            ) {
                                Text(
                                    "Save",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = AtlasPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            }
        ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            item {
                // ── Metadata card ─────────────────────────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-8).dp)
                        .shadow(10.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!vm.isEditing) {
                            // ── VIEW: meta row ────────────────────────────
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                place.category.let { cat ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF0F3FF)
                                    ) {
                                        Text(
                                            text = cat.displayName,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AtlasPrimary
                                        )
                                    }
                                }
                                place.timeOfDay?.let { tod ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF5F5F5)
                                    ) {
                                        Text(
                                            text = "${tod.emoji} ${tod.displayName}",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            fontSize = 12.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                Text(text = place.mood ?: "", fontSize = 22.sp)
                                FavouriteIcon(vm) {}
                            }

                            // Date
                            place.dateVisited?.let {
                                Text(
                                    text = "📅 $it",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            // Tags
                            if (place.tags.isNotEmpty()) DisplayTags(place.tags)

                            // Stars + price
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                place.rating?.let { r ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val currentRating = r.toInt()
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = if (i <= currentRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = Color(0xFFFFC107),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        Text("%.0f".format(r), fontSize = 14.sp, color = Color(0xFF374151))
                                    }
                                }
                                place.costIndicator?.let { PriceRatings(it) }
                                place.costAmount?.let {
                                    Text("($${it})", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                                }
                            }

                        } else {
                            // ── EDIT: collapsible metadata ────────────────
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { vm.metadataExpanded = !vm.metadataExpanded },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0F3FF)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (vm.metadataExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = AtlasPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (vm.metadataExpanded) "Collapse Details" else "Edit Details",
                                        fontWeight = FontWeight.SemiBold,
                                        color = AtlasPrimary,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            AnimatedVisibility(visible = vm.metadataExpanded) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        DatePickerField(
                                            label = "Date Visited",
                                            date = vm.dateVisited,
                                            onDateChange = { vm.updateDateVisited(it) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        FavouriteIcon(vm) { vm.updateFavorite(!vm.isFavorite) }
                                    }
                                    EditTimeOfDay(vm)
                                    EditCategory(vm)
                                    FormSection(title = "How did you feel?") {
                                        EditMoodRatings(vm) {}
                                    }
                                    EditTags(vm)
                                    StarRatings(vm, vm.rating) { vm.updateRating(it) }
                                    FormSection(title = "Price") {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            EditPriceRatings(vm.price) { vm.updatePrice(it) }
                                            OutlinedTextField(
                                                value = vm.costAmount?.toString() ?: "",
                                                onValueChange = { vm.updateCostAmount(it.toDoubleOrNull()) },
                                                modifier = Modifier.fillMaxWidth(),
                                                placeholder = { Text("Amount spent (e.g. 45.50)", color = Color(0xFF9CA3AF)) },
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = AtlasPrimary,
                                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                                    focusedContainerColor = Color(0xFFF9FAFB),
                                                    unfocusedContainerColor = Color(0xFFF9FAFB)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Photos ────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (!vm.isEditing || !vm.isEditable) {
                        if (place.photos.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = 2
                            ) {
                                place.photos.forEachIndexed { index, uri ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clickable { fullscreenIndex = index }
                                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(0.dp)
                                    ) {
                                        PlatformImage(
                                            uri = uri,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val selectedPhotos = place.photos.map { uri ->
                            SelectedPhoto(
                                id = uri,
                                uri = uri,
                                caption = place.photoCaptions[uri] ?: ""
                            )
                        }
                        PhotoPickerGrid(
                            photos = selectedPhotos,
                            coverPhotoId = place.thumbnailPhoto,
                            onPhotosSelected = { updatedList ->
                                vm.updatePhotos(
                                    photos = updatedList.map { it.uri },
                                    thumbnail = updatedList.firstOrNull()?.id,
                                    captions = updatedList.associate { it.uri to it.caption }
                                )
                            },
                            onPhotoRemoved = { removedPhoto ->
                                val filtered = selectedPhotos.filter { it.id != removedPhoto.id }
                                vm.updatePhotos(
                                    photos = filtered.map { it.uri },
                                    thumbnail = filtered.firstOrNull()?.id,
                                    captions = filtered.associate { it.uri to it.caption }
                                )
                            },
                            onCoverPhotoSelected = { coverId ->
                                vm.updatePhotos(
                                    photos = selectedPhotos.map { it.uri },
                                    thumbnail = coverId,
                                    captions = selectedPhotos.associate { it.uri to it.caption }
                                )
                            },
                            maxPhotos = 10,
                            showCoverSelection = true
                        )
                    }
                }

            }

            // ── Notes card ────────────────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(6.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "✍️",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                "Notes & Memories",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )
                        }

                        if (!vm.isEditing) {
                            val annotated = parseMarkdown(place.notes)
                            val uriHandler = LocalUriHandler.current
                            if (place.notes.isBlank()) {
                                Text(
                                    "No notes added yet.",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 14.sp
                                )
                            } else {
                                ClickableText(
                                    text = annotated,
                                    style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF374151)),
                                    onClick = { offset ->
                                        annotated.getStringAnnotations("URL", offset, offset)
                                            .firstOrNull()
                                            ?.let { uriHandler.openUri(it.item) }
                                    }
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = vm.editingText,
                                    onValueChange = { vm.updateEditingText(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    placeholder = { Text("Write your notes here… (Markdown supported)", color = Color(0xFF9CA3AF)) },
                                    maxLines = 8,
                                    shape = RoundedCornerShape(12.dp),
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
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF374151)
                                )
                                val previewAnnotated = parseMarkdown(vm.editingText)
                                val uriHandler = LocalUriHandler.current
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF9FAFB)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        if (vm.editingText.isBlank()) {
                                            Text("Preview will appear here…", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                                        } else {
                                            ClickableText(
                                                text = previewAnnotated,
                                                style = MaterialTheme.typography.bodyLarge,
                                                onClick = { offset ->
                                                    previewAnnotated.getStringAnnotations("URL", offset, offset)
                                                        .firstOrNull()
                                                        ?.let { uriHandler.openUri(it.item) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        fullscreenIndex?.let { index ->
            val imageUri = place.photos.getOrNull(index) ?: return@let
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(index, place.photos.size) {
                        var totalDrag = 0f
                        var handled = false
                        detectHorizontalDragGestures(
                            onDragStart = {
                                totalDrag = 0f
                                handled = false
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                                if (handled) return@detectHorizontalDragGestures

                                when {
                                    totalDrag <= -80f && index < place.photos.lastIndex -> {
                                        fullscreenIndex = index + 1
                                        handled = true
                                    }
                                    totalDrag >= 80f && index > 0 -> {
                                        fullscreenIndex = index - 1
                                        handled = true
                                    }
                                }
                            }
                        )
                    }
            ) {
                PlatformImage(
                    uri = imageUri,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 56.dp, horizontal = 8.dp),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { fullscreenIndex = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                place.photoCaptions[imageUri]?.takeIf { it.isNotBlank() }?.let { caption ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = caption,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }

                if (index > 0) {
                    IconButton(
                        onClick = { fullscreenIndex = index - 1 },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color.White
                        )
                    }
                }

                if (index < place.photos.lastIndex) {
                    IconButton(
                        onClick = { fullscreenIndex = index + 1 },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    // ── Delete dialog ─────────────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Text("Delete Pin", fontWeight = FontWeight.Bold, color = Color(0xFF1F1F1F))
            },
            text = {
                Text(
                    "Are you sure you want to delete this pin? This can't be undone.",
                    color = Color(0xFF6B7280)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deletePlace()
                        showDeleteDialog = false
                        onDeletePin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancel", color = Color(0xFF374151)) }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Sub-composables (kept in this file, matching the original structure)
// ---------------------------------------------------------------------------

@Composable
fun EditMoodRatings(vm: SinglePinViewModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MoodOptions.presets.forEach { (emoji, label) ->
            FilterChip(
                selected = vm.selectedMood == emoji,
                onClick = { vm.updateMood(if (vm.selectedMood == emoji) null else emoji) },
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

@Composable
fun StarRatings(
    vm: SinglePinViewModel,
    rating: Float?,
    onRatingChange: (Float?) -> Unit
) {
    FormSection(title = "Rating") {
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
                        .size(28.dp)
                        .clickable(enabled = vm.isEditable && vm.isEditing) {
                            onRatingChange(if (currentRating == i) null else i.toFloat())
                        }
                )
            }
        }
    }
}

@Composable
fun PriceRatings(price: CostLevel) {
    val costIndex = when (price) {
        CostLevel.BUDGET   -> 1
        CostLevel.MODERATE -> 2
        CostLevel.EXPENSIVE -> 3
        CostLevel.LUXURY   -> 4
    }
    Row {
        for (i in 1..costIndex) {
            Icon(
                Icons.Filled.AttachMoney,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun EditPriceRatings(price: CostLevel?, onPriceChange: (Int?) -> Unit) {
    val costIndex = when (price) {
        CostLevel.BUDGET    -> 1
        CostLevel.MODERATE  -> 2
        CostLevel.EXPENSIVE -> 3
        CostLevel.LUXURY    -> 4
        null                -> 0
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..4) {
            Icon(
                imageVector = if (i <= costIndex) Icons.Filled.AttachMoney else Icons.Outlined.AttachMoney,
                contentDescription = null,
                tint = if (i <= costIndex) Color(0xFF10B981) else Color(0xFFD1D5DB),
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        onPriceChange(if (costIndex == i) null else i)
                    }
            )
        }
    }
}

@Composable
fun FavouriteIcon(
    vm: SinglePinViewModel,
    onClick: (Boolean) -> Unit,
) {
    IconButton(
        onClick = { onClick(!vm.isFavorite) },
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Favourite",
            tint = if (vm.isFavorite) Color(0xFFEF4444) else Color(0xFFD1D5DB),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun EditTags(vm: SinglePinViewModel) {
    FormSection(title = "Tags") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (vm.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vm.tags.forEach { tag ->
                        AssistChip(
                            onClick = { vm.updateTags(vm.tags.filter { it != tag }) },
                            label = { Text(tag, color = AtlasPrimary) },
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
                    value = vm.newTag,
                    onValueChange = { vm.newTag = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a tag…", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AtlasPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    )
                )
                Button(
                    onClick = {
                        if (vm.newTag.isNotBlank()) {
                            vm.tags = vm.tags + vm.newTag.trim()
                            vm.newTag = ""
                        }
                    },
                    enabled = vm.newTag.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add tag", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun DisplayTags(tags: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFF0F3FF)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AtlasPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategory(vm: SinglePinViewModel) {
    var categoryExpanded by remember { mutableStateOf(false) }
    FormSection(title = "Category *") {
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = vm.selectedCategory.displayName,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                shape = RoundedCornerShape(12.dp),
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
                        onClick = { vm.updateCategory(category); categoryExpanded = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimeOfDay(vm: SinglePinViewModel) {
    FormSection(title = "Time of Day") {
        ExposedDropdownMenuBox(
            expanded = vm.timeExpanded,
            onExpandedChange = { vm.timeExpanded = !vm.timeExpanded }
        ) {
            OutlinedTextField(
                value = vm.timeOfDay?.let { "${it.emoji} ${it.displayName}" } ?: "Not selected",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vm.timeExpanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AtlasPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                )
            )
            ExposedDropdownMenu(
                expanded = vm.timeExpanded,
                onDismissRequest = { vm.timeExpanded = false },
                containerColor = Color.White
            ) {
                DropdownMenuItem(
                    text = { Text("None", color = Color(0xFF9CA3AF)) },
                    onClick = { vm.updateTimeOfDay(null); vm.timeExpanded = false }
                )
                TimeOfDay.values().forEach { time ->
                    DropdownMenuItem(
                        text = { Text("${time.emoji} ${time.displayName}", color = Color(0xFF374151)) },
                        onClick = { vm.updateTimeOfDay(time); vm.timeExpanded = false }
                    )
                }
            }
        }
    }
}
