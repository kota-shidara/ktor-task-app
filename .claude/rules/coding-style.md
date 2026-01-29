# コーディングスタイル

## イミュータビリティ（最重要）

常に新しいオブジェクトを生成し、絶対にミューテーションしないこと:

```javascript
// 悪い例: ミューテーション
function updateUser(user, name) {
  user.name = name  // MUTATION!
  return user
}

// 良い例: イミュータビリティ
function updateUser(user, name) {
  return {
    ...user,
    name
  }
}
```

## ファイル構成

少数の大きなファイルより多数の小さなファイルを優先:
- 高凝集・低結合
- 通常200〜400行、最大800行
- 大きなコンポーネントからユーティリティを抽出する
- 型別ではなく、機能・ドメイン別に整理する

## エラーハンドリング

常に包括的にエラーを処理すること:

```typescript
try {
  const result = await riskyOperation()
  return result
} catch (error) {
  console.error('Operation failed:', error)
  throw new Error('Detailed user-friendly message')
}
```

## 入力バリデーション

ユーザー入力は常にバリデーションすること:

```typescript
import { z } from 'zod'

const schema = z.object({
  email: z.string().email(),
  age: z.number().int().min(0).max(150)
})

const validated = schema.parse(input)
```

## コード品質チェックリスト

作業完了前に確認すること:
- [ ] コードが読みやすく、適切な命名がされている
- [ ] 関数が小さい（50行未満）
- [ ] ファイルが適切なサイズである（800行未満）
- [ ] 深いネストがない（4階層以上は不可）
- [ ] 適切なエラーハンドリングがある
- [ ] console.log 文が残っていない
- [ ] ハードコードされた値がない
- [ ] ミューテーションがない（イミュータブルパターンを使用）
