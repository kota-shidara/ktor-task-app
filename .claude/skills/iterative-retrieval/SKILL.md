---
name: iterative-retrieval
description: サブエージェントのコンテキスト問題を解決するための段階的コンテキスト取得パターン
---

# 反復的取得パターン

マルチエージェントワークフローにおける「コンテキスト問題」を解決します。サブエージェントは作業を開始するまで、どのコンテキストが必要かわかりません。

## 問題

サブエージェントは限られたコンテキストで起動されます。以下がわかりません:
- どのファイルに関連コードが含まれるか
- コードベースにどんなパターンが存在するか
- プロジェクトがどんな用語を使っているか

標準的なアプローチでは失敗します:
- **すべて送る**: コンテキスト制限を超過
- **何も送らない**: エージェントが重要な情報を欠く
- **必要なものを推測**: しばしば不正確

## 解決策: 反復的取得

4フェーズのループでコンテキストを段階的に洗練:

```
┌─────────────────────────────────────────────┐
│                                             │
│   ┌──────────┐      ┌──────────┐            │
│   │ DISPATCH │─────▶│ EVALUATE │            │
│   └──────────┘      └──────────┘            │
│        ▲                  │                 │
│        │                  ▼                 │
│   ┌──────────┐      ┌──────────┐            │
│   │   LOOP   │◀─────│  REFINE  │            │
│   └──────────┘      └──────────┘            │
│                                             │
│        最大3サイクル、その後処理続行           │
└─────────────────────────────────────────────┘
```

### フェーズ1: DISPATCH（ディスパッチ）

候補ファイルを収集するための初期の広範なクエリ:

```javascript
// Start with high-level intent
const initialQuery = {
  patterns: ['src/**/*.ts', 'lib/**/*.ts'],
  keywords: ['authentication', 'user', 'session'],
  excludes: ['*.test.ts', '*.spec.ts']
};

// Dispatch to retrieval agent
const candidates = await retrieveFiles(initialQuery);
```

### フェーズ2: EVALUATE（評価）

取得したコンテンツの関連性を評価:

```javascript
function evaluateRelevance(files, task) {
  return files.map(file => ({
    path: file.path,
    relevance: scoreRelevance(file.content, task),
    reason: explainRelevance(file.content, task),
    missingContext: identifyGaps(file.content, task)
  }));
}
```

スコアリング基準:
- **高 (0.8-1.0)**: 対象機能を直接実装
- **中 (0.5-0.7)**: 関連パターンや型を含む
- **低 (0.2-0.4)**: 間接的に関連
- **なし (0-0.2)**: 無関係、除外

### フェーズ3: REFINE（洗練）

評価に基づいて検索条件を更新:

```javascript
function refineQuery(evaluation, previousQuery) {
  return {
    // Add new patterns discovered in high-relevance files
    patterns: [...previousQuery.patterns, ...extractPatterns(evaluation)],

    // Add terminology found in codebase
    keywords: [...previousQuery.keywords, ...extractKeywords(evaluation)],

    // Exclude confirmed irrelevant paths
    excludes: [...previousQuery.excludes, ...evaluation
      .filter(e => e.relevance < 0.2)
      .map(e => e.path)
    ],

    // Target specific gaps
    focusAreas: evaluation
      .flatMap(e => e.missingContext)
      .filter(unique)
  };
}
```

### フェーズ4: LOOP（ループ）

洗練された条件で繰り返し（最大3サイクル）:

```javascript
async function iterativeRetrieve(task, maxCycles = 3) {
  let query = createInitialQuery(task);
  let bestContext = [];

  for (let cycle = 0; cycle < maxCycles; cycle++) {
    const candidates = await retrieveFiles(query);
    const evaluation = evaluateRelevance(candidates, task);

    // Check if we have sufficient context
    const highRelevance = evaluation.filter(e => e.relevance >= 0.7);
    if (highRelevance.length >= 3 && !hasCriticalGaps(evaluation)) {
      return highRelevance;
    }

    // Refine and continue
    query = refineQuery(evaluation, query);
    bestContext = mergeContext(bestContext, highRelevance);
  }

  return bestContext;
}
```

## 実践例

### 例1: バグ修正のコンテキスト

```
Task: "Fix the authentication token expiry bug"

Cycle 1:
  DISPATCH: Search for "token", "auth", "expiry" in src/**
  EVALUATE: Found auth.ts (0.9), tokens.ts (0.8), user.ts (0.3)
  REFINE: Add "refresh", "jwt" keywords; exclude user.ts

Cycle 2:
  DISPATCH: Search refined terms
  EVALUATE: Found session-manager.ts (0.95), jwt-utils.ts (0.85)
  REFINE: Sufficient context (2 high-relevance files)

Result: auth.ts, tokens.ts, session-manager.ts, jwt-utils.ts
```

### 例2: 機能実装

```
Task: "Add rate limiting to API endpoints"

Cycle 1:
  DISPATCH: Search "rate", "limit", "api" in routes/**
  EVALUATE: No matches - codebase uses "throttle" terminology
  REFINE: Add "throttle", "middleware" keywords

Cycle 2:
  DISPATCH: Search refined terms
  EVALUATE: Found throttle.ts (0.9), middleware/index.ts (0.7)
  REFINE: Need router patterns

Cycle 3:
  DISPATCH: Search "router", "express" patterns
  EVALUATE: Found router-setup.ts (0.8)
  REFINE: Sufficient context

Result: throttle.ts, middleware/index.ts, router-setup.ts
```

## エージェントとの統合

エージェントプロンプトでの使用:

```markdown
When retrieving context for this task:
1. Start with broad keyword search
2. Evaluate each file's relevance (0-1 scale)
3. Identify what context is still missing
4. Refine search criteria and repeat (max 3 cycles)
5. Return files with relevance >= 0.7
```

## ベストプラクティス

1. **広く始めて段階的に絞る** - 初期クエリを過度に指定しない
2. **コードベースの用語を学ぶ** - 最初のサイクルで命名規則が明らかになることが多い
3. **不足しているものを追跡** - 明示的なギャップの特定が洗練を駆動する
4. **「十分」で止める** - 高関連性の3ファイルは、中程度の10ファイルに勝る
5. **自信を持って除外** - 低関連性のファイルが関連性を持つことはない

## 関連リンク

- [The Longform Guide](https://x.com/affaanmustafa/status/2014040193557471352) - サブエージェントオーケストレーションセクション
- `continuous-learning` スキル - 時間とともに改善するパターン
- `~/.claude/agents/`のエージェント定義
