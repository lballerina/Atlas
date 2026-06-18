package ca.uwaterloo.atlas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.atlas.domain.place.CostLevel
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.PlaceFilter
import ca.uwaterloo.atlas.domain.place.PlaceModel
import ca.uwaterloo.atlas.domain.place.PlaceSortType
import ca.uwaterloo.atlas.domain.place.TimeOfDay
import ca.uwaterloo.atlas.domain.trip.TripAccessMode
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.domain.trip.TripModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class SingleTripViewModel(
    private val placeModel: PlaceModel,
    private val tripModel: TripModel,
    private val accessMode: TripAccessMode
) : ViewModel() {

    var currentTrip by mutableStateOf<TripData?>(null)
        private set

    var places by mutableStateOf<List<Place>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    val isEditable: Boolean
        get() = accessMode == TripAccessMode.EDIT

    private val currentTripId = MutableStateFlow<String?>(null)
    private val sortType      = MutableStateFlow(PlaceSortType.DATE_VISITED)
    private val filter        = MutableStateFlow(PlaceFilter())
    private val searchQuery   = MutableStateFlow("")

    init {
        observePlaces()
    }

    // ── Reactive observer — unchanged from Sprint 2 ───────────────────────

    private fun observePlaces() {
        viewModelScope.launch {
            combine(
                placeModel.allPlaces,
                currentTripId.filterNotNull(),
                sortType,
                filter,
                searchQuery
            ) { allPlaces, tripId, sort, filter, query ->

                var result = allPlaces.filter { it.tripId == tripId }

                if (query.isNotBlank()) {
                    result = result.filter { it.name.contains(query, ignoreCase = true) }
                }

                if (filter.selectedCategories.isNotEmpty()) {
                    result = result.filter { it.category in filter.selectedCategories }
                }

                if (filter.favoritesOnly) {
                    result = result.filter { it.isFavorite }
                }

                result = when (sort) {
                    PlaceSortType.DATE_VISITED  -> result.sortedByDescending { it.dateVisited }
                    PlaceSortType.RATING        -> result.sortedByDescending { it.rating ?: 0f }
                    PlaceSortType.ALPHABETICAL  -> result.sortedBy { it.name.lowercase() }
                    PlaceSortType.FAVORITE      -> result.sortedByDescending { it.isFavorite }
                    PlaceSortType.CATEGORY      -> result.sortedBy { it.category.displayName }
                }

                result
            }.collect { sortedFilteredList ->
                places = sortedFilteredList
            }
        }
    }

    // ── Load trip + places from repository ───────────────────────────────

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Ensure the trip cache is populated before trying to find the trip
                tripModel.loadAll()

                // Load places from repository into the StateFlow so the
                // reactive observer above fires immediately
                placeModel.loadPlacesForTrip(tripId)

                currentTrip = tripModel.getMyTrips().find { it.id == tripId }
                    ?: tripModel.getPublicTrips().find { it.id == tripId }

                currentTripId.value = tripId
            } finally {
                isLoading = false
            }
        }
    }

    // ── Sorting & filtering controls ──────────────────────────────────────

    fun updateSort(type: PlaceSortType)      { sortType.value = type }
    fun updateFilter(newFilter: PlaceFilter) { filter.value = newFilter }
    fun updateSearchQuery(query: String)     { searchQuery.value = query }

    // ── Place mutations ───────────────────────────────────────────────────

    fun addPlace(
        name: String,
        category: PlaceCategory,
        latitude: Double,
        longitude: Double,
        address: String,
        photos: List<String>,
        thumbnailPhoto: String?,
        notes: String,
        dateVisited: LocalDate?          = null,
        rating: Float?                   = null,
        mood: String?                    = null,
        tags: List<String>               = emptyList(),
        costIndicator: CostLevel?        = null,
        costAmount: Double?              = null,
        timeOfDay: TimeOfDay?            = null,
        photoCaptions: Map<String, String> = emptyMap(),
        isFavorite: Boolean              = false
    ) {
        if (!isEditable) return
        val tripId = currentTripId.value ?: return

        val newPlace = Place(
            id             = System.currentTimeMillis().toString(),
            tripId         = tripId,
            name           = name,
            category       = category,
            latitude       = latitude,
            longitude      = longitude,
            address        = address,
            photos         = photos,
            thumbnailPhoto = thumbnailPhoto,
            notes          = notes,
            dateVisited    = dateVisited,
            rating         = rating,
            mood           = mood,
            tags           = tags,
            costIndicator  = costIndicator,
            costAmount     = costAmount,
            timeOfDay      = timeOfDay,
            photoCaptions  = photoCaptions,
            isFavorite     = isFavorite
        )

        // suspend call — wrapped in coroutine
        viewModelScope.launch { placeModel.addPlace(newPlace) }
    }

    fun updatePlace(updatedPlace: Place) {
        if (!isEditable) return
        viewModelScope.launch { placeModel.updatePlace(updatedPlace) }
    }

    fun deletePlace(placeId: String) {
        if (!isEditable) return
        viewModelScope.launch { placeModel.deletePlace(placeId) }
    }
}
