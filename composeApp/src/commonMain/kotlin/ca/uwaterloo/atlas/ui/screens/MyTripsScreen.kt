package ca.uwaterloo.atlas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.platform.PlatformImage
import ca.uwaterloo.atlas.ui.components.CreateEditTripDialog
import ca.uwaterloo.atlas.ui.components.TripCoverImage
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.components.TripFilterSheet
import ca.uwaterloo.atlas.ui.components.TripFormData
import ca.uwaterloo.atlas.ui.components.toDisplayString

/**
 * My Trips Screen – shows trips the current user has added.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripsScreen(
    trips: List<TripData>,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    activeFilters: TripFilters = TripFilters(),
    availableTags: Set<String> = emptySet(),
    onApplyFilters: (TripFilters) -> Unit = {},
    onAddTripClick: () -> Unit = {},
    onTripClick: (TripData) -> Unit = {},
    onSaveTrip: (TripFormData) -> Unit = {},
    onEditTrip: (TripData, TripFormData) -> Unit = { _, _ -> },
    onDeleteTrip: (TripData) -> Unit = {}
) {
    var showCreateDialog  by remember { mutableStateOf(false) }
    var showEditDialog    by remember { mutableStateOf(false) }
    var showFilterSheet   by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedTrip      by remember { mutableStateOf<TripData?>(null) }

    Scaffold(
        containerColor = Color(0xFFF7F7FB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7FB))
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4C5BD4),
                                Color(0xFF8D6E95),
                                Color(0xFFC79AA1)
                            )
                        )
                    )
                    .statusBarsPadding()
            ) {                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row() {
                            PlatformImage(
                                uri = "https://ulcxyvywffoxuafjjszo.supabase.co/storage/v1/object/public/photos/smallLogoTransparent.png",
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .size(40.dp),
                                contentScale = ContentScale.Inside
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "COLLECTION",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "My Trips",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF4C5BD4)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("New Trip", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp)
                    .padding(horizontal = 20.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            placeholder = { Text("Search your trips...", color = Color(0xFF9CA3AF), fontSize = 15.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF4C5BD4), modifier = Modifier.size(20.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = Color(0xFF4C5BD4)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        Surface(
                            onClick = { showFilterSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            color = if (activeFilters.isActive) Color(0xFF4C5BD4) else Color(0xFFF3F4F6),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                BadgedBox(
                                    badge = { 
                                        if (activeFilters.isActive) {
                                            Badge(containerColor = Color.White, modifier = Modifier.size(6.dp))
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = if (activeFilters.isActive) Color.White else Color(0xFF4C5BD4),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Trip list ────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (trips.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No trips yet. Tap \"Add Trip\" to create your first one!",
                                color = Color.Gray,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
                items(trips) { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { onTripClick(trip) },
                        onEditClick = {
                            selectedTrip = trip
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            selectedTrip = trip
                            showDeleteConfirm = true
                        }
                    )
                }
            }
        }
    }

    // ── Filter sheet ──────────────────────────────────────────────────────
    if (showFilterSheet) {
        TripFilterSheet(
            filters = activeFilters,
            availableTags = availableTags,
            showVisibility = true,
            onApply = { newFilters ->
                onApplyFilters(newFilters)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    // ── Create dialog ─────────────────────────────────────────────────────
    if (showCreateDialog) {
        CreateEditTripDialog(
            isEdit = false,
            onDismiss = { showCreateDialog = false },
            onSave = { tripData ->
                onSaveTrip(tripData)
                showCreateDialog = false
            }
        )
    }

    // ── Edit dialog ───────────────────────────────────────────────────────
    if (showEditDialog && selectedTrip != null) {
        CreateEditTripDialog(
            isEdit = true,
            existingTrip = selectedTrip,
            onDismiss = {
                showEditDialog = false
                selectedTrip = null
            },
            onSave = { tripData ->
                selectedTrip?.let { trip -> onEditTrip(trip, tripData) }
                showEditDialog = false
                selectedTrip = null
            }
        )
    }

    // ── Delete confirmation dialog ────────────────────────────────────────
    if (showDeleteConfirm && selectedTrip != null) {
        DeleteTripConfirmDialog(
            tripTitle = selectedTrip!!.title,
            onConfirm = {
                selectedTrip?.let { onDeleteTrip(it) }
                showDeleteConfirm = false
                selectedTrip = null
            },
            onDismiss = {
                showDeleteConfirm = false
                selectedTrip = null
            }
        )
    }
}

// ── DeleteTripConfirmDialog ───────────────────────────────────────────────

@Composable
private fun DeleteTripConfirmDialog(
    tripTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Delete Trip?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "\"$tripTitle\" and all its pins will be permanently deleted. This can't be undone.",
                color = Color.Gray
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ── TripCard ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCard(
    trip: TripData,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    // Kept for any call sites that still pass onLongClick
    onLongClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(10.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Cover image ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                TripCoverImage(
                    trip = trip,
                    modifier = Modifier.fillMaxSize()
                )

                // Privacy badge — top-left
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = if (trip.isPublic) "Public" else "Private",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (trip.isPublic) Color(0xFF4C5BD4) else Color(0xFF1F1F1F)
                    )
                }

                // ── 3-dot menu — top-right ────────────────────────────────
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.4f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Trip options",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Edit trip", fontSize = 15.sp, color = Color.Black)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFF4C5BD4)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete trip",
                                    fontSize = 15.sp,
                                    color = Color(0xFFE09090)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFE09090)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            // ── Text content ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = trip.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = trip.location,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (trip.startDate != null && trip.endDate != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = "${trip.startDate.toDisplayString()} — ${trip.endDate.toDisplayString()}",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (trip.placesCount != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F4FF)
                        ) {
                            Text(
                                text = "${trip.placesCount} spots",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color(0xFF4C5BD4),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (trip.tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        trip.tags.take(3).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF3F4F8)
                            ) {
                                Text(
                                    text = "#$tag",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
