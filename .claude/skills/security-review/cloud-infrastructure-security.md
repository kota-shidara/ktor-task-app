| name | description |
|------|-------------|
| cloud-infrastructure-security | クラウドプラットフォームへのデプロイ、インフラ設定、IAMポリシー管理、ロギング/モニタリングのセットアップ、CI/CDパイプラインの実装時に使用するスキル。ベストプラクティスに沿ったクラウドセキュリティチェックリストを提供。 |

# クラウド＆インフラセキュリティスキル

クラウドインフラ、CI/CDパイプライン、デプロイ設定がセキュリティベストプラクティスに従い、業界標準に準拠することを保証するスキル。

## 発動タイミング

- クラウドプラットフォーム（AWS、Vercel、Railway、Cloudflare）へのアプリケーションデプロイ
- IAMロールと権限の設定
- CI/CDパイプラインのセットアップ
- Infrastructure as Code（Terraform、CloudFormation）の実装
- ロギングとモニタリングの設定
- クラウド環境でのシークレット管理
- CDNとエッジセキュリティのセットアップ
- 災害復旧とバックアップ戦略の実装

## クラウドセキュリティチェックリスト

### 1. IAMとアクセス制御

#### 最小権限の原則

```yaml
# ✅ 正しい: 最小限の権限
iam_role:
  permissions:
    - s3:GetObject  # Only read access
    - s3:ListBucket
  resources:
    - arn:aws:s3:::my-bucket/*  # Specific bucket only

# ❌ 間違い: 過剰な権限
iam_role:
  permissions:
    - s3:*  # All S3 actions
  resources:
    - "*"  # All resources
```

#### 多要素認証（MFA）

```bash
# ALWAYS enable MFA for root/admin accounts
aws iam enable-mfa-device \
  --user-name admin \
  --serial-number arn:aws:iam::123456789:mfa/admin \
  --authentication-code1 123456 \
  --authentication-code2 789012
```

#### 確認手順

- [ ] 本番でルートアカウントが使用されていない
- [ ] すべての特権アカウントでMFAが有効
- [ ] サービスアカウントが長期認証情報ではなくロールを使用
- [ ] IAMポリシーが最小権限に従っている
- [ ] 定期的なアクセスレビューが実施されている
- [ ] 未使用の認証情報がローテーションまたは削除されている

### 2. シークレット管理

#### クラウドシークレットマネージャー

```typescript
// ✅ 正しい: クラウドシークレットマネージャーを使用
import { SecretsManager } from '@aws-sdk/client-secrets-manager';

const client = new SecretsManager({ region: 'us-east-1' });
const secret = await client.getSecretValue({ SecretId: 'prod/api-key' });
const apiKey = JSON.parse(secret.SecretString).key;

// ❌ 間違い: ハードコードまたは環境変数のみ
const apiKey = process.env.API_KEY; // Not rotated, not audited
```

#### シークレットのローテーション

```bash
# Set up automatic rotation for database credentials
aws secretsmanager rotate-secret \
  --secret-id prod/db-password \
  --rotation-lambda-arn arn:aws:lambda:region:account:function:rotate \
  --rotation-rules AutomaticallyAfterDays=30
```

#### 確認手順

- [ ] すべてのシークレットがクラウドシークレットマネージャー（AWS Secrets Manager、Vercel Secrets）に保存
- [ ] データベース認証情報の自動ローテーションが有効
- [ ] APIキーが少なくとも四半期ごとにローテーション
- [ ] コード、ログ、エラーメッセージにシークレットがない
- [ ] シークレットアクセスの監査ロギングが有効

### 3. ネットワークセキュリティ

#### VPCとファイアウォール設定

```terraform
# ✅ 正しい: 制限されたセキュリティグループ
resource "aws_security_group" "app" {
  name = "app-sg"

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]  # Internal VPC only
  }

  egress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Only HTTPS outbound
  }
}

# ❌ 間違い: インターネットに公開
resource "aws_security_group" "bad" {
  ingress {
    from_port   = 0
    to_port     = 65535
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # All ports, all IPs!
  }
}
```

#### 確認手順

- [ ] データベースが公開アクセス不可
- [ ] SSH/RDPポートがVPN/踏み台のみに制限
- [ ] セキュリティグループが最小権限に従っている
- [ ] ネットワークACLが設定されている
- [ ] VPCフローログが有効

### 4. ロギングとモニタリング

#### CloudWatch/ロギング設定

```typescript
// ✅ 正しい: 包括的なロギング
import { CloudWatchLogsClient, CreateLogStreamCommand } from '@aws-sdk/client-cloudwatch-logs';

const logSecurityEvent = async (event: SecurityEvent) => {
  await cloudwatch.putLogEvents({
    logGroupName: '/aws/security/events',
    logStreamName: 'authentication',
    logEvents: [{
      timestamp: Date.now(),
      message: JSON.stringify({
        type: event.type,
        userId: event.userId,
        ip: event.ip,
        result: event.result,
        // Never log sensitive data
      })
    }]
  });
};
```

#### 確認手順

- [ ] すべてのサービスでCloudWatch/ロギングが有効
- [ ] 認証失敗がログに記録されている
- [ ] 管理者アクションが監査されている
- [ ] ログ保持が設定されている（コンプライアンスのため90日以上）
- [ ] 不審な活動に対するアラートが設定されている
- [ ] ログが一元化され改ざん防止されている

### 5. CI/CDパイプラインセキュリティ

#### 安全なパイプライン設定

```yaml
# ✅ 正しい: 安全なGitHub Actionsワークフロー
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    permissions:
      contents: read  # Minimal permissions

    steps:
      - uses: actions/checkout@v4

      # Scan for secrets
      - name: Secret scanning
        uses: trufflesecurity/trufflehog@main

      # Dependency audit
      - name: Audit dependencies
        run: npm audit --audit-level=high

      # Use OIDC, not long-lived tokens
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: arn:aws:iam::123456789:role/GitHubActionsRole
          aws-region: us-east-1
```

#### サプライチェーンセキュリティ

```json
// package.json - Use lock files and integrity checks
{
  "scripts": {
    "install": "npm ci",  // Use ci for reproducible builds
    "audit": "npm audit --audit-level=moderate",
    "check": "npm outdated"
  }
}
```

#### 確認手順

- [ ] 長期認証情報の代わりにOIDCを使用
- [ ] パイプラインでシークレットスキャン
- [ ] 依存関係の脆弱性スキャン
- [ ] コンテナイメージスキャン（該当する場合）
- [ ] ブランチ保護ルールが適用されている
- [ ] マージ前にコードレビューが必須
- [ ] 署名付きコミットが適用されている

### 6. Cloudflare＆CDNセキュリティ

#### Cloudflareセキュリティ設定

```typescript
// ✅ 正しい: セキュリティヘッダー付きCloudflare Workers
export default {
  async fetch(request: Request): Promise<Response> {
    const response = await fetch(request);

    // Add security headers
    const headers = new Headers(response.headers);
    headers.set('X-Frame-Options', 'DENY');
    headers.set('X-Content-Type-Options', 'nosniff');
    headers.set('Referrer-Policy', 'strict-origin-when-cross-origin');
    headers.set('Permissions-Policy', 'geolocation=(), microphone=()');

    return new Response(response.body, {
      status: response.status,
      headers
    });
  }
};
```

#### WAFルール

```bash
# Enable Cloudflare WAF managed rules
# - OWASP Core Ruleset
# - Cloudflare Managed Ruleset
# - Rate limiting rules
# - Bot protection
```

#### 確認手順

- [ ] OWASPルールでWAFが有効
- [ ] レート制限が設定されている
- [ ] ボット保護がアクティブ
- [ ] DDoS保護が有効
- [ ] セキュリティヘッダーが設定されている
- [ ] SSL/TLSストリクトモードが有効

### 7. バックアップと災害復旧

#### 自動バックアップ

```terraform
# ✅ 正しい: 自動RDSバックアップ
resource "aws_db_instance" "main" {
  allocated_storage     = 20
  engine               = "postgres"

  backup_retention_period = 30  # 30 days retention
  backup_window          = "03:00-04:00"
  maintenance_window     = "mon:04:00-mon:05:00"

  enabled_cloudwatch_logs_exports = ["postgresql"]

  deletion_protection = true  # Prevent accidental deletion
}
```

#### 確認手順

- [ ] 毎日の自動バックアップが設定されている
- [ ] バックアップ保持がコンプライアンス要件を満たしている
- [ ] ポイントインタイムリカバリが有効
- [ ] バックアップテストが四半期ごとに実施されている
- [ ] 災害復旧計画が文書化されている
- [ ] RPOとRTOが定義・テストされている

## デプロイ前クラウドセキュリティチェックリスト

すべての本番クラウドデプロイの前に:

- [ ] **IAM**: ルートアカウント未使用、MFA有効、最小権限ポリシー
- [ ] **シークレット**: すべてのシークレットがローテーション付きクラウドシークレットマネージャーに
- [ ] **ネットワーク**: セキュリティグループが制限され、公開データベースがない
- [ ] **ロギング**: CloudWatch/ロギングが保持付きで有効
- [ ] **モニタリング**: 異常検知のアラートが設定されている
- [ ] **CI/CD**: OIDC認証、シークレットスキャン、依存関係監査
- [ ] **CDN/WAF**: OWASPルールでCloudflare WAFが有効
- [ ] **暗号化**: データが静止時と転送時に暗号化されている
- [ ] **バックアップ**: テスト済みリカバリ付き自動バックアップ
- [ ] **コンプライアンス**: GDPR/HIPAA要件を満たしている（該当する場合）
- [ ] **ドキュメント**: インフラが文書化され、ランブックが作成されている
- [ ] **インシデント対応**: セキュリティインシデント計画が策定されている

## 一般的なクラウドセキュリティ設定ミス

### S3バケットの露出

```bash
# ❌ 間違い: パブリックバケット
aws s3api put-bucket-acl --bucket my-bucket --acl public-read

# ✅ 正しい: 特定のアクセスを持つプライベートバケット
aws s3api put-bucket-acl --bucket my-bucket --acl private
aws s3api put-bucket-policy --bucket my-bucket --policy file://policy.json
```

### RDSの公開アクセス

```terraform
# ❌ 間違い
resource "aws_db_instance" "bad" {
  publicly_accessible = true  # NEVER do this!
}

# ✅ 正しい
resource "aws_db_instance" "good" {
  publicly_accessible = false
  vpc_security_group_ids = [aws_security_group.db.id]
}
```

## リソース

- [AWS Security Best Practices](https://aws.amazon.com/security/best-practices/)
- [CIS AWS Foundations Benchmark](https://www.cisecurity.org/benchmark/amazon_web_services)
- [Cloudflare Security Documentation](https://developers.cloudflare.com/security/)
- [OWASP Cloud Security](https://owasp.org/www-project-cloud-security/)
- [Terraform Security Best Practices](https://www.terraform.io/docs/cloud/guides/recommended-practices/)

**重要**: クラウドの設定ミスはデータ漏洩の最大の原因です。1つの露出したS3バケットや過剰に許可されたIAMポリシーが、インフラ全体を危険にさらす可能性があります。常に最小権限の原則と多層防御を遵守してください。
