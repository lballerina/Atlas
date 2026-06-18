package ca.uwaterloo.atlas.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ca.uwaterloo.atlas.domain.profile.*

/**
 * Row returned by Supabase SELECT on the users table.
 */
@Serializable
data class UserDto(
    val id: String,
    @SerialName("display_name")      val displayName: String     = "",
    @SerialName("avatar_url")        val avatarUrl: String?       = "",
    val email: String                                                   = "",
    val bio: String                                                     = "",
    val gender: String                                                  = "PREFER_NOT_TO_SAY",
    @SerialName("age_range")         val ageRange: String              = "LATE_20S",
    @SerialName("travel_style_tags") val travelStyleTags: List<String> = emptyList(),
    @SerialName("trips_count")       val tripsCount: Int               = 0,
    @SerialName("places_count")      val placesCount: Int              = 0,
    @SerialName("countries_count")   val countriesCount: Int           = 0
) {
    fun toDomain(): UserProfile = UserProfile(
        displayName = displayName,
        avatarUrl   = avatarUrl,
        email       = email,
        bio         = bio,
        gender      = runCatching { Gender.valueOf(gender) }.getOrDefault(Gender.PREFER_NOT_TO_SAY),
        ageRange    = runCatching { AgeRange.valueOf(ageRange) }.getOrDefault(AgeRange.LATE_20S),
        tags        = travelStyleTags
            .mapNotNull { runCatching { TravelStyleTag.valueOf(it) }.getOrNull() }
            .toSet(),
        stats       = TravelStats(
            trips     = tripsCount,
            places    = placesCount,
            countries = countriesCount
        )
    )
}

/**
 * Payload for INSERT — used when creating the users row during sign-up.
 * Includes id because it must match the Supabase Auth UUID.
 */
@Serializable
data class UserInsertDto(
    val id: String,
    @SerialName("display_name")      val displayName: String,
    @SerialName("avatar_url")        val avatarUrl: String?,
    val email: String,
    val bio: String,
    val gender: String,
    @SerialName("age_range")         val ageRange: String,
    @SerialName("travel_style_tags") val travelStyleTags: List<String>,
    @SerialName("trips_count")       val tripsCount: Int,
    @SerialName("places_count")      val placesCount: Int,
    @SerialName("countries_count")   val countriesCount: Int
)

/**
 * Payload for UPDATE — omits id (used as the filter, not a field to update).
 */
@Serializable
data class UserUpdateDto(
    @SerialName("display_name")      val displayName: String,
    @SerialName("avatar_url")        val avatarUrl: String?,
    val bio: String,
    val gender: String,
    @SerialName("age_range")         val ageRange: String,
    @SerialName("travel_style_tags") val travelStyleTags: List<String>,
    @SerialName("trips_count")       val tripsCount: Int,
    @SerialName("places_count")      val placesCount: Int,
    @SerialName("countries_count")   val countriesCount: Int
)

fun UserProfile.toInsertDto(userId: String) = UserInsertDto(
    id              = userId,
    avatarUrl       = avatarUrl,
    displayName     = displayName,
    email           = email,
    bio             = bio,
    gender          = gender.name,
    ageRange        = ageRange.name,
    travelStyleTags = tags.map { it.name },
    tripsCount      = stats.trips,
    placesCount     = stats.places,
    countriesCount  = stats.countries
)

fun UserProfile.toUpdateDto() = UserUpdateDto(
    displayName     = displayName,
    avatarUrl       = avatarUrl,
    bio             = bio,
    gender          = gender.name,
    ageRange        = ageRange.name,
    travelStyleTags = tags.map { it.name },
    tripsCount      = stats.trips,
    placesCount     = stats.places,
    countriesCount  = stats.countries
)
