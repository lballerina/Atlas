package ca.uwaterloo.atlas.domain.place

enum class PlaceSortType {
    DATE_VISITED,
    RATING,
    ALPHABETICAL,
    FAVORITE,
    CATEGORY
}

data class PlaceFilter(
    val selectedCategories: Set<PlaceCategory> = emptySet(),
    val favoritesOnly: Boolean = false
)