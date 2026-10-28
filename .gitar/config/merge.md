# Auto-merge criteria

These criteria are additive to the organization-level auto-merge settings. A
pull request must satisfy both. When a pull request fails these criteria,
leave the merge to a human reviewer. This repository has no low-impact-path
carve-out: every eligible pull request is gated on author and risk alone.

## Scope: `sonarqube-agent-staging` only

Only arm auto-merge for a pull request opened by the
`sonarqube-agent-staging` GitHub App (author login
`sonarqube-agent-staging[bot]`). Never arm auto-merge for a pull request
opened by any other author, regardless of risk rating or review outcome.

## Risk gate: Low risk only

Arm auto-merge only when Gitar's own risk rating for the pull request is Low.
Do not arm on Medium or High risk. Re-check the rating any time the pull
request changes, including a fix Gitar makes to it itself — a push that
raises the risk disarms auto-merge until the rating returns to Low.

## Existing safeguards still apply

Any open blocking review finding and the required status checks (the `Build`
job and the `SonarQube Cloud (staging) CI analysis` scan) must still pass
before merge happens, exactly as for any other auto-merge.
