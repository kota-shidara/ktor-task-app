variable "project_id" {
  description = "GCP project ID"
  type        = string
}

variable "region" {
  description = "Primary region for Cloud Run and Cloud SQL"
  type        = string
  default     = "us-west1"
}

variable "environment" {
  description = "Environment name (e.g., prod)"
  type        = string
  default     = "prod"
}


variable "bff_image" {
  description = "Container image for BFF (e.g., gcr.io/PROJECT/bff:tag)"
  type        = string
}

variable "user_service_image" {
  description = "Container image for user-service"
  type        = string
}

variable "task_service_image" {
  description = "Container image for task-service"
  type        = string
}

variable "frontend_image" {
  description = "Container image for frontend"
  type        = string
}

variable "cloudsql_tier" {
  description = "Cloud SQL instance tier"
  type        = string
  default     = "db-f1-micro"
}

variable "cloudsql_version" {
  description = "Cloud SQL database version"
  type        = string
  default     = "POSTGRES_17"
}

variable "cloudsql_disk_size_gb" {
  description = "Cloud SQL disk size (GB)"
  type        = number
  default     = 20
}

variable "enable_deletion_protection" {
  description = "Enable deletion protection for Cloud SQL and LB resources"
  type        = bool
  default     = true
}

variable "bff_min_instances" {
  type    = number
  default = 0
}

variable "bff_max_instances" {
  type    = number
  default = 10
}

variable "frontend_min_instances" {
  type    = number
  default = 0
}

variable "frontend_max_instances" {
  type    = number
  default = 10
}

variable "backend_min_instances" {
  type    = number
  default = 0
}

variable "backend_max_instances" {
  type    = number
  default = 10
}

variable "artifact_registry_repository_id" {
  description = "Artifact Registry repository ID for Docker images"
  type        = string
}

variable "vpc_connector_max_instances" {
  description = "Max instances for Serverless VPC Access Connector"
  type        = number
  default     = 3
}

variable "vpc_connector_min_instances" {
  description = "Min instances for Serverless VPC Access Connector (must be >= 2)"
  type        = number
  default     = 2
}
