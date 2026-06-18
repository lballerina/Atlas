package ca.uwaterloo.atlas.domain

import ca.uwaterloo.atlas.domain.profile.AgeRange
import ca.uwaterloo.atlas.domain.profile.Gender
import ca.uwaterloo.atlas.domain.profile.TravelStats
import ca.uwaterloo.atlas.domain.profile.TravelStyleTag
import ca.uwaterloo.atlas.domain.profile.UserProfile
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class UserProfileTest {

    // very basic unit test for now, as per Piazza @55_f1
    @Test
    fun `user profile created correctly`() = runTest {
        val testProfile = UserProfile(
            displayName = "Test User",
            avatarUrl = null,
            email = "test@example.com",
            bio = "Test Bio",
            gender = Gender.MALE,
            ageRange = AgeRange.TEENS,
            tags = setOf(TravelStyleTag.SOLO),
            stats = TravelStats(
                3, 3, 3
            )
        )

        assertEquals("Test User", testProfile.displayName)
        assertEquals("test@example.com", testProfile.email)
        assertEquals("Test Bio", testProfile.bio)
        assertEquals(Gender.MALE, testProfile.gender)
        assertEquals(AgeRange.TEENS, testProfile.ageRange)
        assertEquals(setOf(TravelStyleTag.SOLO), testProfile.tags)
        assertEquals(3, testProfile.stats.trips)
        assertEquals(3, testProfile.stats.places)
        assertEquals(3, testProfile.stats.countries)
    }
}