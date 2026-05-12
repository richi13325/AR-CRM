# Skill Registry

**Delegator use only.** Any agent that launches sub-agents reads this registry to resolve compact rules, then injects them directly into sub-agent prompts. Sub-agents do NOT read this registry or individual SKILL.md files.

See `_shared/skill-resolver.md` for the full resolution protocol.

## User Skills

| Trigger | Skill | Path |
|---------|-------|------|
| When creating a pull request, opening a PR, or preparing changes for review | branch-pr | C:/Users/richi/.claude/skills/branch-pr/SKILL.md |
| When user says "judgment day", "judgment-day", "review adversarial", "dual review", "doble review", "juzgar", "que lo juzguen" | judgment-day | C:/Users/richi/.claude/skills/judgment-day/SKILL.md |
| When writing Go tests, using teatest, or adding test coverage | go-testing | C:/Users/richi/.claude/skills/go-testing/SKILL.md |
| When creating a GitHub issue, reporting a bug, or requesting a feature | issue-creation | C:/Users/richi/.claude/skills/issue-creation/SKILL.md |
| Writing JavaScript in n8n Code nodes, using $input/$json/$node syntax, making HTTP requests with $helpers, working with dates using DateTime, troubleshooting Code node errors, or choosing between Code node modes | n8n-code-javascript | C:/Users/richi/.claude/skills/n8n-code-javascript/SKILL.md |
| Writing Python in n8n Code nodes, using _input/_json/_node syntax, working with standard library, or need to understand Python limitations in n8n Code nodes | n8n-code-python | C:/Users/richi/.claude/skills/n8n-code-python/SKILL.md |
| Writing n8n expressions, using {{}} syntax, accessing $json/$node variables, troubleshooting expression errors, or working with webhook data in workflows | n8n-expression-syntax | C:/Users/richi/.claude/skills/n8n-expression-syntax/SKILL.md |
| Searching for nodes, validating configurations, accessing templates, managing workflows, managing credentials, auditing instance security, or using any n8n-mcp tool | n8n-mcp-tools-expert | C:/Users/richi/.claude/skills/n8n-mcp-tools-expert/SKILL.md |
| Configuring nodes, understanding property dependencies, determining required fields, choosing between get_node detail levels, or learning common configuration patterns by node type | n8n-node-configuration | C:/Users/richi/.claude/skills/n8n-node-configuration/SKILL.md |
| Encountering validation errors, validation warnings, false positives, operator structure issues, or need help understanding validation results | n8n-validation-expert | C:/Users/richi/.claude/skills/n8n-validation-expert/SKILL.md |
| Building new workflows, designing workflow structure, choosing workflow patterns, planning workflow architecture, or asking about webhook processing, HTTP API integration, database operations, AI agent workflows, or scheduled tasks | n8n-workflow-patterns | C:/Users/richi/.claude/skills/n8n-workflow-patterns/SKILL.md |
| When user asks to create a new skill, add agent instructions, or document patterns for AI | skill-creator | C:/Users/richi/.claude/skills/skill-creator/SKILL.md |

## Compact Rules

Pre-digested rules per skill. Delegators copy matching blocks into sub-agent prompts as `## Project Standards (auto-resolved)`.

### branch-pr
- Follow the issue-first enforcement system — no PR without linked issue
- Use conventional commits only; never add "Co-Authored-By" or AI attribution
- Run git status, git diff, and git log in parallel before creating PR
- Draft PR summary analyzing ALL commits, not just the latest
- Push with -u flag when branch has no upstream tracking

### judgment-day
- Launch two independent blind judge sub-agents simultaneously to review the same target
- Synthesize their findings and apply fixes
- Re-judge until both pass or escalate after 2 iterations
- Trigger phrases: "judgment day", "judgment-day", "review adversarial", "dual review", "doble review", "juzgar", "que lo juzguen"

### go-testing
- Use teatest for TUI testing with Bubbletea
- Follow Go testing patterns: table-driven tests, testify/assert for assertions
- Mock external dependencies; test behavior, not implementation
- Coverage: use -cover flag; aim for meaningful coverage, not 100%

### issue-creation
- Follow the issue-first workflow; every change starts with an issue
- Use conventional labels and descriptions
- Before creating, run mem_search to check if similar issue already exists
- Include reproduction steps, expected vs actual behavior

### n8n-code-javascript
- Use $input/$json/$node for data access; $helpers for utilities
- DateTime for date manipulation; avoid native Date methods
- HTTP requests via $helpers.httpRequest; handle errors gracefully
- Choose between Code node modes (JavaScript/Python) based on task complexity

### n8n-code-python
- Use _input/_json/_node for data access
- Standard library only; no external packages unless pre-installed
- Python limitations in n8n: no threading, limited memory
- Handle None/null values explicitly to avoid runtime errors

### n8n-expression-syntax
- Use {{}} for expressions; $json/$node for variable access
- Common errors: missing brackets, typos in field names, type mismatches
- Validate expressions before deploying; test with sample data
- Webhook data: access via $json.webhookName.field

### n8n-mcp-tools-expert
- Tool selection: use most specific tool for the task
- Parameter formats vary; check tool documentation
- Validate configurations before executing
- Use templates to accelerate workflow creation

### n8n-node-configuration
- Determine required fields per node type
- Property dependencies: some fields appear based on other field values
- Choose get_node detail level based on what you need
- Common patterns: HTTP Request node config, Database node config, AI Agent node config

### n8n-validation-expert
- Validation errors: read the error message carefully; it usually points to the exact problem
- False positives: check if node configuration is correct
- Operator structure: ensure operators are properly nested
- Validation profiles affect what errors are shown; choose appropriate profile

### n8n-workflow-patterns
- Webhook processing: validate input, handle errors, return appropriate response
- HTTP API integration: use HTTP Request node with proper error handling
- Database operations: use dedicated database nodes when available
- AI agent workflows: define clear instructions and data flow
- Scheduled tasks: use Cron node; account for timezone

### skill-creator
- Create SKILL.md following Agent Skills spec (frontmatter with name, description, trigger)
- Include compact rules (5-15 lines), critical patterns, and gotchas
- Place in appropriate skills directory (user-level or project-level)
- Update skill-registry after creating new skill

## Project Conventions

| File | Path | Notes |
|------|------|-------|
| MASTER_RULES.md | C:\Users\richi\OneDrive\Documentos\GitHub\CRM2\MASTER_RULES.md | Project architecture rules, delegates to layer-specific rules |
| DOMAIN_RULES.md | C:\Users\richi\OneDrive\Documentos\GitHub\CRM2\DOMAIN_RULES.md | Domain layer rules |
| APPLICATION_RULES.md | C:\Users\richi\OneDrive\Documentos\GitHub\CRM2\APPLICATION_RULES.md | Application layer rules |
| INFRASTRUCTURE_RULES.md | C:\Users\richi\OneDrive\Documentos\GitHub\CRM2\INFRASTRUCTURE_RULES.md | Infrastructure layer rules |
| BOOT_RULES.md | C:\Users\richi\OneDrive\Documentos\GitHub\CRM2\BOOT_RULES.md | Boot/configuration rules |

No index files (AGENTS.md, CLAUDE.md, .cursorrules) found at project level.

Read the convention files listed above for project-specific patterns and rules. All referenced paths have been extracted — no need to read index files to discover more.