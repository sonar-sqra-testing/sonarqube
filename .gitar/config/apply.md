# Auto-apply criteria

Additive to the organization's auto-apply settings; a fix is applied only if
eligible under both. Saving this file turns on neither auto-apply, approval,
nor merging — auto-apply is enabled per pull request; auto-approve and
auto-merge have their own criteria.

Only auto-apply fixes on a PR opened by the `sonarqube-agent-staging` GitHub
App (`sonarqube-agent-staging[bot]`). For any other author, this file adds no
eligibility. No further restriction beyond author scope.
