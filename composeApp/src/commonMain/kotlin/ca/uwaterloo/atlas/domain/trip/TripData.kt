package ca.uwaterloo.atlas.domain.trip

import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.DrawableResource

/**
 * Core domain/business object representing a trip.
 *
 * This is the canonical representation of a trip used throughout the Domain
 * and UI layers. It is intentionally separate from any data-layer concerns
 * (no JSON annotations, no database columns).
 *
 * Note: [drawableRes] is a temporary nullable field that holds a local placeholder
 * image resource. It will be removed once remote image loading is
 * integrated in a later sprint — at that point [imageUrl] takes over and this
 * field is dropped entirely. It defaults to null so domain/unit tests never
 * need to construct a DrawableResource (which is a Compose-internal type).
 */
data class TripData(
    val id: String,
    val title: String,
    val location: String,
    val imageUrl: String,
    val drawableRes: DrawableResource? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val placesCount: Int? = null,
    val isPublic: Boolean = true,
    val author: String? = null,
    val authorAvatarUrl: String? = null,
    val tags: List<String> = emptyList()
)
