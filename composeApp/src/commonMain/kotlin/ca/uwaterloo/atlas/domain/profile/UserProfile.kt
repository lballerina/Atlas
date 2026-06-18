package ca.uwaterloo.atlas.domain.profile

data class UserProfile(
    val displayName: String,
    val avatarUrl: String?,
    val email: String,
    val bio: String,
    val gender: Gender,
    val ageRange: AgeRange,
    val tags: Set<TravelStyleTag>,
    val stats: TravelStats
)
