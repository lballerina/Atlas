package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.domain.profile.UserProfile
import ca.uwaterloo.atlas.domain.trip.TripData

/**
 * DB Interface for current-user profile persistence.
 *
 * Intentionally separate from [UserCredentialRepository] — credentials are
 * auth-layer concerns, profile data lives in the `users` table.
 */
interface UserProfileRepository {
    suspend fun getProfile(userId: String): UserProfile
    suspend fun updateProfile(userId: String, profile: UserProfile)
    suspend fun getMyPublicTrips(userId: String): List<TripData>
    suspend fun getSavedTrips(userId: String): List<TripData>
}
