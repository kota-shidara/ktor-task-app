package com.example.com.example.task.domain.infrastracture.cloudtasks

interface NotionExportEnqueuer {
    fun enqueue(userId: Int)
}
