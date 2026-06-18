package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.ui.components.TripFormData

/**
 * DB Interface for trip persistence.
 */
interface TripRepository {
    suspend fun getMyTrips(userId: String): List<TripData>
    suspend fun getPublicTrips(): List<TripData>
    suspend fun createTrip(userId: String, tripData: TripFormData): TripData
    suspend fun updateTrip(id: String, tripData: TripFormData): TripData
    suspend fun deleteTrip(id: String)
    suspend fun getSavedTripIds(userId: String): Set<String>
    suspend fun saveTripForUser(userId: String, tripId: String)
    suspend fun unsaveTripForUser(userId: String, tripId: String)
}
