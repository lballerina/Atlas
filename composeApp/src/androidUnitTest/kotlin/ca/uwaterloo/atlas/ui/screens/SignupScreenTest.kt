package ca.uwaterloo.atlas.ui.screens

import androidx.activity.ComponentActivity
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.theme.AtlasTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import ca.uwaterloo.atlas.data.repository.MockUserCredentialRepository
import ca.uwaterloo.atlas.domain.credential.UserCredential
import ca.uwaterloo.atlas.domain.credential.UserCredentialModel
import ca.uwaterloo.atlas.viewmodel.LoginViewModel
import ca.uwaterloo.atlas.viewmodel.SignupViewModel
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
 * UI tests for [SignupScreen]
 */

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SignupScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // create mocks
    private val sampleUser = UserCredential(email = "testemail@test.com", password = "123456")

    private fun testUserCredentialModel(): UserCredentialModel {
        return UserCredentialModel(repository = MockUserCredentialRepository(initialUsers = listOf(sampleUser)))
    }

    private fun testSignupViewModel(): SignupViewModel {
        return SignupViewModel(model = testUserCredentialModel())
    }

    @Test
    fun `confirm text is showing`() {
        composeTestRule.setContent {
            SignupScreen(
                vm = testSignupViewModel(),
                onSwitchClick = { null }
            )
        }
        composeTestRule.onNodeWithText("ATLAS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create\nAccount").assertIsDisplayed()
    }

    // note: difficult to unit test the actual switch because the state is reset upon click
    // so we just test that it can be clicked as a unit test
    @Test
    fun `switch button is clickable`() {
        composeTestRule.setContent {
            SignupScreen(
                vm = testSignupViewModel(),
                onSwitchClick = { }
            )
        }

        // select the TAG and ensure it is clickable
        composeTestRule.onNodeWithTag("switchLogin").assertHasClickAction()
    }


    @Test
    fun `input fields are working`() {
        composeTestRule.setContent {
            SignupScreen(
                vm = testSignupViewModel(),
                onSwitchClick = { }
            )
        }

        // select the TAG and ensure it is inputtable
        composeTestRule.onAllNodesWithTag("signupInput").assertAll(hasSetTextAction())
    }
}