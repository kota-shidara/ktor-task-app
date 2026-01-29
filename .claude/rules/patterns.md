# 共通パターン

## API レスポンス形式

```typescript
interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
  meta?: {
    total: number
    page: number
    limit: number
  }
}
```

## カスタムフックパターン

```typescript
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value)

  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay)
    return () => clearTimeout(handler)
  }, [value, delay])

  return debouncedValue
}
```

## リポジトリパターン（TypeScript / フロントエンド・BFF）

```typescript
interface Repository<T> {
  findAll(filters?: Filters): Promise<T[]>
  findById(id: string): Promise<T | null>
  create(data: CreateDto): Promise<T>
  update(id: string, data: UpdateDto): Promise<T>
  delete(id: string): Promise<void>
}
```

## リポジトリパターン（Kotlin / Exposed ORM）

```kotlin
// Exposed DSL によるデータアクセス
object Tasks : Table("tasks") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val completed = bool("completed").default(false)
    val userId = integer("user_id")
    override val primaryKey = PrimaryKey(id)
}

// トランザクション内でクエリを実行
fun findByUserId(userId: Int): List<Task> = transaction {
    Tasks.selectAll().where { Tasks.userId eq userId }
        .map { it.toTask() }
}

fun create(dto: CreateTaskDto): Task = transaction {
    val id = Tasks.insert {
        it[title] = dto.title
        it[description] = dto.description
        it[userId] = dto.userId
    } get Tasks.id
    findById(id)!!
}
```

## Ktor ルーティングパターン

```kotlin
fun Route.taskRoutes(taskService: TaskService) {
    route("/api/tasks") {
        get {
            val tasks = taskService.findAll()
            call.respond(tasks)
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")
            val task = taskService.findById(id)
                ?: throw NotFoundException("Task not found")
            call.respond(task)
        }
        post {
            val dto = call.receive<CreateTaskDto>()
            val task = taskService.create(dto)
            call.respond(HttpStatusCode.Created, task)
        }
    }
}
```

## スケルトンプロジェクト

新機能を実装する場合:
1. 実績のあるスケルトンプロジェクトを検索する
2. 並列エージェントで選択肢を評価する:
   - セキュリティ評価
   - 拡張性分析
   - 適合度スコアリング
   - 実装計画の策定
3. 最適なものをクローンして基盤とする
4. 実績ある構造の中で反復開発する
