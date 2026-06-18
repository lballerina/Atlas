package ca.uwaterloo.atlas.viewmodel

import ca.uwaterloo.atlas.data.MockDB
import ca.uwaterloo.atlas.data.repository.MockTripRepository
import ca.uwaterloo.atlas.domain.trip.TripModel
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.components.TripFormData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [MyTripsViewModel] using [MockTripRepository] backed by [MockDB].
 *
 * The test dispatcher is installed as Main so that [viewModelScope] coroutines
 * run synchronously under [advanceUntilIdle].
 *
 * Note: [MyTripsViewModel.createTrip] and [updateTrip] contain a branch that
 * uploads images for "content://" URIs via [getImageUploader()].  That path
 * is platform-specific and untestable in a JVM unit test, so all test data
 * uses plain strings (empty or https://) to exercise the fast path.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyTripsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val andrewId = MockDB.currentUserProfile.email

    private lateinit var repository: MockTripRepository
    private lateinit var model: TripModel
    private lateinit var vm: MyTripsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = MockTripRepository(currentUserId = andrewId)
        model = TripModel(repository = repository, currentUserId = andrewId)
        // Inject the test dispatcher so all coroutines run on the same scheduler
        vm = MyTripsViewModel(model, dispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private fun sampleForm(
        name: String      = "New Trip",
        destination: String = "Test City",
        imageUrl: String  = "",
        startDate: LocalDate = LocalDate(2025, 11, 1),
        endDate: LocalDate   = LocalDate(2025, 11, 7),
        isPublic: Boolean    = false,
        tags: List<String>   = listOf("Solo")
    ) = TripFormData(
        name        = name,
        destination = destination,
        imageUrl    = imageUrl,
        startDate   = startDate,
        endDate     = endDate,
        isPublic    = isPublic,
        tags        = tags
    )

    // ── Initial load ──────────────────────────────────────────────────────

    @Test
    fun `init triggers load and populates trips`() = runTest {
        advanceUntilIdle()

        val expected = MockDB.trips.filter { it.author == null }
        assertEquals(expected.size, vm.trips.size)
    }

    @Test
    fun `init sets isLoading to false after load`() = runTest {
        advanceUntilIdle()

        assertFalse(vm.isLoading)
    }

    @Test
    fun `init sets no error message on success`() = runTest {
        advanceUntilIdle()

        assertNull(vm.errorMessage)
    }

    @Test
    fun `trips contain only my trips (no author field)`() = runTest {
        advanceUntilIdle()

        vm.trips.forEach { trip ->
            assertNull("My trips must not have an author set", trip.author)
        }
    }

    // ── loadTrips (explicit refresh) ──────────────────────────────────────

    @Test
    fun `loadTrips refreshes state without error`() = runTest {
        advanceUntilIdle()
        val first = vm.trips.size

        vm.loadTrips()
        advanceUntilIdle()

        assertEquals(first, vm.trips.size)
        assertNull(vm.errorMessage)
    }

    // ── filteredTrips / search ────────────────────────────────────────────

    @Test
    fun `filteredTrips returns all trips when query is blank`() = runTest {
        advanceUntilIdle()

        assertEquals(vm.trips.size, vm.filteredTrips.size)
    }

    @Test
    fun `updateSearchQuery filters by title`() = runTest {
        advanceUntilIdle()

        // "Paris" appears in "Summer in Paris" which is a my-trip
        vm.updateSearchQuery("Paris")

        assertTrue(vm.filteredTrips.isNotEmpty())
        vm.filteredTrips.forEach { trip ->
            assertTrue(
                trip.title.contains("Paris", ignoreCase = true) ||
                        trip.location.contains("Paris", ignoreCase = true) ||
                        trip.tags.any { it.contains("Paris", ignoreCase = true) }
            )
        }
    }

    @Test
    fun `updateSearchQuery with no match returns empty filteredTrips`() = runTest {
        advanceUntilIdle()

        vm.updateSearchQuery("zzznomatch999")

        assertTrue(vm.filteredTrips.isEmpty())
    }

    @Test
    fun `updateSearchQuery blank resets filter`() = runTest {
        advanceUntilIdle()
        val total = vm.trips.size

        vm.updateSearchQuery("Paris")
        vm.updateSearchQuery("")

        assertEquals(total, vm.filteredTrips.size)
    }

    // ── applyFilters ──────────────────────────────────────────────────────

    @Test
    fun `applyFilters stores the given TripFilters`() = runTest {
        advanceUntilIdle()

        val filters = TripFilters(selectedTags = setOf("Culture"))
        vm.applyFilters(filters)

        assertEquals(filters, vm.activeFilters)
    }

    @Test
    fun `applyFilters by tag narrows filteredTrips`() = runTest {
        advanceUntilIdle()

        // "Culture" tag appears in trip id="1" ("Summer in Paris")
        vm.applyFilters(TripFilters(selectedTags = setOf("Culture")))

        assertTrue(vm.filteredTrips.isNotEmpty())
        vm.filteredTrips.forEach { trip ->
            assertTrue(trip.tags.any { it.equals("Culture", ignoreCase = true) })
        }
    }

    @Test
    fun `applyFilters showPrivate=false hides private trips`() = runTest {
        advanceUntilIdle()

        vm.applyFilters(TripFilters(showPublic = true, showPrivate = false))

        vm.filteredTrips.forEach { assertTrue(it.isPublic) }
    }

    @Test
    fun `applyFilters showPublic=false hides public trips`() = runTest {
        advanceUntilIdle()

        vm.applyFilters(TripFilters(showPublic = false, showPrivate = true))

        vm.filteredTrips.forEach { assertFalse(it.isPublic) }
    }

    // ── availableTags ─────────────────────────────────────────────────────

    @Test
    fun `availableTags returns all distinct tags across loaded trips`() = runTest {
        advanceUntilIdle()

        val expected = vm.trips.flatMap { it.tags }.toSet()
        assertEquals(expected, vm.availableTags)
    }

    // ── createTrip ────────────────────────────────────────────────────────

    @Test
    fun `createTrip increases trips count by one`() = runTest {
        advanceUntilIdle()
        val before = vm.trips.size

        vm.createTrip(sampleForm(name = "New Adventure"))
        advanceUntilIdle()

        assertEquals(before + 1, vm.trips.size)
    }

    @Test
    fun `createTrip reflects title in trips list`() = runTest {
        advanceUntilIdle()

        vm.createTrip(sampleForm(name = "My Summer Trip"))
        advanceUntilIdle()

        assertTrue(vm.trips.any { it.title == "My Summer Trip" })
    }

    @Test
    fun `createTrip reflects destination in trips list`() = runTest {
        advanceUntilIdle()

        vm.createTrip(sampleForm(destination = "Lisbon, Portugal"))
        advanceUntilIdle()

        assertTrue(vm.trips.any { it.location == "Lisbon, Portugal" })
    }

    @Test
    fun `createTrip sets correct dates`() = runTest {
        advanceUntilIdle()

        val start = LocalDate(2026, 4, 1)
        val end   = LocalDate(2026, 4, 10)
        vm.createTrip(sampleForm(startDate = start, endDate = end))
        advanceUntilIdle()

        val created = vm.trips.first { it.startDate == start }
        assertEquals(end, created.endDate)
    }

    @Test
    fun `createTrip does not set an error message on success`() = runTest {
        advanceUntilIdle()

        vm.createTrip(sampleForm())
        advanceUntilIdle()

        assertNull(vm.errorMessage)
    }

    @Test
    fun `creating multiple trips keeps all in the list`() = runTest {
        advanceUntilIdle()
        val before = vm.trips.size

        vm.createTrip(sampleForm(name = "Trip A"))
        advanceUntilIdle()
        vm.createTrip(sampleForm(name = "Trip B"))
        advanceUntilIdle()

        assertEquals(before + 2, vm.trips.size)
    }

    @Test
    fun `createTrip rejects end date before start date`() = runTest {
        advanceUntilIdle()
        val before = vm.trips.size

        vm.createTrip(
            sampleForm(
                startDate = LocalDate(2026, 4, 10),
                endDate = LocalDate(2026, 4, 1)
            )
        )
        advanceUntilIdle()

        assertEquals(before, vm.trips.size)
        assertEquals("End date must be on or after start date.", vm.errorMessage)
    }

    // ── updateTrip ────────────────────────────────────────────────────────

    @Test
    fun `updateTrip changes the title of an existing trip`() = runTest {
        advanceUntilIdle()

        val target = vm.trips.first()
        val form = sampleForm(
            name        = "Renamed Trip",
            destination = target.location,
            startDate   = target.startDate ?: LocalDate(2025, 1, 1),
            endDate     = target.endDate   ?: LocalDate(2025, 1, 7)
        )
        vm.updateTrip(target, form)
        advanceUntilIdle()

        assertTrue(vm.trips.any { it.id == target.id && it.title == "Renamed Trip" })
    }

    @Test
    fun `updateTrip does not change the list size`() = runTest {
        advanceUntilIdle()
        val before = vm.trips.size

        val target = vm.trips.first()
        vm.updateTrip(target, sampleForm(
            name        = "Unchanged Count",
            destination = target.location,
            startDate   = target.startDate ?: LocalDate(2025, 1, 1),
            endDate     = target.endDate   ?: LocalDate(2025, 1, 7)
        ))
        advanceUntilIdle()

        assertEquals(before, vm.trips.size)
    }

    @Test
    fun `updateTrip updates tags`() = runTest {
        advanceUntilIdle()

        val target = vm.trips.first()
        val newTags = listOf("Adventure", "Nature")
        vm.updateTrip(target, sampleForm(
            name        = target.title,
            destination = target.location,
            startDate   = target.startDate ?: LocalDate(2025, 1, 1),
            endDate     = target.endDate   ?: LocalDate(2025, 1, 7),
            tags        = newTags
        ))
        advanceUntilIdle()

        val updated = vm.trips.first { it.id == target.id }
        assertEquals(newTags, updated.tags)
    }

    @Test
    fun `updateTrip rejects end date before start date`() = runTest {
        advanceUntilIdle()
        val target = vm.trips.first()
        val original = target.copy()

        vm.updateTrip(
            target,
            sampleForm(
                name = "Should Not Save",
                destination = target.location,
                startDate = LocalDate(2026, 5, 12),
                endDate = LocalDate(2026, 5, 1)
            )
        )
        advanceUntilIdle()

        val after = vm.trips.first { it.id == target.id }
        assertEquals(original.title, after.title)
        assertEquals("End date must be on or after start date.", vm.errorMessage)
    }

    // ── deleteTrip ────────────────────────────────────────────────────────

    @Test
    fun `deleteTrip removes the trip from the list`() = runTest {
        advanceUntilIdle()

        val target = vm.trips.first()
        vm.deleteTrip(target.id)
        advanceUntilIdle()

        assertNull(vm.trips.firstOrNull { it.id == target.id })
    }

    @Test
    fun `deleteTrip decreases list size by one`() = runTest {
        advanceUntilIdle()
        val before = vm.trips.size

        vm.deleteTrip(vm.trips.first().id)
        advanceUntilIdle()

        assertEquals(before - 1, vm.trips.size)
    }

    @Test
    fun `deleteTrip on unknown ID leaves list unchanged`() = runTest {
        advanceUntilIdle()
        val before = vm.trips.size

        vm.deleteTrip("nonexistent_id")
        advanceUntilIdle()

        assertEquals(before, vm.trips.size)
    }

    @Test
    fun `deleting all trips results in empty list`() = runTest {
        advanceUntilIdle()

        val ids = vm.trips.map { it.id }
        ids.forEach { vm.deleteTrip(it); advanceUntilIdle() }

        assertTrue(vm.trips.isEmpty())
    }

    // ── clearError ────────────────────────────────────────────────────────

    @Test
    fun `clearError nullifies errorMessage`() = runTest {
        advanceUntilIdle()

        vm.clearError()

        assertNull(vm.errorMessage)
    }
}
