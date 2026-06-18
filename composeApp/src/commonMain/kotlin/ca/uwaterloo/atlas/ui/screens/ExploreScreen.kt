package ca.uwaterloo.atlas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.platform.PlatformImage
import ca.uwaterloo.atlas.ui.components.TripCoverImage
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.components.TripFilterSheet
import ca.uwaterloo.atlas.ui.components.toDisplayString

/**
 * Explore Screen – view other users' public trips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    trips: List<TripData>,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    activeFilters: TripFilters = TripFilters(),
    availableTags: Set<String> = emptySet(),
    onApplyFilters: (TripFilters) -> Unit = {},
    onTripClick: (TripData) -> Unit = {},
    onAuthorClick: (String) -> Unit = {},
    savedTripIds: Set<String> = emptySet(),
    onToggleSave: (String) -> Unit = {}
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF7F7FB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7FB))
                .padding(bottom = paddingValues.calculateBottomPadding())
                .navigationBarsPadding()
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
            ) {                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.Start
                ) {
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
                                text = "EXPLORE",
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
                        text = "Discover Journeys",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    
                    Text(
                        text = "Find your next adventure from the community",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
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
                            placeholder = { Text("Search destinations...", color = Color(0xFF9CA3AF), fontSize = 15.sp) },
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

            // Trip list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (trips.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🗺️", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
                            Text(text = "No adventures found", color = Color(0xFF1F1F1F), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Try adjusting your search or filters", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
                items(trips) { trip ->
                    ExploreTripCard(
                        trip = trip,
                        isSaved = trip.id in savedTripIds,
                        onClick = { onTripClick(trip) },
                        onAuthorClick = onAuthorClick,
                        onToggleSave = { onToggleSave(trip.id) }
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        TripFilterSheet(
            filters = activeFilters,
            availableTags = availableTags,
            showVisibility = false,
            onApply = { newFilters ->
                onApplyFilters(newFilters)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreTripCard(
    trip: TripData,
    isSaved: Boolean = false,
    onClick: () -> Unit = {},
    onAuthorClick: (String) -> Unit = {},
    onToggleSave: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // --- Top Visual Section ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                TripCoverImage(
                    trip = trip,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                // Top Actions: Save
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(44.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    IconButton(onClick = onToggleSave) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isSaved) "Unsave" else "Save",
                            tint = if (isSaved) Color(0xFF4C5BD4) else Color(0xFF1F1F1F),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Bottom Info: Location
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(trip.location, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- Details Section ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title & Date Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (trip.startDate != null && trip.endDate != null) {
                        Text(
                            text = "${trip.startDate.toDisplayString()} • ${trip.endDate.toDisplayString()}".uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4C5BD4),
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = trip.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        lineHeight = 28.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Author Row
                if (trip.author != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onAuthorClick(trip.author) }
                            .semantics { contentDescription = "Author ${trip.author}" }
                            .background(Color(0xFFF1F4FF))
                            .padding(8.dp)
                    ) {
                        if (trip.authorAvatarUrl != null) {
                            PlatformImage(
                                uri = trip.authorAvatarUrl,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF4C5BD4), Color(0xFF8D6E95))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = trip.author.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = trip.author,
                            fontSize = 15.sp,
                            color = Color(0xFF1F1F1F),
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.weight(1f))
                    }
                }

                // Tags Section
                if (trip.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        trip.tags.take(3).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F4FF)
                            ) {
                                Text(
                                    text = "#$tag",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4C5BD4)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
