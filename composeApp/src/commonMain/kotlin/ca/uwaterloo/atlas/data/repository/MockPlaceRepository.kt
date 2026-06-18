package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.data.MockDB
import ca.uwaterloo.atlas.domain.place.Place

/**
 * Mock (in-memory) implementation of [PlaceRepository].
 *
 * Used by unit tests and as the default data source while Supabase is not
 * yet wired in. Seeded from [MockDB] by default so existing test data
 * continues to appear in the app unchanged.
 *
 * Only use in unit tests — swap to [RemotePlaceRepository] in App.kt.
 */
class MockPlaceRepository(
    initialPlaces: List<Place> = MockDB.places
) : PlaceRepository {

    // Mutable in-memory store — changes are visible within the same session
    private val places = initialPlaces.toMutableList()

    override suspend fun getPlacesForTrip(tripId: String): List<Place> =
        places.filter { it.tripId == tripId }

    override suspend fun getPlaceById(id: String): Place? =
        places.find { it.id == id }

    override suspend fun addPlace(place: Place): Place {
        places.add(place)
        return place
    }

    override suspend fun updatePlace(place: Place): Place {
        val index = places.indexOfFirst { it.id == place.id }
        require(index != -1) { "Place ${place.id} not found" }
        places[index] = place
        return place
    }

    override suspend fun deletePlace(id: String) {
        places.removeAll { it.id == id }
    }
}
