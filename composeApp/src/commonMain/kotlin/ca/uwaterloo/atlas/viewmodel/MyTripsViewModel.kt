package ca.uwaterloo.atlas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.domain.trip.TripModel
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.components.TripFormData
import ca.uwaterloo.atlas.platform.getImageUploader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyTripsViewModel(
    private val model: TripModel,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

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

    val filteredTrips: List<TripData>
        get() = model.filterTrips(trips = trips, query = searchQuery, filters = activeFilters)

    val availableTags: Set<String>
        get() = trips.flatMap { it.tags }.toSet()

    init { loadTrips() }

    fun loadTrips() {
        viewModelScope.launch(dispatcher) {
            isLoading = true
            errorMessage = null
            try {
                model.loadAll()
                trips = model.getMyTrips()
                println("[MyTrips] loadTrips → ${trips.size} trips")
            } catch (e: Exception) {
                println("[MyTrips] loadTrips FAILED: ${e::class.simpleName}: ${e.message}")
                errorMessage = "Failed to load trips: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun createTrip(tripData: TripFormData) {
        viewModelScope.launch(dispatcher) {
            try {
                if (hasInvalidDateRange(tripData)) {
                    errorMessage = "End date must be on or after start date."
                    return@launch
                }
                println("[MyTrips] createTrip starting: ${tripData.name}")
                
                // Upload image if it is a local URI
                val finalImageUrl = if (tripData.imageUrl.startsWith("content://")) {
                    val uploader = getImageUploader()
                    println("[MyTrips] Local URI detected, uploading: ${tripData.imageUrl}")
                    val result = uploader.uploadImage(tripData.imageUrl, "photos")
                    println("[MyTrips] Upload result: $result")
                    result ?: tripData.imageUrl
                } else {
                    tripData.imageUrl
                }
                
                val finalTripData = tripData.copy(imageUrl = finalImageUrl)
                model.createTrip(finalTripData)
                trips = model.getMyTrips()
                println("[MyTrips] createTrip succeeded → ${trips.size} trips now")
            } catch (e: Exception) {
                println("[MyTrips] createTrip FAILED: ${e::class.simpleName}: ${e.message}")
                errorMessage = "Failed to create trip: ${e.message}"
            }
        }
    }

    fun updateTrip(trip: TripData, tripData: TripFormData) {
        viewModelScope.launch(dispatcher) {
            try {
                if (hasInvalidDateRange(tripData)) {
                    errorMessage = "End date must be on or after start date."
                    return@launch
                }
                println("[MyTrips] updateTrip starting: ${trip.id}")
                
                // Upload image if it is a local URI
                val finalImageUrl = if (tripData.imageUrl.startsWith("content://")) {
                    val uploader = getImageUploader()
                    println("[MyTrips] Local URI detected, uploading: ${tripData.imageUrl}")
                    val result = uploader.uploadImage(tripData.imageUrl, "photos")
                    println("[MyTrips] Upload result: $result")
                    result ?: tripData.imageUrl
                } else {
                    tripData.imageUrl
                }
                
                val finalTripData = tripData.copy(imageUrl = finalImageUrl)
                model.updateTrip(trip.id, finalTripData)
                trips = model.getMyTrips()
                println("[MyTrips] updateTrip succeeded")
            } catch (e: Exception) {
                println("[MyTrips] updateTrip FAILED: ${e::class.simpleName}: ${e.message}")
                errorMessage = "Failed to update trip: ${e.message}"
            }
        }
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch(dispatcher) {
            try {
                println("[MyTrips] deleteTrip starting: $tripId")
                model.deleteTrip(tripId)
                trips = model.getMyTrips()
                println("[MyTrips] deleteTrip succeeded → ${trips.size} trips now")
            } catch (e: Exception) {
                println("[MyTrips] deleteTrip FAILED: ${e::class.simpleName}: ${e.message}")
                errorMessage = "Failed to delete trip: ${e.message}"
            }
        }
    }

    fun updateSearchQuery(query: String) { searchQuery = query }
    fun applyFilters(filters: TripFilters) { activeFilters = filters }
    fun clearError() { errorMessage = null }

    private fun hasInvalidDateRange(tripData: TripFormData): Boolean {
        val start = tripData.startDate ?: return false
        val end = tripData.endDate ?: return false
        return end < start
    }
}
