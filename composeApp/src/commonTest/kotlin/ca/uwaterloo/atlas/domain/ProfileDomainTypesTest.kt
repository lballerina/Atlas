package ca.uwaterloo.atlas.domain

import ca.uwaterloo.atlas.domain.profile.AgeRange
import ca.uwaterloo.atlas.domain.profile.Gender
import ca.uwaterloo.atlas.domain.profile.TravelStats
import ca.uwaterloo.atlas.domain.profile.TravelStyleTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileDomainTypesTest {

    @Test
    fun `gender labels are stable`() {
        assertEquals("Male", Gender.MALE.label)
        assertEquals("Non-binary", Gender.NON_BINARY.label)
    }

    @Test
    fun `ageRange labels are stable`() {
        assertEquals("Teens", AgeRange.TEENS.label)
        assertEquals("50+", AgeRange.FIFTIES_PLUS.label)
    }

    @Test
    fun `travelStyleTag labels are stable`() {
        assertEquals("Solo", TravelStyleTag.SOLO.label)
        assertEquals("Photography", TravelStyleTag.PHOTOGRAPHY.label)
    }

    @Test
    fun `travelStats keeps numeric counters`() {
        val stats = TravelStats(trips = 2, places = 8, countries = 3)
        assertEquals(2, stats.trips)
        assertEquals(8, stats.places)
        assertEquals(3, stats.countries)
    }

    @Test
    fun `profile enums expose non empty sets`() {
        assertTrue(Gender.entries.isNotEmpty())
        assertTrue(AgeRange.entries.isNotEmpty())
        assertTrue(TravelStyleTag.entries.isNotEmpty())
    }
}
