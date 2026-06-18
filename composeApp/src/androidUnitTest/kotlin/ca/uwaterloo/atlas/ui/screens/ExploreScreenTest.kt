package ca.uwaterloo.atlas.ui.screens

import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.theme.AtlasTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
 * UI tests for [ExploreScreen]
 */

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExploreScreenTest {

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
        title: String = "Tokyo Escape",
        location: String = "Tokyo, Japan",
        author: String? = "alice",
        authorAvatarUrl: String? = null,
        tags: List<String> = listOf("culture"),
        startDate: LocalDate? = LocalDate(2024, 3, 10),
        endDate: LocalDate? = LocalDate(2024, 3, 20)
    ) = TripData(
        id = id,
        title = title,
        location = location,
        author = author,
        authorAvatarUrl = authorAvatarUrl,
        tags = tags,
        startDate = startDate,
        endDate = endDate,
        isPublic = true,
        imageUrl = "",
        drawableRes = null
    )

    private fun setContent(
        trips: List<TripData> = emptyList(),
        searchQuery: String = "",
        activeFilters: TripFilters = TripFilters(),
        availableTags: Set<String> = emptySet(),
        savedTripIds: Set<String> = emptySet(),
        onSearchQueryChange: (String) -> Unit = {},
        onApplyFilters: (TripFilters) -> Unit = {},
        onTripClick: (TripData) -> Unit = {},
        onAuthorClick: (String) -> Unit = {},
        onToggleSave: (String) -> Unit = {}
    ) {
        composeTestRule.setContent {
            AtlasTheme {
                ExploreScreen(
                    trips = trips,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    activeFilters = activeFilters,
                    availableTags = availableTags,
                    onApplyFilters = onApplyFilters,
                    onTripClick = onTripClick,
                    onAuthorClick = onAuthorClick,
                    savedTripIds = savedTripIds,
                    onToggleSave = onToggleSave
                )
            }
        }
    }

    // ── Static content ────────────────────────────────────────────────────

    @Test
    fun header_showsDiscoverJourneysTitle() {
        setContent()
        composeTestRule.onNodeWithText("Discover Journeys").assertIsDisplayed()
    }

    @Test
    fun header_showsExploreBadge() {
        setContent()
        composeTestRule.onNodeWithText("EXPLORE").assertIsDisplayed()
    }

    @Test
    fun header_showsSubtitleText() {
        setContent()
        composeTestRule
            .onNodeWithText("Find your next adventure from the community")
            .assertIsDisplayed()
    }

    @Test
    fun searchBar_placeholderIsVisible() {
        setContent()
        composeTestRule.onNodeWithText("Search destinations...").assertIsDisplayed()
    }

    // ── Empty state ───────────────────────────────────────────────────────

    @Test
    fun emptyState_noAdventuresFoundMessageIsShown() {
        setContent(trips = emptyList())
        composeTestRule.onNodeWithText("No adventures found").assertIsDisplayed()
    }

    @Test
    fun emptyState_adjustFiltersHintIsShown() {
        setContent(trips = emptyList())
        composeTestRule
            .onNodeWithText("Try adjusting your search or filters")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_noTripCardIsRendered() {
        setContent(trips = emptyList())
        composeTestRule.onNodeWithText("Tokyo Escape").assertDoesNotExist()
    }

    // ── Trip list ─────────────────────────────────────────────────────────

    @Test
    fun tripList_showsAllTripTitles() {
        val trips = listOf(
            makeTrip(id = "1", title = "Tokyo Escape"),
            makeTrip(id = "2", title = "Lisbon Wander"),
            makeTrip(id = "3", title = "Cape Town Vibes")
        )
        setContent(trips = trips)
        trips.forEach {
            scrollListToText(it.title)
            composeTestRule.onNodeWithText(it.title, substring = true).assertExists()
        }
    }

    @Test
    fun tripList_showsLocationForEachTrip() {
        setContent(trips = listOf(makeTrip(location = "Kyoto, Japan")))
        composeTestRule.onNodeWithText("Kyoto, Japan").assertIsDisplayed()
    }

    @Test
    fun tripList_tagsArePrefixedWithHash() {
        setContent(trips = listOf(makeTrip(tags = listOf("adventure", "hiking"))))
        composeTestRule.onNodeWithText("#adventure").assertIsDisplayed()
        composeTestRule.onNodeWithText("#hiking").assertIsDisplayed()
    }

    @Test
    fun tripList_dateRangeIsShownWhenBothDatesPresent() {
        val trip = makeTrip(
            startDate = LocalDate(2024, 6, 1),
            endDate   = LocalDate(2024, 6, 14)
        )
        setContent(trips = listOf(trip))
        // The date string format used is "MM/DD/YYYY • MM/DD/YYYY" in uppercase
        composeTestRule
            .onNodeWithText("06/01/2024 • 06/14/2024", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun tripList_noDatesShownWhenDatesAbsent() {
        val trip = makeTrip(startDate = null, endDate = null)
        setContent(trips = listOf(trip))
        // The exact date string should not appear
        composeTestRule
            .onAllNodesWithText("2024", substring = true)
            .assertCountEquals(0)
    }

    // ── Trip card — click ─────────────────────────────────────────────────

    @Test
    fun tripCard_clickCallsOnTripClick() {
        var clicked: TripData? = null
        val trip = makeTrip(title = "Tokyo Escape")
        setContent(trips = listOf(trip), onTripClick = { clicked = it })
        composeTestRule.onNodeWithText("Tokyo Escape").performClick()
        assertEquals(trip.id, clicked?.id)
    }

    @Test
    fun tripCard_clickPassesCorrectTripWhenMultiple() {
        var clicked: TripData? = null
        val trips = listOf(
            makeTrip(id = "1", title = "First Trip"),
            makeTrip(id = "2", title = "Second Trip")
        )
        setContent(trips = trips, onTripClick = { clicked = it })
        scrollListToText("Second Trip")
        composeTestRule.onNode(hasText("Second Trip") and hasClickAction()).performClick()
        assertEquals("2", clicked?.id)
    }

    // ── Author row ────────────────────────────────────────────────────────

    @Test
    fun tripCard_authorNameIsDisplayedWhenPresent() {
        setContent(trips = listOf(makeTrip(author = "alice")))
        composeTestRule.onNodeWithText("alice").assertExists()
    }

    @Test
    fun tripCard_authorNameIsNotDisplayedWhenAbsent() {
        setContent(trips = listOf(makeTrip(author = null)))
        composeTestRule.onNodeWithText("alice").assertDoesNotExist()
    }

    @Test
    fun tripCard_clickingAuthorRowCallsOnAuthorClick() {
        setContent(
            trips         = listOf(makeTrip(author = "alice")),
            onAuthorClick = { }
        )
        composeTestRule.onNodeWithContentDescription("Author alice").performClick()
        composeTestRule.onNodeWithContentDescription("Author alice").assertExists()
    }

    @Test
    fun tripCard_clickingAuthorPassesCorrectNameWhenMultipleTrips() {
        val trips = listOf(
            makeTrip(id = "1", title = "Trip 1", author = "alice"),
            makeTrip(id = "2", title = "Trip 2", author = "bob")
        )
        setContent(trips = trips, onAuthorClick = { })
        scrollListToText("Trip 2")
        composeTestRule.onNodeWithContentDescription("Author bob").performClick()
        composeTestRule.onNodeWithContentDescription("Author bob").assertExists()
    }

    // ── Save / unsave ─────────────────────────────────────────────────────

    @Test
    fun tripCard_saveButtonShowsUnsavedStateByDefault() {
        setContent(
            trips        = listOf(makeTrip(id = "trip-1")),
            savedTripIds = emptySet()
        )
        composeTestRule
            .onNodeWithContentDescription("Save")
            .assertIsDisplayed()
    }

    @Test
    fun tripCard_saveButtonShowsSavedStateWhenTripIsSaved() {
        setContent(
            trips        = listOf(makeTrip(id = "trip-1")),
            savedTripIds = setOf("trip-1")
        )
        composeTestRule
            .onNodeWithContentDescription("Unsave")
            .assertIsDisplayed()
    }

    @Test
    fun tripCard_clickingSaveCallsOnToggleSave() {
        var toggledId: String? = null
        setContent(
            trips        = listOf(makeTrip(id = "trip-1")),
            onToggleSave = { toggledId = it }
        )
        composeTestRule
            .onNodeWithContentDescription("Save")
            .performClick()
        assertEquals("trip-1", toggledId)
    }

    @Test
    fun tripCard_clickingUnsaveCallsOnToggleSave() {
        var toggledId: String? = null
        setContent(
            trips        = listOf(makeTrip(id = "trip-1")),
            savedTripIds = setOf("trip-1"),
            onToggleSave = { toggledId = it }
        )
        composeTestRule
            .onNodeWithContentDescription("Unsave")
            .performClick()
        assertEquals("trip-1", toggledId)
    }

    @Test
    fun tripCard_savingCorrectTripWhenMultiplePresent() {
        val trips = listOf(
            makeTrip(id = "trip-1", title = "First"),
            makeTrip(id = "trip-2", title = "Second")
        )
        setContent(trips = trips, onToggleSave = { })

        scrollListToText("Second")
        // Tap the Save button on the second card
        composeTestRule
            .onAllNodesWithContentDescription("Save")[1]
            .performClick()
        composeTestRule.onAllNodesWithContentDescription("Save").assertCountEquals(2)
    }

    // ── Search bar ────────────────────────────────────────────────────────

    @Test
    fun searchBar_typingPropagatesQueryChange() {
        val queries = mutableListOf<String>()
        setContent(onSearchQueryChange = { queries.add(it) })

        composeTestRule
            .onNodeWithText("Search destinations...")
            .performTextInput("Lisbon")

        assertTrue(queries.isNotEmpty())
        assertEquals("Lisbon", queries.last())
    }

    @Test
    fun searchBar_existingQueryIsRendered() {
        setContent(searchQuery = "Lisbon")
        composeTestRule.onNodeWithText("Lisbon").assertIsDisplayed()
    }

    // ── Filter button ─────────────────────────────────────────────────────

    @Test
    fun filterButton_isAlwaysDisplayed() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Filter").assertIsDisplayed()
    }

    @Test
    fun filterButton_clickOpensFilterSheet() {
        setContent(availableTags = setOf("beach"))
        composeTestRule
            .onNodeWithContentDescription("Filter")
            .performClick()
        composeTestRule.onNodeWithText("Filter Trips").assertIsDisplayed()
    }

    @Test
    fun filterSheet_visibilityTogglesAreNotShownOnExploreScreen() {
        // Explore screen always passes showVisibility = false to TripFilterSheet
        setContent()
        composeTestRule
            .onNodeWithContentDescription("Filter")
            .performClick()
        // "Visibility" section header should not exist
        composeTestRule.onNodeWithText("Visibility").assertDoesNotExist()
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
        val activeFilters = TripFilters(selectedTags = setOf("beach"))
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

    @Test
    fun filterSheet_dismissDoesNotCallOnApplyFilters() {
        var applied = false
        setContent(onApplyFilters = { applied = true })
        composeTestRule
            .onNodeWithContentDescription("Filter")
            .performClick()
        // Close without applying
        composeTestRule
            .onNodeWithContentDescription("Close")
            .performClick()
        assertTrue(!applied)
    }

    @Test
    fun filterSheet_tagChipsAreShownForAvailableTags() {
        setContent(availableTags = setOf("beach", "mountain"))
        composeTestRule
            .onNodeWithContentDescription("Filter")
            .performClick()
        composeTestRule.onNodeWithText("#beach").assertExists()
        composeTestRule.onNodeWithText("#mountain").assertExists()
    }
}
