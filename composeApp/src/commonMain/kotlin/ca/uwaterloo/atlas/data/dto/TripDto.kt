package ca.uwaterloo.atlas.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.ui.components.TripFormData
import kotlinx.datetime.LocalDate

/**
 * Nested object Supabase returns for the joined users table.
 * When you select "*, users!author_id(display_name)", the JSON is:
 *   { "id": "...", "users": { "display_name": "Test User" }, ... }
 * This class maps that nested object.
 */
@Serializable
data class UserJoinDto(
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url")   val avatarUrl: String?   = null
)

/**
 * Row returned by Supabase SELECT on the trips table.
 * The [users] field is populated only when the query includes the join:
 *   Columns.raw("*, users!author_id(display_name, avatar_url)")
 */
@Serializable
data class TripDto(
    val id: String,
    @SerialName("author_id")  val authorId: String?   = null,
    val title: String,
    val location: String,
    @SerialName("image_url")  val imageUrl: String    = "",
    @SerialName("start_date") val startDate: String?  = null,
    @SerialName("end_date")   val endDate: String?    = null,
    @SerialName("is_public")  val isPublic: Boolean   = true,
    val tags: List<String>                            = emptyList(),
    // Populated by the join — null when querying without the join
    val users: UserJoinDto?                           = null
) {
    fun toDomain(): TripData = TripData(
        id        = id,
        title     = title,
        location  = location,
        imageUrl  = imageUrl,
        startDate = startDate?.let { LocalDate.parse(it) },
        endDate   = endDate?.let   { LocalDate.parse(it) },
        isPublic  = isPublic,
        tags      = tags,
        // Extract display_name and avatar_url from the nested join object
        author    = users?.displayName,
        authorAvatarUrl = users?.avatarUrl
    )
}

/**
 * Payload for INSERT — omits id (server-generated) and users (join field).
 */
@Serializable
data class TripInsertDto(
    @SerialName("author_id")  val authorId: String? = null,
    val title: String,
    val location: String,
    @SerialName("image_url")  val imageUrl: String,
    @SerialName("start_date") val startDate: String?,
    @SerialName("end_date")   val endDate: String?,
    @SerialName("is_public")  val isPublic: Boolean,
    val tags: List<String>
)

/**
 * Payload for UPDATE — omits id and author_id (never updated).
 */
@Serializable
data class TripUpdateDto(
    val title: String,
    val location: String,
    @SerialName("image_url")  val imageUrl: String,
    @SerialName("start_date") val startDate: String?,
    @SerialName("end_date")   val endDate: String?,
    @SerialName("is_public")  val isPublic: Boolean,
    val tags: List<String>
)

fun TripFormData.toInsertDto(authorId: String) = TripInsertDto(
    authorId  = authorId,
    title     = name,
    location  = destination,
    imageUrl  = imageUrl,
    startDate = startDate?.toString(),
    endDate   = endDate?.toString(),
    isPublic  = isPublic,
    tags      = tags
)

fun TripFormData.toUpdateDto() = TripUpdateDto(
    title     = name,
    location  = destination,
    imageUrl  = imageUrl,
    startDate = startDate?.toString(),
    endDate   = endDate?.toString(),
    isPublic  = isPublic,
    tags      = tags
)
