output "frontend_http_url" {
  value = "http://${google_compute_global_address.lb_ip.address}"
}

output "bff_url" {
  value = google_cloud_run_v2_service.bff.uri
}

output "user_service_url" {
  value = google_cloud_run_v2_service.user.uri
}

output "task_service_url" {
  value = google_cloud_run_v2_service.task.uri
}
