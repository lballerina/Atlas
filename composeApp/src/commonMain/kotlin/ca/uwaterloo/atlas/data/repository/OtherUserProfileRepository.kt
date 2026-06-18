package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.domain.profile.UserProfile
import ca.uwaterloo.atlas.domain.trip.TripData

/**
 * DB Interface for reading another user's public profile.
 *
 * Read-only — no mutations. Looked up by display name for now;
 * switch to UUID lookup once auth is fully wired in.
 */
interface OtherUserProfileRepository {
    suspend fun getProfile(authorName: String): UserProfile?
    suspend fun getPublicTrips(authorName: String): List<TripData>
}
