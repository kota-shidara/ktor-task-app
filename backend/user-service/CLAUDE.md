# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がuser-serviceで作業する際のガイダンスを提供します。

## 概要

ユーザー認証・管理機能を提供するバックエンドサービス（Ktor）。ポート8090で起動。

## 開発コマンド

```bash
./gradlew run         # ローカル起動 (port 8090)
./gradlew test        # テスト実行
./gradlew build       # JARビルド
```

## データベース接続

- DB: PostgreSQL (user_db)
- ホスト: localhost:5434
- ユーザー: user_service

## 環境変数

| 変数名 | 説明 | デフォルト |
|--------|------|-----------|
| `STORAGE_JDBCURL` | JDBC接続URL | `jdbc:postgresql://localhost:5434/user_db` |
| `STORAGE_USER` | DBユーザー | `user_service` |
| `STORAGE_PASSWORD` | DBパスワード | `password` |
