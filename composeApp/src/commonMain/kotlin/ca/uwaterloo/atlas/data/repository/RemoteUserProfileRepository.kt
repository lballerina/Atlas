package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.SupabaseClient
import ca.uwaterloo.atlas.data.dto.TripDto
import ca.uwaterloo.atlas.data.dto.UserDto
import ca.uwaterloo.atlas.data.dto.toUpdateDto
import ca.uwaterloo.atlas.domain.profile.TravelStats
import ca.uwaterloo.atlas.domain.profile.UserProfile
import ca.uwaterloo.atlas.domain.trip.TripData
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

@Serializable
private data class SavedTripRow(val trips: TripDto? = null)

@Serializable
private data class PlaceIdOnly(val id: String)

class RemoteUserProfileRepository : UserProfileRepository {

    private val db get() = SupabaseClient.client.postgrest

    override suspend fun getProfile(userId: String): UserProfile {
        // 1. Fetch the user row (for bio, tags, etc.)
        val userDto = db["users"]
            .select { filter { eq("id", userId) } }
            .decodeSingle<UserDto>()

        // 2. Fetch all trips by this user to calculate dynamic stats
        val myTrips = db["trips"]
            .select { filter { eq("author_id", userId) } }
            .decodeList<TripDto>()

        val tripsCount = myTrips.size
        
        // Countries: unique last-part of the location string (e.g. "Paris, France" -> "France")
        val countriesCount = myTrips.map { it.location.split(",").last().trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .size

        // 3. Fetch all places for all of these trips to get a real count
        val tripIds = myTrips.map { it.id }
        val placesCount = if (tripIds.isNotEmpty()) {
            db["places"]
                .select(Columns.raw("id")) {
                    filter { isIn("trip_id", tripIds) }
                }
                .decodeList<PlaceIdOnly>()
                .size
                } else 0

        val result = userDto.toDomain().copy(
            stats = TravelStats(
                trips = tripsCount,
                places = placesCount,
                countries = countriesCount
            )
        )
        
        println("[Profile] getProfile userId=$userId → dynamic stats: T=$tripsCount, P=$placesCount, C=$countriesCount")
        return result
    }

    override suspend fun updateProfile(userId: String, profile: UserProfile) {
        db["users"]
            .update(profile.toUpdateDto()) { filter { eq("id", userId) } }
        println("[Profile] updateProfile userId=$userId → ${profile.displayName}")
    }

    override suspend fun getMyPublicTrips(userId: String): List<TripData> {
        val result = db["trips"]
            .select {
                filter {
                    eq("author_id", userId)
                    eq("is_public", true)
                }
            }
            .decodeList<TripDto>()
            .map { it.toDomain() }
        println("[Profile] getMyPublicTrips userId=$userId → ${result.size} trips")
        return result
    }

    override suspend fun getSavedTrips(userId: String): List<TripData> {
        val result = db["saved_trips"]
            .select(Columns.raw("trips(*)")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<SavedTripRow>()
            .mapNotNull { it.trips?.toDomain() }
        println("[Profile] getSavedTrips userId=$userId → ${result.size} trips")
        return result
    }
}
