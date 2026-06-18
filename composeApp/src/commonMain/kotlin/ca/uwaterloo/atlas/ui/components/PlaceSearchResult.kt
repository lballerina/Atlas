package ca.uwaterloo.atlas.ui.components

data class PlaceSearchResult(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)