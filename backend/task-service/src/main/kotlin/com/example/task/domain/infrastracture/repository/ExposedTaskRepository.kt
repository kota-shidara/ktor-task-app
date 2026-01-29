package com.example.com.example.task.domain.infrastracture.repository

import com.example.com.example.db.DatabaseFactory.dbQuery
import com.example.com.example.task.domain.infrastracture.persistence.TaskEntity
import com.example.com.example.task.domain.infrastracture.persistence.TaskTable
import com.example.com.example.task.domain.model.Task
import com.example.com.example.task.domain.repository.TaskRepository
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class ExposedTaskRepository : TaskRepository {
    override suspend fun findById(id: Int): Task? = dbQuery {
        TaskEntity.findById(id)?.toDomain()
    }

    override suspend fun findAllByUserId(userId: Int): List<Task> = dbQuery {
        TaskEntity.find {
            TaskTable.userId eq userId
        }
            .map { it.toDomain() }
    }

    override suspend fun create(task: Task): Task = dbQuery {
        TaskEntity.new {
            title = task.title
            description = task.description
            priority = task.priority
            status = task.status
            userId = task.userId
        }.toDomain()
    }

    override suspend fun update(task: Task): Boolean = dbQuery {
        val id = task.id ?: return@dbQuery false
        val entity = TaskEntity.findById(id) ?: return@dbQuery false
        entity.apply {
            title = task.title
            description = task.description
            priority = task.priority
            status = task.status
        }
        true
    }

    override suspend fun delete(id: Int): Boolean = dbQuery {
        val entity = TaskEntity.findById(id)
        entity?.delete()
        entity != null
    }

    override suspend fun deleteAllByUserId(userId: Int): Int = dbQuery {
        TaskTable.deleteWhere { TaskTable.userId eq userId }
    }

    private fun TaskEntity.toDomain() =
        Task(
            id = this.id.value,
            title = this.title,
            description = this.description,
            priority = this.priority,
            status = this.status,
            userId = this.userId
        )
}