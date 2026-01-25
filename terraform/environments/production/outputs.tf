output "frontend_url" {
  value = module.app.frontend_url
}

output "frontend_load_balancer_ip" {
  value = module.app.frontend_load_balancer_ip
}

output "frontend_http_url" {
  value = module.app.frontend_http_url
}

output "user_service_url" {
  value = module.app.user_service_url
}

output "task_service_url" {
  value = module.app.task_service_url
}
