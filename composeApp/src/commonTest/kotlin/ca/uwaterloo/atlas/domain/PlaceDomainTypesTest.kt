package ca.uwaterloo.atlas.domain

import ca.uwaterloo.atlas.domain.place.CostLevel
import ca.uwaterloo.atlas.domain.place.MoodOptions
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.PlaceFilter
import ca.uwaterloo.atlas.domain.place.PlaceSortType
import ca.uwaterloo.atlas.domain.place.TimeOfDay
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaceDomainTypesTest {

    @Test
    fun `placeCategory fromDisplayName resolves known category`() {
        assertEquals(PlaceCategory.CAFE, PlaceCategory.fromDisplayName("Cafe"))
    }

    @Test
    fun `placeCategory fromDisplayName falls back to OTHER`() {
        assertEquals(PlaceCategory.OTHER, PlaceCategory.fromDisplayName("Unknown Type"))
    }

    @Test
    fun `place stores rich metadata fields`() {
        val date = LocalDate(2026, 4, 1)
        val place = Place(
            id = "p1",
            tripId = "t1",
            name = "Cafe Atlas",
            category = PlaceCategory.CAFE,
            latitude = 43.47,
            longitude = -80.54,
            address = "200 University Ave W",
            photos = listOf("u1"),
            thumbnailPhoto = "u1",
            notes = "Great coffee",
            dateVisited = date,
            rating = 4f,
            mood = "😊",
            tags = listOf("cozy"),
            costIndicator = CostLevel.MODERATE,
            costAmount = 12.5,
            timeOfDay = TimeOfDay.MORNING,
            photoCaptions = mapOf("u1" to "window seat"),
            isFavorite = true
        )

        assertEquals("Cafe Atlas", place.name)
        assertEquals(PlaceCategory.CAFE, place.category)
        assertEquals(date, place.dateVisited)
        assertEquals(CostLevel.MODERATE, place.costIndicator)
        assertEquals(TimeOfDay.MORNING, place.timeOfDay)
        assertTrue(place.isFavorite)
    }

    @Test
    fun `place sorting and filter types expose expected defaults`() {
        assertTrue(PlaceSortType.entries.contains(PlaceSortType.RATING))
        val filter = PlaceFilter()
        assertTrue(filter.selectedCategories.isEmpty())
        assertFalse(filter.favoritesOnly)
    }

    @Test
    fun `mood presets include common reactions`() {
        assertTrue(MoodOptions.presets.isNotEmpty())
        assertTrue(MoodOptions.presets.any { it.first == "😊" })
        assertTrue(MoodOptions.presets.any { it.second == "Delicious" })
    }
}
