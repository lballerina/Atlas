package ca.uwaterloo.atlas.domain

import ca.uwaterloo.atlas.data.repository.MockPlaceRepository
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.PlaceModel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.*

class PlaceModelTest {

    private fun model() = PlaceModel(repository = MockPlaceRepository(initialPlaces = emptyList()))

    // ── Fixtures ─────────────────────────────────────────────

    private val samplePlace = Place(
        id = "test_place_1",
        tripId = "trip_1",
        name = "Test Cafe",
        category = PlaceCategory.CAFE,
        latitude = 0.0,
        longitude = 0.0,
        address = "Test Address",
        notes = "Nice coffee",
        dateVisited = LocalDate(2025, 7, 1),
        rating = 4.5f,
        isFavorite = true
    )

    private val updatedPlace = samplePlace.copy(
        name = "Updated Cafe",
        notes = "Updated notes",
        rating = 5.0f
    )

    // ── getPlaceById ─────────────────────────────────────────

    @Test
    fun `getPlaceById returns null for unknown id`() = runTest {
        val m = model()
        val result = m.getPlaceById("does_not_exist")
        assertNull(result)
    }

    @Test
    fun `getPlaceById returns correct place after add`() = runTest {
        val m = model()
        m.addPlace(samplePlace)

        val result = m.getPlaceById("test_place_1")

        assertNotNull(result)
        assertEquals("Test Cafe", result.name)
    }

    // ── getPlacesForTrip ─────────────────────────────────────

    @Test
    fun `getPlacesForTrip returns only places for given trip`() = runTest {
        val m = model()

        val p1 = samplePlace
        val p2 = samplePlace.copy(id = "p2", tripId = "trip_2")

        m.addPlace(p1)
        m.addPlace(p2)

        val trip1Places = m.getPlacesForTrip("trip_1")

        assertTrue(trip1Places.all { it.tripId == "trip_1" })
        assertTrue(trip1Places.any { it.id == "test_place_1" })
        assertFalse(trip1Places.any { it.id == "p2" })
    }

    // ── addPlace ─────────────────────────────────────────────

    @Test
    fun `addPlace increases total place count`() = runTest {
        val m = model()
        val before = m.allPlaces.value.size

        m.addPlace(samplePlace)

        val after = m.allPlaces.value.size
        assertEquals(before + 1, after)
    }

    @Test
    fun `addPlace emits new place in StateFlow`() = runTest {
        val m = model()
        m.addPlace(samplePlace)

        val exists = m.allPlaces.value.any { it.id == "test_place_1" }
        assertTrue(exists)
    }

    // ── updatePlace ───────────────────────────────────────────

    @Test
    fun `updatePlace modifies existing place`() = runTest {
        val m = model()
        m.addPlace(samplePlace)

        m.updatePlace(updatedPlace)

        val result = m.getPlaceById("test_place_1")

        assertNotNull(result)
        assertEquals("Updated Cafe", result.name)
        assertEquals("Updated notes", result.notes)
        assertEquals(5.0f, result.rating)
    }

    @Test
    fun `updatePlace does nothing if id not found`() = runTest {
        val m = model()

        try {
            m.updatePlace(updatedPlace)
        } catch (e: Exception) {
            // ignore
        }

        val result = m.getPlaceById("test_place_1")
        assertNull(result)
    }

    // ── deletePlace ───────────────────────────────────────────

    @Test
    fun `deletePlace removes place`() = runTest {
        val m = model()
        m.addPlace(samplePlace)

        m.deletePlace("test_place_1")

        val result = m.getPlaceById("test_place_1")
        assertNull(result)
    }

    @Test
    fun `deletePlace does nothing for unknown id`() = runTest {
        val m = model()
        val before = m.allPlaces.value.size

        m.deletePlace("unknown_id")

        val after = m.allPlaces.value.size
        assertEquals(before, after)
    }

    // ── StateFlow integrity ───────────────────────────────────

    @Test
    fun `StateFlow reflects latest list after multiple mutations`() = runTest {
        val m = model()

        m.addPlace(samplePlace)
        m.updatePlace(updatedPlace)
        m.deletePlace("test_place_1")

        assertFalse(m.allPlaces.value.any { it.id == "test_place_1" })
    }
}