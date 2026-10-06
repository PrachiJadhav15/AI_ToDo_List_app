package com.example.aito_dolistapp

import com.example.aito_dolistapp.data.Task
import com.example.aito_dolistapp.data.TaskDao
import com.example.aito_dolistapp.data.TaskRepository
import com.example.aito_dolistapp.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeTaskDao: FakeTaskDao
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: TaskViewModel

    class FakeTaskDao : TaskDao {
        private val _tasks = MutableStateFlow<List<Task>>(emptyList())
        private var nextId = 1L

        override fun getAllTasks(): Flow<List<Task>> = _tasks

        override suspend fun getTaskById(id: Long): Task? = _tasks.value.find { it.id == id }

        override fun getTaskByIdFlow(id: Long): Flow<Task?> = MutableStateFlow(null)

        override suspend fun insertTask(task: Task): Long {
            val newId = if (task.id == 0L) nextId++ else task.id
            val taskToInsert = task.copy(id = newId)
            _tasks.value = listOf(taskToInsert) + _tasks.value
            return newId
        }

        override suspend fun updateTask(task: Task) {
            _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
        }

        override suspend fun deleteTask(task: Task) {
            _tasks.value = _tasks.value.filter { it.id != task.id }
        }

        override suspend fun deleteTaskById(id: Long) {
            _tasks.value = _tasks.value.filter { it.id != id }
        }

        override suspend fun deleteAllTasks() {
            _tasks.value = emptyList()
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeTaskDao = FakeTaskDao()
        repository = TaskRepository(fakeTaskDao)
        viewModel = TaskViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAddTask() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.tasks.collect {} }
        
        viewModel.addTask("Test Task", "Test Description")
        testScheduler.advanceUntilIdle()

        val tasks = viewModel.tasks.value
        assertEquals(1, tasks.size)
        assertEquals("Test Task", tasks[0].title)
        assertEquals("Test Description", tasks[0].description)
        assertEquals(false, tasks[0].isCompleted)
        collectJob.cancel()
    }

    @Test
    fun testToggleTaskCompletion() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.tasks.collect {} }

        viewModel.addTask("Task 1", "Desc 1")
        testScheduler.advanceUntilIdle()

        val task = viewModel.tasks.value[0]
        assertEquals(false, task.isCompleted)

        viewModel.toggleTaskCompletion(task)
        testScheduler.advanceUntilIdle()

        val updatedTask = viewModel.tasks.value[0]
        assertEquals(true, updatedTask.isCompleted)
        collectJob.cancel()
    }

    @Test
    fun testDeleteTask() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.tasks.collect {} }

        viewModel.addTask("Task to delete", "Desc")
        testScheduler.advanceUntilIdle()
        assertEquals(1, viewModel.tasks.value.size)

        val task = viewModel.tasks.value[0]
        viewModel.deleteTask(task)
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.tasks.value.size)
        collectJob.cancel()
    }

    @Test
    fun testSelectTask() = runTest(testDispatcher) {
        assertNull(viewModel.selectedTask.value)

        val task = Task(id = 1L, title = "Selected", description = "Desc")
        viewModel.selectTask(task)

        assertNotNull(viewModel.selectedTask.value)
        assertEquals("Selected", viewModel.selectedTask.value?.title)

        viewModel.selectTask(null)
        assertNull(viewModel.selectedTask.value)
    }

    @Test
    fun testSearchQueryFiltering() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.filteredTasks.collect {} }

        viewModel.addTask("Buy Milk", "Groceries")
        viewModel.addTask("Code Review", "Work")
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.filteredTasks.value.size)

        viewModel.setSearchQuery("Milk")
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.filteredTasks.value.size)
        assertEquals("Buy Milk", viewModel.filteredTasks.value[0].title)
        collectJob.cancel()
    }
}
