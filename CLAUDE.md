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
docker compose up -d                          # DB + Pub/Subエミュレータ起動 (compose.yaml)
./scripts/setup-pubsub-emulator.sh            # Pub/Subトピック・サブスクリプション作成

# PUBSUB_EMULATOR_HOST を設定して各サービスを起動
PUBSUB_EMULATOR_HOST=localhost:8085 cd backend/user-service && ./gradlew run      # ユーザーサービス
PUBSUB_EMULATOR_HOST=localhost:8085 cd backend/task-service && ./gradlew run      # タスクサービス
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
- ユーザー削除時、User ServiceがPub/Subへ`user.deleted`イベントを発行し、Task Serviceが受信して該当ユーザーのタスクを全削除

## Pub/Sub 環境変数

| 変数 | サービス | ローカル | Cloud Run |
|------|---------|---------|-----------|
| `PUBSUB_EMULATOR_HOST` | 両方 | `localhost:8085` | (未設定) |
| `GCP_PROJECT_ID` | 両方 | `local-project` | Terraform設定 |
| `PUBSUB_TOPIC_USER_EVENTS` | user-service | `user-events` | Terraform設定 |
| `PUBSUB_SUBSCRIPTION_USER_EVENTS` | task-service | `task-service-user-events` | Terraform設定 |

## Gitブランチ命名規則

| プレフィックス | 用途 |
|---------------|------|
| `feature/` | 新機能追加 |
| `optimize/` | パフォーマンス最適化 |
| `fix/` | バグ修正 |
| `refactor/` | リファクタリング |

例: `feature/add-user-profile`, `fix/login-error`, `refactor/task-service-cleanup`
