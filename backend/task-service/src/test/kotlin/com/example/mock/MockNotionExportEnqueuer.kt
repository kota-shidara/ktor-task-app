package com.example.mock

import com.example.com.example.task.domain.infrastracture.cloudtasks.NotionExportEnqueuer

class MockNotionExportEnqueuer : NotionExportEnqueuer {
    val enqueuedUserIds = mutableListOf<Int>()

    override fun enqueue(userId: Int) {
        enqueuedUserIds.add(userId)
    }
}
