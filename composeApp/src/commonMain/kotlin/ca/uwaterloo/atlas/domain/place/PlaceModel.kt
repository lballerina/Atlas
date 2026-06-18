package ca.uwaterloo.atlas.domain.place

import ca.uwaterloo.atlas.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Domain Model for places.
 *
 * Owns all business logic that operates on [Place]:
 *  - Delegating persistence to [PlaceRepository] (Mock or Remote)
 *  - Exposing a [StateFlow] so [SingleTripViewModel] can reactively combine
 *    place changes with sort/filter state via [kotlinx.coroutines.flow.combine]
 *
 * To switch backends, change the [repository] passed in from App.kt —
 * nothing else in this class or above it needs to change.
 */
class PlaceModel(
    private val repository: PlaceRepository
) {
    // StateFlow keeps the reactive combine() in SingleTripViewModel working
    // exactly as before — emitting a new list whenever anything mutates.
    private val _allPlaces = MutableStateFlow<List<Place>>(emptyList())

    val allPlaces: StateFlow<List<Place>> = _allPlaces.asStateFlow()

    // ── Bootstrap ─────────────────────────────────────────────────────────

    /**
     * Loads all places for a given trip from the repository.
     * Called from SingleTripViewModel when a trip is opened.
     */
    suspend fun loadPlacesForTrip(tripId: String) {
        val loaded = repository.getPlacesForTrip(tripId)
        // Merge into the flow: keep all other trips' places, replace this trip's
        _allPlaces.value = _allPlaces.value.filterNot { it.tripId == tripId } + loaded
    }

    // ── Queries ───────────────────────────────────────────────────────────

    suspend fun getPlaceById(id: String): Place? {
        val cached = _allPlaces.value.find { it.id == id }
        if (cached != null) return cached

        val fetched = repository.getPlaceById(id)
        if (fetched != null) {
            _allPlaces.value = _allPlaces.value + fetched
        }
        return fetched
    }

    fun getPlacesForTrip(tripId: String): List<Place> =
        _allPlaces.value.filter { it.tripId == tripId }

    // ── Mutations ─────────────────────────────────────────────────────────

    suspend fun addPlace(place: Place) {
        val saved = repository.addPlace(place)
        _allPlaces.value = _allPlaces.value + saved
    }

    suspend fun updatePlace(updatedPlace: Place) {
        val saved = repository.updatePlace(updatedPlace)
        _allPlaces.value = _allPlaces.value.map {
            if (it.id == saved.id) saved else it
        }
    }

    suspend fun deletePlace(id: String) {
        repository.deletePlace(id)
        _allPlaces.value = _allPlaces.value.filterNot { it.id == id }
    }
}
