package ca.uwaterloo.atlas.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.ui.components.AddPlaceDialog
import ca.uwaterloo.atlas.ui.components.PlaceFormData
import atlas.composeapp.generated.resources.Res
import atlas.composeapp.generated.resources.compose_multiplatform
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.PlaceFilter
import ca.uwaterloo.atlas.domain.place.PlaceSortType
import ca.uwaterloo.atlas.platform.PlatformImage
import ca.uwaterloo.atlas.platform.TripMap
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection

// ---------------------------------------------------------------------------
// Three-state sheet (PEEK / HALF / FULL) — see file header.
// ---------------------------------------------------------------------------
enum class SheetHeightState { PEEK, HALF, FULL }

// Atlas brand colours (mirrors Login/Signup/Profile)
private val AtlasPrimary   = Color(0xFF4C5BD4)
private val AtlasGradStart = Color(0xFF4C5BD4)
private val AtlasGradMid   = Color(0xFF8D6E95)
private val AtlasGradEnd   = Color(0xFFC79AA1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSingleTripScreen(
    trip: TripData?,
    places: List<Place>,
    isEditable: Boolean,
    onBackClick: () -> Unit,
    onAddPlace: (PlaceFormData) -> Unit,
    onPlaceClick: (Place) -> Unit = {},
    onDeletePlaces: (List<String>) -> Unit,
    onSortSelected: (PlaceSortType) -> Unit,
    onFilterChanged: (PlaceFilter) -> Unit,
    onSearchChanged: (String) -> Unit
) {
    var showAddPlaceDialog  by remember { mutableStateOf(false) }
    var selectedSort        by remember { mutableStateOf(PlaceSortType.DATE_VISITED) }
    var selectedCategories  by remember { mutableStateOf(setOf<PlaceCategory>()) }
    var favoritesOnly       by remember { mutableStateOf(false) }
    var searchText          by remember { mutableStateOf("") }
    var editMode            by remember { mutableStateOf(false) }
    val selectedPlaces      = remember { mutableStateListOf<Place>() }
    var showDeleteDialog    by remember { mutableStateOf(false) }
    var selectedPlaceForMap by remember { mutableStateOf<Place?>(null) }

    LaunchedEffect(trip?.id) { selectedPlaceForMap = null }

    val selectedPlaceIndex = selectedPlaceForMap?.let { sel ->
        places.indexOfFirst { it.id == sel.id }
    }

    var sheetHeightState by remember { mutableStateOf(SheetHeightState.HALF) }

    val scaffoldSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )
    val scope = rememberCoroutineScope()

    val goHalf: () -> Unit = {
        sheetHeightState = SheetHeightState.HALF
        scope.launch { scaffoldSheetState.bottomSheetState.show() }
    }
    val goPeek: () -> Unit = {
        sheetHeightState = SheetHeightState.PEEK
        scope.launch { scaffoldSheetState.bottomSheetState.partialExpand() }
    }
    val goFull: () -> Unit = {
        sheetHeightState = SheetHeightState.FULL
        scope.launch { scaffoldSheetState.bottomSheetState.expand() }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedPlaceIndex) {
        selectedPlaceIndex?.takeIf { it >= 0 }?.let { listState.animateScrollToItem(it) }
    }

    Scaffold(
        topBar = {
            // Gradient header matching Login / OtherProfile
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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trip?.title ?: "Loading…",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!trip?.location.isNullOrBlank()) {
                            Text(
                                text = trip!!.location,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (isEditable) {
                        IconButton(
                            onClick = { showAddPlaceDialog = true },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add place", tint = Color.White)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF7F7FB)
    ) { paddingValues ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val screenHeight = maxHeight
            val halfHeight   = maxHeight * 0.5f

            val currentPeekHeight = when (sheetHeightState) {
                SheetHeightState.PEEK -> 72.dp
                SheetHeightState.HALF -> halfHeight
                SheetHeightState.FULL -> halfHeight
            }

            BottomSheetScaffold(
                modifier = Modifier.fillMaxSize(),
                scaffoldState = scaffoldSheetState,
                sheetPeekHeight = currentPeekHeight,
                sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                sheetContainerColor = Color.White,

                sheetDragHandle = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White,
                                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            )
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp),
                            shape = RoundedCornerShape(2.dp),
                            color = Color(0xFFD1D5DB)
                        ) {}
                    }
                },

                sheetContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // ── Fixed sheet header ────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Search field
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = {
                                    searchText = it
                                    onSearchChanged(it)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search places…", color = Color(0xFF9CA3AF)) },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AtlasPrimary,
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color(0xFFF9FAFB),
                                    unfocusedContainerColor = Color(0xFFF9FAFB),
                                    cursorColor = AtlasPrimary
                                ),
                                singleLine = true
                            )

                            Spacer(Modifier.height(10.dp))

                            // Sort / filter / favourites row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SortDropdown(
                                    selectedSort = selectedSort,
                                    onSortSelected = {
                                        selectedSort = it
                                        onSortSelected(it)
                                    }
                                )
                                Spacer(Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = favoritesOnly,
                                        onCheckedChange = {
                                            favoritesOnly = it
                                            onFilterChanged(PlaceFilter(selectedCategories, favoritesOnly))
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = AtlasPrimary)
                                    )
                                    Text(
                                        "Favourites",
                                        fontSize = 13.sp,
                                        color = Color(0xFF374151)
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                CategoryDropdown(
                                    selectedCategories = selectedCategories,
                                    onCategoryToggle = { category ->
                                        selectedCategories =
                                            if (selectedCategories.contains(category))
                                                selectedCategories - category
                                            else
                                                selectedCategories + category
                                        onFilterChanged(PlaceFilter(selectedCategories, favoritesOnly))
                                    }
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            // Places header row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Places Visited",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F1F1F)
                                )
                                Surface(
                                    modifier = Modifier.padding(start = 8.dp),
                                    shape = RoundedCornerShape(50),
                                    color = AtlasPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        "${places.size}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AtlasPrimary
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                if (isEditable) {
                                    if (!editMode) {
                                        TextButton(onClick = { editMode = true }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = AtlasPrimary
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Edit", color = AtlasPrimary, fontSize = 14.sp)
                                        }
                                    } else {
                                        Row {
                                            TextButton(
                                                onClick = { showDeleteDialog = true },
                                                enabled = selectedPlaces.isNotEmpty()
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (selectedPlaces.isNotEmpty()) Color(0xFFEF4444) else Color.Gray
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "Delete (${selectedPlaces.size})",
                                                    color = if (selectedPlaces.isNotEmpty()) Color(0xFFEF4444) else Color.Gray,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            TextButton(onClick = {
                                                editMode = false
                                                selectedPlaces.clear()
                                            }) {
                                                Text("Cancel", color = Color(0xFF6B7280), fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(6.dp))
                        }

                        // ── Place list ────────────────────────────────────
                        val isExpanded =
                            scaffoldSheetState.bottomSheetState.currentValue == SheetValue.Expanded
                        val listHeight = if (isExpanded) screenHeight * 0.65f else 200.dp

                        val blockSheetScroll = remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(
                                    available: androidx.compose.ui.geometry.Offset,
                                    source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
                                ): androidx.compose.ui.geometry.Offset {
                                    val isScrollingUp = available.y > 0
                                    val listAtTop = listState.firstVisibleItemIndex == 0 &&
                                            listState.firstVisibleItemScrollOffset == 0
                                    return if (isScrollingUp && listAtTop) available
                                    else androidx.compose.ui.geometry.Offset.Zero
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(listHeight)
                                .nestedScroll(blockSheetScroll),
                            contentPadding = PaddingValues(
                                start = 16.dp, end = 16.dp, bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(places, key = { it.id }) { place ->
                                val isCardSelected =
                                    if (editMode) selectedPlaces.contains(place)
                                    else selectedPlaceForMap?.id == place.id

                                PlaceCard(
                                    place = place,
                                    editMode = editMode,
                                    isSelected = isCardSelected,
                                    onToggleSelect = {
                                        if (selectedPlaces.contains(place)) selectedPlaces.remove(place)
                                        else selectedPlaces.add(place)
                                    },
                                    onClick = {
                                        if (editMode) {
                                            if (selectedPlaces.contains(place)) selectedPlaces.remove(place)
                                            else selectedPlaces.add(place)
                                        } else {
                                            if (selectedPlaceForMap?.id == place.id) onPlaceClick(place)
                                            else { selectedPlaceForMap = place; goPeek() }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { _ ->
                TripMap(
                    places = places,
                    selectedPlace = selectedPlaceForMap,
                    modifier = Modifier.fillMaxSize(),
                    onMapTapped = { goPeek() },
                    onMarkerTapped = { place ->
                        selectedPlaceForMap = place
                        goHalf()
                    }
                )
            }
        }
    }

    if (isEditable && showAddPlaceDialog) {
        AddPlaceDialog(
            onDismiss = { showAddPlaceDialog = false },
            onSave = {
                onAddPlace(it)
                showAddPlaceDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Text(
                    "Delete Places",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F)
                )
            },
            text = {
                Text(
                    "Remove ${selectedPlaces.size} selected place${if (selectedPlaces.size == 1) "" else "s"}? This can't be undone.",
                    color = Color(0xFF6B7280)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlaces(selectedPlaces.map { it.id })
                        selectedPlaces.clear()
                        editMode = false
                        showDeleteDialog = false
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
// PlaceCard
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCard(
    place: Place,
    editMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit
) {
    val cardColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF0F3FF) else Color.White,
        animationSpec = tween(250),
        label = "cardColor"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 2.dp,
        animationSpec = tween(250),
        label = "cardElevation"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .shadow(cardElevation, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4C5BD4))
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            if (editMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4C5BD4))
                )
                Spacer(Modifier.width(8.dp))
            }

            // Thumbnail
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(110.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Color(0xFFE5E7EB)
            ) {
                val imageUri = place.thumbnailPhoto ?: place.photos.firstOrNull()
                if (imageUri != null) {
                    PlatformImage(
                        uri = imageUri,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("📍", fontSize = 28.sp)
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = place.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF0F3FF)
                    ) {
                        Text(
                            text = place.category.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4C5BD4)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        place.dateVisited?.let {
                            Text("📅 $it", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        }
                        place.rating?.let {
                            Text("⭐ %.1f".format(it), fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        }
                        place.costIndicator?.let {
                            Text(it.symbol, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                        }
                        if (place.isFavorite) Text("❤️", fontSize = 11.sp)
                    }
                }

                if (place.notes.isNotBlank()) {
                    Text(
                        text = place.notes,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SortDropdown
// ---------------------------------------------------------------------------
@Composable
fun SortDropdown(
    selectedSort: PlaceSortType,
    onSortSelected: (PlaceSortType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = "Sort: ${selectedSort.name.replace("_", " ")}",
                color = Color(0xFF4C5BD4),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            PlaceSortType.values().forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(
                            sort.name.replace("_", " "),
                            color = if (sort == selectedSort) Color(0xFF4C5BD4) else Color(0xFF374151),
                            fontWeight = if (sort == selectedSort) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = { onSortSelected(sort); expanded = false }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CategoryDropdown
// ---------------------------------------------------------------------------
@Composable
fun CategoryDropdown(
    selectedCategories: Set<PlaceCategory>,
    onCategoryToggle: (PlaceCategory) -> Unit,
    onClearFilters: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val hasFilters = selectedCategories.isNotEmpty()
    Box {
        TextButton(onClick = { expanded = true }, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = if (hasFilters) "Filter (${selectedCategories.size})" else "Filter",
                color = if (hasFilters) Color(0xFF4C5BD4) else Color(0xFF4C5BD4),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            PlaceCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedCategories.contains(category),
                                onCheckedChange = { onCategoryToggle(category) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4C5BD4))
                            )
                            Text(category.displayName, color = Color(0xFF374151))
                        }
                    },
                    onClick = { onCategoryToggle(category) }
                )
            }
            HorizontalDivider(color = Color(0xFFE5E7EB))
            DropdownMenuItem(
                text = {
                    Text(
                        "Clear Filters",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                onClick = { onClearFilters(); expanded = false }
            )
        }
    }
}
