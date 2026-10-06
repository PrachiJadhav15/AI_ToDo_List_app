package com.example.aito_dolistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aito_dolistapp.data.AppDatabase
import com.example.aito_dolistapp.data.TaskRepository
import com.example.aito_dolistapp.ui.components.TodoAppScaffold
import com.example.aito_dolistapp.ui.theme.AIToDoListAppTheme
import com.example.aito_dolistapp.ui.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())

        setContent {
            AIToDoListAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewModel: TaskViewModel = viewModel(
                        factory = TaskViewModel.provideFactory(repository)
                    )
                    TodoAppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}
