package ca.uwaterloo.atlas.domain.trip

import ca.uwaterloo.atlas.data.repository.TripRepository
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.components.TripFormData

/**
 * Domain Model for trips.
 *
 * Owns all business logic that operates on [TripData]:
 *  - Delegating persistence to [TripRepository] (Mock or Remote)
 *  - Caching the last-loaded snapshot for synchronous filtering
 *  - Filtering by search query, visibility, tags, and date range
 *  - CRUD mutations (create, update, delete)
 *  - Save / unsave trips authored by other users
 *
 * ViewModels hold an instance of this and delegate all data work to it.
 * They are responsible only for exposing state to the UI.
 */
class TripModel(
    private val repository: TripRepository,
    private val currentUserId: String = "currentUser"
) {
    // ── In-memory cache ───────────────────────────────────────────────────
    // Populated by [loadAll]. Used for synchronous filtering so the UI
    // doesn't need to suspend just to re-filter an already-loaded list.

    private var _cachedMyTrips: List<TripData>     = emptyList()
    private var _cachedPublicTrips: List<TripData>  = emptyList()
    private var _cachedSavedIds: Set<String>        = emptySet()

    // ── Bootstrap ─────────────────────────────────────────────────────────

    /**
     * Fetches all data from the repository and populates the local cache.
     * Called once from each ViewModel's init block (via [loadTrips]).
     */
    suspend fun loadAll() {
        _cachedMyTrips     = repository.getMyTrips(currentUserId)
        _cachedPublicTrips = repository.getPublicTrips()
        _cachedSavedIds    = repository.getSavedTripIds(currentUserId)
    }

    // ── Queries (synchronous — read from cache) ────────────────────────────

    fun getMyTrips(): List<TripData>      = _cachedMyTrips
    fun getMyPublicTrips(): List<TripData> = _cachedMyTrips.filter { it.isPublic }
    fun getPublicTrips(): List<TripData>  = _cachedPublicTrips

    fun getPublicTripsByAuthor(authorName: String): List<TripData> =
        _cachedPublicTrips.filter { it.author == authorName }

    fun getMyTripTags(): Set<String>     = _cachedMyTrips.flatMap { it.tags }.toSet()
    fun getPublicTripTags(): Set<String> = _cachedPublicTrips.flatMap { it.tags }.toSet()

    // ── Saved trips ───────────────────────────────────────────────────────

    fun getSavedTripIds(): Set<String> = _cachedSavedIds

    fun getSavedTrips(): List<TripData> =
        (_cachedMyTrips + _cachedPublicTrips).filter { it.id in _cachedSavedIds }

    fun isSaved(id: String): Boolean = id in _cachedSavedIds

    /**
     * Toggles the saved state of a trip, updates the repository, and reflects
     * the change in the local cache immediately (optimistic update).
     * Returns true if the trip is now saved, false if it was unsaved.
     */
    suspend fun toggleSave(id: String): Boolean {
        return if (id in _cachedSavedIds) {
            repository.unsaveTripForUser(currentUserId, id)
            _cachedSavedIds = _cachedSavedIds - id
            false
        } else {
            repository.saveTripForUser(currentUserId, id)
            _cachedSavedIds = _cachedSavedIds + id
            true
        }
    }

    // ── Filtering (pure logic — no I/O) ───────────────────────────────────

    fun filterTrips(
        trips: List<TripData>,
        query: String,
        filters: TripFilters
    ): List<TripData> {
        var result = trips

        if (query.isNotBlank()) {
            result = result.filter { trip ->
                trip.title.contains(query, ignoreCase = true) ||
                        trip.location.contains(query, ignoreCase = true) ||
                        trip.author?.contains(query, ignoreCase = true) == true ||
                        trip.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

        result = result.filter { trip ->
            (trip.isPublic && filters.showPublic) ||
                    (!trip.isPublic && filters.showPrivate)
        }

        if (filters.selectedTags.isNotEmpty()) {
            result = result.filter { trip ->
                filters.selectedTags.any { filterTag ->
                    trip.tags.any { it.equals(filterTag, ignoreCase = true) }
                }
            }
        }

        filters.startDate?.let { filterStart ->
            result = result.filter { trip ->
                trip.endDate == null || trip.endDate >= filterStart
            }
        }
        filters.endDate?.let { filterEnd ->
            result = result.filter { trip ->
                trip.startDate == null || trip.startDate <= filterEnd
            }
        }

        return result
    }

    // ── Mutations (suspend — hit repository then refresh cache) ───────────

    suspend fun createTrip(tripData: TripFormData): TripData {
        val created = repository.createTrip(currentUserId, tripData)
        // Append to cache so ViewModels see it instantly on next getMyTrips()
        _cachedMyTrips = _cachedMyTrips + created
        return created
    }

    suspend fun updateTrip(id: String, tripData: TripFormData): TripData {
        val updated = repository.updateTrip(id, tripData)
        _cachedMyTrips = _cachedMyTrips.map { if (it.id == id) updated else it }
        return updated
    }

    suspend fun deleteTrip(id: String) {
        repository.deleteTrip(id)
        _cachedMyTrips = _cachedMyTrips.filterNot { it.id == id }
    }
}
