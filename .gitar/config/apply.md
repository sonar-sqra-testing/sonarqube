# Auto-apply criteria

Additive to the organization's auto-apply settings; a fix is applied only if
eligible under both. Saving this file doesn't arm auto-apply on a PR — see
`.github/workflows/gitar-auto-apply.yml` for that, which also covers
CI-failure fixing/recovery (governed separately from this file).

Only auto-apply fixes on a PR opened by the `sonarqube-agent-staging` GitHub
App (`sonarqube-agent-staging[bot]`). For any other author, this file adds no
eligibility. No further restriction beyond author scope.
