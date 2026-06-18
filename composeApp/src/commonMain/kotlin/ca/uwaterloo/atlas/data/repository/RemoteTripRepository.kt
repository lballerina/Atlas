package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.SupabaseClient
import ca.uwaterloo.atlas.data.dto.TripDto
import ca.uwaterloo.atlas.data.dto.toInsertDto
import ca.uwaterloo.atlas.data.dto.toUpdateDto
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.ui.components.TripFormData
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class RemoteTripRepository(
    private val currentUserId: String
) : TripRepository {

    private val db get() = SupabaseClient.client.postgrest

    override suspend fun getMyTrips(userId: String): List<TripData> {
        val result = db["trips"]
            .select { filter { eq("author_id", userId) } }
            .decodeList<TripDto>()
            .map { it.toDomain() }
        println("[Trips] getMyTrips userId=$userId → ${result.size} trips")
        return result
    }

    override suspend fun getPublicTrips(): List<TripData> {
        val result = db["trips"]
            .select(Columns.raw("*, users!author_id(display_name, avatar_url)")) {
                filter {
                    eq("is_public", true)
                    neq("author_id", currentUserId)
                }
            }
            .decodeList<TripDto>()
            .map { it.toDomain() }
        println("[Trips] getPublicTrips → ${result.size} trips")
        return result
    }

    override suspend fun createTrip(userId: String, tripData: TripFormData): TripData {
        db["trips"].insert(tripData.toInsertDto(userId))

        // Fetch the newly created row back
        val result = db["trips"]
            .select {
                filter {
                    eq("author_id", userId)
                    eq("title", tripData.name)
                }
                limit(1)
                order("created_at", Order.DESCENDING)
            }
            .decodeSingle<TripDto>()
            .toDomain()
        println("[Trips] createTrip → ${result.id}")
        return result
    }

    override suspend fun updateTrip(id: String, tripData: TripFormData): TripData {
        db["trips"].update(tripData.toUpdateDto()) {
            filter { eq("id", id) }
        }

        val result = db["trips"]
            .select { filter { eq("id", id) } }
            .decodeSingle<TripDto>()
            .toDomain()
        println("[Trips] updateTrip $id → ${result.title}")
        return result
    }

    override suspend fun deleteTrip(id: String) {
        db["trips"].delete { filter { eq("id", id) } }
        println("[Trips] deleteTrip $id")
    }

    override suspend fun getSavedTripIds(userId: String): Set<String> =
        db["saved_trips"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<Map<String, String>>()
            .mapNotNull { it["trip_id"] }
            .toSet()

    override suspend fun saveTripForUser(userId: String, tripId: String) {
        db["saved_trips"].insert(SavedTripInsertDto(userId, tripId))
    }

    override suspend fun unsaveTripForUser(userId: String, tripId: String) {
        db["saved_trips"].delete {
            filter {
                eq("user_id", userId)
                eq("trip_id", tripId)
            }
        }
    }
}

@kotlinx.serialization.Serializable
private data class SavedTripInsertDto(
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("trip_id") val tripId: String
)
