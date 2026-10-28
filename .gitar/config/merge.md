# Auto-merge criteria

- Only arm auto-merge on a PR opened by the `sonarqube-agent-staging` GitHub
  App (`sonarqube-agent-staging[bot]`). Never arm it for any other author.
- Only arm it when Gitar's own risk rating for the PR is Low. Re-check on
  every change to the PR — a push that raises the rating disarms auto-merge
  until it's Low again.
