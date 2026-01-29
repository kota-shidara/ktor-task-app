---
description: テスト駆動開発ワークフローを実施します。インターフェースを定義し、テストを先に生成し、テストを通す最小限のコードを実装します。80%以上のカバレッジを確保します。
---

# TDD コマンド

このコマンドは **tdd-guide** エージェントを呼び出し、テスト駆動開発の手法を実施します。

## このコマンドの機能

1. **インターフェースの定義** - 型/インターフェースを最初に定義
2. **テストを先に生成** - 失敗するテストを書く (RED)
3. **最小限のコードを実装** - テストを通すために必要な分だけ書く (GREEN)
4. **リファクタリング** - テストを維持しながらコードを改善 (REFACTOR)
5. **カバレッジの確認** - 80%以上のテストカバレッジを確保

## 使用場面

`/tdd` を使う場面:
- 新機能の実装
- 新しい関数/コンポーネントの追加
- バグ修正（まずバグを再現するテストを書く）
- 既存コードのリファクタリング
- 重要なビジネスロジックの構築

## 動作の仕組み

tdd-guide エージェントは以下を行います:

1. **インターフェースを定義** - 入出力の型を決める
2. **失敗するテストを書く** (コードがまだ存在しないため)
3. **テストを実行** し、正しい理由で失敗することを確認
4. **最小限の実装を書く** - テストを通すため
5. **テストを実行** し、パスすることを確認
6. **リファクタリング** - テストを維持しながらコードを改善
7. **カバレッジを確認** し、80%未満の場合はテストを追加

## TDD サイクル

```
RED → GREEN → REFACTOR → REPEAT

RED:      失敗するテストを書く
GREEN:    テストを通す最小限のコードを書く
REFACTOR: コードを改善し、テストを維持する
REPEAT:   次の機能/シナリオへ
```

## 使用例

```
User: /tdd I need a function to calculate market liquidity score

Agent (tdd-guide):
# TDD Session: Market Liquidity Score Calculator

## Step 1: Define Interface (SCAFFOLD)

```typescript
// lib/liquidity.ts
export interface MarketData {
  totalVolume: number
  bidAskSpread: number
  activeTraders: number
  lastTradeTime: Date
}

export function calculateLiquidityScore(market: MarketData): number {
  // TODO: Implementation
  throw new Error('Not implemented')
}
```

## Step 2: Write Failing Test (RED)

```typescript
// lib/liquidity.test.ts
import { calculateLiquidityScore } from './liquidity'

describe('calculateLiquidityScore', () => {
  it('should return high score for liquid market', () => {
    const market = {
      totalVolume: 100000,
      bidAskSpread: 0.01,
      activeTraders: 500,
      lastTradeTime: new Date()
    }

    const score = calculateLiquidityScore(market)

    expect(score).toBeGreaterThan(80)
    expect(score).toBeLessThanOrEqual(100)
  })

  it('should return low score for illiquid market', () => {
    const market = {
      totalVolume: 100,
      bidAskSpread: 0.5,
      activeTraders: 2,
      lastTradeTime: new Date(Date.now() - 86400000) // 1 day ago
    }

    const score = calculateLiquidityScore(market)

    expect(score).toBeLessThan(30)
    expect(score).toBeGreaterThanOrEqual(0)
  })

  it('should handle edge case: zero volume', () => {
    const market = {
      totalVolume: 0,
      bidAskSpread: 0,
      activeTraders: 0,
      lastTradeTime: new Date()
    }

    const score = calculateLiquidityScore(market)

    expect(score).toBe(0)
  })
})
```

## Step 3: Run Tests - Verify FAIL

```bash
npm test lib/liquidity.test.ts

FAIL lib/liquidity.test.ts
  ✕ should return high score for liquid market (2 ms)
    Error: Not implemented

1 test failed, 0 passed
```

✅ Tests fail as expected. Ready to implement.

## Step 4: Implement Minimal Code (GREEN)

```typescript
// lib/liquidity.ts
export function calculateLiquidityScore(market: MarketData): number {
  // Handle zero volume edge case
  if (market.totalVolume === 0) {
    return 0
  }

  // Calculate component scores (0-100 scale)
  const volumeScore = Math.min(market.totalVolume / 1000, 100)
  const spreadScore = Math.max(100 - (market.bidAskSpread * 1000), 0)
  const traderScore = Math.min(market.activeTraders / 10, 100)

  // Recent activity bonus
  const hoursSinceLastTrade = (Date.now() - market.lastTradeTime.getTime()) / (1000 * 60 * 60)
  const recencyScore = Math.max(100 - (hoursSinceLastTrade * 10), 0)

  // Weighted average
  const score = (
    volumeScore * 0.4 +
    spreadScore * 0.3 +
    traderScore * 0.2 +
    recencyScore * 0.1
  )

  return Math.min(Math.max(score, 0), 100) // Clamp to 0-100
}
```

## Step 5: Run Tests - Verify PASS

```bash
npm test lib/liquidity.test.ts

PASS lib/liquidity.test.ts
  ✓ should return high score for liquid market (3 ms)
  ✓ should return low score for illiquid market (2 ms)
  ✓ should handle edge case: zero volume (1 ms)

3 tests passed
```

✅ All tests passing!

## Step 6: Refactor (IMPROVE)

```typescript
// lib/liquidity.ts - Refactored with constants and better readability
const WEIGHTS = {
  VOLUME: 0.4,
  SPREAD: 0.3,
  TRADERS: 0.2,
  RECENCY: 0.1,
} as const

const SCALE_FACTORS = {
  VOLUME: 1000,
  SPREAD: 1000,
  TRADERS: 10,
  RECENCY_PENALTY: 10,
} as const

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max)
}

export function calculateLiquidityScore(market: MarketData): number {
  if (market.totalVolume === 0) return 0

  const volumeScore = Math.min(market.totalVolume / SCALE_FACTORS.VOLUME, 100)
  const spreadScore = clamp(100 - (market.bidAskSpread * SCALE_FACTORS.SPREAD), 0, 100)
  const traderScore = Math.min(market.activeTraders / SCALE_FACTORS.TRADERS, 100)

  const hoursSinceLastTrade = (Date.now() - market.lastTradeTime.getTime()) / (1000 * 60 * 60)
  const recencyScore = clamp(100 - (hoursSinceLastTrade * SCALE_FACTORS.RECENCY_PENALTY), 0, 100)

  const weightedScore =
    volumeScore * WEIGHTS.VOLUME +
    spreadScore * WEIGHTS.SPREAD +
    traderScore * WEIGHTS.TRADERS +
    recencyScore * WEIGHTS.RECENCY

  return clamp(weightedScore, 0, 100)
}
```

## Step 7: Verify Tests Still Pass

```bash
npm test lib/liquidity.test.ts

PASS lib/liquidity.test.ts
  ✓ should return high score for liquid market (3 ms)
  ✓ should return low score for illiquid market (2 ms)
  ✓ should handle edge case: zero volume (1 ms)

3 tests passed
```

✅ Refactoring complete, tests still passing!

## Step 8: Check Coverage

```bash
npm test -- --coverage lib/liquidity.test.ts

File           | % Stmts | % Branch | % Funcs | % Lines
---------------|---------|----------|---------|--------
liquidity.ts   |   100   |   100    |   100   |   100

Coverage: 100% ✅ (Target: 80%)
```

✅ TDD session complete!
```

## TDD ベストプラクティス

**推奨:**
- テストは実装の前に書く
- テストを実行し、実装前に失敗することを確認する
- テストを通す最小限のコードを書く
- テストがグリーンになってからリファクタリングする
- エッジケースとエラーシナリオを追加する
- 80%以上のカバレッジを目指す（重要なコードは100%）

**非推奨:**
- テスト前に実装を書く
- 変更ごとのテスト実行を省略する
- 一度に大量のコードを書く
- 失敗するテストを無視する
- 実装の詳細をテストする（振る舞いをテストすべき）
- すべてをモックする（統合テストを優先する）

## 含めるべきテストの種類

**ユニットテスト**（関数レベル）:
- 正常系シナリオ
- エッジケース（空、null、最大値）
- エラー条件
- 境界値

**統合テスト**（コンポーネントレベル）:
- APIエンドポイント
- データベース操作
- 外部サービス呼び出し
- Hooksを含むReactコンポーネント

**E2Eテスト**（`/e2e` コマンドを使用）:
- 重要なユーザーフロー
- 複数ステップのプロセス
- フルスタック統合

## カバレッジ要件

- **最低80%** - すべてのコード
- **100%必須** - 以下の場合:
  - 金融計算
  - 認証ロジック
  - セキュリティ重要コード
  - コアビジネスロジック

## 重要な注意事項

**必須**: テストは実装の前に書かなければなりません。TDDサイクルは:

1. **RED** - 失敗するテストを書く
2. **GREEN** - テストを通す実装をする
3. **REFACTOR** - コードを改善する

REDフェーズを省略しないでください。テスト前にコードを書かないでください。

## 他のコマンドとの連携

- `/plan` で最初に構築内容を理解する
- `/tdd` でテストとともに実装する
- `/build-and-fix` でビルドエラー発生時に対応する
- `/code-review` で実装をレビューする
- `/test-coverage` でカバレッジを確認する

## 関連エージェント

このコマンドは以下に配置された `tdd-guide` エージェントを呼び出します:
`~/.claude/agents/tdd-guide.md`

また、以下の `tdd-workflow` スキルを参照できます:
`~/.claude/skills/tdd-workflow/`
