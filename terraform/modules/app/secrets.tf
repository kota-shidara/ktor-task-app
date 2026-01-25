# 入れ物を作る
resource "google_secret_manager_secret" "user_db_password" {
  secret_id = "${local.name_prefix}-user-db-password"
  replication {
    auto {}
  }
}

# 実際の値を入れる
# Secret Managerは、値を更新すると、同じSecretの新しいバージョンんが増える。
# secret_dataはTerraformのstateに残りうる（平文で）。stateの保護が大切。
resource "google_secret_manager_secret_version" "user_db_password" {
  secret      = google_secret_manager_secret.user_db_password.id
  secret_data = random_password.user_db.result
}

resource "google_secret_manager_secret" "task_db_password" {
  secret_id = "${local.name_prefix}-task-db-password"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "task_db_password" {
  secret      = google_secret_manager_secret.task_db_password.id
  secret_data = random_password.task_db.result
}

resource "google_secret_manager_secret_iam_member" "run_secret_accessor_user_db_password" {
  secret_id = google_secret_manager_secret.user_db_password.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.user.email}"
}

resource "google_secret_manager_secret_iam_member" "run_secret_accessor_task_db_password" {
  secret_id = google_secret_manager_secret.task_db_password.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.task.email}"
}
