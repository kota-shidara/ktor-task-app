---
name: eval-harness
description: 評価駆動開発（EDD）の原則を実装するClaude Codeセッション用の正式な評価フレームワーク
tools: Read, Write, Edit, Bash, Grep, Glob
---

# 評価ハーネススキル

評価駆動開発（EDD）の原則を実装するClaude Codeセッション用の正式な評価フレームワーク。

## 哲学

評価駆動開発は評価を「AI開発のユニットテスト」として扱います:
- 実装前に期待される動作を定義
- 開発中に継続的に評価を実行
- 変更ごとにリグレッションを追跡
- 信頼性測定にpass@kメトリクスを使用

## 評価タイプ

### 能力評価
Claudeが以前できなかったことができるかテスト:
```markdown
[CAPABILITY EVAL: feature-name]
Task: Description of what Claude should accomplish
Success Criteria:
  - [ ] Criterion 1
  - [ ] Criterion 2
  - [ ] Criterion 3
Expected Output: Description of expected result
```

### リグレッション評価
変更が既存機能を壊していないことを確認:
```markdown
[REGRESSION EVAL: feature-name]
Baseline: SHA or checkpoint name
Tests:
  - existing-test-1: PASS/FAIL
  - existing-test-2: PASS/FAIL
  - existing-test-3: PASS/FAIL
Result: X/Y passed (previously Y/Y)
```

## 採点タイプ

### 1. コードベース採点
コードを使用した決定論的チェック:
```bash
# ファイルに期待パターンが含まれるか確認
grep -q "export function handleAuth" src/auth.ts && echo "PASS" || echo "FAIL"

# テストが通るか確認
npm test -- --testPathPattern="auth" && echo "PASS" || echo "FAIL"

# ビルドが成功するか確認
npm run build && echo "PASS" || echo "FAIL"
```

### 2. モデルベース採点
Claudeを使用したオープンエンド出力の評価:
```markdown
[MODEL GRADER PROMPT]
Evaluate the following code change:
1. Does it solve the stated problem?
2. Is it well-structured?
3. Are edge cases handled?
4. Is error handling appropriate?

Score: 1-5 (1=poor, 5=excellent)
Reasoning: [explanation]
```

### 3. 人間による採点
手動レビュー用にフラグ:
```markdown
[HUMAN REVIEW REQUIRED]
Change: Description of what changed
Reason: Why human review is needed
Risk Level: LOW/MEDIUM/HIGH
```

## メトリクス

### pass@k
「k回の試行で少なくとも1回成功」
- pass@1: 初回成功率
- pass@3: 3回以内の成功率
- 一般的な目標: pass@3 > 90%

### pass^k
「k回すべてが成功」
- 信頼性のより高い基準
- pass^3: 3回連続成功
- クリティカルパスに使用

## 評価ワークフロー

### 1. 定義（コーディング前）
```markdown
## EVAL DEFINITION: feature-xyz

### 能力評価
1. Can create new user account
2. Can validate email format
3. Can hash password securely

### リグレッション評価
1. Existing login still works
2. Session management unchanged
3. Logout flow intact

### 成功メトリクス
- pass@3 > 90% for capability evals
- pass^3 = 100% for regression evals
```

### 2. 実装
定義された評価を通過するコードを書く。

### 3. 評価
```bash
# 能力評価を実行
[Run each capability eval, record PASS/FAIL]

# リグレッション評価を実行
npm test -- --testPathPattern="existing"

# レポートを生成
```

### 4. レポート
```markdown
EVAL REPORT: feature-xyz
========================

能力評価:
  create-user:     PASS (pass@1)
  validate-email:  PASS (pass@2)
  hash-password:   PASS (pass@1)
  全体:            3/3 passed

リグレッション評価:
  login-flow:      PASS
  session-mgmt:    PASS
  logout-flow:     PASS
  全体:            3/3 passed

メトリクス:
  pass@1: 67% (2/3)
  pass@3: 100% (3/3)

ステータス: レビュー準備完了
```

## 統合パターン

### 実装前
```
/eval define feature-name
```
`.claude/evals/feature-name.md`に評価定義ファイルを作成

### 実装中
```
/eval check feature-name
```
現在の評価を実行してステータスを報告

### 実装後
```
/eval report feature-name
```
完全な評価レポートを生成

## 評価の保存

プロジェクト内に評価を保存:
```
.claude/
  evals/
    feature-xyz.md      # 評価定義
    feature-xyz.log     # 評価実行履歴
    baseline.json       # リグレッションベースライン
```

## ベストプラクティス

1. **コーディング前に評価を定義** - 成功基準を明確に考えることを強制
2. **評価を頻繁に実行** - リグレッションを早期に検出
3. **pass@kの推移を追跡** - 信頼性の傾向を監視
4. **可能な限りコード採点を使用** - 決定論的 > 確率的
5. **セキュリティは人間がレビュー** - セキュリティチェックを完全自動化しない
6. **評価は高速に保つ** - 遅い評価は実行されない
7. **評価をコードとバージョン管理** - 評価はファーストクラスの成果物

## 例: 認証の追加

```markdown
## EVAL: add-authentication

### フェーズ1: 定義（10分）
能力評価:
- [ ] User can register with email/password
- [ ] User can login with valid credentials
- [ ] Invalid credentials rejected with proper error
- [ ] Sessions persist across page reloads
- [ ] Logout clears session

リグレッション評価:
- [ ] Public routes still accessible
- [ ] API responses unchanged
- [ ] Database schema compatible

### フェーズ2: 実装（所要時間は不定）
[コードを書く]

### フェーズ3: 評価
実行: /eval check add-authentication

### フェーズ4: レポート
EVAL REPORT: add-authentication
==============================
能力: 5/5 passed (pass@3: 100%)
リグレッション: 3/3 passed (pass^3: 100%)
ステータス: リリース可能
```
