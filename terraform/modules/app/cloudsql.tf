resource "google_sql_database_instance" "main" {
  name             = "${local.name_prefix}-db"
  database_version = var.cloudsql_version
  region           = var.region

  deletion_protection = var.enable_deletion_protection

  settings {
    tier              = var.cloudsql_tier
    edition           = "ENTERPRISE"
    disk_size         = var.cloudsql_disk_size_gb
    disk_autoresize   = true
    availability_type = "ZONAL"

    ip_configuration {
      ipv4_enabled    = false
      private_network = google_compute_network.main.id
    }
  }

  depends_on = [google_service_networking_connection.private_vpc_connection]
}

# resource "<リソースタイプ>" "<ローカル名>" {}
# リソースタイプ: terraformはこれを、googleプロバイダーが提供するsql_databaseリソース種別として認識する
# ローカル名: Google Cloud上のリソース名ではなく、terraformのコード内でこのリソースを参照する際に利用される
resource "google_sql_database" "user_db" {
  name     = "user_db"
  instance = google_sql_database_instance.main.name
}

resource "google_sql_database" "task_db" {
  name     = "task_db"
  instance = google_sql_database_instance.main.name
}

resource "random_password" "user_db" {
  length  = 20
  special = true
}

resource "random_password" "task_db" {
  length  = 20
  special = true
}

resource "google_sql_user" "user_service" {
  name     = "user_service"
  instance = google_sql_database_instance.main.name
  password = random_password.user_db.result
}

resource "google_sql_user" "task_service" {
  name     = "task_service"
  instance = google_sql_database_instance.main.name
  password = random_password.task_db.result
}
