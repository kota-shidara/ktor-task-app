resource "google_service_account" "user" {
  account_id   = "${local.name_prefix}-user"
  display_name = "${local.name_prefix} User"
}

resource "google_service_account" "task" {
  account_id   = "${local.name_prefix}-task"
  display_name = "${local.name_prefix} Task"
}

resource "google_service_account" "bff" {
  account_id   = "${local.name_prefix}-bff"
  display_name = "${local.name_prefix} BFF"
}

resource "google_service_account" "frontend" {
  account_id   = "${local.name_prefix}-frontend"
  display_name = "${local.name_prefix} Frontend"
}
