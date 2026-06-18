package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.SupabaseClient
import ca.uwaterloo.atlas.data.dto.PlaceDto
import ca.uwaterloo.atlas.data.dto.toInsertDto
import ca.uwaterloo.atlas.domain.place.Place
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class RemotePlaceRepository : PlaceRepository {

    private val db get() = SupabaseClient.client.postgrest

    override suspend fun getPlacesForTrip(tripId: String): List<Place> {
        val result = db["places"]
            .select { filter { eq("trip_id", tripId) } }
            .decodeList<PlaceDto>()
            .map { it.toDomain() }
        println("[Places] getPlacesForTrip tripId=$tripId → ${result.size} places")
        return result
    }

    override suspend fun getPlaceById(id: String): Place? =
        db["places"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<PlaceDto>()
            ?.toDomain()

    override suspend fun addPlace(place: Place): Place {
        db["places"].insert(place.toInsertDto())

        val result = db["places"]
            .select {
                filter {
                    eq("trip_id", place.tripId)
                    eq("name", place.name)
                }
                limit(1)
                order("created_at", Order.DESCENDING)
            }
            .decodeSingle<PlaceDto>()
            .toDomain()
        println("[Places] addPlace → ${result.id} (${result.name})")
        return result
    }

    override suspend fun updatePlace(place: Place): Place {
        db["places"].update(place.toInsertDto()) {
            filter { eq("id", place.id) }
        }

        val result = db["places"]
            .select { filter { eq("id", place.id) } }
            .decodeSingle<PlaceDto>()
            .toDomain()
        println("[Places] updatePlace ${place.id} → ${result.name}")
        return result
    }

    override suspend fun deletePlace(id: String) {
        db["places"].delete { filter { eq("id", id) } }
        println("[Places] deletePlace $id")
    }
}
