package ca.uwaterloo.atlas.domain

import ca.uwaterloo.atlas.domain.credential.UserCredential
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class UserCredentialsTest {

    // very basic unit test for now, as per Piazza @55_f1
    @Test
    fun `user credentials created correctly`() = runTest {
        val testCredentials = UserCredential("testEmail", "testPassword")
        assertEquals(testCredentials.email, "testEmail")
        assertEquals(testCredentials.password, "testPassword")
    }
}