package ca.uwaterloo.atlas.viewmodel

import ca.uwaterloo.atlas.data.repository.MockPlaceRepository
import ca.uwaterloo.atlas.data.repository.MockTripRepository
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.PlaceFilter
import ca.uwaterloo.atlas.domain.place.PlaceModel
import ca.uwaterloo.atlas.domain.place.PlaceSortType
import ca.uwaterloo.atlas.domain.trip.TripAccessMode
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.domain.trip.TripModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class SingleTripViewModelTest {

    private fun runVmTest(block: suspend TestScope.() -> Unit) =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            try {
                block()
            } finally {
                Dispatchers.resetMain()
            }
        }
    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private fun testTripModel(): TripModel {
        return TripModel(
            repository = MockTripRepository(
                initialTrips = listOf(
                    TripData(
                        id = "1",
                        title = "Test Trip",
                        location = "Test City",
                        imageUrl = "",
                        startDate = LocalDate(2025, 1, 1),
                        endDate = LocalDate(2025, 1, 2),
                        isPublic = true
                    )
                )
            )
        )
    }

    private fun testPlaceModel() =
        PlaceModel(repository = MockPlaceRepository(initialPlaces = emptyList()))

    private fun viewModel(
        placeModel: PlaceModel,
        tripModel: TripModel,
        accessMode: TripAccessMode = TripAccessMode.EDIT
    ) = SingleTripViewModel(placeModel, tripModel, accessMode)

    private fun samplePlace(
        id: String,
        name: String,
        rating: Float = 3f,
        favorite: Boolean = false,
        category: PlaceCategory = PlaceCategory.CAFE
    ) = Place(
        id = id,
        tripId = "1",
        name = name,
        category = category,
        latitude = 0.0,
        longitude = 0.0,
        address = "",
        notes = "",
        dateVisited = LocalDate(2025, 7, 1),
        rating = rating,
        isFavorite = favorite
    )

    // ─────────────────────────────────────────────
    // Load Trip
    // ─────────────────────────────────────────────

    @Test
    fun `loadTrip sets currentTrip`() = runVmTest {
        val vm = viewModel(testPlaceModel(), testTripModel())

        vm.loadTrip("1")
        advanceUntilIdle()

        assertNotNull(vm.currentTrip)
        assertEquals("1", vm.currentTrip!!.id)
    }

    // ─────────────────────────────────────────────
    // Reactive Place Loading
    // ─────────────────────────────────────────────

    @Test
    fun `places load reactively when trip is set`() = runVmTest {
        val placeModel = testPlaceModel()
        val vm = viewModel(placeModel, testTripModel())

        placeModel.addPlace(samplePlace("p1", "Cafe A"))
        placeModel.addPlace(samplePlace("p2", "Cafe B"))

        vm.loadTrip("1")
        advanceUntilIdle()

        assertEquals(2, vm.places.size)
    }

    // ─────────────────────────────────────────────
    // Search
    // ─────────────────────────────────────────────

    @Test
    fun `searchQuery filters by name`() = runVmTest {
        val placeModel = testPlaceModel()
        val vm = viewModel(placeModel, testTripModel())

        placeModel.addPlace(samplePlace("p1", "Eiffel Cafe"))
        placeModel.addPlace(samplePlace("p2", "Louvre Museum"))

        vm.loadTrip("1")
        advanceUntilIdle()

        vm.updateSearchQuery("Eiffel")
        advanceUntilIdle()

        assertEquals(1, vm.places.size)
        assertEquals("Eiffel Cafe", vm.places.first().name)
    }

    // ─────────────────────────────────────────────
    // Category Filter
    // ─────────────────────────────────────────────

    @Test
    fun `filter by category works`() = runVmTest {
        val placeModel = testPlaceModel()
        val vm = viewModel(placeModel, testTripModel())

        placeModel.addPlace(samplePlace("p1", "Cafe A", category = PlaceCategory.CAFE))
        placeModel.addPlace(samplePlace("p2", "Museum A", category = PlaceCategory.MUSEUM))

        vm.loadTrip("1")
        advanceUntilIdle()

        vm.updateFilter(
            PlaceFilter(selectedCategories = setOf(PlaceCategory.MUSEUM))
        )
        advanceUntilIdle()

        assertEquals(1, vm.places.size)
        assertEquals("Museum A", vm.places.first().name)
    }

    // ─────────────────────────────────────────────
    // Favorites Filter
    // ─────────────────────────────────────────────

    @Test
    fun `favoritesOnly filter works`() = runVmTest {
        val placeModel = testPlaceModel()
        val vm = viewModel(placeModel, testTripModel())

        placeModel.addPlace(samplePlace("p1", "Cafe A", favorite = true))
        placeModel.addPlace(samplePlace("p2", "Cafe B", favorite = false))

        vm.loadTrip("1")
        advanceUntilIdle()

        vm.updateFilter(PlaceFilter(favoritesOnly = true))
        advanceUntilIdle()

        assertEquals(1, vm.places.size)
        assertTrue(vm.places.first().isFavorite)
    }

    // ─────────────────────────────────────────────
    // Sorting
    // ─────────────────────────────────────────────

    @Test
    fun `sorting by rating orders descending`() = runVmTest {
        val placeModel = testPlaceModel()
        val vm = viewModel(placeModel, testTripModel())

        placeModel.addPlace(samplePlace("p1", "Low", rating = 2f))
        placeModel.addPlace(samplePlace("p2", "High", rating = 5f))

        vm.loadTrip("1")
        advanceUntilIdle()

        vm.updateSort(PlaceSortType.RATING)
        advanceUntilIdle()

        assertEquals("High", vm.places.first().name)
    }

    @Test
    fun `sorting alphabetical orders by name`() = runVmTest {
        val placeModel = testPlaceModel()
        val vm = viewModel(placeModel, testTripModel())

        placeModel.addPlace(samplePlace("p1", "Zoo"))
        placeModel.addPlace(samplePlace("p2", "Aquarium"))

        vm.loadTrip("1")
        advanceUntilIdle()

        vm.updateSort(PlaceSortType.ALPHABETICAL)
        advanceUntilIdle()

        assertEquals("Aquarium", vm.places.first().name)
    }

    // ─────────────────────────────────────────────
    // Reactive Mutation
    // ─────────────────────────────────────────────

    @Test
    fun `adding place after load updates automatically`() = runVmTest {
        val placeModel = testPlaceModel()
        val vm = viewModel(placeModel, testTripModel())

        vm.loadTrip("1")
        advanceUntilIdle()

        placeModel.addPlace(samplePlace("p1", "New Place"))
        advanceUntilIdle()

        assertTrue(vm.places.any { it.name == "New Place" })
    }

    @Test
    fun `deletePlace removes from list automatically`() = runVmTest {
        val placeModel = testPlaceModel()
        val vm = viewModel(placeModel, testTripModel())

        placeModel.addPlace(samplePlace("p1", "Delete Me"))

        vm.loadTrip("1")
        advanceUntilIdle()

        placeModel.deletePlace("p1")
        advanceUntilIdle()

        assertFalse(vm.places.any { it.id == "p1" })
    }
}