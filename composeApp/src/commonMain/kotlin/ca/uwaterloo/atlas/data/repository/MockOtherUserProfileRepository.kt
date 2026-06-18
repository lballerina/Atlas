package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.data.MockDB
import ca.uwaterloo.atlas.domain.profile.UserProfile
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.domain.trip.TripModel

/**
 * Mock (in-memory) implementation of [OtherUserProfileRepository].
 *
 * Looks up profiles from [MockDB.otherUsers] and delegates trip queries
 * to the shared [TripModel] cache.
 *
 * Only use in unit tests — swap to [RemoteOtherUserProfileRepository] in App.kt.
 */
class MockOtherUserProfileRepository(
    private val tripModel: TripModel
) : OtherUserProfileRepository {

    override suspend fun getProfile(authorName: String): UserProfile? {
        val baseProfile = MockDB.otherUsers[authorName] ?: return null
        val publicTrips = tripModel.getPublicTripsByAuthor(authorName)
        val tripIds = publicTrips.map { it.id }.toSet()
        
        // Count actual places from MockDB.places that belong to this traveler's public trips
        val actualPlacesCount = MockDB.places.count { it.tripId in tripIds }
        
        val totalCountries = publicTrips.map { it.location.split(",").last().trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .size

        return baseProfile.copy(
            stats = ca.uwaterloo.atlas.domain.profile.TravelStats(
                trips = publicTrips.size,
                places = actualPlacesCount,
                countries = totalCountries
            )
        )
    }

    override suspend fun getPublicTrips(authorName: String): List<TripData> =
        tripModel.getPublicTripsByAuthor(authorName)
}
