
# 新しくサービスアカウントが追加されたら
- 新しくサービスアカウントを作成するたびに、deployerがactAsを使えるように（Cloud Runへのデプロイで必要）、権限の設定が必要
- 新マイクロサービスができるなどが利用シーンと想定される
```sh
PROJECT_ID="ktor-task-app-staging"
DEPLOYER_SA="github-actions-deployer@${PROJECT_ID}.iam.gserviceaccount.com"

# サービスアカウント名をここに記入
RUNTIME_SAS=(
  "ktor-task-app-staging-bff"
  "ktor-task-app-staging-frontend"
  "ktor-task-app-staging-user"
  "ktor-task-app-staging-task"
)

for sa_name in "${RUNTIME_SAS[@]}"; do
  sa_email="${sa_name}@${PROJECT_ID}.iam.gserviceaccount.com"

  # 無ければ作る
  gcloud iam service-accounts describe "${sa_email}" \
    --project "${PROJECT_ID}" >/dev/null 2>&1 || \
  gcloud iam service-accounts create "${sa_name}" \
    --project "${PROJECT_ID}" \
    --display-name "Cloud Run runtime (${sa_name})"

  # deployer が actAs できるようにする
  gcloud iam service-accounts add-iam-policy-binding "${sa_email}" \
    --project "${PROJECT_ID}" \
    --member "serviceAccount:${DEPLOYER_SA}" \
    --role "roles/iam.serviceAccountUser"
done
```

# workflow.yamlを動かすまでのコマンドのログ
```sh
PROJECT_ID="ktor-task-app-staging"
PROJECT_NUMBER="$(gcloud projects describe "${PROJECT_ID}" --format='value(projectNumber)')"

POOL="github-actions-pool"
PROVIDER="github-actions-provider"

REPO="kota-shidara/ktor-task-app"

SA_NAME="github-actions-deployer"
SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

# 1) Service Account 作成
gcloud iam service-accounts create "${SA_NAME}" \
  --project "${PROJECT_ID}" \
  --display-name "GitHub Actions Deployer"

# 2) Workload Identity Pool 作成
gcloud iam workload-identity-pools create "${POOL}" \
  --project "${PROJECT_ID}" \
  --location "global" \
  --display-name "GitHub Actions Pool"

# 3) OIDC Provider 作成 (GitHub Actions)
gcloud iam workload-identity-pools providers create-oidc "${PROVIDER}" \
  --project "${PROJECT_ID}" \
  --location "global" \
  --workload-identity-pool "${POOL}" \
  --display-name "GitHub Actions Provider" \
  --issuer-uri "https://token.actions.githubusercontent.com" \
  --attribute-mapping "google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.ref=assertion.ref" \
  --attribute-condition "assertion.repository=='kota-shidara/ktor-task-app'"

# 4) GitHub リポジトリに対して SA を impersonate できるように紐付け
gcloud iam service-accounts add-iam-policy-binding "${SA_EMAIL}" \
  --project "${PROJECT_ID}" \
  --role "roles/iam.workloadIdentityUser" \
  --member "principalSet://iam.googleapis.com/projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/${POOL}/attribute.repository/${REPO}"

# 5) デプロイ権限付与
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
  --member "serviceAccount:${SA_EMAIL}" \
  --role "roles/run.admin"

gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
  --member "serviceAccount:${SA_EMAIL}" \
  --role "roles/artifactregistry.writer"
```
- 上記に加え、「新しくサービスアカウントが追加されたら」のコマンド実行、GitHubのsecretsの設定、エラーで案内されたapiの権限許可（iamcredentials）を実行した