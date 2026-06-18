package ca.uwaterloo.atlas.domain

import ca.uwaterloo.atlas.domain.trip.TripAccessMode
import ca.uwaterloo.atlas.domain.trip.TripData
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TripDomainTypesTest {

    @Test
    fun `tripData supports required and optional fields`() {
        val start = LocalDate(2026, 4, 1)
        val end = LocalDate(2026, 4, 7)
        val trip = TripData(
            id = "t1",
            title = "Spring Trip",
            location = "Tokyo, Japan",
            imageUrl = "https://example.com/cover.jpg",
            startDate = start,
            endDate = end,
            placesCount = 6,
            isPublic = false,
            author = "alice",
            authorAvatarUrl = "https://example.com/alice.png",
            tags = listOf("culture", "food")
        )

        assertEquals("t1", trip.id)
        assertEquals(start, trip.startDate)
        assertEquals(end, trip.endDate)
        assertEquals(6, trip.placesCount)
        assertEquals("alice", trip.author)
        assertEquals(listOf("culture", "food"), trip.tags)
        assertNull(trip.drawableRes)
    }

    @Test
    fun `tripAccessMode includes edit and view only`() {
        assertTrue(TripAccessMode.entries.contains(TripAccessMode.EDIT))
        assertTrue(TripAccessMode.entries.contains(TripAccessMode.VIEW_ONLY))
        assertEquals(2, TripAccessMode.entries.size)
    }
}
