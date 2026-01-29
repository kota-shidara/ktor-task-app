# プロジェクトガイドラインスキル

ktor-react-task-app のプロジェクト固有ガイドライン。

---

## 使用タイミング

このスキルはこのプロジェクトで作業する際に参照してください。プロジェクトスキルには以下が含まれます:
- アーキテクチャ概要
- ファイル構造
- コードパターン
- テスト要件
- デプロイワークフロー

---

## アーキテクチャ概要

**技術スタック:**
- **フロントエンド**: React 19 + TypeScript + Vite + Tailwind CSS
- **BFF**: Express.js + TypeScript + Node.js
- **バックエンド**: Kotlin + Ktor + Exposed ORM
- **データベース**: PostgreSQL 17（2インスタンス）
- **ビルド**: Gradle（backend）、npm（frontend/BFF）
- **JVM**: Java 21
- **デプロイ**: Cloud Run + Docker Compose

**サービス構成:**
```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (:5173)                        │
│  React 19 + TypeScript + Vite + Tailwind CSS                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        BFF (:3001)                           │
│  Express.js + TypeScript                                    │
│  X-User-Authorization ヘッダー処理                            │
│  Cloud Run 環境では Google IAM トークンを付与                   │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
┌──────────────────────┐        ┌──────────────────────┐
│ user-service (:8090) │        │ task-service (:8091) │
│ Kotlin + Ktor        │        │ Kotlin + Ktor        │
│ Exposed ORM          │        │ Exposed ORM          │
│ PostgreSQL (:5434)   │        │ PostgreSQL (:5435)   │
└──────────────────────┘        └──────────────────────┘
```

---

## ファイル構造

```
ktor-react-task-app/
├── frontend/                    # React フロントエンド
│   ├── src/
│   │   ├── components/          # React コンポーネント
│   │   ├── hooks/               # カスタムフック
│   │   ├── types/               # TypeScript 型定義
│   │   └── App.tsx              # メインアプリ
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
│
├── bff/                         # Backend For Frontend
│   ├── src/
│   │   └── index.ts             # Express サーバー + プロキシ設定
│   ├── package.json
│   └── tsconfig.json
│
├── backend/
│   ├── user-service/            # ユーザー管理サービス
│   │   ├── src/main/kotlin/     # Kotlin ソース
│   │   │   ├── Application.kt   # Ktor アプリケーション
│   │   │   ├── routes/          # ルートハンドラ
│   │   │   ├── models/          # データモデル
│   │   │   ├── services/        # ビジネスロジック
│   │   │   └── database/        # DB 設定・テーブル定義
│   │   ├── src/test/kotlin/     # テスト
│   │   └── build.gradle.kts     # Gradle 設定
│   │
│   └── task-service/            # タスク管理サービス
│       ├── src/main/kotlin/     # Kotlin ソース
│       │   ├── Application.kt
│       │   ├── routes/
│       │   ├── models/
│       │   ├── services/
│       │   └── database/
│       ├── src/test/kotlin/
│       └── build.gradle.kts
│
├── compose.yaml                 # Docker Compose (DB)
├── CLAUDE.md                    # Claude Code ガイダンス
└── .claude/                     # Claude Code 設定
```

---

## コードパターン

### API レスポンスフォーマット（Kotlin）

```kotlin
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

// 使用例
call.respond(ApiResponse(success = true, data = tasks))
call.respond(HttpStatusCode.BadRequest, ApiResponse<Nothing>(success = false, error = "Invalid input"))
```

### フロントエンド API 呼び出し（TypeScript）

```typescript
interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
}

async function fetchApi<T>(
  endpoint: string,
  options?: RequestInit
): Promise<ApiResponse<T>> {
  try {
    const response = await fetch(`/api${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'X-User-Authorization': getToken(),
        ...options?.headers,
      },
    })

    if (!response.ok) {
      return { success: false, error: `HTTP ${response.status}` }
    }

    return await response.json()
  } catch (error) {
    return { success: false, error: String(error) }
  }
}
```

### カスタムフック（React）

```typescript
import { useState, useCallback } from 'react'

interface UseApiState<T> {
  data: T | null
  loading: boolean
  error: string | null
}

export function useApi<T>(
  fetchFn: () => Promise<ApiResponse<T>>
) {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: false,
    error: null,
  })

  const execute = useCallback(async () => {
    setState(prev => ({ ...prev, loading: true, error: null }))

    const result = await fetchFn()

    if (result.success) {
      setState({ data: result.data!, loading: false, error: null })
    } else {
      setState({ data: null, loading: false, error: result.error! })
    }
  }, [fetchFn])

  return { ...state, execute }
}
```

---

## テスト要件

### バックエンド（Gradle + JUnit/Kotlin Test）

```bash
# テスト実行
cd backend/user-service && ./gradlew test
cd backend/task-service && ./gradlew test

# カバレッジ付きテスト
cd backend/user-service && ./gradlew test jacocoTestReport
```

**テスト構造:**
```kotlin
class TaskServiceTest {
    private val taskRepository = mockk<TaskRepository>()
    private val taskService = TaskService(taskRepository)

    @Test
    fun `should create task successfully`() {
        val dto = CreateTaskRequest(title = "Test Task")
        val expected = Task(id = 1, title = "Test Task", ...)

        every { taskRepository.create(dto, 1) } returns expected

        val result = taskService.create(dto, userId = 1)

        assertEquals(expected, result)
        verify { taskRepository.create(dto, 1) }
    }
}
```

### フロントエンド（npm）

```bash
# Lint チェック
cd frontend && npm run lint

# E2E テスト（Playwright）
cd frontend && npx playwright test
```

---

## ローカル開発の起動順序

```bash
# 1. DB 起動
docker compose up -d

# 2. バックエンドサービス起動
cd backend/user-service && ./gradlew run
cd backend/task-service && ./gradlew run

# 3. BFF 起動
cd bff && npm run dev

# 4. フロントエンド起動
cd frontend && npm run dev
```

## デプロイワークフロー

### デプロイ前チェックリスト

- [ ] すべてのテストがローカルで通過
- [ ] `./gradlew build` が成功（backend 両サービス）
- [ ] `npm run build` が成功（frontend, BFF）
- [ ] ハードコードされたシークレットがない
- [ ] 環境変数がドキュメント化されている
- [ ] データベースマイグレーションが準備済み

### デプロイコマンド

```bash
# Build and deploy
cd backend/user-service && ./gradlew build
cd backend/task-service && ./gradlew build
cd bff && npm run build
cd frontend && npm run build

# Cloud Run deploy
gcloud run deploy user-service --source backend/user-service
gcloud run deploy task-service --source backend/task-service
gcloud run deploy bff --source bff
gcloud run deploy frontend --source frontend
```

### 環境変数

```bash
# Backend (.env / application.conf)
STORAGE_JDBCURL=jdbc:postgresql://localhost:5434/user_db
STORAGE_USER=postgres
STORAGE_PASSWORD=postgres

# BFF (.env)
USER_SERVICE_URL=http://localhost:8090
TASK_SERVICE_URL=http://localhost:8091
PORT=3001

# Frontend (.env)
VITE_API_URL=http://localhost:3001
```

---

## 重要ルール

1. コード、コメント、ドキュメントに**絵文字を使わない**
2. **不変性** - オブジェクトや配列をミューテートしない
3. **TDD** - 実装前にテストを書く
4. **80%カバレッジ**を最低限達成
5. **多数の小さなファイル** - 通常200-400行、最大800行
6. 本番コードに**console.log禁止**
7. try/catchによる**適切なエラーハンドリング**
8. Zod（frontend/BFF）/ Ktor バリデーション（backend）による**入力バリデーション**

---

## 関連スキル

- `coding-standards/` - 一般的なコーディングベストプラクティス
- `backend-patterns/` - Ktor/Exposed パターン
- `frontend-patterns/` - React パターン
- `postgres-patterns/` - PostgreSQL パターン
- `tdd-workflow/` - テスト駆動開発手法
