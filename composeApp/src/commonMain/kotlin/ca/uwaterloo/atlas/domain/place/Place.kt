package ca.uwaterloo.atlas.domain.place
import kotlinx.datetime.LocalDate

// Represents a place/destination in a trip
data class Place(
    val id: String,
    val tripId: String,
    val name: String,
    val category: PlaceCategory,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val photos: List<String> = emptyList(), // URLs or file paths
    val thumbnailPhoto: String? = null, // Main photo to display
    val notes: String = "",
    val dateVisited: LocalDate? = null,

    val rating: Float? = null,                 // 0.0 - 5.0
    val mood: String? = null,                  // Emojis
    val tags: List<String> = emptyList(),      // ["romantic", "outdoor"]
    val costIndicator: CostLevel? = null,      // Enum: BUDGET($), MODERATE($$), EXPENSIVE($$$)
    val costAmount: Double? = null,            // e.g., 45.50 (actual amount spent)
    val timeOfDay: TimeOfDay? = null,          // time of day visited, Enum: MORNING, AFTERNOON, EVENING
    val photoCaptions: Map<String, String> = emptyMap(), // photoId -> caption
    val isFavorite: Boolean = false
)

// Categories for different types of places
enum class PlaceCategory(val displayName: String) {
    CAFE("Cafe"),
    RESTAURANT("Restaurant"),
    MUSEUM("Museum"),
    PARK("Park"),
    VIEWPOINT("Viewpoint"),
    HOTEL("Hotel"),
    SHOPPING("Shopping"),
    ATTRACTION("Attraction"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(name: String): PlaceCategory {
            return values().find { it.displayName == name } ?: OTHER
        }
    }
}

// Cost level indicator (like Google Maps)
enum class CostLevel(val displayName: String, val symbol: String) {
    BUDGET("Budget", "$"),
    MODERATE("Moderate", "$$"),
    EXPENSIVE("Expensive", "$$$"),
    LUXURY("Luxury", "$$$$");
}

// Time of day when place was visited
enum class TimeOfDay(val displayName: String, val emoji: String) {
    MORNING("Morning", "🌅"),
    AFTERNOON("Afternoon", "☀️"),
    EVENING("Evening", "🌆"),
    NIGHT("Night", "🌙");
}

// Preset mood options with emojis
object MoodOptions {
    val presets = listOf(
        "😊" to "Happy",
        "😌" to "Relaxed",
        "🤩" to "Excited",
        "😍" to "Loved it",
        "🤔" to "Interesting",
        "😴" to "Boring",
        "😋" to "Delicious",
        "📸" to "Photogenic",
        "🎉" to "Fun",
        "🙏" to "Peaceful"
    )
}