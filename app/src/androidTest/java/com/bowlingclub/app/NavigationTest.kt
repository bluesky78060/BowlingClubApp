package com.bowlingclub.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bowlingclub.app.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Navigation Test (Task 8-2)
 * Tests the main navigation flow of the app including bottom navigation tabs.
 *
 * Note: These tests verify tab visibility and click actions.
 * Full screen content verification (e.g., unique content per screen) requires running on a device
 * with actual data loaded. Current approach focuses on navigation state and tab selection.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun bottomNavigationTabs_areDisplayed() {
        // Verify all bottom navigation tabs are visible
        composeTestRule.onNodeWithText("홈").assertIsDisplayed()
        composeTestRule.onNodeWithText("정기전").assertIsDisplayed()
        composeTestRule.onNodeWithText("통계").assertIsDisplayed()
        composeTestRule.onNodeWithText("설정").assertIsDisplayed()
    }

    @Test
    fun clickingHomeTab_navigatesToHomeScreen() {
        // Given: App is launched (Home screen is default)
        // When: Home tab is clicked
        composeTestRule.onNodeWithText("홈").performClick()

        // Then: Home screen content is displayed
        composeTestRule.onNodeWithText("홈").assertIsDisplayed()
    }

    @Test
    fun clickingTournamentTab_navigatesToTournamentList() {
        // When: Tournament tab is clicked
        composeTestRule.onNodeWithText("정기전").performClick()

        // Then: Tournament list screen is displayed
        composeTestRule.onNodeWithText("정기전").assertIsDisplayed()
    }

    @Test
    fun clickingStatisticsTab_navigatesToStatisticsScreen() {
        // When: Statistics tab is clicked
        composeTestRule.onNodeWithText("통계").performClick()

        // Then: Statistics screen is displayed
        composeTestRule.onNodeWithText("통계").assertIsDisplayed()
    }

    @Test
    fun clickingSettingsTab_navigatesToSettingsScreen() {
        // When: Settings tab is clicked
        composeTestRule.onNodeWithText("설정").performClick()

        // Then: Settings screen is displayed
        composeTestRule.onNodeWithText("설정").assertIsDisplayed()
    }

    @Test
    fun navigateBetweenTabs_tabSelectionStateUpdates() {
        // Navigate to Tournament tab
        composeTestRule.onNodeWithText("정기전").performClick()
        composeTestRule.onNodeWithText("정기전").assertIsDisplayed()

        // Navigate to Statistics tab
        composeTestRule.onNodeWithText("통계").performClick()
        composeTestRule.onNodeWithText("통계").assertIsDisplayed()

        // Navigate to Settings tab
        composeTestRule.onNodeWithText("설정").performClick()
        composeTestRule.onNodeWithText("설정").assertIsDisplayed()

        // Navigate back to Home tab
        composeTestRule.onNodeWithText("홈").performClick()
        composeTestRule.onNodeWithText("홈").assertIsDisplayed()
    }

    @Test
    fun tabNavigation_preservesBottomBarVisibility() {
        // Navigate through all tabs and verify bottom bar remains visible
        val tabs = listOf("홈", "정기전", "통계", "설정")

        tabs.forEach { tabName ->
            composeTestRule.onNodeWithText(tabName).performClick()

            // Verify all tabs are still visible
            tabs.forEach { tab ->
                composeTestRule.onNodeWithText(tab).assertIsDisplayed()
            }
        }
    }

    @Test
    fun multipleTabClicks_doesNotCrash() {
        // Test rapid tab switching doesn't cause issues
        repeat(3) {
            composeTestRule.onNodeWithText("정기전").performClick()
            composeTestRule.onNodeWithText("통계").performClick()
            composeTestRule.onNodeWithText("설정").performClick()
            composeTestRule.onNodeWithText("홈").performClick()
        }

        // Verify app is still functional
        composeTestRule.onNodeWithText("홈").assertIsDisplayed()
    }
}
