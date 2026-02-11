package com.example.mock

import com.example.com.example.task.domain.infrastracture.notion.NotionExporter
import com.example.com.example.task.domain.model.Task

class MockNotionExporter : NotionExporter {
    val exportedTasks = mutableListOf<List<Task>>()

    override suspend fun exportTasks(tasks: List<Task>) {
        exportedTasks.add(tasks)
    }
}
