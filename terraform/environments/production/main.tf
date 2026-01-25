# モジュール呼び出し。これを参照するときに、module.appと書ける
module "app" {
  source = "../../modules/app"

  # こちら側のvar.project_idを、相手（modules/app側）のvariable "project_id"に渡す、という意味
  # 実際にそれぞれの値の定義元に飛べば確認できる
  project_id                 = var.project_id
  region                     = var.region
  environment                = var.environment
  bff_image                  = var.bff_image
  frontend_image             = var.frontend_image
  user_service_image         = var.user_service_image
  task_service_image         = var.task_service_image
  artifact_registry_repository_id = var.artifact_registry_repository_id
  cloudsql_tier              = var.cloudsql_tier
  cloudsql_version           = var.cloudsql_version
  cloudsql_disk_size_gb       = var.cloudsql_disk_size_gb
  enable_deletion_protection = var.enable_deletion_protection
  bff_min_instances          = var.bff_min_instances
  bff_max_instances          = var.bff_max_instances
  frontend_min_instances     = var.frontend_min_instances
  frontend_max_instances     = var.frontend_max_instances
  backend_min_instances      = var.backend_min_instances
  backend_max_instances      = var.backend_max_instances
}
