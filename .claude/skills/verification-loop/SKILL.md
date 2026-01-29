# 検証ループスキル

Claude Codeセッション用の包括的な検証システム。

## 使用タイミング

以下の場面でこのスキルを呼び出してください:
- 機能や大きなコード変更の完了後
- PR作成前
- 品質ゲートの通過を確認したい時
- リファクタリング後

## 検証フェーズ

### フェーズ1: ビルド検証
```bash
# Backend (Kotlin + Ktor)
cd backend/user-service && ./gradlew build 2>&1 | tail -20
cd backend/task-service && ./gradlew build 2>&1 | tail -20

# BFF (Express + TypeScript)
cd bff && npm run build 2>&1 | tail -20

# Frontend (React + Vite)
cd frontend && npm run build 2>&1 | tail -20
```

ビルドが失敗した場合、続行前に停止して修正すること。

### フェーズ2: 型チェック
```bash
# Frontend / BFF (TypeScript)
cd frontend && npx tsc --noEmit 2>&1 | head -30
cd bff && npx tsc --noEmit 2>&1 | head -30

# Backend (Kotlin コンパイル)
cd backend/user-service && ./gradlew compileKotlin 2>&1 | tail -20
cd backend/task-service && ./gradlew compileKotlin 2>&1 | tail -20
```

すべての型エラーを報告。クリティカルなものは続行前に修正。

### フェーズ3: Lintチェック
```bash
# Frontend (ESLint)
cd frontend && npm run lint 2>&1 | head -30
```

### フェーズ4: テストスイート
```bash
# Backend テスト
cd backend/user-service && ./gradlew test 2>&1 | tail -50
cd backend/task-service && ./gradlew test 2>&1 | tail -50

# Frontend lint (テストがある場合)
cd frontend && npm run lint 2>&1 | tail -30
```

レポート:
- テスト総数: X
- 通過: X
- 失敗: X
- カバレッジ: X%

### フェーズ5: セキュリティスキャン
```bash
# シークレットの検出（TypeScript/JavaScript）
grep -rn "sk-" --include="*.ts" --include="*.js" --include="*.tsx" frontend/ bff/ 2>/dev/null | head -10
grep -rn "api_key" --include="*.ts" --include="*.js" --include="*.tsx" frontend/ bff/ 2>/dev/null | head -10

# シークレットの検出（Kotlin）
grep -rn "sk-" --include="*.kt" backend/ 2>/dev/null | head -10
grep -rn "api_key\|password\s*=" --include="*.kt" backend/ 2>/dev/null | head -10

# console.log の検出
grep -rn "console.log" --include="*.ts" --include="*.tsx" frontend/src/ 2>/dev/null | head -10

# println の検出（Kotlin）
grep -rn "println" --include="*.kt" backend/ 2>/dev/null | head -10
```

### フェーズ6: 差分レビュー
```bash
# Show what changed
git diff --stat
git diff HEAD~1 --name-only
```

変更された各ファイルをレビュー:
- 意図しない変更
- エラーハンドリングの欠落
- 潜在的なエッジケース

## 出力フォーマット

すべてのフェーズ実行後、検証レポートを作成:

```
検証レポート
==================

ビルド:
  user-service:  [PASS/FAIL]
  task-service:  [PASS/FAIL]
  bff:           [PASS/FAIL]
  frontend:      [PASS/FAIL]

型チェック:
  TypeScript:    [PASS/FAIL] (Xエラー)
  Kotlin:        [PASS/FAIL] (Xエラー)

Lint:           [PASS/FAIL] (X警告)

テスト:
  user-service:  [PASS/FAIL] (X/Y通過)
  task-service:  [PASS/FAIL] (X/Y通過)

セキュリティ:    [PASS/FAIL] (X件の問題)
差分:           [X ファイル変更]

総合:           PR[準備完了/未完了]

修正すべき問題:
1. ...
2. ...
```

## 継続モード

長時間セッションでは、15分ごとまたは大きな変更の後に検証を実行:

```markdown
メンタルチェックポイントを設定:
- 各関数の完了後
- コンポーネントの完成後
- 次のタスクに移る前

実行: /verify
```

## フックとの統合

このスキルはPostToolUseフックを補完しますが、より深い検証を提供します。
フックは問題を即座にキャッチし、このスキルは包括的なレビューを提供します。
