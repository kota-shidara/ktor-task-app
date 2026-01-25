resource "google_artifact_registry_repository" "docker" {
  repository_id = var.artifact_registry_repository_id
  location      = var.region
  format        = "DOCKER"
  description   = "Docker images for ktor-task-app-prod/staging"

  // APIの有効化は先に行う必要がある
  depends_on = [
    google_project_service.services
  ]
}
