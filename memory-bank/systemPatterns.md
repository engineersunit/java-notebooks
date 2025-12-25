# System Patterns — java-notebooks

Last updated: 2025-12-25

Architecture Overview
- Repository is intentionally flat and simple:
  - Root contains interactive Java notebooks (.ijnb) for exploratory work.
  - Each demo lives in a small, feature-focused subfolder with its own README and minimal Java sources.
  - images/ contains supporting diagrams and figures.
- No build system by default; examples are runnable with javac/java. Optional tooling can be added per-demo if it improves clarity.

Key Technical Decisions and Patterns
1) Runnable-first minimalism
   - Favor single-file or tiny multi-file demos to reduce setup overhead.
   - Provide explicit javac/java commands in each demo README.
   - Avoid heavy frameworks or build tools unless the demo specifically requires them.

2) Feature detection and graceful degradation
   - Newer JDK features (e.g., HTTP/3 in JDK 26) should be detected dynamically when possible.
   - Example: HTTP/3 enum availability may be probed via `HttpClient.Version.valueOf("HTTP_3")`, falling back to HTTP/2 or HTTP/1.1 as needed.

3) Explicit runtime flags for advanced behavior
   - For demos involving JEP 500 (final-field mutation), document and require explicit enable/deny options:
     - `--enable-final-field-mutation=...`
     - `--illegal-final-field-mutation=allow|warn|debug|deny`
   - Note that `--add-opens` may still be required for deep reflection across modules, but it is not sufficient alone for JEP 500 mutation semantics.

4) Isolation of concerns
   - Each demo is independent and self-contained.
   - Notebooks are exploratory and do not impose structure on demos.
   - Cross-links are documentation-level only (no Java code dependencies across demos).

Component Relationships
- Notebooks (.ijnb)
  - Interactive, exploratory code and notes.
  - May mirror or extend the concepts demonstrated by the demos.
- Demos
  - http3-demo/
    - Demonstrates opt-in HTTP/3 usage in `java.net.http.HttpClient`.
    - Shows negotiated protocol and fallback behavior.
  - jep500-demo/
    - Demonstrates effects of JEP 500 (final-field mutation) and relevant JVM options.

Critical Implementation Paths
- HTTP/3 path (http3-demo)
  - Prefer HTTP/3 if available (JDK 26+), otherwise fallback to HTTP/2/1.1.
  - Prints response status, negotiated protocol, and optionally `Alt-Svc` header when present.
  - Network dependencies: UDP/QUIC on port 443 must be allowed; otherwise fallback occurs.

- Final-field mutation path (jep500-demo)
  - Demonstrates deep reflection attempts to mutate final fields.
  - Default JDK 26 behavior: warn; future default likely deny.
  - Uses CLI options to control behavior and enable/deny scenarios.
  - Diagnostics via `--illegal-final-field-mutation=debug` and JFR event recording.

Conventions
- Documentation co-located with code:
  - Each demo has a README.md with purpose, prerequisites, build/run steps, and flags.
- Version targets:
  - JDK 21+ generally recommended; specific demos may require JDK 26.
- Portability:
  - Keep commands POSIX-friendly; note platform-specific adjustments if needed.

Cross-References
- Root README.md — repository overview, prerequisites, notebook usage, and demo quick starts.
- http3-demo/README.md — HTTP/3 behavior, discovery/forcing notes, troubleshooting steps.
- jep500-demo/README.md — JEP 500 semantics, flags, diagnostics, and migration guidance.
