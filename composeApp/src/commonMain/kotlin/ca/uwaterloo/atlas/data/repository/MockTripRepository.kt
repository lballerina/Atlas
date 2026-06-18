package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.data.MockDB
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.ui.components.TripFormData

/**
 * Mock (in-memory) implementation of [TripRepository].
 *
 * [currentUserId] is used to seed the correct saved-trip IDs for the
 * logged-in user. Andrew's account gets MockDB.savedTripIds; every other
 * account starts with an empty saved set.
 *
 * Saved IDs are copied into a fresh MutableSet — never a reference to
 * MockDB.savedTripIds — so different user instances don't share state.
 */
class MockTripRepository(
    private val currentUserId: String = "",
    initialTrips: List<TripData> = MockDB.trips
) : TripRepository {

    private val trips = initialTrips.toMutableList()

    // Only Andrew's account gets pre-seeded saved trips.
    // Every other userId starts with an empty set.
    private val savedIds: MutableSet<String> = if (currentUserId == MockDB.currentUserProfile.email) {
        MockDB.savedTripIds.toMutableSet()  // copy, not reference
    } else {
        mutableSetOf()
    }

    override suspend fun getMyTrips(userId: String): List<TripData> =
        trips.filter { it.author == null }

    override suspend fun getPublicTrips(): List<TripData> =
        trips.filter { it.isPublic && it.author != null }

    override suspend fun createTrip(userId: String, tripData: TripFormData): TripData {
        val newTrip = TripData(
            id          = "mock_trip_${System.currentTimeMillis()}",
            title       = tripData.name,
            location    = tripData.destination,
            imageUrl    = tripData.imageUrl,
            drawableRes = null,
            placesCount = 0,
            startDate   = tripData.startDate,
            endDate     = tripData.endDate,
            isPublic    = tripData.isPublic,
            tags        = tripData.tags,
            author      = null
        )
        trips.add(newTrip)
        return newTrip
    }

    override suspend fun updateTrip(id: String, tripData: TripFormData): TripData {
        val index = trips.indexOfFirst { it.id == id }
        require(index != -1) { "Trip $id not found" }
        val updated = trips[index].copy(
            title     = tripData.name,
            location  = tripData.destination,
            imageUrl  = tripData.imageUrl,
            startDate = tripData.startDate,
            endDate   = tripData.endDate,
            isPublic  = tripData.isPublic,
            tags      = tripData.tags
        )
        trips[index] = updated
        return updated
    }

    override suspend fun deleteTrip(id: String) {
        trips.removeAll { it.id == id }
    }

    override suspend fun getSavedTripIds(userId: String): Set<String> =
        savedIds.toSet()

    override suspend fun saveTripForUser(userId: String, tripId: String) {
        savedIds.add(tripId)
    }

    override suspend fun unsaveTripForUser(userId: String, tripId: String) {
        savedIds.remove(tripId)
    }
}
