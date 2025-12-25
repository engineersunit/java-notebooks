# Project Brief — java-notebooks

Last updated: 2025-12-25

Overview
- This repository is a playground for exploring Java features via interactive notebooks (.ijnb) and minimal standalone demo programs.
- Goals: rapid experiments, reproducible reference snippets, and concise demos illustrating specific JDK features/JEPs.

Scope
- Include small, self-contained examples and notebooks:
  - Notebooks (.ijnb) for interactive learning and quick iteration.
  - Minimal Java demos (single-file or tiny folder) showcasing a focused concept.
- Keep per-demo documentation short and actionable (how to build/run, flags, prerequisites).

Out of Scope (Non-goals)
- Large multi-module builds and complex frameworks by default.
- Heavy build tooling unless a demo specifically requires it.
- Long-form tutorials; prefer short runnable examples with pointers to official docs.

Repository Structure (current)
- Root notebooks:
  - CircularDependency.ijnb — explore circular dependency concepts.
  - Http3Notebook.ijnb — explore HTTP/3 concepts.
  - VirtualThreadsNotebook.ijnb — explore virtual threads (Project Loom).
- Demos:
  - http3-demo/
    - Http3Demo.java
    - README.md — JEP 517 demo: HTTP/3 for HttpClient (JDK 26). Prefers HTTP/3, prints negotiated protocol; resilient fallback to HTTP/2/1.1.
  - jep500-demo/
    - C.java, args.txt
    - README.md — JEP 500 demo: “Prepare to Make final mean final” (JDK 26). Covers enabling/controlling final-field mutation, warn/deny modes, and related flags.
- images/
  - connections.png — supporting diagrams.

Primary Users
- Repo owner and collaborators experimenting with Java.
- Anyone seeking concise, runnable references for recent/advanced Java features.

Development Assumptions
- JDK installed and on PATH (JDK 21+ recommended; some demos target JDK 26 features).
- VS Code with Oracle Java extension for .ijnb notebooks is recommended but not mandatory.
- Demos kept as plain Java for simplicity (javac/java). Optional build tools may be used per demo.

Success Criteria
- Each notebook/demo can be opened, compiled, and run with minimal setup.
- Each demo includes a README with prerequisites and clear run instructions.
- The repository remains easy to navigate; new demos are additive and do not break existing ones.

Risks and Considerations
- Feature-dependent demos (e.g., JDK 26) should handle absence of newer APIs gracefully or document requirements clearly.
- Networking demos (HTTP/3) depend on environment/network (e.g., UDP/QUIC 443).
- Reflection and security options (JEP 500) may need explicit JVM flags; document prominently.

Evolving Directions
- Add more notebooks for emerging JEPs and platform features.
- Optional: per-demo Gradle/Maven samples (kept minimal) when instructive.
- Optional: CI snippets to validate examples on specific JDK versions.

Maintenance Guidelines
- Add a short README to every new demo folder.
- Keep examples self-contained and small.
- Update this brief when the repo scope or organization changes.
