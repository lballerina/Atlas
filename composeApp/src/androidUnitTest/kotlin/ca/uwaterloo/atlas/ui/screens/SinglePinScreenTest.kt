package ca.uwaterloo.atlas.ui.screens

import androidx.activity.ComponentActivity
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.theme.AtlasTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import ca.uwaterloo.atlas.data.repository.MockPlaceRepository
import ca.uwaterloo.atlas.data.repository.MockUserCredentialRepository
import ca.uwaterloo.atlas.domain.credential.UserCredential
import ca.uwaterloo.atlas.domain.credential.UserCredentialModel
import ca.uwaterloo.atlas.domain.place.CostLevel
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.PlaceModel
import ca.uwaterloo.atlas.domain.place.TimeOfDay
import ca.uwaterloo.atlas.domain.trip.TripAccessMode
import ca.uwaterloo.atlas.viewmodel.LoginViewModel
import ca.uwaterloo.atlas.viewmodel.SignupViewModel
import ca.uwaterloo.atlas.viewmodel.SinglePinViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.collections.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for [SinglePinScreen]
 */

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SinglePinScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // create mocks
    private val samplePlace = Place(
        id = "1",
        tripId = "1",
        name = "test name",
        category = PlaceCategory.RESTAURANT,
        latitude = 12.34,
        longitude = 12.34,
        address = "test address",
        photos = listOf("https://ulcxyvywffoxuafjjszo.supabase.co/storage/v1/object/public/photos/smallLogoTransparent.png"),
        thumbnailPhoto = "https://ulcxyvywffoxuafjjszo.supabase.co/storage/v1/object/public/photos/smallLogoTransparent.png",
        notes = "test notes",
        dateVisited = LocalDate(2026, 3, 3),

        rating = 5f,
        mood = "😊",
        tags = listOf("test tag 1", "test tag 2"),
        costIndicator = CostLevel.MODERATE,
        costAmount = 20.0,
        timeOfDay = TimeOfDay.AFTERNOON,
        photoCaptions = mapOf(
            "https://ulcxyvywffoxuafjjszo.supabase.co/storage/v1/object/public/photos/smallLogoTransparent.png" to "test caption 1"
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

    @Test
    fun `confirm text is showing`() {
        composeTestRule.setContent {
            SinglePinScreen(
                placeId = "1", // test
                vm = testSinglePinModel(),
                onBackClick = {},
                onDeletePin = {}
            )
        }
        composeTestRule.onNodeWithText("test name").assertIsDisplayed()
    }

    @Test
    fun `back button is clickable`() {
            composeTestRule.setContent {
                SinglePinScreen(
                    placeId = "1", // test
                    vm = testSinglePinModel(),
                    onBackClick = {},
                    onDeletePin = {}
                )
            }

        // select the TAG and ensure it is clickable
        composeTestRule.onNodeWithTag("backButton").assertHasClickAction()
    }

    @Test
    fun `edit button is clickable`() {
            composeTestRule.setContent {
                SinglePinScreen(
                    placeId = "1", // test
                    vm = testSinglePinModel(),
                    onBackClick = {},
                    onDeletePin = {}
                )
            }

        // select the TAG and ensure it is clickable
        composeTestRule.onNodeWithTag("editButton").assertHasClickAction()
    }
}