# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がtask-serviceで作業する際のガイダンスを提供します。

## 概要

タスク管理機能を提供するバックエンドサービス（Ktor）。ポート8091で起動。

## 開発コマンド

```bash
./gradlew run         # ローカル起動 (port 8091)
./gradlew test        # テスト実行
./gradlew build       # JARビルド
```

## データベース接続

- DB: PostgreSQL (task_db)
- ホスト: localhost:5435
- ユーザー: task_service

## 環境変数

| 変数名 | 説明 | デフォルト |
|--------|------|-----------|
| `STORAGE_JDBCURL` | JDBC接続URL | `jdbc:postgresql://localhost:5435/task_db` |
| `STORAGE_USER` | DBユーザー | `task_service` |
| `STORAGE_PASSWORD` | DBパスワード | `password` |
| `PUBSUB_EMULATOR_HOST` | Pub/Subエミュレータホスト（ローカル用） | - |
| `GCP_PROJECT_ID` | GCPプロジェクトID | `local-project` (application.yaml) |
| `PUBSUB_SUBSCRIPTION_USER_EVENTS` | ユーザーイベント用サブスクリプション | `task-service-user-events` (application.yaml) |
