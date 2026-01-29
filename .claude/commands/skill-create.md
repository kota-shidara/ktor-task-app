---
name: skill-create
description: ローカルのgit履歴を分析してコーディングパターンを抽出し、SKILL.mdファイルを生成します。Skill Creator GitHub Appのローカル版です。
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /skill-create - ローカルスキル生成

リポジトリのgit履歴を分析してコーディングパターンを抽出し、チームの慣習をClaudeに教えるSKILL.mdファイルを生成します。

## 使い方

```bash
/skill-create                    # 現在のリポジトリを分析
/skill-create --commits 100      # 直近100コミットを分析
/skill-create --output ./skills  # カスタム出力ディレクトリ
/skill-create --instincts        # continuous-learning-v2用のinstinctsも生成
```

## 機能概要

1. **Git履歴の解析** - コミット、ファイル変更、パターンを分析
2. **パターンの検出** - 繰り返されるワークフローや規約を特定
3. **SKILL.mdの生成** - 有効なClaude Codeスキルファイルを作成
4. **Instinctsの生成（オプション）** - continuous-learning-v2システム向け

## 分析ステップ

### ステップ1: Gitデータの収集

```bash
# ファイル変更を含む直近のコミットを取得
git log --oneline -n ${COMMITS:-200} --name-only --pretty=format:"%H|%s|%ad" --date=short

# ファイルごとのコミット頻度を取得
git log --oneline -n 200 --name-only | grep -v "^$" | grep -v "^[a-f0-9]" | sort | uniq -c | sort -rn | head -20

# コミットメッセージのパターンを取得
git log --oneline -n 200 | cut -d' ' -f2- | head -50
```

### ステップ2: パターンの検出

以下のパターンタイプを探します:

| パターン | 検出方法 |
|---------|---------|
| **コミット規約** | コミットメッセージの正規表現 (feat:, fix:, chore:) |
| **ファイルの同時変更** | 常に一緒に変更されるファイル |
| **ワークフローの順序** | 繰り返されるファイル変更パターン |
| **アーキテクチャ** | フォルダ構造と命名規約 |
| **テストパターン** | テストファイルの場所、命名、カバレッジ |

### ステップ3: SKILL.mdの生成

出力形式:

```markdown
---
name: {repo-name}-patterns
description: Coding patterns extracted from {repo-name}
version: 1.0.0
source: local-git-analysis
analyzed_commits: {count}
---

# {Repo Name} Patterns

## Commit Conventions
{detected commit message patterns}

## Code Architecture
{detected folder structure and organization}

## Workflows
{detected repeating file change patterns}

## Testing Patterns
{detected test conventions}
```

### ステップ4: Instinctsの生成（--instincts指定時）

continuous-learning-v2との連携:

```yaml
---
id: {repo}-commit-convention
trigger: "when writing a commit message"
confidence: 0.8
domain: git
source: local-repo-analysis
---

# Use Conventional Commits

## Action
Prefix commits with: feat:, fix:, chore:, docs:, test:, refactor:

## Evidence
- Analyzed {n} commits
- {percentage}% follow conventional commit format
```

## 出力例

TypeScriptプロジェクトで `/skill-create` を実行した場合の出力例:

```markdown
---
name: my-app-patterns
description: Coding patterns from my-app repository
version: 1.0.0
source: local-git-analysis
analyzed_commits: 150
---

# My App Patterns

## Commit Conventions

This project uses **conventional commits**:
- `feat:` - New features
- `fix:` - Bug fixes
- `chore:` - Maintenance tasks
- `docs:` - Documentation updates

## Code Architecture

```
src/
├── components/     # React components (PascalCase.tsx)
├── hooks/          # Custom hooks (use*.ts)
├── utils/          # Utility functions
├── types/          # TypeScript type definitions
└── services/       # API and external services
```

## Workflows

### Adding a New Component
1. Create `src/components/ComponentName.tsx`
2. Add tests in `src/components/__tests__/ComponentName.test.tsx`
3. Export from `src/components/index.ts`

### Database Migration
1. Modify `src/db/schema.ts`
2. Run `pnpm db:generate`
3. Run `pnpm db:migrate`

## Testing Patterns

- Test files: `__tests__/` directories or `.test.ts` suffix
- Coverage target: 80%+
- Framework: Vitest
```

## GitHub Appとの連携

高度な機能（10,000以上のコミット分析、チーム共有、自動PR）については、[Skill Creator GitHub App](https://github.com/apps/skill-creator)をご利用ください:

- インストール: [github.com/apps/skill-creator](https://github.com/apps/skill-creator)
- 任意のissueで `/skill-creator analyze` とコメント
- 生成されたスキルを含むPRが届きます

## 関連コマンド

- `/instinct-import` - 生成されたinstinctsのインポート
- `/instinct-status` - 学習済みinstinctsの確認
- `/evolve` - instinctsをスキル/エージェントにクラスタリング

---

*[Everything Claude Code](https://github.com/affaan-m/everything-claude-code)の一部です*
