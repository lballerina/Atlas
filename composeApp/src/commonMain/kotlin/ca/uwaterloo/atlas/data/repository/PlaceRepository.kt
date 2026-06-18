package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.domain.place.Place

/**
 * DB Interface for place persistence.
 */
interface PlaceRepository {
    suspend fun getPlacesForTrip(tripId: String): List<Place>
    suspend fun getPlaceById(id: String): Place?
    suspend fun addPlace(place: Place): Place
    suspend fun updatePlace(place: Place): Place
    suspend fun deletePlace(id: String)
}
