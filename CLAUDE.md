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
