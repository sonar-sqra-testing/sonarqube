# Auto-approve criteria

These criteria are additive to the organization-level auto-approve settings. A
pull request must satisfy both. This file only narrows eligibility; it can
never approve something the organization settings would not.

## Scope: `sonarqube-agent-staging` only

Only consider auto-approval for a pull request opened by the
`sonarqube-agent-staging` GitHub App (author login
`sonarqube-agent-staging[bot]`). For a pull request opened by any other
author, this file adds no eligibility — leave auto-approve entirely to the
organization default and human review.

## Otherwise, defer to the organization review

For a pull request opened by `sonarqube-agent-staging`, apply the
organization's existing auto-approve criteria (clean review, no open blocking
finding) without further repo-specific restriction.
