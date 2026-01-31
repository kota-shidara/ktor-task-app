resource "google_pubsub_topic" "user_registered" {
  name    = "user-registered"
  project = var.project_id

  message_retention_duration = "86400s" # 24時間

  depends_on = [google_project_service.services["pubsub.googleapis.com"]]
}

resource "google_pubsub_subscription" "task_service_user_registered" {
  name    = "task-service-user-registered-sub"
  project = var.project_id
  topic   = google_pubsub_topic.user_registered.id

  ack_deadline_seconds = 30

  # 処理失敗時のリトライ設定
  retry_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }

  # 未確認メッセージの保持期間（7日）
  message_retention_duration = "604800s"

  # サブスクリプションの有効期限（無期限）
  expiration_policy {
    ttl = ""
  }

  depends_on = [google_pubsub_topic.user_registered]
}

resource "google_pubsub_topic" "user_deleted" {
  name    = "user-deleted"
  project = var.project_id

  message_retention_duration = "86400s" # 24時間

  depends_on = [google_project_service.services["pubsub.googleapis.com"]]
}

resource "google_pubsub_subscription" "task_service_user_deleted" {
  name    = "task-service-user-deleted-sub"
  project = var.project_id
  topic   = google_pubsub_topic.user_deleted.id

  ack_deadline_seconds = 30

  # 処理失敗時のリトライ設定
  retry_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }

  # 未確認メッセージの保持期間（7日）
  message_retention_duration = "604800s"

  # サブスクリプションの有効期限（無期限）
  expiration_policy {
    ttl = ""
  }

  depends_on = [google_pubsub_topic.user_deleted]
}

# user-service にトピックへのパブリッシュ権限を付与
resource "google_pubsub_topic_iam_member" "user_registered_publisher" {
  project = var.project_id
  topic   = google_pubsub_topic.user_registered.name
  role    = "roles/pubsub.publisher"
  member  = "serviceAccount:${google_service_account.user.email}"
}

resource "google_pubsub_topic_iam_member" "user_deleted_publisher" {
  project = var.project_id
  topic   = google_pubsub_topic.user_deleted.name
  role    = "roles/pubsub.publisher"
  member  = "serviceAccount:${google_service_account.user.email}"
}

# task-service にサブスクリプションの購読権限を付与
resource "google_pubsub_subscription_iam_member" "task_user_registered_subscriber" {
  project      = var.project_id
  subscription = google_pubsub_subscription.task_service_user_registered.name
  role         = "roles/pubsub.subscriber"
  member       = "serviceAccount:${google_service_account.task.email}"
}

resource "google_pubsub_subscription_iam_member" "task_user_deleted_subscriber" {
  project      = var.project_id
  subscription = google_pubsub_subscription.task_service_user_deleted.name
  role         = "roles/pubsub.subscriber"
  member       = "serviceAccount:${google_service_account.task.email}"
}
