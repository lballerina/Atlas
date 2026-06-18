package ca.uwaterloo.atlas.domain

import ca.uwaterloo.atlas.domain.credential.UserCredential
import ca.uwaterloo.atlas.domain.credential.UserCredentialModel
import ca.uwaterloo.atlas.data.repository.MockUserCredentialRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for [ca.uwaterloo.atlas.domain.userCredential.UserCredentialModel].
 */
class UserCredentialRepositoryTest {

    // ── Fixture setup ─────────────────────────────────────────────────────

    /**
     * Provides a [ca.uwaterloo.atlas.domain.userCredential.UserCredentialModel]-like environment with controlled test data
     * rather than relying on MockDB so tests don't break when mock data changes.
     *
     */
    private val user1 = UserCredential(
        email = "testEmail",
        password = "testPassword",
    )

    private val user2 = UserCredential(
        email = "testEmail2",
        password = "testPassword2",
    )

    private val repository = MockUserCredentialRepository (initialUsers = listOf(user1, user2)) // USE MOCK DATA!!!

    private val model = UserCredentialModel(
        repository = repository,
    )

    @Test
    fun `login succeeds with correct vals`() = runTest { // need to suspend the test to wait for login to finish
        val result = model.login("testEmail", "testPassword")
        assertTrue(result)
    }

    @Test
    fun `login fails with wrong val`() = runTest {
        val result = model.login("testEmail", "wrongPassword")
        assertFalse(result)
    }

    @Test
    fun `signUp succeeds`() = runTest {
        val newUser = model.signUp("testEmail3", "testPassword3")

        // check values reflect correctly
        assertEquals("testEmail3", newUser.email)
        assertTrue(model.emailExists("testEmail3"))

        // check that the user is the one we just created
        val userId = model.getCurrentUserId()
        assertNotNull(userId)
        assertEquals("testEmail3", userId)
    }

    @Test
    fun `emailExists works`() = runTest {
        assertTrue(model.emailExists("testEmail"))
        assertFalse(model.emailExists("testEmail4"))
    }

}
