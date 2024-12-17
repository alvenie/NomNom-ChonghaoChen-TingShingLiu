package com.example.nomnom.pages

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.nomnom.HomeViewModel
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRandomRestaurantButton() {
        composeTestRule.setContent {
            SearchPage(navController = rememberNavController(), homeViewModel = HomeViewModel())
        }

        composeTestRule.onNodeWithText("Random Restaurant").performClick()

        // Assert that the RoulettePage is displayed
        composeTestRule.onNodeWithText("Selected:").assertIsDisplayed()
    }
}