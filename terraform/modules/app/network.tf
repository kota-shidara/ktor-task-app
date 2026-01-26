# # VPCの作成
#       - 何を作る: 1つの VPC（仮想ネットワーク）自体。
#       - なぜ必要: サブネットやピアリング、VPC コネクタは VPC にぶら下がるため、VPC がないと始まりません。
#       - この構成での役割: Cloud SQL のプライベート接続と、Cloud Run から VPC 内へ出る経路の基盤になります。
#       - 注意点: auto_create_subnetworks = false なので「自動でサブネットを作らない」。自分で CIDR を明示管理するため。
resource "google_compute_network" "main" {
  name                    = "${local.name_prefix}-vpc"
  auto_create_subnetworks = false
}

#      - 何を作る: Private Service Access (PSA) 用の「内部 IP の予約レンジ」。
#      - なぜ必要: Cloud SQL などの Google 管理サービスを VPC 内に“プライベート IP”で配置するには、Google 側が使う IP 範囲を予約しておく必要がある。
#      - この構成での役割: Cloud SQL にプライベート IP を割り当てるための範囲を確保している。
#      - 注意点:
#          - purpose = "VPC_PEERING" が PSA 用の予約であることを示す。
#          - prefix_length = 16 なので 10.x.x.x/16 相当の大きめレンジを確保。将来的に複数のマネージドサービスを使う余地ができる。
resource "google_compute_global_address" "private_service_range" {
  name          = "${local.name_prefix}-psa"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  address       = "10.20.0.0"
  prefix_length = 16
  network       = google_compute_network.main.id
}

#      - 何を作る: VPC と Google の「サービスネットワーク」(servicenetworking.googleapis.com) のピアリング接続。
#      - なぜ必要: PSA で予約したレンジを実際に Google 管理サービス側が使えるように“つなぐ”ため。
#      - この構成での役割: Cloud SQL を VPC 内のプライベート IP で利用可能にする接続。これが無いと Cloud SQL はプライベート IP を持てない。
#      - 依存関係: reserved_peering_ranges に上の private_service_rangeを渡すので、予約レンジとセットで必要。
resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.main.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_service_range.name]
}


#      - 何を作る: Serverless VPC Access Connector（Cloud Run などサーバレスが VPC 内に出るための通路）。
#      - なぜ必要: Cloud Run はデフォルトだと VPC 内に直接入れないので、プライベート IP の Cloud SQL に到達できない。
#      - この構成での役割: run.tf の Cloud Run サービスで vpc_access { connector = ... egress = "PRIVATE_RANGES_ONLY" } として使われ、CloudRun の通信を VPC 内に流す。
#      - 注意点:
#          - ip_cidr_range = "10.10.16.0/28" は Connector 自体が使う小さいレンジ。サブネット本体のレンジと被らない必要がある。
#          - Connector はリージョン固定なので Cloud Run と同リージョンで使う必要がある。
resource "google_vpc_access_connector" "run" {
  name          = local.vpc_connector_name
  region        = var.region
  network       = google_compute_network.main.name
  ip_cidr_range = "10.10.16.0/28"
  min_instances = var.vpc_connector_min_instances
  max_instances = var.vpc_connector_max_instances
}
