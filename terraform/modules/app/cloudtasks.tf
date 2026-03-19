# Cloud Tasks キュー（Notion出力の非同期ジョブ用）
resource "google_cloud_tasks_queue" "notion_export" {
  name     = "${local.name_prefix}-notion-export-queue"
  location = var.region
  project  = var.project_id

  rate_limits {
    max_dispatches_per_second = 1
  }

  retry_config {
    max_attempts = 3
  }

  depends_on = [google_project_service.services["cloudtasks.googleapis.com"]]
}

# Cloud Tasks がジョブ投入時に使用するサービスアカウント
resource "google_service_account" "cloud_tasks_invoker" {
  account_id   = "${local.name_prefix}-ct-invoker"
  display_name = "Cloud Tasks Invoker"
  project      = var.project_id
}

# task-service SA に Cloud Tasks キューへのエンキュー権限を付与
resource "google_project_iam_member" "task_service_cloudtasks_enqueuer" {
  project = var.project_id
  role    = "roles/cloudtasks.enqueuer"
  member  = "serviceAccount:${google_service_account.task.email}"
}

# task-service SA に自身の Cloud Run サービス URL を取得する権限を付与
# (起動時に Cloud Run Admin API で callback URL を自動解決するために必要)
resource "google_project_iam_member" "task_service_run_viewer" {
  project = var.project_id
  role    = "roles/run.viewer"
  member  = "serviceAccount:${google_service_account.task.email}"
}

# Cloud Tasks invoker SA に task-service Cloud Run の呼び出し権限を付与
resource "google_cloud_run_v2_service_iam_member" "task_invoker_cloudtasks" {
  name     = google_cloud_run_v2_service.task.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.cloud_tasks_invoker.email}"
}
