package com.example.aito_dolistapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aito_dolistapp.data.Task
import com.example.aito_dolistapp.ui.theme.AIToDoListAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailPane(
    taskId: Long?,
    tasks: List<Task>,
    onSaveTask: (title: String, description: String, existingTask: Task?) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val existingTask = remember(taskId, tasks) {
        if (taskId != null && taskId > 0L) tasks.find { it.id == taskId } else null
    }

    var title by remember(existingTask) { mutableStateOf(existingTask?.title ?: "") }
    var description by remember(existingTask) { mutableStateOf(existingTask?.description ?: "") }

    LaunchedEffect(taskId, existingTask) {
        if (taskId != null && taskId > 0L && existingTask == null) {
            onClose()
        }
    }

    val isNew = taskId == null || taskId == -1L

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back / Close"
                        )
                    }
                },
                actions = {
                    if (!isNew && existingTask != null) {
                        IconButton(onClick = {
                            onDeleteTask(existingTask)
                            onClose()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete Task",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSaveTask(title, description, existingTask)
                                onClose()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = "Save Task"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = MaterialTheme.shapes.large
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSaveTask(title, description, existingTask)
                            onClose()
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(imageVector = Icons.Rounded.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDetailPanePreview() {
    val sampleTasks = listOf(
        Task(id = 1L, title = "Design UI Layout", description = "Create adaptive two-pane scaffold", isCompleted = false)
    )
    AIToDoListAppTheme {
        TaskDetailPane(
            taskId = 1L,
            tasks = sampleTasks,
            onSaveTask = { _, _, _ -> },
            onDeleteTask = {},
            onClose = {}
        )
    }
}
