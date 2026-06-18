package ca.uwaterloo.atlas.viewmodel

import ca.uwaterloo.atlas.data.repository.MockPlaceRepository
import ca.uwaterloo.atlas.domain.trip.TripAccessMode
import ca.uwaterloo.atlas.domain.place.CostLevel
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.PlaceModel
import ca.uwaterloo.atlas.domain.place.TimeOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class SinglePinViewModelTest {

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

    // create mocks

    private val samplePlace = Place(
        id = "1",
        tripId = "1",
        name = "test name",
        category = PlaceCategory.RESTAURANT,
        latitude = 12.34,
        longitude = 12.34,
        address = "test address",
        photos = listOf("test photo 1", "test photo 2"),
        thumbnailPhoto = "test thumbnailPhoto",
        notes = "test notes",
        dateVisited = LocalDate(2026, 3, 3),

        rating = 5f,
        mood = "😊",
        tags = listOf("test tag 1", "test tag 2"),
        costIndicator = CostLevel.MODERATE,
        costAmount = 20.0,
        timeOfDay = TimeOfDay.AFTERNOON,
        photoCaptions = mapOf(
            "test photo 1" to "test caption 1",
            "test photo 2" to "test caption 2"
        ),
        isFavorite = true
    )

    private fun testPlaceModel(): PlaceModel {
        return PlaceModel(repository = MockPlaceRepository(initialPlaces = listOf(samplePlace)))
    }

    private fun testSinglePinModel() = SinglePinViewModel(
        testPlaceModel(),
        accessMode = TripAccessMode.EDIT
    )

    // wrapper function to test that editing and cancelling reverts to the OG state
    // in each unit test, we define the update separately
    private fun editAndCancel(update: () -> Unit) {
        val vm = testSinglePinModel()

        val oldRating = vm.rating
        val oldPrice = vm.price
        val oldCategory = vm.selectedCategory
        val oldTime = vm.timeOfDay
        val oldFav = vm.isFavorite
        val oldMood = vm.selectedMood
        val oldTags = vm.tags.toList()

        vm.startEditing(vm.place!!.notes)
        update() // this will differ depending on the unit test
        vm.cancelEditing()

        // should be the same as before
        assertEquals(oldRating, vm.rating)
        assertEquals(oldPrice, vm.price)
        assertEquals(oldCategory, vm.selectedCategory)
        assertEquals(oldTime, vm.timeOfDay)
        assertEquals(oldFav, vm.isFavorite)
        assertEquals(oldMood, vm.selectedMood)
        assertEquals(oldTags, vm.tags)
        assertFalse(vm.isEditing)
    }

    @Test
    fun `loadPlace loads correctly`() = runVmTest {
        val vm = testSinglePinModel()

        vm.loadPlace("1")
        advanceUntilIdle()

        assertNotNull(vm.place)

        assertEquals("1", samplePlace.id)
        assertEquals("1", samplePlace.tripId)
        assertEquals("test name", samplePlace.name)
        assertEquals(PlaceCategory.RESTAURANT, samplePlace.category)
        assertEquals(12.34, samplePlace.latitude)
        assertEquals(12.34, samplePlace.longitude)
        assertEquals("test address", samplePlace.address)
        assertEquals(listOf("test photo 1", "test photo 2"), samplePlace.photos)
        assertEquals("test thumbnailPhoto", samplePlace.thumbnailPhoto)
        assertEquals("test notes", samplePlace.notes)
        assertEquals(LocalDate(2026, 3, 3), samplePlace.dateVisited)
        assertEquals(5f, samplePlace.rating)
        assertEquals("😊", samplePlace.mood)
        assertEquals(listOf("test tag 1", "test tag 2"), samplePlace.tags)
        assertEquals(CostLevel.MODERATE, samplePlace.costIndicator)
        assertEquals(20.0, samplePlace.costAmount)
        assertEquals(TimeOfDay.AFTERNOON, samplePlace.timeOfDay)
        assertEquals(
            mapOf("test photo 1" to "test caption 1", "test photo 2" to "test caption 2"),
            samplePlace.photoCaptions
        )
        assertTrue(samplePlace.isFavorite)
    }

    // UNIT TESTS: update, then save/cancel each field

    @Test
    fun `editing states are correct`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        vm.startEditing(vm.place!!.notes)
        assertEquals(vm.isEditing, true)
        vm.finishEditing()
        assertEquals(vm.isEditing, false)
    }

    @Test
    fun `save and update rating`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val newRating = 8f
        vm.startEditing(vm.place!!.notes)
        vm.updateRating(newRating)
        vm.finishEditing()
        assertEquals(newRating, vm.rating)
    }

    @Test
    fun `save and cancel rating`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val ogRating = vm.rating
        val newRating = 8f
        vm.startEditing(vm.place!!.notes)
        vm.updateRating(newRating)
        vm.cancelEditing()
        assertEquals(vm.rating, ogRating)
    }

    @Test
    fun `save and update price`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val newPrice = 3 // expensive
        vm.startEditing(vm.place!!.notes)
        vm.updatePrice(newPrice)
        vm.finishEditing()
        assertEquals(CostLevel.EXPENSIVE, vm.price)
    }

    @Test
    fun `save and cancel price`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val ogPrice = vm.price
        vm.startEditing(vm.place!!.notes)
        vm.updatePrice(1) // budget
        vm.cancelEditing()
        assertEquals(ogPrice, vm.price)
    }

    @Test
    fun `save and update category`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val newCategory = PlaceCategory.CAFE
        vm.startEditing(vm.place!!.notes)
        vm.updateCategory(newCategory)
        vm.finishEditing()
        assertEquals(newCategory, vm.selectedCategory)
    }

    @Test
    fun `save and cancel category`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val ogCategory = vm.selectedCategory
        vm.startEditing(vm.place!!.notes)
        vm.updateCategory(PlaceCategory.MUSEUM)
        vm.cancelEditing()
        assertEquals(ogCategory, vm.selectedCategory)
    }

    @Test
    fun `save and update timeOfDay`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val newTime = TimeOfDay.EVENING
        vm.startEditing(vm.place!!.notes)
        vm.updateTimeOfDay(newTime)
        vm.finishEditing()
        assertEquals(newTime, vm.timeOfDay)
    }

    @Test
    fun `save and cancel timeOfDay`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val ogTime = vm.timeOfDay
        vm.startEditing(vm.place!!.notes)
        vm.updateTimeOfDay(TimeOfDay.MORNING)
        vm.cancelEditing()
        assertEquals(ogTime, vm.timeOfDay)
    }

    @Test
    fun `save and update isFavorite`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        vm.startEditing(vm.place!!.notes)
        vm.updateFavorite(false)
        vm.finishEditing()
        assertFalse(vm.isFavorite)
    }

    @Test
    fun `save and cancel isFavorite`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val ogFav = vm.isFavorite
        vm.startEditing(vm.place!!.notes)
        vm.updateFavorite(false)
        vm.cancelEditing()
        assertEquals(ogFav, vm.isFavorite)
    }

    @Test
    fun `save and update tags`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val newTags = listOf("new1", "new2")
        vm.startEditing(vm.place!!.notes)
        vm.updateTags(newTags)
        vm.finishEditing()
        assertEquals(newTags, vm.tags)
    }

    @Test
    fun `save and cancel tags`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val ogTags = vm.tags.toList()
        vm.startEditing(vm.place!!.notes)
        vm.updateTags(listOf("new1"))
        vm.cancelEditing()
        assertEquals(ogTags, vm.tags)
    }

    @Test
    fun `save and update mood`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val newMood = "😎"
        vm.startEditing(vm.place!!.notes)
        vm.updateMood(newMood)
        vm.finishEditing()
        assertEquals(newMood, vm.selectedMood)
    }

    @Test
    fun `save and cancel mood`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val ogMood = vm.selectedMood
        vm.startEditing(vm.place!!.notes)
        vm.updateMood("😢")
        vm.cancelEditing()
        assertEquals(ogMood, vm.selectedMood)
    }

    @Test
    fun `save and update dateVisited`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val newDate = LocalDate(2026, 12, 31)
        vm.startEditing(vm.place!!.notes)
        vm.updateDateVisited(newDate)
        vm.finishEditing()
        assertEquals(newDate, vm.dateVisited)
    }

    @Test
    fun `save and cancel dateVisited`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val ogDate = vm.dateVisited
        vm.startEditing(vm.place!!.notes)
        vm.updateDateVisited(LocalDate(2026, 1, 1))
        vm.cancelEditing()
        assertEquals(ogDate, vm.dateVisited)
    }

    @Test
    fun `photos upload correctly`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()
        val newPhotos = listOf("newPhoto1")
        val newThumbnail = "newThumbnail"
        val newCaptions = mapOf(
            "newPhoto1" to "newCaption1",
        )

        vm.updatePhotos(newPhotos, newThumbnail, newCaptions)
        advanceUntilIdle()
        val updatedPlace = vm.place!!

        assertEquals(newPhotos, updatedPlace.photos)
        assertEquals("newPhoto1", updatedPlace.thumbnailPhoto)
        assertEquals(newCaptions, updatedPlace.photoCaptions)
    }

    @Test
    fun `updatePhotos caps to 10 photos`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()

        val photos = (1..12).map { "photo-$it" }
        val captions = photos.associateWith { "caption-$it" }

        vm.updatePhotos(
            photos = photos,
            thumbnail = "photo-1",
            captions = captions
        )
        advanceUntilIdle()

        val updated = vm.place!!
        assertEquals(10, updated.photos.size)
        assertEquals((1..10).map { "photo-$it" }, updated.photos)
    }

    @Test
    fun `updatePhotos keeps thumbnail and captions consistent after capping`() = runVmTest {
        val vm = testSinglePinModel()
        vm.loadPlace("1")
        advanceUntilIdle()

        val photos = (1..12).map { "photo-$it" }
        val captions = photos.associateWith { "caption-$it" }

        vm.updatePhotos(
            photos = photos,
            // Outside capped range -> should fallback to first kept photo.
            thumbnail = "photo-12",
            captions = captions
        )
        advanceUntilIdle()

        val updated = vm.place!!
        assertEquals("photo-1", updated.thumbnailPhoto)
        assertEquals(10, updated.photoCaptions.size)
        assertEquals((1..10).map { "photo-$it" }.toSet(), updated.photoCaptions.keys)
    }
}
