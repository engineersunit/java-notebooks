# Active Context — java-notebooks

Last updated: 2025-12-25

Current Focus
- Initialize the Cline Memory Bank for this repository.
- Establish core documentation to preserve context across sessions.

Recent Changes
- Created memory-bank/ directory and initialized:
  - projectbrief.md — repository scope, structure, goals.
  - productContext.md — purpose, user goals, value proposition, constraints.
- No code changes to demos or notebooks.

Next Steps
- Author remaining core memory bank files:
  - systemPatterns.md — architecture, patterns, relationships.
  - techContext.md — stack, tools, versions, setup.
  - progress.md — status, known issues, roadmap.
- Add project-specific .clinerules with the official Cline Memory Bank instructions to enforce reading memory-bank files at task start.
- Optional follow-ups:
  - Add per-demo quick-glance snippets (commands, flags) into progress.md.
  - Expand systemPatterns.md with cross-file relationships as more demos are added.

Active Decisions and Considerations
- Keep demos minimal (single-file or tiny folders) with per-demo README instructions.
- Favor dynamic capability detection where possible (e.g., HTTP/3 version detection) to support multiple JDK versions.
- Document runtime constraints (network UDP/QUIC for HTTP/3; JEP 500 flags for final-field mutation).
- Prefer plain javac/java execution paths; only introduce build tooling when clearly beneficial for a demo.

Important Patterns and Preferences
- Notebooks (.ijnb) for interactive exploration using the Oracle Java VS Code extension.
- Each demo:
  - States minimum JDK version (some target JDK 26 features).
  - Lists exact compile/run commands and flags.
  - Remains self-contained.
- Documentation emphasizes “runnable first,” then links to official references.

Learnings and Insights
- HTTP/3 demo (JEP 517-like usage) should prefer HTTP/3 but gracefully fall back when unavailable due to network/environment.
- JEP 500 behavior (final-field mutation):
  - Warns by default in JDK 26; future default likely deny.
  - Requires explicit enable flags to allow mutation; --add-opens may still be needed for deep reflection.
- Editors without .ijnb support can copy cells into .java files to run.

Open Questions / Future Clarifications
- Add CI matrix (optional) to validate demos across JDK versions.
- Consider minimal Gradle/Maven samples for selected demos (only if instructional value outweighs complexity).
