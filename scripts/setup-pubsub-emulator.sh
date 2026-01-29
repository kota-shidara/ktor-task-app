#!/bin/bash
set -euo pipefail

PUBSUB_EMULATOR_HOST="${PUBSUB_EMULATOR_HOST:-localhost:8085}"
PROJECT_ID="${GCP_PROJECT_ID:-local-project}"
TOPIC_ID="user-events"
SUBSCRIPTION_ID="task-service-user-events"

BASE_URL="http://${PUBSUB_EMULATOR_HOST}"

echo "Waiting for Pub/Sub emulator at ${PUBSUB_EMULATOR_HOST}..."
for i in $(seq 1 30); do
  if curl -s "${BASE_URL}" > /dev/null 2>&1; then
    echo "Pub/Sub emulator is ready."
    break
  fi
  if [ "$i" -eq 30 ]; then
    echo "Timeout: Pub/Sub emulator not available."
    exit 1
  fi
  sleep 1
done

echo "Creating topic: ${TOPIC_ID}"
curl -s -X PUT "${BASE_URL}/v1/projects/${PROJECT_ID}/topics/${TOPIC_ID}" \
  -H "Content-Type: application/json" -d '{}'
echo ""

echo "Creating subscription: ${SUBSCRIPTION_ID}"
curl -s -X PUT "${BASE_URL}/v1/projects/${PROJECT_ID}/subscriptions/${SUBSCRIPTION_ID}" \
  -H "Content-Type: application/json" \
  -d "{\"topic\": \"projects/${PROJECT_ID}/topics/${TOPIC_ID}\"}"
echo ""

echo "Pub/Sub emulator setup complete."
echo "  Topic: projects/${PROJECT_ID}/topics/${TOPIC_ID}"
echo "  Subscription: projects/${PROJECT_ID}/subscriptions/${SUBSCRIPTION_ID}"
