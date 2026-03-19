package com.example.com.example.task.domain.infrastracture.notion

import com.example.com.example.task.domain.model.Task

interface NotionExporter {
    suspend fun exportTasks(tasks: List<Task>)
}
