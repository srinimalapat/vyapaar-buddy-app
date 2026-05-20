// srini_claude_agent — powered by Claude Sonnet
// Reads project context + diff + CI results → posts a review comment on GitHub.

import fs from 'fs';
import https from 'https';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '../..');

// ── Inputs ────────────────────────────────────────────────────────────────────
const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY;
const GITHUB_TOKEN      = process.env.GITHUB_TOKEN;
const REPO              = process.env.REPO;             // owner/repo
const COMMIT_SHA        = process.env.COMMIT_SHA;
const COMMIT_MSG        = process.env.COMMIT_MSG || '';
const BRANCH            = process.env.BRANCH || 'develop';
const PR_NUMBER         = process.env.PR_NUMBER;
const EVENT_NAME        = process.env.EVENT_NAME;

const BACKEND_COMPILE      = process.env.BACKEND_COMPILE      || '❓ unknown';
const BACKEND_TESTS        = process.env.BACKEND_TESTS        || '❓ unknown';
const BACKEND_TEST_OUTPUT  = process.env.BACKEND_TEST_OUTPUT  || '';
const FRONTEND_TYPECHECK   = process.env.FRONTEND_TYPECHECK   || '❓ unknown';

if (!ANTHROPIC_API_KEY) { console.error('ANTHROPIC_API_KEY not set'); process.exit(1); }

// ── Read project context ──────────────────────────────────────────────────────
const claudeMd   = safeRead(path.join(ROOT, 'CLAUDE.md'), 3000);
const diffStat   = safeRead('/tmp/diff_stat.txt', 1000);
const diffCode   = safeRead('/tmp/diff_code.txt', 4000);

function safeRead(filePath, maxChars) {
  try {
    const content = fs.readFileSync(filePath, 'utf8');
    return content.length > maxChars ? content.slice(0, maxChars) + '\n...(truncated)' : content;
  } catch { return '(not available)'; }
}

// ── Build Claude prompt ───────────────────────────────────────────────────────
const systemPrompt = `You are srini_claude_agent, a CI code reviewer for the Vyapaar Buddy project.
You have deep context about this codebase. Be concise, specific, and actionable.
Format your response in GitHub-flavoured Markdown.
Always end with a "### 🔮 Suggested Next Steps" section (max 3 bullet points).`;

const userPrompt = `## Push to \`${BRANCH}\`
**Commit:** \`${COMMIT_SHA?.slice(0, 7)}\` — ${COMMIT_MSG.split('\n')[0]}

## CI Results
| Check | Status |
|---|---|
| Backend compile | ${BACKEND_COMPILE} |
| Backend tests | ${BACKEND_TESTS} |
| Frontend typecheck | ${FRONTEND_TYPECHECK} |

${BACKEND_TEST_OUTPUT ? `<details><summary>Test output</summary>\n\n\`\`\`\n${BACKEND_TEST_OUTPUT.slice(0, 1500)}\n\`\`\`\n</details>` : ''}

## Changed Files
\`\`\`
${diffStat}
\`\`\`

## Code Diff (Java / TypeScript)
\`\`\`diff
${diffCode}
\`\`\`

## Project Context (CLAUDE.md excerpt)
${claudeMd}

---
Please review the changes above. Cover:
1. **Code quality** — any issues, anti-patterns, or missing error handling?
2. **Architecture** — does it follow the project's layered pattern (Controller→Service→Repository)?
3. **Security** — any obvious vulnerabilities (SQL injection, missing auth checks, exposed secrets)?
4. **Tests** — are the changes covered? Anything critical untested?
5. **Suggested next steps** — what should be tackled next based on what changed?`;

// ── Call Claude API ───────────────────────────────────────────────────────────
async function callClaude() {
  const body = JSON.stringify({
    model: 'claude-sonnet-4-6',
    max_tokens: 1024,
    system: systemPrompt,
    messages: [{ role: 'user', content: userPrompt }],
  });

  return new Promise((resolve, reject) => {
    const req = https.request({
      hostname: 'api.anthropic.com',
      path: '/v1/messages',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': ANTHROPIC_API_KEY,
        'anthropic-version': '2023-06-01',
        'Content-Length': Buffer.byteLength(body),
      },
    }, res => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try { resolve(JSON.parse(data)); }
        catch (e) { reject(new Error('Failed to parse Claude response: ' + data)); }
      });
    });
    req.on('error', reject);
    req.write(body);
    req.end();
  });
}

// ── Post GitHub comment ───────────────────────────────────────────────────────
async function postGitHubComment(reviewText) {
  const header = `## 🤖 srini_claude_agent — Code Review
> Branch: \`${BRANCH}\` | Commit: \`${COMMIT_SHA?.slice(0, 7)}\`

`;
  const footer = `\n\n---\n*Powered by [Claude Sonnet 4.6](https://anthropic.com) · [srini_claude_agent](.github/workflows/claude-agent.yml)*`;
  const commentBody = header + reviewText + footer;

  const isPR = EVENT_NAME === 'pull_request' && PR_NUMBER;
  const apiPath = isPR
    ? `/repos/${REPO}/issues/${PR_NUMBER}/comments`
    : `/repos/${REPO}/commits/${COMMIT_SHA}/comments`;

  const body = JSON.stringify({ body: commentBody });

  return new Promise((resolve, reject) => {
    const req = https.request({
      hostname: 'api.github.com',
      path: apiPath,
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${GITHUB_TOKEN}`,
        'Accept': 'application/vnd.github+json',
        'User-Agent': 'srini_claude_agent',
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(body),
      },
    }, res => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          console.log(`✅ Comment posted (HTTP ${res.statusCode})`);
          resolve();
        } else {
          reject(new Error(`GitHub API error ${res.statusCode}: ${data}`));
        }
      });
    });
    req.on('error', reject);
    req.write(body);
    req.end();
  });
}

// ── Main ──────────────────────────────────────────────────────────────────────
(async () => {
  try {
    console.log('🤖 srini_claude_agent starting review...');
    const response = await callClaude();

    if (response.error) {
      throw new Error(`Claude API error: ${JSON.stringify(response.error)}`);
    }

    const reviewText = response.content?.[0]?.text;
    if (!reviewText) throw new Error('No text in Claude response');

    console.log('📝 Review generated, posting to GitHub...');
    await postGitHubComment(reviewText);
    console.log('✅ Done!');
  } catch (err) {
    console.error('❌ srini_claude_agent failed:', err.message);
    process.exit(1);
  }
})();
