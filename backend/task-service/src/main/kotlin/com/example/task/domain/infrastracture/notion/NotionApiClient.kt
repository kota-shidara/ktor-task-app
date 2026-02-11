package com.example.com.example.task.domain.infrastracture.notion

import com.example.com.example.task.domain.model.Task
import com.example.com.example.task.domain.model.TaskPriority
import com.example.com.example.task.domain.model.TaskStatus
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.io.Closeable

private const val NOTION_RICH_TEXT_MAX_LENGTH = 2000

class NotionApiClient(
    private val apiKey: String,
    private val pageId: String
) : NotionExporter, Closeable {

    private val logger = LoggerFactory.getLogger(NotionApiClient::class.java)
    private val client = HttpClient(CIO)
    private val notionVersion = "2025-09-03"

    override suspend fun exportTasks(tasks: List<Task>) {
        val blocks = buildBlocks(tasks)

        val response = client.patch("https://api.notion.com/v1/blocks/$pageId/children") {
            header("Authorization", "Bearer $apiKey")
            header("Notion-Version", notionVersion)
            contentType(ContentType.Application.Json)
            setBody(blocks.toString())
        }

        if (response.status.isSuccess()) {
            logger.info("Successfully exported ${tasks.size} tasks to Notion page $pageId")
        } else {
            val body = response.bodyAsText()
            logger.error("Failed to export tasks to Notion: ${response.status} - $body")
            error("Notion API returned ${response.status}: $body")
        }
    }

    override fun close() {
        client.close()
    }

    private fun buildBlocks(tasks: List<Task>): JsonObject {
        val children = buildJsonArray {
            add(buildHeadingBlock("未完了タスク一覧"))
            for (task in tasks) {
                add(buildTaskBlock(task))
            }
        }
        return buildJsonObject {
            put("children", children)
        }
    }

    private fun buildHeadingBlock(text: String): JsonObject = buildJsonObject {
        put("object", "block")
        put("type", "heading_2")
        put("heading_2", buildJsonObject {
            put("rich_text", buildJsonArray {
                add(buildJsonObject {
                    put("type", "text")
                    put("text", buildJsonObject {
                        put("content", text)
                    })
                })
            })
        })
    }

    private fun buildTaskBlock(task: Task): JsonObject {
        val statusLabel = when (task.status) {
            TaskStatus.PENDING -> "未着手"
            TaskStatus.IN_PROGRESS -> "進行中"
            TaskStatus.COMPLETED -> "完了"
        }
        val priorityLabel = when (task.priority) {
            TaskPriority.HIGH -> "高"
            TaskPriority.MEDIUM -> "中"
            TaskPriority.LOW -> "低"
        }
        val description = task.description?.let { " - $it" } ?: ""
        val content = "[$statusLabel][優先度:$priorityLabel] ${task.title}$description"
        val truncatedContent = content.take(NOTION_RICH_TEXT_MAX_LENGTH)

        return buildJsonObject {
            put("object", "block")
            put("type", "bulleted_list_item")
            put("bulleted_list_item", buildJsonObject {
                put("rich_text", buildJsonArray {
                    add(buildJsonObject {
                        put("type", "text")
                        put("text", buildJsonObject {
                            put("content", truncatedContent)
                        })
                    })
                })
            })
        }
    }
}
