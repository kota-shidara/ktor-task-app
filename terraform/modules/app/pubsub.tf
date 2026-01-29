resource "google_pubsub_topic" "user_events" {
  name    = "${local.name_prefix}-user-events"
  project = var.project_id

  depends_on = [google_project_service.services]
}

resource "google_pubsub_subscription" "task_service_user_events" {
  name    = "${local.name_prefix}-task-service-user-events"
  topic   = google_pubsub_topic.user_events.id
  project = var.project_id

  ack_deadline_seconds = 20

  retry_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }

  depends_on = [google_pubsub_topic.user_events]
}

# User Service: publish to topic
resource "google_pubsub_topic_iam_member" "user_service_publisher" {
  topic   = google_pubsub_topic.user_events.id
  role    = "roles/pubsub.publisher"
  member  = "serviceAccount:${google_service_account.user.email}"
}

# Task Service: subscribe from subscription
resource "google_pubsub_subscription_iam_member" "task_service_subscriber" {
  subscription = google_pubsub_subscription.task_service_user_events.id
  role         = "roles/pubsub.subscriber"
  member       = "serviceAccount:${google_service_account.task.email}"
}
