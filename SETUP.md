# Setup Guide

## Prerequisites

- Docker & Docker Compose
- JDK 17+
- Node.js 18+

## 1. Infrastructure Setup

Start the Postgres databases and Pub/Sub emulator.

```bash
docker compose up -d
```

This will start:
- `task-app-user-db` (PostgreSQL) on port 5434
- `task-app-task-db` (PostgreSQL) on port 5435
- `task-app-pubsub-emulator` (Google Cloud Pub/Sub emulator) on port 8085

## 2. Backend Services

Both backend services require the `PUBSUB_EMULATOR_HOST` environment variable to connect to the local Pub/Sub emulator.

### User Service

Open a terminal:

```bash
cd backend/user-service
PUBSUB_EMULATOR_HOST=localhost:8085 ./gradlew run
```

Runs on `http://localhost:8090`.

### Task Service

Open another terminal:

```bash
cd backend/task-service
PUBSUB_EMULATOR_HOST=localhost:8085 ./gradlew run
```

Runs on `http://localhost:8091`.

> **Note:** `PUBSUB_EMULATOR_HOST` を省略すると Pub/Sub のトピック/サブスクリプション自動作成がスキップされ、ユーザー退会時のイベント連携が動作しません。

## 3. BFF (Backend For Frontend)

Open another terminal:

```bash
cd bff
npm install
npm run dev
```

Runs on `http://localhost:3001`. Proxies requests to User and Task services.

## 4. Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173` (or similar).

## Usage

1. Go to Frontend URL.
2. Register a user (e.g. `testuser`).
3. Login with `testuser`.
4. Create tasks, update status, delete tasks.
5. Delete your account from the dashboard (triggers Pub/Sub event to clean up related tasks).

# terraform削除時
- VPCネットワークピアリングは、手動で削除する必要がある（terraform destroyを繰り返してもエラーとなる）