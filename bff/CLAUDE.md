# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がBFF（Backend For Frontend）で作業する際のガイダンスを提供します。

## 概要

フロントエンドとバックエンドサービス間のプロキシ（Express.js + TypeScript）。ポート3001で起動。

## 開発コマンド

```bash
npm run dev           # 開発モード起動 (ts-node-dev)
npm run build         # TypeScriptコンパイル
npm start             # 本番実行
```

## 環境変数

| 変数名 | 説明 | デフォルト |
|--------|------|-----------|
| `PORT` | BFFのリッスンポート | `3001` |
| `USER_SERVICE_URL` | user-serviceのURL | `http://localhost:8090` |
| `TASK_SERVICE_URL` | task-serviceのURL | `http://localhost:8091` |

## プロキシルーティング

- `/api/auth/*` → user-service
- `/api/*` → task-service
