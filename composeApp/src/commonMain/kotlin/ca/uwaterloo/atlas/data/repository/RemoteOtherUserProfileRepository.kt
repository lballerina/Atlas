package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.SupabaseClient
import ca.uwaterloo.atlas.data.dto.TripDto
import ca.uwaterloo.atlas.data.dto.UserDto
import ca.uwaterloo.atlas.domain.profile.TravelStats
import ca.uwaterloo.atlas.domain.profile.UserProfile
import ca.uwaterloo.atlas.domain.trip.TripData
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

@Serializable
private data class OtherPlaceIdOnly(val id: String)

/**
 * Live Supabase implementation of [OtherUserProfileRepository].
 *
 * Looks up other users by display_name for now.
 * When full auth is wired in, switch the lookup key to UUID for reliability.
 *
 * To activate: swap [MockOtherUserProfileRepository] for this in App.kt.
 */
class RemoteOtherUserProfileRepository : OtherUserProfileRepository {

    private val db get() = SupabaseClient.client.postgrest

    override suspend fun getProfile(authorName: String): UserProfile? {
        val userDto = db["users"]
            .select { filter { eq("display_name", authorName) } }
            .decodeSingleOrNull<UserDto>() ?: return null

        // Calculate dynamic stats for this traveler
        // Note: For other users, we typically show stats based on their PUBLIC activity
        val publicTrips = db["trips"]
            .select {
                filter {
                    eq("author_id", userDto.id)
                    eq("is_public", true)
                }
            }
            .decodeList<TripDto>()

        val tripsCount = publicTrips.size
        
        val countriesCount = publicTrips.map { it.location.split(",").last().trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .size

        val tripIds = publicTrips.map { it.id }
        val placesCount = if (tripIds.isNotEmpty()) {
            db["places"]
                .select(Columns.raw("id")) {
                    filter { isIn("trip_id", tripIds) }
                }
                .decodeList<OtherPlaceIdOnly>()
                .size
        } else 0

        return userDto.toDomain().copy(
            stats = TravelStats(
                trips = tripsCount,
                places = placesCount,
                countries = countriesCount
            )
        )
    }

    override suspend fun getPublicTrips(authorName: String): List<TripData> {
        // Join users → trips via author_id to resolve by display_name
        val userRow = db["users"]
            .select { filter { eq("display_name", authorName) } }
            .decodeSingleOrNull<UserDto>() ?: return emptyList()

        return db["trips"]
            .select {
                filter {
                    eq("author_id", userRow.id)
                    eq("is_public", true)
                }
            }
            .decodeList<TripDto>()
            .map { it.toDomain() }
    }
}
