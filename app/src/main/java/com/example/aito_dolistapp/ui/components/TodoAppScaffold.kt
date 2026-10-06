package com.example.aito_dolistapp.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aito_dolistapp.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TodoAppScaffold(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val navigator = rememberListDetailPaneScaffoldNavigator<Long?>()
    val coroutineScope = rememberCoroutineScope()

    val currentTaskId = navigator.currentDestination?.contentKey

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        modifier = modifier.fillMaxSize(),
        listPane = {
            AnimatedPane {
                TaskListPane(
                    tasks = tasks,
                    filteredTasks = filteredTasks,
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                    selectedTaskId = currentTaskId,
                    onTaskSelected = { taskId ->
                        coroutineScope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, taskId)
                        }
                    },
                    onTaskToggle = { task ->
                        viewModel.toggleTaskCompletion(task)
                    },
                    onTaskDelete = { task ->
                        viewModel.deleteTask(task)
                        if (currentTaskId == task.id) {
                            coroutineScope.launch {
                                navigator.navigateBack()
                            }
                        }
                    },
                    onAddTask = {
                        coroutineScope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, -1L)
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                TaskDetailPane(
                    taskId = currentTaskId,
                    tasks = tasks,
                    onSaveTask = { title, description, existingTask ->
                        if (existingTask != null) {
                            viewModel.updateTask(
                                existingTask.copy(
                                    title = title,
                                    description = description
                                )
                            )
                        } else {
                            viewModel.addTask(title, description)
                        }
                    },
                    onDeleteTask = { task ->
                        viewModel.deleteTask(task)
                    },
                    onClose = {
                        coroutineScope.launch {
                            navigator.navigateBack()
                        }
                    }
                )
            }
        }
    )
}
