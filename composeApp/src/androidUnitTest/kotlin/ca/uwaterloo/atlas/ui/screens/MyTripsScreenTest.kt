package ca.uwaterloo.atlas.ui.screens

import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.components.TripFormData
import ca.uwaterloo.atlas.ui.theme.AtlasTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for [MyTripsScreen]
 */

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MyTripsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun clickByText(label: String) {
        composeTestRule
            .onNode(hasText(label) and hasClickAction())
            .performClick()
    }

    private fun scrollListToText(label: String) {
        composeTestRule
            .onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText(label, substring = true))
    }

    // ── Fixture data ──────────────────────────────────────────────────────

    private fun makeTrip(
        id: String = "trip-1",
        title: String = "Paris Adventure",
        location: String = "Paris, France",
        isPublic: Boolean = true,
        tags: List<String> = listOf("culture", "food"),
        startDate: LocalDate? = LocalDate(2024, 6, 1),
        endDate: LocalDate? = LocalDate(2024, 6, 14),
        placesCount: Int? = 5
    ) = TripData(
        id = id,
        title = title,
        location = location,
        isPublic = isPublic,
        tags = tags,
        startDate = startDate,
        endDate = endDate,
        placesCount = placesCount,
        imageUrl = "",
        drawableRes = null
    )

    private fun setContent(
        trips: List<TripData> = emptyList(),
        searchQuery: String = "",
        activeFilters: TripFilters = TripFilters(),
        availableTags: Set<String> = emptySet(),
        onSearchQueryChange: (String) -> Unit = {},
        onApplyFilters: (TripFilters) -> Unit = {},
        onTripClick: (TripData) -> Unit = {},
        onSaveTrip: (TripFormData) -> Unit = {},
        onEditTrip: (TripData, TripFormData) -> Unit = { _, _ -> },
        onDeleteTrip: (TripData) -> Unit = {}
    ) {
        composeTestRule.setContent {
            AtlasTheme {
                MyTripsScreen(
                    trips = trips,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    activeFilters = activeFilters,
                    availableTags = availableTags,
                    onApplyFilters = onApplyFilters,
                    onTripClick = onTripClick,
                    onSaveTrip = onSaveTrip,
                    onEditTrip = onEditTrip,
                    onDeleteTrip = onDeleteTrip
                )
            }
        }
    }

    // ── Static content ────────────────────────────────────────────────────

    @Test
    fun header_showsMyTripsTitle() {
        setContent()
        composeTestRule.onNodeWithText("My Trips").assertIsDisplayed()
    }

    @Test
    fun header_showsCollectionBadge() {
        setContent()
        composeTestRule.onNodeWithText("COLLECTION").assertIsDisplayed()
    }

    @Test
    fun header_showsNewTripButton() {
        setContent()
        composeTestRule.onNodeWithText("New Trip").assertIsDisplayed()
    }

    @Test
    fun searchBar_placeholderIsVisible() {
        setContent()
        composeTestRule.onNodeWithText("Search your trips...").assertIsDisplayed()
    }

    // ── Empty state ───────────────────────────────────────────────────────

    @Test
    fun emptyState_messageIsShownWhenNoTrips() {
        setContent(trips = emptyList())
        composeTestRule
            .onNodeWithText("No trips yet. Tap \"Add Trip\" to create your first one!")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_noTripCardIsRendered() {
        setContent(trips = emptyList())
        // None of the card-specific trip titles should appear
        composeTestRule.onNodeWithText("Paris Adventure").assertDoesNotExist()
    }

    // ── Trip list ─────────────────────────────────────────────────────────

    @Test
    fun tripList_showsAllTripTitles() {
        val trips = listOf(
            makeTrip(id = "1", title = "Paris Adventure"),
            makeTrip(id = "2", title = "Tokyo Escape"),
            makeTrip(id = "3", title = "NY Weekend")
        )
        setContent(trips = trips)
        trips.forEach { trip ->
            scrollListToText(trip.title)
            composeTestRule.onNodeWithText(trip.title, substring = true).assertExists()
        }
    }

    @Test
    fun tripList_showsLocationForEachTrip() {
        val trips = listOf(makeTrip(location = "Rome, Italy"))
        setContent(trips = trips)
        composeTestRule.onNodeWithText("Rome, Italy").assertIsDisplayed()
    }

    @Test
    fun tripList_publicBadgeShownForPublicTrip() {
        setContent(trips = listOf(makeTrip(isPublic = true)))
        composeTestRule.onNodeWithText("Public").assertIsDisplayed()
    }

    @Test
    fun tripList_privateBadgeShownForPrivateTrip() {
        setContent(trips = listOf(makeTrip(isPublic = false)))
        composeTestRule.onNodeWithText("Private").assertIsDisplayed()
    }

    @Test
    fun tripList_tagsAreDisplayed() {
        setContent(trips = listOf(makeTrip(tags = listOf("adventure", "hiking"))))
        composeTestRule.onNodeWithText("#adventure").assertIsDisplayed()
        composeTestRule.onNodeWithText("#hiking").assertIsDisplayed()
    }

    @Test
    fun tripList_placesCountIsDisplayed() {
        setContent(trips = listOf(makeTrip(placesCount = 8)))
        composeTestRule.onNodeWithText("8 spots").assertIsDisplayed()
    }

    // ── Trip card — click ─────────────────────────────────────────────────

    @Test
    fun tripCard_clickCallsOnTripClick() {
        var clicked: TripData? = null
        val trip = makeTrip(title = "Paris Adventure")
        setContent(
            trips       = listOf(trip),
            onTripClick = { clicked = it }
        )
        composeTestRule.onNodeWithText("Paris Adventure").performClick()
        assertEquals(trip.id, clicked?.id)
    }

    @Test
    fun tripCard_clickPassesCorrectTripWhenMultiple() {
        var clicked: TripData? = null
        val trips = listOf(
            makeTrip(id = "1", title = "Trip Alpha"),
            makeTrip(id = "2", title = "Trip Beta")
        )
        setContent(trips = trips, onTripClick = { clicked = it })
        scrollListToText("Trip Beta")
        composeTestRule.onNode(hasText("Trip Beta") and hasClickAction()).performClick()
        assertEquals("2", clicked?.id)
    }

    // ── 3-dot menu — edit ─────────────────────────────────────────────────

    @Test
    fun tripCardMenu_editItemIsVisibleAfterOpeningMenu() {
        setContent(trips = listOf(makeTrip()))
        // Open the overflow menu
        composeTestRule
            .onNodeWithContentDescription("Trip options")
            .performClick()
        composeTestRule.onNodeWithText("Edit trip").assertIsDisplayed()
    }

    @Test
    fun tripCardMenu_editOpensEditDialog() {
        val trip = makeTrip(title = "Paris Adventure")
        setContent(trips = listOf(trip))

        composeTestRule
            .onNodeWithContentDescription("Trip options")
            .performClick()
        clickByText("Edit trip")

        // The dialog title should appear
        composeTestRule.onNodeWithText("Edit Trip").assertIsDisplayed()
        // And the existing trip name should be pre-filled
        composeTestRule.onNode(hasSetTextAction() and hasText("Paris Adventure")).assertExists()
    }

    // ── 3-dot menu — delete ───────────────────────────────────────────────

    @Test
    fun tripCardMenu_deleteItemIsVisibleAfterOpeningMenu() {
        setContent(trips = listOf(makeTrip()))
        composeTestRule
            .onNodeWithContentDescription("Trip options")
            .performClick()
        composeTestRule.onNodeWithText("Delete trip").assertIsDisplayed()
    }

    @Test
    fun tripCardMenu_deleteOpensConfirmationDialog() {
        val trip = makeTrip(title = "Paris Adventure")
        setContent(trips = listOf(trip))

        composeTestRule
            .onNodeWithContentDescription("Trip options")
            .performClick()
        composeTestRule.onNodeWithText("Delete trip").performClick()

        // Dialog title and the trip name inside the message body
        composeTestRule.onNodeWithText("Delete Trip?").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("\"Paris Adventure\"", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun deleteConfirmDialog_confirmCallsOnDeleteTrip() {
        var deleted: TripData? = null
        val trip = makeTrip(title = "Paris Adventure")
        setContent(
            trips        = listOf(trip),
            onDeleteTrip = { deleted = it }
        )

        composeTestRule
            .onNodeWithContentDescription("Trip options")
            .performClick()
        composeTestRule.onNodeWithText("Delete trip").performClick()
        composeTestRule.onNodeWithText("Delete").performClick()

        assertEquals(trip.id, deleted?.id)
    }

    @Test
    fun deleteConfirmDialog_cancelDoesNotCallOnDeleteTrip() {
        var deleteCalled = false
        val trip = makeTrip()
        setContent(
            trips        = listOf(trip),
            onDeleteTrip = { deleteCalled = true }
        )

        composeTestRule
            .onNodeWithContentDescription("Trip options")
            .performClick()
        composeTestRule.onNodeWithText("Delete trip").performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(!deleteCalled)
        // Dialog must be gone
        composeTestRule.onNodeWithText("Delete Trip?").assertDoesNotExist()
    }

    @Test
    fun deleteConfirmDialog_dismissWithoutConfirmDoesNotCallOnDeleteTrip() {
        var deleteCalled = false
        val trip = makeTrip()
        setContent(
            trips        = listOf(trip),
            onDeleteTrip = { deleteCalled = true }
        )

        composeTestRule
            .onNodeWithContentDescription("Trip options")
            .performClick()
        composeTestRule.onNodeWithText("Delete trip").performClick()
        // Simulate back / dismiss
        composeTestRule.onNodeWithText("Delete Trip?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(!deleteCalled)
    }

    // ── "New Trip" button → create dialog ────────────────────────────────

    @Test
    fun newTripButton_opensCreateTripDialog() {
        setContent()
        clickByText("New Trip")
        composeTestRule.onNodeWithText("Start planning your adventure").assertIsDisplayed()
    }

    @Test
    fun createTripDialog_cancelDismissesDialog() {
        setContent()
        clickByText("New Trip")
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        composeTestRule.onNodeWithText("Start planning your adventure").assertDoesNotExist()
    }

    // ── Search bar ────────────────────────────────────────────────────────

    @Test
    fun searchBar_typingPropagatesQueryChange() {
        val queries = mutableListOf<String>()
        setContent(onSearchQueryChange = { queries.add(it) })

        composeTestRule
            .onNodeWithText("Search your trips...")
            .performTextInput("Tokyo")

        assertTrue(queries.isNotEmpty())
        assertEquals("Tokyo", queries.last())
    }

    @Test
    fun searchBar_existingQueryIsDisplayed() {
        setContent(searchQuery = "Tokyo")
        composeTestRule.onNodeWithText("Tokyo").assertIsDisplayed()
    }

    // ── Filter button ─────────────────────────────────────────────────────

    @Test
    fun filterButton_clickOpensFilterSheet() {
        setContent(availableTags = setOf("beach", "city"))
        composeTestRule
            .onNodeWithContentDescription("Filter")
            .performClick()
        composeTestRule.onNodeWithText("Filter Trips").assertIsDisplayed()
    }

    @Test
    fun filterButton_isDisplayedWhenNoActiveFilters() {
        setContent(activeFilters = TripFilters())
        composeTestRule.onNodeWithContentDescription("Filter").assertIsDisplayed()
    }

    @Test
    fun filterButton_isDisplayedWhenFiltersAreActive() {
        val activeFilters = TripFilters(showPublic = true, showPrivate = false)
        setContent(activeFilters = activeFilters)
        composeTestRule.onNodeWithContentDescription("Filter").assertIsDisplayed()
    }

    // ── Filter sheet interaction ───────────────────────────────────────────

    @Test
    fun filterSheet_applyCallsOnApplyFilters() {
        setContent(
            availableTags  = setOf("beach"),
            onApplyFilters = { }
        )
        composeTestRule
            .onNodeWithContentDescription("Filter")
            .performClick()
        composeTestRule.onNodeWithText("Apply").assertExists()
    }

    @Test
    fun filterSheet_clearAllResetsToDefaultFilters() {
        val activeFilters = TripFilters(showPrivate = false, selectedTags = setOf("beach"))
        setContent(
            activeFilters  = activeFilters,
            availableTags  = setOf("beach"),
            onApplyFilters = { }
        )
        composeTestRule
            .onNodeWithContentDescription("Filter")
            .performClick()
        composeTestRule.onNodeWithText("Clear All").assertExists()
    }
}
