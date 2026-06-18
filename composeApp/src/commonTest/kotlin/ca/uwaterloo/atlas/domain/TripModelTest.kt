package ca.uwaterloo.atlas.domain.trip

import ca.uwaterloo.atlas.data.MockDB
import ca.uwaterloo.atlas.data.repository.MockTripRepository
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.components.TripFormData
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [TripModel] using [MockTripRepository] backed by [MockDB].
 */
class TripModelTest {

    // Andrew's email is used by MockTripRepository to seed savedTripIds.
    private val andrewId = MockDB.currentUserProfile.email

    private lateinit var repository: MockTripRepository
    private lateinit var model: TripModel

    @Before
    fun setUp() {
        repository = MockTripRepository(currentUserId = andrewId)
        model = TripModel(repository = repository, currentUserId = andrewId)
    }

    // ── loadAll ───────────────────────────────────────────────────────────

    @Test
    fun `loadAll populates my trips from MockDB`() = runTest {
        model.loadAll()

        // MockDB trips with author == null are "mine"
        val expected = MockDB.trips.filter { it.author == null }
        assertEquals(expected.size, model.getMyTrips().size)
    }

    @Test
    fun `loadAll populates public trips from MockDB`() = runTest {
        model.loadAll()

        val expected = MockDB.trips.filter { it.isPublic && it.author != null }
        assertEquals(expected.size, model.getPublicTrips().size)
    }

    @Test
    fun `loadAll populates saved trip IDs for Andrew`() = runTest {
        model.loadAll()

        // MockDB.savedTripIds seeds 101 and 103 for Andrew's account
        assertEquals(MockDB.savedTripIds, model.getSavedTripIds())
    }

    @Test
    fun `loadAll returns empty saved IDs for unknown user`() = runTest {
        val anonModel = TripModel(
            repository = MockTripRepository(currentUserId = "unknown@user.com"),
            currentUserId = "unknown@user.com"
        )
        anonModel.loadAll()

        assertTrue(anonModel.getSavedTripIds().isEmpty())
    }

    // ── getMyTrips / getMyPublicTrips ─────────────────────────────────────

    @Test
    fun `getMyTrips returns only trips without an author`() = runTest {
        model.loadAll()

        model.getMyTrips().forEach { trip ->
            assertNull("Expected no author on my trip ${trip.id}", trip.author)
        }
    }

    @Test
    fun `getMyPublicTrips returns subset of my trips that are public`() = runTest {
        model.loadAll()

        val myPublic = model.getMyPublicTrips()
        myPublic.forEach { trip ->
            assertNull(trip.author)
            assertTrue("Expected isPublic=true for ${trip.id}", trip.isPublic)
        }
        assertTrue(myPublic.size <= model.getMyTrips().size)
    }

    // ── getPublicTrips / getPublicTripsByAuthor ───────────────────────────

    @Test
    fun `getPublicTrips returns only trips with an author`() = runTest {
        model.loadAll()

        model.getPublicTrips().forEach { trip ->
            assertNotNull("Expected an author on public trip ${trip.id}", trip.author)
            assertTrue(trip.isPublic)
        }
    }

    @Test
    fun `getPublicTripsByAuthor returns only trips by that author`() = runTest {
        model.loadAll()

        val sofiaTrips = model.getPublicTripsByAuthor("Sofia Martinez")
        assertTrue(sofiaTrips.isNotEmpty())
        sofiaTrips.forEach { assertEquals("Sofia Martinez", it.author) }
    }

    @Test
    fun `getPublicTripsByAuthor returns empty list for unknown author`() = runTest {
        model.loadAll()

        val result = model.getPublicTripsByAuthor("Nobody Here")
        assertTrue(result.isEmpty())
    }

    // ── Tags helpers ──────────────────────────────────────────────────────

    @Test
    fun `getMyTripTags returns all distinct tags from my trips`() = runTest {
        model.loadAll()

        val expected = model.getMyTrips().flatMap { it.tags }.toSet()
        assertEquals(expected, model.getMyTripTags())
    }

    @Test
    fun `getPublicTripTags returns all distinct tags from public trips`() = runTest {
        model.loadAll()

        val expected = model.getPublicTrips().flatMap { it.tags }.toSet()
        assertEquals(expected, model.getPublicTripTags())
    }

    // ── Save / unsave ─────────────────────────────────────────────────────

    @Test
    fun `isSaved returns true for pre-seeded trip`() = runTest {
        model.loadAll()

        assertTrue(model.isSaved("101"))
        assertTrue(model.isSaved("103"))
    }

    @Test
    fun `isSaved returns false for trip not in saved set`() = runTest {
        model.loadAll()

        assertFalse(model.isSaved("102"))
    }

    @Test
    fun `toggleSave saves an unsaved trip and returns true`() = runTest {
        model.loadAll()

        assertFalse(model.isSaved("102"))
        val result = model.toggleSave("102")
        assertTrue(result)
        assertTrue(model.isSaved("102"))
    }

    @Test
    fun `toggleSave unsaves a saved trip and returns false`() = runTest {
        model.loadAll()

        assertTrue(model.isSaved("101"))
        val result = model.toggleSave("101")
        assertFalse(result)
        assertFalse(model.isSaved("101"))
    }

    @Test
    fun `getSavedTrips returns TripData objects for saved IDs`() = runTest {
        model.loadAll()

        val saved = model.getSavedTrips()
        val ids = saved.map { it.id }.toSet()
        assertEquals(model.getSavedTripIds(), ids)
    }

    // ── filterTrips ───────────────────────────────────────────────────────

    @Test
    fun `filterTrips by title query is case-insensitive`() = runTest {
        model.loadAll()
        val all = model.getPublicTrips()

        val result = model.filterTrips(all, "barcelona", TripFilters())
        assertTrue(result.isNotEmpty())
        result.forEach { assertTrue(it.title.contains("Barcelona", ignoreCase = true)) }
    }

    @Test
    fun `filterTrips by location query`() = runTest {
        model.loadAll()
        val all = model.getPublicTrips()

        val result = model.filterTrips(all, "new york", TripFilters())
        assertTrue(result.isNotEmpty())
        result.forEach { assertTrue(it.location.contains("New York", ignoreCase = true)) }
    }

    @Test
    fun `filterTrips by author query`() = runTest {
        model.loadAll()
        val all = model.getPublicTrips()

        val result = model.filterTrips(all, "Sofia", TripFilters())
        assertTrue(result.isNotEmpty())
        result.forEach {
            assertTrue(it.author?.contains("Sofia", ignoreCase = true) == true)
        }
    }

    @Test
    fun `filterTrips by tag query`() = runTest {
        model.loadAll()
        val all = model.getPublicTrips()

        val result = model.filterTrips(all, "Adventure", TripFilters())
        assertTrue(result.isNotEmpty())
        result.forEach { trip ->
            assertTrue(trip.tags.any { it.equals("Adventure", ignoreCase = true) })
        }
    }

    @Test
    fun `filterTrips blank query returns all trips`() = runTest {
        model.loadAll()
        val all = model.getPublicTrips()

        val result = model.filterTrips(all, "", TripFilters())
        assertEquals(all.size, result.size)
    }

    @Test
    fun `filterTrips with showPublic=false hides public trips`() = runTest {
        model.loadAll()
        val all = model.getMyTrips()

        val filters = TripFilters(showPublic = false, showPrivate = true)
        val result = model.filterTrips(all, "", filters)
        result.forEach { assertFalse(it.isPublic) }
    }

    @Test
    fun `filterTrips with showPrivate=false hides private trips`() = runTest {
        model.loadAll()
        val all = model.getMyTrips()

        val filters = TripFilters(showPublic = true, showPrivate = false)
        val result = model.filterTrips(all, "", filters)
        result.forEach { assertTrue(it.isPublic) }
    }

    @Test
    fun `filterTrips by selectedTags keeps only trips containing that tag`() = runTest {
        model.loadAll()
        val all = model.getPublicTrips()

        val filters = TripFilters(selectedTags = setOf("Foodie"))
        val result = model.filterTrips(all, "", filters)
        assertTrue(result.isNotEmpty())
        result.forEach { trip ->
            assertTrue(trip.tags.any { it.equals("Foodie", ignoreCase = true) })
        }
    }

    @Test
    fun `filterTrips by date range excludes out-of-range trips`() = runTest {
        model.loadAll()
        val all = model.getPublicTrips()

        // Only trips that overlap June 2025 should survive
        val filters = TripFilters(
            startDate = LocalDate(2025, 6, 1),
            endDate   = LocalDate(2025, 6, 30)
        )
        val result = model.filterTrips(all, "", filters)
        result.forEach { trip ->
            val endOk   = trip.endDate == null   || trip.endDate   >= LocalDate(2025, 6, 1)
            val startOk = trip.startDate == null || trip.startDate <= LocalDate(2025, 6, 30)
            assertTrue("Trip ${trip.id} should not survive date filter", endOk && startOk)
        }
    }

    @Test
    fun `filterTrips returns empty list when no trips match query`() = runTest {
        model.loadAll()
        val all = model.getPublicTrips()

        val result = model.filterTrips(all, "zzznomatch999", TripFilters())
        assertTrue(result.isEmpty())
    }

    // ── createTrip ────────────────────────────────────────────────────────

    @Test
    fun `createTrip appends to my trips cache`() = runTest {
        model.loadAll()
        val before = model.getMyTrips().size

        val form = TripFormData(
            name        = "Test Trip",
            destination = "Test City",
            imageUrl    = "",
            startDate   = LocalDate(2025, 11, 1),
            endDate     = LocalDate(2025, 11, 7),
            isPublic    = false,
            tags        = listOf("Solo")
        )
        val created = model.createTrip(form)

        assertEquals("Test Trip", created.title)
        assertEquals(before + 1, model.getMyTrips().size)
    }

    @Test
    fun `createTrip uses provided fields correctly`() = runTest {
        model.loadAll()

        val form = TripFormData(
            name        = "Berlin Weekend",
            destination = "Berlin, Germany",
            imageUrl    = "https://example.com/berlin.jpg",
            startDate   = LocalDate(2026, 3, 1),
            endDate     = LocalDate(2026, 3, 3),
            isPublic    = true,
            tags        = listOf("Culture", "Solo")
        )
        val created = model.createTrip(form)

        assertEquals("Berlin Weekend",          created.title)
        assertEquals("Berlin, Germany",         created.location)
        assertEquals("https://example.com/berlin.jpg", created.imageUrl)
        assertTrue(created.isPublic)
        assertEquals(listOf("Culture", "Solo"), created.tags)
    }

    // ── updateTrip ────────────────────────────────────────────────────────

    @Test
    fun `updateTrip replaces the trip in cache`() = runTest {
        model.loadAll()

        val original = model.getMyTrips().first()
        val form = TripFormData(
            name        = "Updated Title",
            destination = original.location,
            imageUrl    = original.imageUrl,
            startDate   = original.startDate,
            endDate     = original.endDate,
            isPublic    = original.isPublic,
            tags        = original.tags
        )
        val updated = model.updateTrip(original.id, form)

        assertEquals("Updated Title", updated.title)
        val cached = model.getMyTrips().first { it.id == original.id }
        assertEquals("Updated Title", cached.title)
    }

    @Test
    fun `updateTrip does not change list size`() = runTest {
        model.loadAll()
        val before = model.getMyTrips().size

        val trip = model.getMyTrips().first()
        val form = TripFormData(
            name        = "Renamed",
            destination = trip.location,
            imageUrl    = trip.imageUrl,
            startDate   = trip.startDate,
            endDate     = trip.endDate,
            isPublic    = trip.isPublic,
            tags        = trip.tags
        )
        model.updateTrip(trip.id, form)

        assertEquals(before, model.getMyTrips().size)
    }

    // ── deleteTrip ────────────────────────────────────────────────────────

    @Test
    fun `deleteTrip removes the trip from cache`() = runTest {
        model.loadAll()
        val target = model.getMyTrips().first()
        val before = model.getMyTrips().size

        model.deleteTrip(target.id)

        assertEquals(before - 1, model.getMyTrips().size)
        assertNull(model.getMyTrips().firstOrNull { it.id == target.id })
    }

    @Test
    fun `deleteTrip on unknown ID leaves list unchanged`() = runTest {
        model.loadAll()
        val before = model.getMyTrips().size

        // MockTripRepository uses removeAll — a non-existent ID is a no-op
        model.deleteTrip("nonexistent_id")

        assertEquals(before, model.getMyTrips().size)
    }
}
