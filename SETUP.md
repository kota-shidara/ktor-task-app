# Setup Guide

## Prerequisites

- Docker & Docker Compose
- JDK 17+
- Node.js 18+

## 1. Database Setup

Start the Postgres databases for User Service and Task Service.

```bash
docker-compose up -d
```

This will start `user-db` on port 5432 and `task-db` on port 5433.

## 2. Backend Services

### User Service

Open a terminal:

```bash
cd backend/user-service
./gradlew run
```

Runs on `http://localhost:8080`.

### Task Service

Open another terminal:

```bash
cd backend/task-service
./gradlew run
```

Runs on `http://localhost:8081`.

## 3. BFF (Backend For Frontend)

Open another terminal:

```bash
cd bff
npm install
npm run dev
```

Runs on `http://localhost:3000`. Proxies requests to User and Task services.

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
