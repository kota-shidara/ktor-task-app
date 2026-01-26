# グローバルな固定外部IPを予約する
resource "google_compute_global_address" "lb_ip" {
  name = "${local.name_prefix}-lb-ip"
}

# Cloud Runをロードバランサのバックエンドとして接続するためのアダプタ
resource "google_compute_region_network_endpoint_group" "frontend_neg" {
  name                  = "${local.name_prefix}-frontend-neg"
  region                = var.region
  network_endpoint_type = "SERVERLESS"

  cloud_run {
    service = google_cloud_run_v2_service.frontend.name
  }
}

resource "google_compute_region_network_endpoint_group" "bff_neg" {
  name                  = "${local.name_prefix}-bff-neg"
  region                = var.region
  network_endpoint_type = "SERVERLESS"

  cloud_run {
    service = google_cloud_run_v2_service.bff.name
  }
}

# URL Mapが参照する転送先の定義
resource "google_compute_backend_service" "frontend" {
  name                  = "${local.name_prefix}-frontend-backend"
  protocol              = "HTTP"
  load_balancing_scheme = "EXTERNAL_MANAGED"
  port_name             = "http"
  timeout_sec           = 30

  backend {
    group = google_compute_region_network_endpoint_group.frontend_neg.id
  }

  depends_on = [google_cloud_run_v2_service_iam_member.frontend_invoker]
}

resource "google_compute_backend_service" "bff" {
  name                  = "${local.name_prefix}-bff-backend"
  protocol              = "HTTP"
  load_balancing_scheme = "EXTERNAL_MANAGED"
  port_name             = "http"
  timeout_sec           = 30

  backend {
    group = google_compute_region_network_endpoint_group.bff_neg.id
  }

  depends_on = [google_cloud_run_v2_service_iam_member.bff_invoker]
}

# ルーティングの核
# パスやホスト名でどのバックエンドへ振り分けるか を定義します
resource "google_compute_url_map" "app" {
  name            = "${local.name_prefix}-app-urlmap"
  default_service = google_compute_backend_service.frontend.id

  host_rule {
    hosts        = ["*"]
    path_matcher = "main"
  }

  path_matcher {
    name            = "main"
    default_service = google_compute_backend_service.frontend.id

    path_rule {
      paths   = ["/api/*"]
      service = google_compute_backend_service.bff.id
    }
  }
}

# 受け取ったHTTPリクエストを解析し、URLマップへ受け渡すだけ
resource "google_compute_target_http_proxy" "app" {
  name    = "${local.name_prefix}-app-http-proxy"
  url_map = google_compute_url_map.app.id
}

# インターネットからの入口
# ip_addressに固定グローバルIPを割り当て、ポート80で待ち受ける
# targetとしてHTTP proxyを設定し、そこからURL Map → Backend Service → NEG → Cloud Runへ流れる
resource "google_compute_global_forwarding_rule" "app_http" {
  name                  = "${local.name_prefix}-app-http"
  target                = google_compute_target_http_proxy.app.id
  port_range            = "80"
  ip_address            = google_compute_global_address.lb_ip.address
  load_balancing_scheme = "EXTERNAL_MANAGED"
}
