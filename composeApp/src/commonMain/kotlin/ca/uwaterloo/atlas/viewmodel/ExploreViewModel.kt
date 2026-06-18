package ca.uwaterloo.atlas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.domain.trip.TripModel
import ca.uwaterloo.atlas.ui.components.TripFilters
import kotlinx.coroutines.launch

class ExploreViewModel(private val model: TripModel) : ViewModel() {

    // ── UI state ──────────────────────────────────────────────────────────

    var trips by mutableStateOf<List<TripData>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var activeFilters by mutableStateOf(TripFilters())
        private set

    var savedTripIds by mutableStateOf<Set<String>>(emptySet())
        private set

    // ── Derived state ─────────────────────────────────────────────────────

    val filteredTrips: List<TripData>
        get() = model.filterTrips(trips = trips, query = searchQuery, filters = activeFilters)

    val availableTags: Set<String>
        get() = trips.flatMap { it.tags }.toSet()

    // ── Init ──────────────────────────────────────────────────────────────

    init { loadTrips() }

    // ── Data loading ──────────────────────────────────────────────────────

    fun loadTrips() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                model.loadAll()           // fetches from repository → populates cache
                trips = model.getPublicTrips()
                savedTripIds = model.getSavedTripIds()
            } catch (e: Exception) {
                errorMessage = "Failed to load trips: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // ── User actions ──────────────────────────────────────────────────────

    fun updateSearchQuery(query: String) { searchQuery = query }
    fun applyFilters(filters: TripFilters) { activeFilters = filters }
    fun clearError() { errorMessage = null }

    /**
     * Toggles saved state, persists via the repository (suspend inside model),
     * and refreshes [savedTripIds] so the bookmark icon updates immediately.
     */
    fun toggleSave(tripId: String) {
        viewModelScope.launch {
            model.toggleSave(tripId)
            savedTripIds = model.getSavedTripIds()
        }
    }
}
