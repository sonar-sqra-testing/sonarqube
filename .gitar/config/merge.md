# Auto-merge criteria

Additive to the organization's auto-merge settings; a PR must satisfy both.
Otherwise, leave the merge to a human.

- Only arm auto-merge on a PR opened by the `sonarqube-agent-staging` GitHub
  App (`sonarqube-agent-staging[bot]`). Never arm it for any other author.
- Only arm it when Gitar's own risk rating for the PR is Low. Re-check on
  every change to the PR — a push that raises the rating disarms auto-merge
  until it's Low again.
- Blocking review findings and required checks (`Build`,
  `SonarQube Cloud (staging) CI analysis`) must still pass, as always.
