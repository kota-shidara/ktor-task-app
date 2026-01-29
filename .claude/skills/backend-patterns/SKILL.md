---
name: backend-patterns
description: バックエンドアーキテクチャパターン、API設計、データベース最適化。Kotlin + Ktor + Exposed ORM によるバックエンドサービスと、Express.js BFF のベストプラクティス。
---

# バックエンド開発パターン

マイクロサービスアーキテクチャのタスク管理アプリケーション向けバックエンドパターン。

## アーキテクチャ概要

```
Frontend (React) --> BFF (Express.js :3001) --> Backend Services
                                                  |-- user-service (Ktor :8090)
                                                  |-- task-service (Ktor :8091)
```

## Ktor ルーティングパターン

### RESTful API構造

```kotlin
// ✅ リソースベースのURL
fun Route.taskRoutes(taskService: TaskService) {
    route("/api/tasks") {
        get {                           // GET /api/tasks - 一覧取得
            val tasks = taskService.findAll()
            call.respond(tasks)
        }
        get("/{id}") {                  // GET /api/tasks/:id - 単一取得
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")
            val task = taskService.findById(id)
                ?: throw NotFoundException("Task not found")
            call.respond(task)
        }
        post {                          // POST /api/tasks - 作成
            val dto = call.receive<CreateTaskRequest>()
            val task = taskService.create(dto)
            call.respond(HttpStatusCode.Created, task)
        }
        put("/{id}") {                 // PUT /api/tasks/:id - 更新
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")
            val dto = call.receive<UpdateTaskRequest>()
            val task = taskService.update(id, dto)
            call.respond(task)
        }
        delete("/{id}") {              // DELETE /api/tasks/:id - 削除
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")
            taskService.delete(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
```

### ルートの登録

```kotlin
fun Application.configureRouting() {
    val taskService = TaskService()

    routing {
        taskRoutes(taskService)
        healthRoutes()
    }
}
```

## Ktor プラグインパターン（ミドルウェア）

### ContentNegotiation（JSONシリアライゼーション）

```kotlin
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}
```

### StatusPages（エラーハンドリング）

```kotlin
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(error = cause.message ?: "Bad request")
            )
        }
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(error = cause.message ?: "Not found")
            )
        }
        exception<Exception> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(error = "Internal server error")
            )
        }
    }
}
```

### 認証ミドルウェア

```kotlin
fun Application.configureAuthentication() {
    install(Authentication) {
        bearer("auth-bearer") {
            authenticate { tokenCredential ->
                // トークンを検証してユーザー情報を返す
                val user = verifyToken(tokenCredential.token)
                if (user != null) UserIdPrincipal(user.id.toString()) else null
            }
        }
    }
}

// 認証が必要なルートで使用
routing {
    authenticate("auth-bearer") {
        taskRoutes(taskService)
    }
}
```

## Exposed ORM データアクセスパターン

### テーブル定義

```kotlin
object Tasks : Table("tasks") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val completed = bool("completed").default(false)
    val userId = integer("user_id").references(Users.id)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}
```

### リポジトリパターン

```kotlin
class TaskRepository {
    fun findAll(): List<Task> = transaction {
        Tasks.selectAll()
            .orderBy(Tasks.createdAt, SortOrder.DESC)
            .map { it.toTask() }
    }

    fun findById(id: Int): Task? = transaction {
        Tasks.selectAll()
            .where { Tasks.id eq id }
            .singleOrNull()
            ?.toTask()
    }

    fun findByUserId(userId: Int): List<Task> = transaction {
        Tasks.selectAll()
            .where { Tasks.userId eq userId }
            .orderBy(Tasks.createdAt, SortOrder.DESC)
            .map { it.toTask() }
    }

    fun create(dto: CreateTaskRequest, userId: Int): Task = transaction {
        val id = Tasks.insert {
            it[title] = dto.title
            it[description] = dto.description
            it[this.userId] = userId
        } get Tasks.id

        findById(id)!!
    }

    fun update(id: Int, dto: UpdateTaskRequest): Task = transaction {
        Tasks.update({ Tasks.id eq id }) {
            dto.title?.let { title -> it[Tasks.title] = title }
            dto.description?.let { desc -> it[Tasks.description] = desc }
            dto.completed?.let { completed -> it[Tasks.completed] = completed }
            it[updatedAt] = LocalDateTime.now()
        }
        findById(id)!!
    }

    fun delete(id: Int): Boolean = transaction {
        Tasks.deleteWhere { Tasks.id eq id } > 0
    }
}
```

### ResultRow からモデルへの変換

```kotlin
private fun ResultRow.toTask() = Task(
    id = this[Tasks.id],
    title = this[Tasks.title],
    description = this[Tasks.description],
    completed = this[Tasks.completed],
    userId = this[Tasks.userId],
    createdAt = this[Tasks.createdAt],
    updatedAt = this[Tasks.updatedAt]
)
```

## サービスレイヤーパターン

```kotlin
class TaskService(
    private val taskRepository: TaskRepository = TaskRepository()
) {
    fun findAll(): List<Task> = taskRepository.findAll()

    fun findById(id: Int): Task? = taskRepository.findById(id)

    fun findByUserId(userId: Int): List<Task> = taskRepository.findByUserId(userId)

    fun create(dto: CreateTaskRequest, userId: Int): Task {
        // ビジネスロジック（バリデーション等）
        require(dto.title.isNotBlank()) { "Title must not be blank" }
        return taskRepository.create(dto, userId)
    }

    fun update(id: Int, dto: UpdateTaskRequest): Task {
        val existing = taskRepository.findById(id)
            ?: throw NotFoundException("Task not found: $id")
        return taskRepository.update(id, dto)
    }

    fun delete(id: Int) {
        val deleted = taskRepository.delete(id)
        if (!deleted) throw NotFoundException("Task not found: $id")
    }
}
```

## データモデルパターン

### Kotlinx Serialization

```kotlin
@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String? = null,
    val completed: Boolean = false,
    val userId: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String? = null
)

@Serializable
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val completed: Boolean? = null
)

@Serializable
data class ErrorResponse(
    val error: String
)
```

## データベース接続パターン

### HikariCP + Exposed

```kotlin
fun Application.configureDatabase() {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = environment.config.property("storage.jdbcURL").getString()
        driverClassName = "org.postgresql.Driver"
        username = environment.config.propertyOrNull("storage.user")?.getString() ?: "postgres"
        password = environment.config.propertyOrNull("storage.password")?.getString() ?: ""
        maximumPoolSize = 10
        minimumIdle = 2
        idleTimeout = 600000
        connectionTimeout = 30000
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    // テーブル作成（開発環境用）
    transaction {
        SchemaUtils.create(Users, Tasks)
    }
}
```

## BFF プロキシパターン（Express.js）

### プロキシルーティング

```typescript
// BFF は Frontend からのリクエストを Backend サービスにプロキシ
const USER_SERVICE_URL = process.env.USER_SERVICE_URL || 'http://localhost:8090'
const TASK_SERVICE_URL = process.env.TASK_SERVICE_URL || 'http://localhost:8091'

// 認証リクエストを user-service にプロキシ
app.use('/api/auth', createProxyMiddleware({
  target: USER_SERVICE_URL,
  changeOrigin: true
}))

// タスクリクエストを task-service にプロキシ
app.use('/api/tasks', createProxyMiddleware({
  target: TASK_SERVICE_URL,
  changeOrigin: true
}))
```

### X-User-Authorization ヘッダー処理

```typescript
// Frontend は X-User-Authorization ヘッダーでトークンを送信
app.use((req, res, next) => {
  const userToken = req.headers['x-user-authorization']
  if (userToken) {
    // Backend サービスへの転送時に Authorization ヘッダーに変換
    req.headers['authorization'] = `Bearer ${userToken}`
  }
  next()
})
```

## N+1クエリの防止

```kotlin
// ❌ 悪い例: N+1クエリ問題
val tasks = transaction {
    Tasks.selectAll().map { row ->
        val user = Users.selectAll()
            .where { Users.id eq row[Tasks.userId] }
            .single()  // Nクエリ発行!
        TaskWithUser(row.toTask(), user.toUser())
    }
}

// ✅ 良い例: JOINを使用
val tasks = transaction {
    (Tasks innerJoin Users)
        .selectAll()
        .map { row ->
            TaskWithUser(
                task = row.toTask(),
                user = row.toUser()
            )
        }
}

// ✅ 良い例: バッチ取得
val tasks = transaction {
    val taskRows = Tasks.selectAll().toList()
    val userIds = taskRows.map { it[Tasks.userId] }.distinct()
    val users = Users.selectAll()
        .where { Users.id inList userIds }
        .associate { it[Users.id] to it.toUser() }

    taskRows.map { row ->
        TaskWithUser(
            task = row.toTask(),
            user = users[row[Tasks.userId]]!!
        )
    }
}
```

## エラーハンドリングパターン

### カスタム例外

```kotlin
class TaskNotFoundException(id: Int) : NotFoundException("Task not found: $id")
class UnauthorizedException(message: String) : Exception(message)
class ForbiddenException(message: String) : Exception(message)
```

### 統一的なエラーレスポンス

```kotlin
// StatusPages プラグインで一元管理
install(StatusPages) {
    exception<NotFoundException> { call, cause ->
        call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message ?: "Not found"))
    }
    exception<UnauthorizedException> { call, cause ->
        call.respond(HttpStatusCode.Unauthorized, ErrorResponse(cause.message ?: "Unauthorized"))
    }
    exception<IllegalArgumentException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Bad request"))
    }
}
```

**重要**: バックエンドパターンはスケーラブルで保守性の高いサービスを実現します。Ktor のプラグインシステムと Exposed のトランザクション管理を適切に活用してください。
