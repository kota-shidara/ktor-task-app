resource "google_cloud_run_v2_service" "bff" {
  name     = local.run_services.bff.name
  location = var.region

  ingress = "INGRESS_TRAFFIC_INTERNAL_LOAD_BALANCER"

  template {
    service_account = google_service_account.bff.email

    containers {
      image = local.run_services.bff.image

      ports {
        container_port = 8080
      }

      env {
        name  = "USER_SERVICE_URL"
        value = google_cloud_run_v2_service.user.uri
      }

      env {
        name  = "TASK_SERVICE_URL"
        value = google_cloud_run_v2_service.task.uri
      }
    }

    scaling {
      min_instance_count = var.bff_min_instances
      max_instance_count = var.bff_max_instances      
    }
  }
}

resource "google_cloud_run_v2_service" "frontend" {
  name     = local.run_services.frontend.name
  location = var.region

  ingress = "INGRESS_TRAFFIC_ALL"

  template {
    service_account = google_service_account.frontend.email

    containers {
      image = local.run_services.frontend.image

      ports {
        container_port = 8080
      }
    }

    scaling {
      min_instance_count = var.frontend_min_instances
      max_instance_count = var.frontend_max_instances
    }
  }
}

resource "google_cloud_run_v2_service" "user" {
  name     = local.run_services.user.name
  location = var.region

  ingress = "INGRESS_TRAFFIC_INTERNAL_ONLY"

  template {
    service_account = google_service_account.user.email

    // プライベートIP宛（例えばCloud SQL）だけがvpcコネクタ経由になる。
    // 外部SasSのAPIを叩くなどパブリックIP宛の通信は、Cloud Runの通常の外向き経路（Googleのマネージド経路であり、自前でのNATの設定は不要）から外に出る
    // egress は「外向き通信」を意味します。ingressは逆で、外から入ってくる通信
    vpc_access {
      connector = google_vpc_access_connector.run.id
      egress    = "PRIVATE_RANGES_ONLY"
    }

    containers {
      image = local.run_services.user.image

      ports {
        container_port = 8090
      }

      env {
        name  = "STORAGE_JDBCURL"
        value = "jdbc:postgresql://${google_sql_database_instance.main.private_ip_address}:5432/${google_sql_database.user_db.name}"
      }

      env {
        name  = "STORAGE_DRIVER_CLASSNAME"
        value = "org.postgresql.Driver"
      }

      env {
        name  = "STORAGE_USER"
        value = google_sql_user.user_service.name
      }

      env {
        name = "STORAGE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.user_db_password.secret_id
            version = "latest"
          }
        }
      }
    }

    scaling {
      min_instance_count = var.backend_min_instances
      max_instance_count = var.backend_max_instances
    }
  }

  depends_on = [google_sql_database_instance.main]
}

resource "google_cloud_run_v2_service" "task" {
  name     = local.run_services.task.name
  location = var.region

  ingress = "INGRESS_TRAFFIC_INTERNAL_ONLY"

  template {
    service_account = google_service_account.task.email

    vpc_access {
      connector = google_vpc_access_connector.run.id
      egress    = "PRIVATE_RANGES_ONLY"
    }

    containers {
      image = local.run_services.task.image

      ports {
        container_port = 8091
      }

      env {
        name  = "STORAGE_JDBCURL"
        value = "jdbc:postgresql://${google_sql_database_instance.main.private_ip_address}:5432/${google_sql_database.task_db.name}"
      }

      env {
        name  = "STORAGE_DRIVER_CLASSNAME"
        value = "org.postgresql.Driver"
      }

      env {
        name  = "STORAGE_USER"
        value = google_sql_user.task_service.name
      }

      env {
        name = "STORAGE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.task_db_password.secret_id
            version = "latest"
          }
        }
      }
    }

    scaling {
      min_instance_count = var.backend_min_instances
      max_instance_count = var.backend_max_instances
    }
  }

  depends_on = [google_sql_database_instance.main]
}

# 以下は、Cloud Runを呼び出せる権限を付与している
resource "google_cloud_run_v2_service_iam_member" "bff_invoker" {
  name     = google_cloud_run_v2_service.bff.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.frontend.email}"
}

resource "google_cloud_run_v2_service_iam_member" "bff_invoker_lb" {
  name     = google_cloud_run_v2_service.bff.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "serviceAccount:service-${data.google_project.current.number}@gcp-sa-cloudloadbalancing.iam.gserviceaccount.com"
}

resource "google_cloud_run_v2_service_iam_member" "frontend_invoker" {
  name     = google_cloud_run_v2_service.frontend.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "serviceAccount:service-${data.google_project.current.number}@gcp-sa-cloudloadbalancing.iam.gserviceaccount.com"
}

resource "google_cloud_run_v2_service_iam_member" "user_invoker" {
  name     = google_cloud_run_v2_service.user.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.bff.email}"
}

resource "google_cloud_run_v2_service_iam_member" "task_invoker" {
  name     = google_cloud_run_v2_service.task.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.bff.email}"
}
