package com.example.aito_dolistapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aito_dolistapp.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        clearDatabase()
    }

    @After
    fun tearDown() {
        clearDatabase()
    }

    private fun clearDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getDatabase(context)
        runBlocking {
            db.taskDao().deleteAllTasks()
        }
    }

    @Test
    fun testEmptyStateDisplayed() {
        // When app launches with no tasks, empty state should be displayed
        composeTestRule.onNodeWithText("No tasks yet").assertIsDisplayed()
    }

    @Test
    fun testAddTaskFlow() {
        // Verify initial empty state
        composeTestRule.onNodeWithText("No tasks yet").assertIsDisplayed()

        // Click FAB to add new task
        composeTestRule.onNode(hasContentDescription("Add Task")).performClick()

        // Verify we are in TaskDetailPane ("New Task")
        composeTestRule.onNodeWithText("New Task").assertIsDisplayed()

        // Enter title and description
        composeTestRule.onNodeWithText("Task Title").performTextInput("Buy Groceries")
        composeTestRule.onNodeWithText("Description").performTextInput("Milk, eggs, and bread")

        // Save task
        composeTestRule.onNode(hasContentDescription("Save Task")).performClick()

        // Verify we are back on TaskListPane and task is displayed
        composeTestRule.onNodeWithText("Buy Groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Milk, eggs, and bread").assertIsDisplayed()
    }

    @Test
    fun testToggleTaskCompletion() {
        // Add a task first
        composeTestRule.onNode(hasContentDescription("Add Task")).performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Workout")
        composeTestRule.onNodeWithText("Description").performTextInput("Pushups and running")
        composeTestRule.onNode(hasContentDescription("Save Task")).performClick()

        composeTestRule.onNodeWithText("Workout").assertIsDisplayed()

        // Click checkbox to toggle completion
        composeTestRule.onNode(isToggleable() and hasAnyAncestor(hasText("Workout")))
            .performClick()
    }

    @Test
    fun testDeleteTaskFlow() {
        // Add a task first
        composeTestRule.onNode(hasContentDescription("Add Task")).performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Temporary Task")
        composeTestRule.onNode(hasContentDescription("Save Task")).performClick()

        composeTestRule.onNodeWithText("Temporary Task").assertIsDisplayed()

        // Click delete button
        composeTestRule.onNode(hasContentDescription("Delete Task")).performClick()

        // Verify task is deleted and empty state returns
        composeTestRule.onNodeWithText("Temporary Task").assertDoesNotExist()
        composeTestRule.onNodeWithText("No tasks yet").assertIsDisplayed()
    }

    @Test
    fun testSearchFiltering() {
        // Add two tasks
        composeTestRule.onNode(hasContentDescription("Add Task")).performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Android Development")
        composeTestRule.onNode(hasContentDescription("Save Task")).performClick()

        composeTestRule.onNode(hasContentDescription("Add Task")).performClick()
        composeTestRule.onNodeWithText("Task Title").performTextInput("Read Book")
        composeTestRule.onNode(hasContentDescription("Save Task")).performClick()

        // Both should be displayed
        composeTestRule.onNodeWithText("Android Development").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read Book").assertIsDisplayed()

        // Type search query using hasSetTextAction() to avoid locator mismatch when placeholder disappears
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Android")

        // Filtered task should be displayed, other should not
        composeTestRule.onNodeWithText("Android Development").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read Book").assertDoesNotExist()

        // Replace search query with non-matching query using hasSetTextAction()
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("Nonexistent")

        // Verify "No matching tasks" is displayed
        composeTestRule.onNodeWithText("No matching tasks").assertIsDisplayed()
    }
}
