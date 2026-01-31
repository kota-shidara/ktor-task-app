# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がこのリポジトリで作業する際のガイダンスを提供します。

## プロジェクト概要

マイクロサービスアーキテクチャのタスク管理アプリケーション。

| サービス | ポート | DB |
|---------|--------|-----|
| backend/user-service | 8090 | PostgreSQL (localhost:5434) |
| backend/task-service | 8091 | PostgreSQL (localhost:5435) |
| bff | 3001 | - |
| frontend | 5173 | - |

## ローカル開発の起動順序

```bash
# リポジトリルートで実行
docker compose up -d                          # DB起動 (compose.yaml)

cd backend/user-service && ./gradlew run      # ユーザーサービス
cd backend/task-service && ./gradlew run      # タスクサービス
cd bff && npm run dev                         # BFF
cd frontend && npm run dev                    # フロントエンド
```

## テスト実行

```bash
cd backend/user-service && ./gradlew test
cd backend/task-service && ./gradlew test
cd frontend && npm run lint
```

## サービス間通信

```
Frontend → BFF → Backend Services
```

- フロントエンドは`X-User-Authorization`ヘッダーでトークンを送信
- BFFは`/api/auth/*`をuser-service、`/api/*`をtask-serviceにプロキシ
- Cloud Run環境ではBFFがGoogle IAMトークンを付与

## 環境変数の設定パターン

本番環境で必要な設定値にはデフォルト値を設定しない。環境変数 → config ファイル → error() の順で解決する:

```kotlin
val value = System.getenv("ENV_VAR")
    ?: config.propertyOrNull("section.key")?.getString()
    ?: error("section.key is not configured. Set ENV_VAR env var or section.key in application.yaml")
```

- ローカル開発: application.yaml の値を使用
- Cloud Run: Terraform が設定した環境変数を使用
- どちらも未設定: 起動時に即座に失敗

## Gitブランチ命名規則

| プレフィックス | 用途 |
|---------------|------|
| `feature/` | 新機能追加 |
| `optimize/` | パフォーマンス最適化 |
| `fix/` | バグ修正 |
| `refactor/` | リファクタリング |

例: `feature/add-user-profile`, `fix/login-error`, `refactor/task-service-cleanup`
