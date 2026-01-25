output "frontend_url" {
  value = google_cloud_run_v2_service.frontend.uri
}

output "frontend_load_balancer_ip" {
  value = google_compute_global_address.lb_ip.address
}

output "frontend_http_url" {
  value = "http://${google_compute_global_address.lb_ip.address}"
}

output "user_service_url" {
  value = google_cloud_run_v2_service.user.uri
}

output "task_service_url" {
  value = google_cloud_run_v2_service.task.uri
}
