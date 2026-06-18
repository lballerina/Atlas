package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.data.MockDB
import ca.uwaterloo.atlas.domain.profile.AgeRange
import ca.uwaterloo.atlas.domain.profile.Gender
import ca.uwaterloo.atlas.domain.profile.TravelStats
import ca.uwaterloo.atlas.domain.profile.UserProfile
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.domain.trip.TripModel

/**
 * Mock (in-memory) implementation of [UserProfileRepository].
 *
 * Maintains a per-userId profile map so that:
 *  - The mock user (andrew.anderson@email.com) sees Andrew's profile from MockDB.
 *  - Any new user who signs up gets a fresh default profile, not Andrew's.
 *
 * Only use in unit tests — swap to [RemoteUserProfileRepository] in App.kt.
 */
class MockUserProfileRepository(
    private val tripModel: TripModel
) : UserProfileRepository {

    // Keyed by userId (email string in mock mode).
    // Pre-seeded with the mock user's profile from MockDB.
    private val profiles: MutableMap<String, UserProfile> = mutableMapOf(
        MockDB.currentUserProfile.email to MockDB.currentUserProfile
    )

    override suspend fun getProfile(userId: String): UserProfile {
        // Return existing profile, or create a default one for new users
        val baseProfile = profiles.getOrPut(userId) {
            UserProfile(
                displayName = userId.substringBefore("@").ifBlank { "New User" },
                avatarUrl   = "",
                email       = userId,
                bio         = "",
                gender      = Gender.PREFER_NOT_TO_SAY,
                ageRange    = AgeRange.LATE_20S,
                tags        = emptySet(),
                stats       = TravelStats(trips = 0, places = 0, countries = 0)
            )
        }

        // Only calculate dynamic stats for the primary mock user
        if (userId == MockDB.currentUserProfile.email) {
            val myTrips = tripModel.getMyTrips()
            val myTripIds = myTrips.map { it.id }.toSet()
            
            // Count actual places from MockDB.places that belong to this user's trips
            val actualPlacesCount = MockDB.places.count { it.tripId in myTripIds }
            
            val totalCountries = myTrips.map { it.location.split(",").last().trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .size

            return baseProfile.copy(
                stats = TravelStats(
                    trips = myTrips.size,
                    places = actualPlacesCount,
                    countries = totalCountries
                )
            )
        }

        return baseProfile
    }

    override suspend fun updateProfile(userId: String, profile: UserProfile) {
        profiles[userId] = profile
        // Keep MockDB in sync for the primary mock user so other MockDB
        // consumers (e.g. MockOtherUserProfileRepository) stay consistent
        if (userId == MockDB.currentUserProfile.email) {
            MockDB.currentUserProfile = profile
        }
    }

    override suspend fun getMyPublicTrips(userId: String): List<TripData> =
        tripModel.getMyPublicTrips()

    override suspend fun getSavedTrips(userId: String): List<TripData> =
        tripModel.getSavedTrips()
}
