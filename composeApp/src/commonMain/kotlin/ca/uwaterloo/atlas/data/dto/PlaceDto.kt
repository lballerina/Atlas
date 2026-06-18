package ca.uwaterloo.atlas.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ca.uwaterloo.atlas.domain.place.*
import kotlinx.datetime.LocalDate

/**
 * Row returned by Supabase SELECT on the places table.
 */
@Serializable
data class PlaceDto(
    val id: String,
    @SerialName("trip_id")         val tripId: String,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String                           = "",
    @SerialName("photo_urls")      val photoUrls: List<String>   = emptyList(),
    @SerialName("thumbnail_photo") val thumbnailPhoto: String?   = null,
    val notes: String                             = "",
    @SerialName("date_visited")    val dateVisited: String?      = null,
    val rating: Float?                            = null,
    val mood: String?                             = null,
    val tags: List<String>                        = emptyList(),
    @SerialName("cost_indicator")  val costIndicator: String?    = null,
    @SerialName("cost_amount")     val costAmount: Double?       = null,
    @SerialName("time_of_day")     val timeOfDay: String?        = null,
    @SerialName("is_favorite")     val isFavorite: Boolean       = false,
    val captions: Map<String, String>             = emptyMap()
) {
    fun toDomain(): Place = Place(
        id             = id,
        tripId         = tripId,
        name           = name,
        category       = PlaceCategory.fromDisplayName(category),
        latitude       = latitude,
        longitude      = longitude,
        address        = address,
        photos         = photoUrls,
        thumbnailPhoto = thumbnailPhoto,
        notes          = notes,
        dateVisited    = dateVisited?.let { LocalDate.parse(it) },
        rating         = rating,
        mood           = mood,
        tags           = tags,
        costIndicator  = costIndicator?.let { runCatching { CostLevel.valueOf(it) }.getOrNull() },
        costAmount     = costAmount,
        timeOfDay      = timeOfDay?.let  { runCatching { TimeOfDay.valueOf(it) }.getOrNull() },
        photoCaptions  = captions,
        isFavorite     = isFavorite
    )
}

/**
 * Payload for INSERT and UPDATE on the places table.
 * Omits id (server-generated). Used for both insert and update.
 */
@Serializable
data class PlaceInsertDto(
    @SerialName("trip_id")         val tripId: String,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    @SerialName("photo_urls")      val photoUrls: List<String>,
    @SerialName("thumbnail_photo") val thumbnailPhoto: String?,
    val notes: String,
    @SerialName("date_visited")    val dateVisited: String?,
    val rating: Float?,
    val mood: String?,
    val tags: List<String>,
    @SerialName("cost_indicator")  val costIndicator: String?,
    @SerialName("cost_amount")     val costAmount: Double?,
    @SerialName("time_of_day")     val timeOfDay: String?,
    @SerialName("is_favorite")     val isFavorite: Boolean,
    val captions: Map<String, String> = emptyMap()
)

fun Place.toInsertDto() = PlaceInsertDto(
    tripId         = tripId,
    name           = name,
    category       = category.displayName,
    latitude       = latitude,
    longitude      = longitude,
    address        = address,
    photoUrls      = photos,
    thumbnailPhoto = thumbnailPhoto,
    notes          = notes,
    dateVisited    = dateVisited?.toString(),
    rating         = rating,
    mood           = mood,
    tags           = tags,
    costIndicator  = costIndicator?.name,
    costAmount     = costAmount,
    timeOfDay      = timeOfDay?.name,
    isFavorite     = isFavorite,
    captions       = photoCaptions
)
