provider "google" {
  project = var.project_id
  region  = var.region
}
# ここに記述する必要があるのは、設定が必要なプロバイダのみ。randomは設定不要なプロバイダなので書かれていない。
# versions.tfには、必要なプロバイダの宣言を書く必要がある