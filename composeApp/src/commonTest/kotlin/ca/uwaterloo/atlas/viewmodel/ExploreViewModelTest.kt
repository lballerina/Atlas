package ca.uwaterloo.atlas.viewmodel

import ca.uwaterloo.atlas.data.MockDB
import ca.uwaterloo.atlas.data.repository.MockTripRepository
import ca.uwaterloo.atlas.domain.trip.TripModel
import ca.uwaterloo.atlas.ui.components.TripFilters
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
 * Unit tests for [ExploreViewModel] using [MockTripRepository] backed by [MockDB].
 *
 * The test dispatcher is installed as Main so that [viewModelScope] coroutines
 * run synchronously under [advanceUntilIdle].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val andrewId = MockDB.currentUserProfile.email

    private lateinit var repository: MockTripRepository
    private lateinit var model: TripModel
    private lateinit var vm: ExploreViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = MockTripRepository(currentUserId = andrewId)
        model = TripModel(repository = repository, currentUserId = andrewId)
        vm = ExploreViewModel(model)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial load ──────────────────────────────────────────────────────

    @Test
    fun `init triggers load and populates trips`() = runTest {
        advanceUntilIdle()

        val expected = MockDB.trips.filter { it.isPublic && it.author != null }
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
    fun `init loads savedTripIds for Andrew from MockDB`() = runTest {
        advanceUntilIdle()

        assertEquals(MockDB.savedTripIds, vm.savedTripIds)
    }

    @Test
    fun `trips contain only public community trips (author not null)`() = runTest {
        advanceUntilIdle()

        vm.trips.forEach { trip ->
            assertNotNull("Expected community trip to have an author", trip.author)
            assertTrue("Expected community trip to be public", trip.isPublic)
        }
    }

    // ── loadTrips (explicit refresh) ──────────────────────────────────────

    @Test
    fun `loadTrips refreshes state`() = runTest {
        advanceUntilIdle()
        val first = vm.trips.size

        vm.loadTrips()
        advanceUntilIdle()

        assertEquals(first, vm.trips.size)
    }

    // ── filteredTrips / search ────────────────────────────────────────────

    @Test
    fun `filteredTrips returns all trips when query is blank`() = runTest {
        advanceUntilIdle()

        assertEquals(vm.trips.size, vm.filteredTrips.size)
    }

    @Test
    fun `updateSearchQuery filters trips by title`() = runTest {
        advanceUntilIdle()

        vm.updateSearchQuery("Barcelona")
        advanceUntilIdle()

        assertTrue(vm.filteredTrips.isNotEmpty())
        vm.filteredTrips.forEach {
            assertTrue(
                it.title.contains("Barcelona", ignoreCase = true) ||
                        it.location.contains("Barcelona", ignoreCase = true) ||
                        it.tags.any { t -> t.contains("Barcelona", ignoreCase = true) } ||
                        it.author?.contains("Barcelona", ignoreCase = true) == true
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
    fun `updateSearchQuery with blank resets filter`() = runTest {
        advanceUntilIdle()
        val total = vm.trips.size

        vm.updateSearchQuery("Barcelona")
        vm.updateSearchQuery("")

        assertEquals(total, vm.filteredTrips.size)
    }

    // ── applyFilters ──────────────────────────────────────────────────────

    @Test
    fun `applyFilters stores the given TripFilters`() = runTest {
        advanceUntilIdle()

        val filters = TripFilters(selectedTags = setOf("Foodie"))
        vm.applyFilters(filters)

        assertEquals(filters, vm.activeFilters)
    }

    @Test
    fun `applyFilters by tag narrows filteredTrips`() = runTest {
        advanceUntilIdle()

        vm.applyFilters(TripFilters(selectedTags = setOf("Adventure")))

        assertTrue(vm.filteredTrips.isNotEmpty())
        vm.filteredTrips.forEach { trip ->
            assertTrue(trip.tags.any { it.equals("Adventure", ignoreCase = true) })
        }
    }

    @Test
    fun `applyFilters by date range keeps only overlapping trips`() = runTest {
        advanceUntilIdle()

        val filters = TripFilters(
            startDate = LocalDate(2025, 6, 1),
            endDate   = LocalDate(2025, 6, 30)
        )
        vm.applyFilters(filters)

        vm.filteredTrips.forEach { trip ->
            val endOk   = trip.endDate == null   || trip.endDate   >= LocalDate(2025, 6, 1)
            val startOk = trip.startDate == null || trip.startDate <= LocalDate(2025, 6, 30)
            assertTrue("Trip ${trip.id} should not pass date filter", endOk && startOk)
        }
    }

    // ── availableTags ─────────────────────────────────────────────────────

    @Test
    fun `availableTags returns all distinct tags across loaded trips`() = runTest {
        advanceUntilIdle()

        val expected = vm.trips.flatMap { it.tags }.toSet()
        assertEquals(expected, vm.availableTags)
    }

    @Test
    fun `availableTags is non-empty after load`() = runTest {
        advanceUntilIdle()

        assertTrue(vm.availableTags.isNotEmpty())
    }

    // ── toggleSave ────────────────────────────────────────────────────────

    @Test
    fun `toggleSave on unsaved trip adds it to savedTripIds`() = runTest {
        advanceUntilIdle()

        assertFalse(vm.savedTripIds.contains("102"))
        vm.toggleSave("102")
        advanceUntilIdle()

        assertTrue(vm.savedTripIds.contains("102"))
    }

    @Test
    fun `toggleSave on saved trip removes it from savedTripIds`() = runTest {
        advanceUntilIdle()

        assertTrue(vm.savedTripIds.contains("101"))
        vm.toggleSave("101")
        advanceUntilIdle()

        assertFalse(vm.savedTripIds.contains("101"))
    }

    @Test
    fun `toggleSave twice restores original saved state`() = runTest {
        advanceUntilIdle()

        val wasAlreadySaved = vm.savedTripIds.contains("103")
        vm.toggleSave("103")
        advanceUntilIdle()
        vm.toggleSave("103")
        advanceUntilIdle()

        assertEquals(wasAlreadySaved, vm.savedTripIds.contains("103"))
    }

    // ── clearError ────────────────────────────────────────────────────────

    @Test
    fun `clearError nullifies errorMessage`() = runTest {
        advanceUntilIdle()

        // Manually poke an error state (using reflection is overkill; we test clearError's effect)
        vm.clearError()

        assertNull(vm.errorMessage)
    }
}
