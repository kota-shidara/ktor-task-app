locals {
  name_prefix = "ktor-task-app-${var.environment}"

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
