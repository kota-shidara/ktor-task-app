locals {
  name_prefix = "ktor-task-app-${var.environment}"
  # 固定パターン: 1-25 chars, lowercase, start with a letter, and end with [a-z0-9].
  vpc_connector_name = "vpc-conn-${lower(var.environment)}"

  run_services = {
    bff = {
      name  = "${local.name_prefix}-bff"
      image = var.bff_image
    }
    frontend = {
      name  = "${local.name_prefix}-frontend"
      image = var.frontend_image
    }
    user = {
      name  = "${local.name_prefix}-user"
      image = var.user_service_image
    }
    task = {
      name  = "${local.name_prefix}-task"
      image = var.task_service_image
    }
  }
}
