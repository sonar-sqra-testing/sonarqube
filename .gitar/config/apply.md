# Auto-apply criteria

Repository criteria for Gitar's auto-apply of code-review findings. These
criteria are additive to the organization-level auto-apply settings; a fix is
applied only when it satisfies both. Saving this file does not by itself turn
auto-apply on for a pull request — auto-apply is armed per pull request (see
`.github/workflows/gitar-auto-apply.yml`), and CI-failure fixing and CI
recovery have their own, separate controls that this file does not govern.

## Scope: `sonarqube-agent-staging` only

Only consider auto-apply for a pull request opened by the
`sonarqube-agent-staging` GitHub App (author login
`sonarqube-agent-staging[bot]`). For a pull request opened by any other
author, this file adds no eligibility — leave fixes to a human, as today.

## Otherwise, apply findings without extra repo-specific holds

For a pull request opened by `sonarqube-agent-staging`, apply any
code-review finding that the organization criteria already make eligible;
this file does not add further restriction beyond the author scope above. In
particular, do not withhold a fix solely because of this repository's size or
age — judge each patch on the finding it addresses.
