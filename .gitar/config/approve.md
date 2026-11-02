# Auto-approval criteria

A PR is safe to auto-approve when both of the following are true:

- It was opened by the Remediation Agent's dev8 bot, `sonarqube-agent-dev8[bot]`.
- Gitar's own whole-PR risk assessment is **Low risk**. If no risk assessment is available, this
  criterion is not met.
- The PR does not modify any file under `.gitar/` or `.github/`. PRs touching these paths are never
  auto-approved, regardless of author or risk rating.

Do not auto-approve a PR from any other author, or a `sonarqube-agent-dev8[bot]` PR at any other risk
rating — leave those to a human/org default.
